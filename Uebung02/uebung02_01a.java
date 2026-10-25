import java.io.*;
import java.net.*;

public class uebung02_01a {

    public static void main(String[] args) {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        /*
         * TODO:
         * 1. Open a socket
         * 2. Open an input stream and output stream to the socket
         * 3. Read from and write to the stream according to te server's protocol
         * 4. Close the the stream
         * 5. Close the socket (Streams connected to a socket should be closed before
         * the socket
         * itselfs is closed.)
         * 
         * 
         */

        // 1st statement
        // Create a socket to connect to the server
        // Closed the socket automatically when the try block is exited
        try (Socket socket = new Socket(hostName, portNumber)) {

            // 2nd statement
            // Create a PrintWriter to send data to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // 3rd statement
            // Create a BufferedReader to read data from the server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 4th statement
            // Create a BufferedReader to read user input from the console
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput = stdIn.readLine();

            String request = String.format("GET %s HTTP/1.1\r\nHost: %s\r\nConnection: close\r\n\r\n", userInput,
                    hostName);

            out.print(request);
            out.flush();

            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
            }

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

    }
}