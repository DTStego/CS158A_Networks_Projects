package edu.sjsu.cs158a.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class UDPReceiver {
    public static void main(String[] arg) throws IOException {
        try (var sock = new DatagramSocket(2323))
        {
            sock.setSoTimeout(5000);
            var bytes = new byte[512];
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            var packet = new DatagramPacket(bytes, bytes.length);
            while (true) {
                try
                {
                    sock.receive(packet);
                }
                catch (SocketTimeoutException ex)
                {
                    System.out.println("Took too long for input.");
                    System.exit(0);
                }

                var length = packet.getLength();
                bb.position(0);

                while (bb.position() < length)
                {
                    System.out.println("bb pos " + bb.position());
                    var rand = bb.getInt();
                    System.out.println("bb pos " + bb.position());

                    System.out.println(packet.getSocketAddress() + " " + rand);
                }

                System.out.println("Got: " + new String(bytes, 0, length));
            }
        }
    }
}