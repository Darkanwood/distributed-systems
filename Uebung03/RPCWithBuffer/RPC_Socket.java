package RPCWithBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RPC_Socket {

    /**
     * Verbindet den Client mit dem Server, zeigt dessen Anleitung an und
     * verarbeitet Konsoleneingaben bis zur Eingabe von "bey" oder zum
     * Ende der Eingabe beziehungsweise der Serververbindung.
     *
     * @param args Serveradresse an Position 0 und Portnummer an Position 1
     */
    public static void main(String[] args) {

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostName, portNumber)) {

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BufferedReader stdIn = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));

            // Der Server sendet zuerst die Anleitung als Data-Nachricht im Feld city.
            DataMessage.Data response = DataMessage.Data.parseDelimitedFrom(in);

            if (response == null) {
                System.out.println("Der Server hat die Verbindung beendet.");
                return;
            }

            printResponse(response);

            boolean connected = true;
            while (connected) {
                System.out.print("> ");
                String userInput = stdIn.readLine();

                if (userInput == null || userInput.trim().equals("bey")) {
                    connected = false;
                } else if (!userInput.trim().isEmpty()) {
                    connected = processInput(userInput.trim(), in, out);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prüft einen Befehl, sendet bei gültiger Eingabe die Protobuf-Anfrage
     * und gibt die zugehörige Serverantwort aus. Ungültige Eingaben werden
     * gemeldet und nicht gesendet.
     *
     * @param userInput nicht leere, bereits getrimmte Eingabe mit Befehl und
     *                  Parametern
     * @param in        Eingabestrom der Serververbindung
     * @param out       Ausgabestrom der Serververbindung
     * @return {@code true}, wenn eine weitere Eingabe gelesen werden kann,
     *         auch nach ungültiger Eingabe; {@code false}, wenn der Server
     *         vor einer Antwort die Verbindung beendet hat
     * @throws IOException wenn das Senden oder Lesen einer Nachricht fehlschlägt
     */
    private static boolean processInput(
            String userInput, InputStream in, OutputStream out) throws IOException {

        // Der dritte Teil darf einen Ortsnamen mit Leerzeichen enthalten.
        // [0] Funktion; [1] Index; [2] Ortsname;
        String[] command = userInput.split("\\s+", 3);
        DataMessage.Funktion.Builder request = DataMessage.Funktion.newBuilder();
        DataMessage.Data record = null;

        try {
            if (command[0].equals("getRecord")) {
                if (command.length != 2) {
                    System.out.println("Verwendung: getRecord <index>");
                    return true;
                } else {
                    request.setFunction("getRecord");
                    request.setIndex(Integer.parseInt(command[1]));
                }
            } else if (command[0].equals("addRecord")) {
                if (command.length != 3) {
                    System.out.println("Verwendung: addRecord <index> <ort>");
                    return true;
                } else {
                    int index = Integer.parseInt(command[1]);
                    request.setFunction("addRecord");
                    request.setIndex(index);
                    record = DataMessage.Data.newBuilder()
                            .setIndex(index)
                            .setCity(command[2])
                            .build();
                }
            } else if (command[0].equals("getSize")) {
                if (command.length != 1) {
                    System.out.println("Verwendung: getSize");
                    return true;
                } else {
                    request.setFunction("getSize");
                }
            } else {
                System.out.println("Befehle: getRecord <index>, addRecord <index> <ort>, getSize, bey");
                return true;
            }
        } catch (NumberFormatException e) {
            System.out.println("Der Index muss eine ganze Zahl sein.");
            return true;
        }

        // Pro Aufruf eine Funktion; bei addRecord folgt eine Data-Nachricht.
        request.build().writeDelimitedTo(out);
        if (record != null) {
            record.writeDelimitedTo(out);
        }
        out.flush();

        // Vor dem nächsten Aufruf wird genau eine Data-Antwort gelesen.
        DataMessage.Data response = DataMessage.Data.parseDelimitedFrom(in);
        if (response == null) {
            System.out.println("Der Server hat die Verbindung beendet.");
            return false;
        }
        printResponse(response);
        return true;
    }

    /**
     * Gibt die initiale Anleitung oder eine Antwort des Servers aus.
     * Die Variable city enthält Text oder einen Datensatz. Ist nur
     * code gesetzt, enthält es die Datenbankgröße. Eine Nachricht
     * ohne gesetzte Felder kennzeichnet einen nicht gefundenen Datensatz.
     *
     * @param response vom Server empfangene Protobuf-Nachricht
     */
    private static void printResponse(DataMessage.Data response) {
        // city enthält Text oder einen Datensatz, index bei getSize die Anzahl.
        if (response.hasCity()) {
            System.out.println(response.getCity());
        } else if (response.hasIndex()) {
            System.out.println("Anzahl der Datensätze: " + response.getIndex());
        } else {
            System.out.println("Kein Datensatz gefunden.");
        }
    }
}
