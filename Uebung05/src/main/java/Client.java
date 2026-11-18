import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {

    static private int id = 0;

    /**
     * Gibt das JSON Request Objekt zurück
     * 
     * @param requestText
     * @return
     */
    private static JsonObject readsResult(String requestText) {

        JsonObject request = JsonParser
                .parseString(requestText)
                .getAsJsonObject();

        return request;

    }

    /**
     * Stellt eine Größenabfrage an den Server.
     * Der Server antwortet mit der Größe der Datenbank zum Serverstart.
     * 
     * @param stub Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     */
    private static void getSize(JsonObject request, Gson gson, OutputStream out, BufferedReader in)
            throws IOException {

        request.addProperty("jsonrpc", "2.0");
        request.addProperty("method", "getSize");
        request.addProperty("id", id);

        String requestText = gson.toJson(request);

        out.write((requestText + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        String responseText = in.readLine();

        if (responseText == null) {
            throw new IOException("Server closed connection");
        }

        JsonObject response = readsResult(responseText);
        int result = response.get("result").getAsInt();

        System.out.println("Your order with the number " + id);

        System.out.println("[INFO] There are " + result + " Datasets");

        System.out.println();
        id++;

    }

    /**
     * Stellt eine Speicheranfrage auf den Server.
     * Antwortet bei Erfolg mit [OK], ansosnten mit [FEHLER]
     * 
     * @param stub  Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     * @param index Der Indexeintrag für den Datensatz
     * @param city  Der Stadteintrag für den Datensatz
     */
    private static void addRecord(JsonObject request, Gson gson, OutputStream out, BufferedReader in, String city,
            int index) throws IOException {

        request.addProperty("jsonrpc", "2.0");
        request.addProperty("method", "addRecord");

        JsonObject params = new JsonObject();
        params.addProperty("index", index);
        params.addProperty("city", city);

        request.add("params", params);

        request.addProperty("id", id);

        String requestText = gson.toJson(request);

        out.write((requestText + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        String responseText = in.readLine();

        if (responseText == null) {
            throw new IOException("Server closed connection");
        }

        JsonObject response = readsResult(responseText);
        boolean result = response.get("result").getAsBoolean();

        System.out.println("Your order with the number " + id);

        if (result) {
            System.out.println("[INFO] " + index + " -> " + city + " is included");
        } else {
            System.out.println("[ERROR]: including failed");
        }

        System.out.println();
        id++;
    }

    /**
     * Stellt eine Indexanfrage an den Server.
     * 
     * @param stub  Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     * @param index Der Index für die Suchanfrage.
     */
    private static void getRecord(JsonObject request, Gson gson, OutputStream out, BufferedReader in, int index)
            throws IOException {

        request.addProperty("jsonrpc", "2.0");
        request.addProperty("method", "getRecord");

        JsonObject params = new JsonObject();
        params.addProperty("index", index);

        request.add("params", params);

        request.addProperty("id", id);

        String requestText = gson.toJson(request);

        out.write((requestText + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        String responseText = in.readLine();

        if (responseText == null) {
            throw new IOException("Server closed connection");
        }

        JsonObject response = readsResult(responseText);
        String result = response.get("result").getAsString();

        System.out.println("Your order with the number " + id);

        if (!result.isEmpty()) {
            System.out.println("[INFO] The City at " + index + " is " + result);
        } else {
            System.out.println("[ERROR] There is no city with index " + index);
        }

        System.out.println();
        id++;
    }

    /**
     * Löscht den Inhalt der Datenbank
     * 
     * @param stub Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     */
    public static void deleteData(JsonObject request, Gson gson, OutputStream out, BufferedReader in)
            throws IOException {
        request.addProperty("jsonrpc", "2.0");
        request.addProperty("method", "deleteData");
        request.addProperty("id", id);

        String requestText = gson.toJson(request);

        out.write((requestText + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();

        String responseText = in.readLine();

        if (responseText == null) {
            throw new IOException("Server closed connection");
        }

        JsonObject response = readsResult(responseText);
        boolean result = response.get("result").getAsBoolean();

        System.out.println("Your order with the number " + id);

        if (result) {
            System.out.println("[INFO] Your datas are deleted");
        } else {
            System.out.println("[ERROR] Deleting failed.");
        }
        System.out.println();
        id++;

    }

    public static void main(String[] args) throws InterruptedException {

        // String host = args[0];
        String host = "localhost";

        // int port = Integer.parseInt(args[1]);
        int port = 50051;

        try (
                Socket socket = new Socket(host, port);

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));

                OutputStream out = socket.getOutputStream()

        ) {

            boolean running = true;

            System.out.println("""
                    ========================================
                                 STÄDTE-DATENBANK
                    ========================================
                    Befehle:
                      getSize                   Anzahl anzeigen
                      getRecord <index>         Stadt suchen
                      addRecord <index> <stadt> Stadt speichern
                      deleteData                Löscht den Inhalt der Datenbank
                      tutorialTask              Führt die Tutorial Aufgabe aus
                      exit                      Client beenden

                    Beispiel: getRecord 201
                    ----------------------------------------
                    """);

            String input;

            while (running) {
                System.out.println();
                System.out.print("> ");
                Scanner scanner = new Scanner(System.in);
                input = scanner.nextLine();

                if (input.isBlank()) {
                    continue;
                }

                String[] commands = input.trim().split("\\s+", 3);

                switch (commands[0]) {
                    case "getSize":
                        getSize(new JsonObject(), new Gson(), out, in);
                        break;

                    case "addRecord":
                        addRecord(new JsonObject(), new Gson(), out, in, commands[2], Integer.parseInt(commands[1]));
                        break;

                    case "getRecord":
                        getRecord(new JsonObject(), new Gson(), out, in, Integer.parseInt(commands[1]));
                        break;

                    case "tutorialTask":

                        String[] cities = { "Appen", "Ahrensburg", "Wedel", "Aumühle", "Seevetal", "Quickborn" };

                        Integer[] indexe = { 4101, 4102, 4103, 4104, 4105, 4106 };
                        int length = cities.length;

                        for (int i = 0; i < length; i++) {
                            addRecord(new JsonObject(), new Gson(), out, in, cities[i], indexe[i]);
                        }

                        Integer[] getNumbers = { 4103, 4107 };
                        length = getNumbers.length;

                        for (int i = 0; i < length; i++) {
                            getRecord(new JsonObject(), new Gson(), out, in, indexe[i]);
                        }

                        getSize(new JsonObject(), new Gson(), out, in);

                        break;

                    case "deleteData":
                        deleteData(new JsonObject(), new Gson(), out, in);
                        break;

                    case "exit":
                        running = false;
                        break;

                    default:
                        System.out.println("[Fehler] Eingabefehler. Bitte auf die Eingabeform achten");
                        break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
