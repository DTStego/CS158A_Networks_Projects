package edu.sjsu.cs158a.udp;

public class Conversation
{
    // Points to the current discussion (1 is introduction, 2 is file transfer, 3 is checksum...).
    private short discussionPointer;

    // Name of the sender which is used to identify the transferred file.
    private String nameOfSender;

    public Conversation(short discussionPointer, String nameOfSender)
    {
        this.discussionPointer = discussionPointer;
        this.nameOfSender = nameOfSender;
    }

    public short getDiscussionPointer()
    {
        return discussionPointer;
    }

    public void setDiscussionPointer(short newPointer)
    {
        discussionPointer = newPointer;
    }

    public String getNameOfSender()
    {
        return nameOfSender;
    }
}
