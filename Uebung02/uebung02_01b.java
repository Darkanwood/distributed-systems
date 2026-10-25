import java.net.*;
import java.io.*;

public class uebung02_01b {

    public static void main(String[] args) {

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try {
            URL myURL = new URL("http://" + hostName + ":" + portNumber + "/");

            try (BufferedReader in = 
                new BufferedReader(new InputStreamReader(myURL.openStream()))) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }

            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }

        } catch (MalformedURLException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

    }
}
