import java.io.*;
import java.net.*;

// KnockKnock Server

public class uebung02_02a {
    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            while (true) {

                // Waits until a client starts up and requests a connection on the
                // host and port of this server
                Socket clientSocket = serverSocket.accept();

                // Create a new conektion betwen server and a new socket
                Thread socketThread = new Thread(() -> {

                    System.out.println("Worker gestartet: " + Thread.currentThread().getName());

                    // try-with-resources statement
                    try (
                            Socket socket = clientSocket;

                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(socket.getInputStream()));) {

                        String inputLine;
                        String outputLine;
                        String requestLine;

                        // Read HTTP Request
                        requestLine = in.readLine();
                        System.out.println("Request: " + requestLine);

                        // Read Header
                        while ((inputLine = in.readLine()) != null) {

                            // Break if empty Space
                            if (inputLine.isEmpty()) {
                                break;
                            }
                        }

                        // Kreate HTTP-Response
                        outputLine = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: text/plain\r\n"
                                + "Content-Length: 25\r\n"
                                + "Connection: close\r\n"
                                + "\r\n"
                                + "Praised be the Omnissiah!";

                        // Send HTTP Response
                        out.print(outputLine);
                        out.flush();

                    } catch (IOException e) {
                        System.err.println(
                                "Client error: " + e.getMessage());

                    }

                });

                socketThread.start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
        }

    }
}