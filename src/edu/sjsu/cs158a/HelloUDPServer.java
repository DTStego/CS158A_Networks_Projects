package edu.sjsu.cs158a;

import edu.sjsu.cs158a.udp.Conversation;

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

            String IPAddress = packet.getAddress().getHostAddress();

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

                // Iterate through the conversationList to check if the new handshake is a duplicate.
                for (Map.Entry<Integer, Conversation> entry : conversationList.entrySet())
                {
                    if (entry.getValue().getNameOfSender().equals(name))
                    {
                        System.out.println(msgNum + " " + entry.getKey());

                        // Resend confirmation.
                        bb = ByteBuffer.allocate(100);
                        bb.putShort((short) 1);
                        bb.putInt(entry.getKey());
                        bb.put("hello, i am jimmy".getBytes());

                    }
                }

                // No duplicates! Create a new conversation object and store in the conversation list.
                conversationList.put(conversationAssignment, new Conversation((short) 1, name));
                System.out.println(msgNum + " " + conversationAssignment);

                // Increment counter for future, new conversations.
                conversationAssignment++;

                // Send confirmation
            }
        }

        // -------------------------------------------------------------------------------------

        // Keep sending a confirmation of the first message until the server starts sending the file/second message.
        boolean firstMessageConfirmed = false;

        ByteArrayOutputStream byteArrayCollection = new ByteArrayOutputStream();

        int offsetPointer = -1;

        // Send a confirmation of the first message and retrieve the second message.
        while (true)
        {
            if (!firstMessageConfirmed)
            {
                // Create a ByteBuffer with [the number 1 as a network order 2-byte integer,
                // conversation number as a network order 4-byte integer, "hello, i am <your name>"]
                ByteBuffer bb = ByteBuffer.allocate(100);
                bb.putShort((short) 1);
                bb.putInt(convoNumber);
                bb.put("hello, i am jimmy".getBytes());

                sendPacket(bb, senderPortAddress, 1, convoNumber);
            }

            byte[] messageBytes = receivePacket();

            /* Client sends a file to the server in 100-byte chunks, except for the last chunk possibly with:
             * [the number 2 as a network order 2-byte integer, conversation number as a network order 4-byte integer,
             * offset of bytes as a network order 4-byte integer, bytes of the file]. the file will be name_of_the_sender.txt.
             */
            if (messageBytes != null)
            {
                ByteBuffer bb = ByteBuffer.wrap(messageBytes);

                short msgNum = bb.getShort();
                System.out.println(msgNum + " " + convoNumber);

                // Check if we're currently on the 2nd conversation.
                if (msgNum != 2)
                {
                    // Exit the loop if the third message is sent!
                    if (msgNum == 3)
                    {

                        break;
                    }

                    System.out.println("Wrong Message Conversation: Expected - 2 | Got - " + msgNum);
                    System.exit(2);
                }

                int receivedConvoNumber = bb.getInt();

                // Check if this is the right conversation (unique number).
                if (receivedConvoNumber != convoNumber)
                {
                    System.out.println("These conversation numbers are different! " +
                            "Expected - " + convoNumber + " | Got - " + receivedConvoNumber);
                }

                int receivedOffset = bb.getInt();

                // Check to make sure the offsets are different, otherwise, it means that the message has been repeated.
                if (receivedOffset <= offsetPointer)
                {
                    // Same message has been repeated. Resend the confirmation method.
                    bb = ByteBuffer.allocate(100);
                    bb.putShort((short) 2);
                    bb.putInt(convoNumber);
                    bb.putInt(offsetPointer);

                    sendPacket(bb, senderPortAddress,  2, convoNumber);
                    continue;
                }
                else
                {
                    offsetPointer = receivedOffset;
                }

                byte[] remainingMsg = new byte[messageBytes.length - bb.position()];
                // Fills the bytes array with the rest of the message.
                bb.get(remainingMsg);

                byteArrayCollection.write(remainingMsg);
                firstMessageConfirmed = true;

                /* Server sends [the number 2 as a network order 2-byte integer, conversation number as a
                 * network order 4-byte integer, offset of bytes as a network order 4-byte integer]
                 */
                bb = ByteBuffer.allocate(100);
                bb.putShort((short) 2);
                bb.putInt(convoNumber);
                bb.putInt(offsetPointer);

                sendPacket(bb, senderPortAddress,  2, convoNumber);
            }
        }

        // Save the file contained in ByteArrayOutputStream byteArrayCollection.
        OutputStream outputStream = new FileOutputStream(nameOfSender.concat(".txt"));
        byteArrayCollection.writeTo(outputStream);

        /* the client will send the checksum: [the number 3 as a network order 2-byte integer,
         * conversation number as a network order 4-byte integer, the first 8 bytes of the SHA-256 digest]
         */
        while (true)
        {
            byte[] messageBytes = receivePacket();

            if (messageBytes != null)
            {
                ByteBuffer bb = ByteBuffer.wrap(messageBytes);

                short msgNum = bb.getShort();
                System.out.println(msgNum + " " + convoNumber);

                if (msgNum != 3)
                {
                    System.out.println("Wrong Message Conversation: Expected - 3 | Got - " + msgNum);
                }

                int receivedConvoNumber = bb.getInt();

                // Check if this is the right conversation (unique number).
                if (receivedConvoNumber != convoNumber)
                {
                    System.out.println("These conversation numbers are different! " +
                            "Expected - " + convoNumber + " | Got - " + receivedConvoNumber);
                }

                // Contains the first 8 bytes of the SHA-256 digest.
                byte[] remainingMsg = new byte[messageBytes.length - bb.position()];

                // Fills the bytes array with the rest of the message.
                bb.get(remainingMsg);

                // Generate the SHA-256 hash for the file that was received in the previous conversation.
                byte[] data = Files.readAllBytes(Paths.get(nameOfSender.concat(".txt")));
                byte[] SHAHash = MessageDigest.getInstance("SHA-256").digest(data);

                // Start generating the confirmation message (which includes whether the SHA hash matches).
                // [the number 3 as a network order 2-byte integer, conversation number as a network order 4-byte integer,
                // one byte of 0 for success or 1 for failure, failure message if any]
                bb = ByteBuffer.allocate(100);
                bb.putShort((short) 3);
                bb.putInt(convoNumber);

                // Iterate through the first 8 bytes of the SHA-256 digest and compare with the actual (SHAHash)
                for (int i = 0; i < remainingMsg.length; i++)
                {
                    if (remainingMsg[i] != SHAHash[i])
                    {
                        bb.put((byte) 1);

                        sendPacket(bb, senderPortAddress, 3, convoNumber);

                        System.exit(2);
                    }
                }

                bb.put((byte) 0);

                sendPacket(bb, senderPortAddress, 3, convoNumber);
                break;
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

       // Return the packet for parsing in main.
        return packet;
    }
}
