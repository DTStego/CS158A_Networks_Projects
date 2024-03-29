package edu.sjsu.cs158a.hw1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

// Homework #1
public class Here
{
    public static void main(String[] args) throws IOException
    {
        Socket socket;
        if (args.length > 1)
            socket = new Socket(args[1], Integer.parseInt(args[2]));
        else
            socket = new Socket("cs-reed-02.class.homeofcode.com", 3333);

        // Write things to server
        OutputStream out = socket.getOutputStream();

        // Read things from server
        InputStream in = socket.getInputStream();

        // First character is an ASCII character denoting the length of the message.
        int length = in.read();

        // Server set to send info in chunks of four bytes, need to use readNBytes(lengthOfMsg) to read entire message.
        // The message is stored in ASCII numbers in a byte array.
        byte[] bytes = in.readNBytes(length);

        // Print out the length of the message and the actual message.
        // new String() converts the byte array's ASCII characters into actual words and letters.
        System.out.println(new String(bytes));

        // Example student ID with the initial zero truncated as ints don't accept starting zeros... octal integer.
        int myID = 1555733224;

        /* Attempt to convert 32-bit ID into a byte array, so we can send it to the server (Big-endian).
         * Essentially, the code tries to pinpoint one byte or 8 bits of data per array slot.
         * Ints are 32 bits (XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX). To convert it to a byte array,
         * you need pinpoint every 8 bits to put into the byte array (each index location accepts 8 bits only).
         * You shift a specific 8 bits to the end (which removes all the bits to its right) which leaves
         * you with a specific 8 bits. Then, you put it into the byte array and repeat the process.
         */
//        byte[] idBytes = new byte[4];
//        idBytes[0] = (byte) ((myID >> 24) & 0xff);
//        idBytes[1] = (byte) ((myID >> 16) & 0xff);
//        idBytes[2] = (byte) ((myID >> 8) & 0xff);
//        idBytes[3] = (byte) ((myID) & 0xff);

        // Alternatively, you can use a ByteBuffer to convert it for you.
        byte[] idBytes = ByteBuffer.allocate(4).putInt(myID).array();

        // Converts your name to a byte array to send to server.
        String name = "Jimmy Nguyen";
        byte[] nameBytes = name.getBytes();

        // Sends your ID, length of your name, and your name to the server.
        out.write(idBytes);
        out.write(nameBytes.length);
        out.write(nameBytes);

        // Read the pin that the server gives back (Specified as four bytes).
        var pin = in.readNBytes(4);

        // Give pin back to server.
        out.write(pin);

        // Read and print the success message that the server should give back.
        var successMessage = in.readNBytes(in.read());

        System.out.println(new String(successMessage));
    }
}
