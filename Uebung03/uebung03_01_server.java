import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public static void main(String[] args) {

    int portNumber = Integer.parseInt(args[0]);

    final Object logLock = new Object();

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
                                socket.getInputStream(), StandardCharsets.ISO_8859_1));

                        OutputStream out = socket.getOutputStream()) {

                    LogMessage.writeLogRepport logReport;

                    while ((logReport = LogMessage.writeLogRepport
                            .parseDelimitedFrom(socket.getInputStream())) != null) {

                        String time = Instant.ofEpochSecond(
                                Integer.toUnsignedLong(logReport.getTime()))
                                .atZone(ZoneId.systemDefault())
                                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

                        String who = logReport.getWho();
                        String where = logReport.getWhere();
                        String severity = logReport.getSeverityLevel().name();
                        String message = logReport.getMessage();

                        String output = "[TIME]: " + time + "; "
                                + "[WHO]: " + who + "; "
                                + "[WHERE]: " + where + "; "
                                + "[SEVERITY]: " + severity + "; "
                                + "[MESSAGE]: " + message;

                        synchronized (logLock) {
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter("LogMessages.txt", true))) {

                                writer.write(output);
                                writer.newLine();

                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                            }
                        }

                    }

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