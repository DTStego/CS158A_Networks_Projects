package edu.sjsu.cs158a;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

public class TLS
{
    public static void main(String[] args) throws IOException
    {
        // Contains the Root Certificate Authorities used for the links in the file.
        // Different links can have the same root certificate authority, hence, HashSet!
        LinkedHashMap<X509Certificate, ArrayList<String>> rootCertificates = new LinkedHashMap<>();

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

        File fileName = new File(args[0].concat(".txt"));

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

        // Send each URL in the text file to the match() method.
        String url;
        while ((url = bufferedReader.readLine()) != null)
        {
            identify(url, rootCertificates);
        }
    }

    public static void identify(String URL, LinkedHashMap<X509Certificate, ArrayList<String>> rootCertificates) throws IOException
    {
        try (SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(URL, 443))
        {
            Certificate[] certificates = sslSocket.getSession().getPeerCertificates();

            X509Certificate peerCertificate = (X509Certificate) certificates[0];
            X509Certificate rootCertificate = (X509Certificate) certificates[certificates.length - 1];

            match(peerCertificate, URL);

            // Add the root certificate of the URL into
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

    public static void match(X509Certificate peerCertificate, String URL)
    {

    }
}
