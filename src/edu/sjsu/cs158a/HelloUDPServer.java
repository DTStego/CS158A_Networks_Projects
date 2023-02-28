package edu.sjsu.cs158a;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class HelloUDPServer
{
    static String senderIPAddress = null;
    static int senderPortAddress = -1;
    static DatagramSocket socket;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException
    {
        int port = -1;

        String usageMsg = """
                Usage: HelloUDPServer <port>
                A UDP server that implements the HelloUDP protocol.
                      <port>   port to listen on.""";

        // Program requires one argument that details a port number (int) between 0 and 65535.
        if (args.length < 1)
        {
            System.err.println("Missing required parameter: '<port>'\n" + usageMsg);
            System.exit(2);
        }

        // Retrieve the port number from args and check for incorrect arguments.
        try
        {
            port = Integer.parseInt(args[0]);

            if (port < 0 || port > 65535)
            {
                System.err.println("port must be a number between 0 and 65535\n" + usageMsg);
                System.exit(2);
            }

        }
        catch (NumberFormatException ex)
        {
            System.err.println("port must be a number between 0 and 65535\n" + usageMsg);
            System.exit(2);
        }

        // Reached when a port number has correctly been retrieved but there are additional, unused arguments.
        if (args.length > 1)
        {
            System.err.print("Unmatched arguments from index 1: ");
            // Loop through the arguments past the port number.
            for (int i = 1; i < args.length; i++)
            {
                System.err.print("'" + args[i] + "'");

                if (i != args.length - 1)
                {
                    System.err.print(", ");
                }
            }

            System.err.println("\n" + usageMsg);
            System.exit(2);
        }

        System.out.println("Listening on port " + port);
        socket = new DatagramSocket(port);

        // Contains a list of conversations for simultaneous communication with multiple clients.
        Map<Integer, Conversation> conversationList = new HashMap<>();

        // Assigns this number to any new conversations and increments by one for additional conversations.
        int conversationAssignment = 100;

        while (true)
        {
            DatagramPacket packet = receivePacket();
            byte[] message = packet.getData();

            // Indicates a timeout error from receivePacket();
            if (message == null)
            {
                continue;
            }

            // Truncate filler bytes.
            message = Arrays.copyOf(message, packet.getLength());

            ByteBuffer bb = ByteBuffer.wrap(message);

            // Conversation order number (1, 2, 3, 5).
            short msgNum = bb.getShort();

            // Client attempt to start a new conversation.
            // [the number 1 as a network order 2-byte integer, "hello, i am name (id)"]
            if (msgNum == 1)
            {
                // Allocates and stores message for the introduction (hello, i am [name]) of the conversation.
                byte[] hello = new byte[bb.array().length - bb.position()];
                bb.get(hello);

                // Name of the sender.
                String name = new String(hello);
                name = name.substring(name.indexOf("am ") + 3);

                // Iterate through the conversationList to check if the new handshake is a duplicate.
                for (Map.Entry<Integer, Conversation> entry : conversationList.entrySet())
                {
                    if (entry.getValue().getNameOfSender().equals(name))
                    {
                        System.out.println(msgNum + " " + entry.getKey());

                        // Client did not receive the confirmation message. Resend!
                        if (entry.getValue().getDiscussionPointer() == msgNum)
                        {
                            bb = ByteBuffer.allocate(50);
                            bb.putShort((short) 1);
                            bb.putInt(entry.getKey());
                            bb.put("hello, i am jimmy".getBytes());

                            sendPacket(bb, senderPortAddress, 1, entry.getKey());
                            System.exit(0);
                        }
                        // Error if the discussion has moved on, and we're getting introductions again.
                        else
                        {
                         sendError("We've moved on from the introduction. Current Discussion: "
                                 + entry.getValue().getDiscussionPointer(), entry.getKey());
                        }
                    }
                }

                // No duplicates! Create a new conversation object and store in the conversation list.
                conversationList.put(conversationAssignment, new Conversation((short) 1, name));
                System.out.println(msgNum + " " + conversationAssignment);

                // Send confirmation.
                bb = ByteBuffer.allocate(50);
                bb.putShort((short) 1);
                bb.putInt(conversationAssignment);
                bb.put("hello, i am jimmy".getBytes());
                sendPacket(bb, senderPortAddress, 1, conversationAssignment);

                // Increment counter for future, new conversations.
                conversationAssignment++;
            }

            /* Client sends a file to the server in 100-byte chunks, except for the last chunk possibly with:
             * [the number 2 as a network order 2-byte integer, conversation number as a network order 4-byte integer,
             * offset of bytes as a network order 4-byte integer, bytes of the file]. the file will be name_of_the_sender.txt.
             */
            if (msgNum == 2)
            {
                int conversationNumber = bb.getInt();

                // Find the conversation in conversationList.
                Conversation conversation = conversationList.get(conversationNumber);

                if (conversation == null)
                {
                    sendError("Message out of place! Cannot find conversation.", conversationNumber);
                    System.exit(2);
                }

                conversation.setDiscussionPointer(2);

                // Log where the packet came from.
                System.out.println(msgNum + " " + conversationNumber);

                int receivedOffset = bb.getInt();

                // Check to make sure the new offset is greater than the current offset pointer, otherwise it's a duplicate.
                if (receivedOffset <= conversation.getFileOffset())
                {
                    // Resend confirmation.
                    bb = ByteBuffer.allocate(50);
                    bb.putShort((short) 2);
                    bb.putInt(conversationNumber);
                    bb.putInt(receivedOffset);

                    sendPacket(bb, senderPortAddress, 2, conversationNumber);
                    continue;
                }

                // If the difference between the two offsets is greater than
                // the 100-byte chunk difference, there is clearly some unknown error.
                if (receivedOffset - conversation.getFileOffset() > 101)
                {
                    System.out.println("Major Error!");

                    sendError("File transfer out of place! Current offset: " + conversation.getFileOffset()
                            + " | Got: " + receivedOffset, conversationNumber);
                    System.exit(2);
                }

                conversation.setFileOffset(receivedOffset);

                // Copy the remaining message to fileContents which should contain a chunk of the file.
                byte[] fileContents = new byte[message.length - bb.position()];
                bb.get(fileContents);

                // Add the file contents to the byte[] collection (contains the files).
                conversation.addToByteArrayOutputStream(fileContents);

                // Send confirmation: [the number 3 as a network order 2-byte integer,
                // conversation number as a network order 4-byte integer, the first 8 bytes of the SHA-256 digest]
                bb = ByteBuffer.allocate(50);
                bb.putShort((short) 2);
                bb.putInt(conversationNumber);
                bb.putInt(receivedOffset);

                sendPacket(bb, senderPortAddress, 2, conversationNumber);
            }

            /* Checksum process: [the number 3 as a network order 2-byte integer,
             * conversation number as a network order 4-byte integer, the first 8 bytes of the SHA-256 digest]
             */
            if (msgNum == 3)
            {
                int conversationNumber = bb.getInt();

                Conversation conversation = conversationList.get(conversationNumber);

                if (conversation == null)
                {
                    sendError("Message out of place! Cannot find conversation.", conversationNumber);
                    System.exit(2);
                }

                conversation.setDiscussionPointer(3);

                // Log where the packet came from.
                System.out.println(msgNum + " " + conversationNumber);

                // Create a new file based from the ByteArrayOutputStream in the Conversation object.
                OutputStream outputStream = new FileOutputStream(conversation.getNameOfSender().concat(".txt"));
                conversation.getByteArrayOutputStream().writeTo(outputStream);

                // Should contain eight bytes (Noted in assignment details).
                byte[] receivedHash = new byte[8];

                // Store the remaining message into receivedHash.
                bb.get(receivedHash);

                // Generate the SHA-256 hash for the file that was received in the previous conversation.
                byte[] data = Files.readAllBytes(Paths.get(conversation.getNameOfSender().concat(".txt")));
                byte[] actualHash = MessageDigest.getInstance("SHA-256").digest(data);

                // Start generating the confirmation message (which includes whether the SHA hash matches).
                // [the number 3 as a network order 2-byte integer, conversation number as a network order 4-byte integer,
                // one byte of 0 for success or 1 for failure, failure message if any]
                bb = ByteBuffer.allocate(100);
                bb.putShort((short) 3);
                bb.putInt(conversationNumber);

                // Iterate through the first 8 bytes of the SHA-256 digest and compare with actualHash.
                for (int i = 0; i < receivedHash.length; i++)
                {
                    // Failure detected, send an error.
                    if (receivedHash[i] != actualHash[i])
                    {
                        bb.put((byte) 1);

                        sendPacket(bb, senderPortAddress, 3, conversationNumber);

                        System.exit(2);
                    }
                }

                bb.put((byte) 0);

                sendPacket(bb, senderPortAddress, 3, conversationNumber);
                System.exit(0);
            }
        }
    }

    public static void sendPacket(ByteBuffer bb, int port, int type, int conversationID) throws IOException
    {
        if (senderIPAddress == null)
        {
            System.out.println("No IP Address to send confirmation message to [Error]");
            System.exit(2);
        }

        // Put the data from the ByteBuffer into a byte array.
        byte[] bytes = bb.array();

        // Put the bytes array into a packet to send.
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setAddress(InetAddress.getByName(senderIPAddress));
        packet.setPort(port);

        System.out.println("To " + packet.getSocketAddress() + ": " + type + " " + conversationID);
        socket.send(packet);
    }

    public static DatagramPacket receivePacket() throws IOException
    {
        socket.setSoTimeout(5000);

        byte[] bytes = new byte[512];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        try
        {
            socket.receive(packet);
        }
        catch (SocketTimeoutException ex)
        {
            System.out.println("TIMEOUT ERROR");
            return null;
        }

        System.out.print("From " + packet.getSocketAddress() + ": ");
        senderPortAddress = packet.getPort();
        senderIPAddress = packet.getAddress().getHostAddress();

       // Return the packet for parsing in main.
        return packet;
    }

    // Send an error to the client if a wrong/unknown message got sent.
    public static void sendError(String errorMessage, int conversationID) throws IOException
    {
        ByteBuffer bb = ByteBuffer.allocate(50);
        bb.putShort((short) 5);
        bb.put(errorMessage.getBytes());

        sendPacket(bb, senderPortAddress, 5, conversationID);
    }
}
