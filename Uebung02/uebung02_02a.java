
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

// KnockKnock Server

public class uebung02_02a {
    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            while (true) {

                // Wait for client connection
                Socket clientSocket = serverSocket.accept();

                // Create new thread
                Thread socketThread = new Thread(() -> {

                    System.out.println("Worker gestartet: " + Thread.currentThread().getName());

                    try (
                            Socket socket = clientSocket;
                            BufferedReader in = new BufferedReader(new InputStreamReader(
                                            socket.getInputStream(),StandardCharsets.ISO_8859_1));
                                            
                            OutputStream out = socket.getOutputStream()
                        ) {

                        // Read HTTP Request
                        String requestLine = in.readLine();

                        if (requestLine == null) {
                            return;
                        }

                        System.out.println("Request: " + requestLine);

                        // Read HTTP Headers
                        String inputLine;

                        while ((inputLine = in.readLine()) != null
                                && !inputLine.isEmpty()) {
                            // Read until empty line
                        }

                        // Split Request
                        String[] request = requestLine.split(" ");

                        if (request.length != 3) {
                            out.write(("HTTP/1.1 400 Bad Request\r\n"
                                    + "Content-Length: 0\r\n"
                                    + "Connection: close\r\n\r\n")
                                    .getBytes(StandardCharsets.US_ASCII));
                            return;
                        }

                        String method = request[0];
                        String path = request[1];

                        // Check HTTP Method
                        if (!method.equals("GET")) {
                            out.write(("HTTP/1.1 405 Method Not Allowed\r\n"
                                    + "Allow: GET\r\n"
                                    + "Content-Length: 0\r\n"
                                    + "Connection: close\r\n\r\n")
                                    .getBytes(StandardCharsets.US_ASCII));
                            return;
                        }

                        // Only serve index.html for now
                        if (!path.equals("/index.html")
                                && !path.equals("/")) {

                            out.write(("HTTP/1.1 404 Not Found\r\n"
                                    + "Content-Length: 0\r\n"
                                    + "Connection: close\r\n\r\n")
                                    .getBytes(StandardCharsets.US_ASCII));
                            return;
                        }

                        // Open local file
                        File file = new File("index.html");

                        if (!file.isFile()) {
                            out.write(("HTTP/1.1 404 Not Found\r\n"
                                    + "Content-Length: 0\r\n"
                                    + "Connection: close\r\n\r\n")
                                    .getBytes(StandardCharsets.US_ASCII));
                            return;
                        }

                        // Create HTTP Response
                        String responseHeader = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: text/html; charset=utf-8\r\n"
                                + "Content-Length: " + file.length() + "\r\n"
                                + "Connection: close\r\n"
                                + "\r\n";

                        out.write(responseHeader.getBytes(
                                StandardCharsets.US_ASCII));

                        // Send file using byte stream
                        try (FileInputStream fileInput = new FileInputStream(file)) {

                            byte[] buffer = new byte[4096];
                            int bytesRead;

                            while ((bytesRead = fileInput.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }

                        out.flush();

                        System.out.println("Datei uebertragen: "
                                + file.getName());

                    } catch (IOException e) {
                        System.err.println(
                                "Client error: " + e.getMessage());
                    }

                });

                socketThread.start();
            }

        } catch (IOException e) {
            System.err.println(
                    "Could not listen on port " + portNumber);
        }
    }
}
