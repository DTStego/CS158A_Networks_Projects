package edu.sjsu.cs158a.hw3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Conversation
{
    // Points to the current discussion (1 is introduction, 2 is file transfer, 3 is checksum...).
    private short discussionPointer;

    // Name of the sender which is used to identify the transferred file.
    private final String nameOfSender;

    // For storing chunks of byte[] data in an arrayList type structure.
    private final ByteArrayOutputStream byteArrayOutputStream;

    // Stores the progress of the file transfer.
    private int fileOffset;

    public Conversation(short discussionPointer, String nameOfSender)
    {
        this.discussionPointer = discussionPointer;
        this.nameOfSender = nameOfSender;
        fileOffset = -1;
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    public short getDiscussionPointer()
    {
        return discussionPointer;
    }

    // Accepts an int to convert into a short for simplicity (No casting in main code).
    public void setDiscussionPointer(int newPointer)
    {
        discussionPointer = (short) newPointer;
    }

    public String getNameOfSender()
    {
        return nameOfSender;
    }

    public void addToByteArrayOutputStream(byte[] bytes) throws IOException
    {
        byteArrayOutputStream.write(bytes);
    }

    public ByteArrayOutputStream getByteArrayOutputStream()
    {
        return byteArrayOutputStream;
    }

    public void setFileOffset(int newOffset)
    {
        fileOffset = newOffset;
    }

    public int getFileOffset()
    {
        return fileOffset;
    }
}
