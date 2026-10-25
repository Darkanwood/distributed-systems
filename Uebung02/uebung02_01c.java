import java.io.*;
import java.net.*;

public class uebung02_01c {

    public static void main(String[] args) {

        if(args.length != 4) {
            System.err.println("Usage: java uebung02_01c <host name> <port number> <proxy host> <proxy port>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        String proxyHost = args[2];
        int proxyPort = Integer.parseInt(args[3]);

        try {
            URL myURL = new URL("http://" + hostName + ":" + portNumber + "/");

            InetSocketAddress socketAddress = new InetSocketAddress(proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
            URLConnection connection = myURL.openConnection(proxy);

            try (BufferedReader in = 
                new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

    }
}
