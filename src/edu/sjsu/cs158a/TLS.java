package edu.sjsu.cs158a;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;

public class TLS
{
    /**
     * Project to match URL names with certificate alt-names. Displays further information about a site's root CA.
     * @param args Name of a file that's stored in the root directory. Contains HTTPS website names line by line.
     */
    public static void main(String[] args) throws IOException
    {
        // Contains the Root Certificate Authorities used for the links in the file.
        // Different links can have the same root certificate authority, hence, TreeMap (No duplicates)!
        // A TreeMap is used over a HashMap because we need to sort based on the X500Principal's Name.
        TreeMap<X509Certificate, ArrayList<String>> rootCertificates = new TreeMap<>(
                Comparator.comparing(cert -> cert.getIssuerX500Principal().getName())
        );

        // Make sure there is only one parameter!
        if (args.length < 1)
        {
            System.err.println("Please input the name of a file containing links to webpages in the arguments!");
            System.exit(2);
        }

        if (args.length > 1)
        {
            System.err.println("Too Many Arguments!");
            System.exit(2);
        }

        File fileName = new File(args[0]);

        BufferedReader bufferedReader = null;

        try
        {
            // Check if a file can be found. If not, catch the exception in the catch block and exit.
            bufferedReader = new BufferedReader(new FileReader(fileName));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File not found: " + fileName.getAbsolutePath());
            e.printStackTrace();
            System.exit(2);
        }

        // Send each URL in the text file to the identify() method.
        String url;
        while ((url = bufferedReader.readLine()) != null)
        {
            identify(url, rootCertificates);
        }

        // Print out information about the root certificates and the links that correlate to them.
        for (Map.Entry<X509Certificate, ArrayList<String>> certificate : rootCertificates.entrySet())
        {
            System.out.println(certificate.getKey().getIssuerX500Principal() + " " + certificate.getValue());
        }
    }

    /**
     * Connect to an HTTPS website and collect the peer and root certificate. Match the URL name with the
     * alt-names in the peer certificate and store the root certificate in rootCertificates. Check the HTTPS Status.
     *
     * @param URL Link to an HTTPS webpage which an SSLSocket will connect to (and grab certificates from).
     * @param rootCertificates A TreeMap that contains a list of all root certificates that
     *                         URLs in the argument text file uses/interacts with.
     */
    public static void identify(String URL, TreeMap<X509Certificate, ArrayList<String>> rootCertificates) throws IOException
    {
        // Connect to a URL using SSLSocket on port 443 (HTTPS).
        try (SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(URL, 443))
        {
            // Grabs the certificate chain for the webpage.
            Certificate[] certificates = sslSocket.getSession().getPeerCertificates();

            X509Certificate peerCertificate = (X509Certificate) certificates[0];

            // The root certificate should be the last one.
            X509Certificate rootCertificate = (X509Certificate) certificates[certificates.length - 1];

            match(peerCertificate, URL);
            checkHTTPStatus(sslSocket);

            // Add the root certificate and the webpage that uses it into the TreeMap rootCertificates.
            if (rootCertificates.containsKey(rootCertificate))
            {
                rootCertificates.get(rootCertificate).add(URL);
            }
            else
            {
                rootCertificates.put(rootCertificate, new ArrayList<>(Collections.singletonList(URL)));
            }
        }
    }

    /**
     * Attempts to match the URL with the alternative names contained within the X509 Certificate.
     * Prints out if the attempt failed or succeeded.
     *
     * @param peerCertificate The First X509 Certificate from a webpage that contains alt-names to ID the URL.
     * @param URL String that identifies the webpage/site.
     */
    public static void match(X509Certificate peerCertificate, String URL)
    {
        try
        {
            // Retrieve the list of alternative names (Basically a map <Integer, String>
            // with the int being an identifier for the String). In a size=2 List.
            //     List.get(0) returns the Integer
            //     List.get(1) returns the String (Most likely a URL)
            for (List<?> altNames : peerCertificate.getSubjectAlternativeNames())
            {
                // Isolate the actual alternative name, i.e., disregard the Integer identifier.
                String altName = (String) altNames.get(1);

                // Check if the alternative name matches the original URL exactly (no wildcard check).
                if (altName.equals(URL))
                {
                    System.out.println(URL + " matched " + altName);
                    return;
                }

                // Only continue wildcard check if the alternative name actually contains a wildcard.
                if (!altName.contains("*"))
                    continue;

                int indexOfPattern = -1;

                // Attempt to match the pattern after the wildcard with the original URL.
                if ((indexOfPattern = URL.indexOf(altName.substring(1))) != -1)
                {
                    // If the original URL does not contain any more periods, it matches the wildcard pattern.
                    if (!URL.substring(0, indexOfPattern).contains("."))
                    {
                        System.out.println(URL + " matched " + altName);
                        return;
                    }
                }
            }

            // If no alternative names from peerCertificate.getSubjectAlternativeNames() exists, print log.
            System.out.println(URL + " didn't match any name: " + peerCertificate.getSubjectAlternativeNames());
        }
        catch (CertificateParsingException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Request the robots.txt file from a webpage and print the first line from the file.
     * @param socket SSLSocket used to connect with the webpage/site.
     */
    public static void checkHTTPStatus(SSLSocket socket) throws IOException
    {
        // Used to read from client.
        InputStream inputStream = socket.getInputStream();

        // Used to write to client.
        OutputStream outputStream = socket.getOutputStream();

        if (inputStream == null || outputStream == null)
        {
            System.err.println("Cannot resolve socket information. Closing connection!");
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            return;
        }

        // Send a robots.txt request to the server.
        ByteBuffer bb = ByteBuffer.wrap("GET /robots.txt HTTP/1.0\r\n\r\n".getBytes());
        outputStream.write(bb.array());

        // Retrieve the entire robots.txt file and print out the first line.
        String message = new String(inputStream.readAllBytes());
        System.out.println(message.substring(0, message.indexOf("\n") - 1));
    }
}
