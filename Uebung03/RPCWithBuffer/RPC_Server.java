package RPCWithBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/**
 * Stellt Datenbankoperationen über eine dauerhafte TCP-Verbindung mit
 * längengerahmten Protocol-Buffers-Nachrichten bereit. Jeder Client wird
 * in einem eigenen Thread mit Zugriff auf dieselbe Datenbank verarbeitet.
 */
public class RPC_Server {

    /**
     * Lädt die Datenbank aus RPC_Database.json im Arbeitsverzeichnis
     * und nimmt Clientverbindungen auf dem angegebenen Port an.
     * Jeder Client erhält zunächst eine Anleitung und anschließend für jede
     * Anfrage eine Antwort, bis seine Verbindung beendet wird.
     * Ein- und Ausgabefehler werden auf der Fehlerkonsole ausgegeben.
     *
     * @param args Portnummer des Servers an Position 0
     */
    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);

        try {
            // Alle Client-Threads verwenden dieselbe Datenbankinstanz.
            RPC_Database database = new RPC_Database(Path.of("RPC_Database.json"));

            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    Thread socketThread = new Thread(() -> {
                        System.out.println("Worker gestartet: " + Thread.currentThread().getName());

                        try (Socket socket = clientSocket) {
                            InputStream in = socket.getInputStream();
                            OutputStream out = socket.getOutputStream();

                            DataMessage.Data.newBuilder()
                                    .setCity("Befehle:\n"
                                            + "addRecord <index> <ort>\n"
                                            + "getRecord <index>\n"
                                            + "getSize\n"
                                            + "bey beendet die Verbindung.")
                                    .build().writeDelimitedTo(out);
                            out.flush();

                            DataMessage.Funktion request;
                            while ((request = DataMessage.Funktion.parseDelimitedFrom(in)) != null) {
                                DataMessage.Data response = processRequest(request, in, database);
                                response.writeDelimitedTo(out);
                                out.flush();
                            }
                        } catch (IOException e) {
                            System.err.println("Clientfehler: " + e.getMessage());
                        }
                    });

                    socketThread.start();
                }
            }
        } catch (IOException e) {
            System.err.println("Serverfehler: " + e.getMessage());
        }
    }

    /**
     * Führt getRecord, addRecord oder getSize aus
     * und erstellt die zugehörige Antwort. Für addRecord wird eine
     * zusätzliche Data-Nachricht aus der Clientverbindung gelesen.
     *
     * @param request empfangene Anfrage mit Funktionsnamen und gegebenenfalls Index
     * @param in Eingabestrom der anfragenden Clientverbindung
     * @param database gemeinsame Datenbank mit synchronisierten Zugriffsmethoden
     * @return Antwort mit Datensatz, Datenbankgröße, Bestätigung oder Fehlertext;
     *         eine Nachricht ohne gesetzte Felder bei einem nicht gefundenen Datensatz
     * @throws IOException wenn die zusätzliche Nachricht nicht gelesen werden kann
     *         oder die Verbindung vor ihrem Empfang beendet wird
     */
    private static DataMessage.Data processRequest(
            DataMessage.Funktion request, InputStream in, RPC_Database database) throws IOException {

        DataMessage.Data.Builder response = DataMessage.Data.newBuilder();

        switch (request.getFunction()) {
            case "getRecord":
                if (!request.hasIndex()) {
                    return response.setCity("Fehler: Index fehlt.").build();
                }
                String record = database.getRecord(request.getIndex());
                if (record != null) {
                    response.setIndex(request.getIndex()).setCity(record);
                }
                break;

            case "addRecord":
                // Der Client sendet die Daten als zweite, längengerahmte Nachricht.
                DataMessage.Data data = DataMessage.Data.parseDelimitedFrom(in);
                if (data == null) {
                    throw new IOException("Verbindung vor dem Empfang des Datensatzes beendet.");
                }
                if (!request.hasIndex() || !data.hasIndex() || !data.hasCity()
                        || request.getIndex() != data.getIndex()) {
                    return response.setCity("Fehler: Ungültiger Datensatz.").build();
                }
                try {
                    database.addRecord(data.getIndex(), data.getCity());
                    response.setCity("Datensatz " + data.getIndex() + " gespeichert.");
                } catch (IOException e) {
                    System.err.println("Datenbankfehler: " + e.getMessage());
                    response.setCity("Fehler: Datensatz konnte nicht gespeichert werden.");
                }
                break;

            case "getSize":
                response.setIndex(database.getSize());
                break;

            default:
                response.setCity("Fehler: Unbekannte Funktion.");
                break;
        }

        return response.build();
    }
}
