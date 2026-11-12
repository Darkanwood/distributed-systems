import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.routeguide.ComData;
import io.grpc.examples.routeguide.DataFunctionGrpc;

public class Client {
    /**
     * Stellt eine Größenabfrage an den Server.
     * Der Server antwortet mit der Größe der Datenbank zum Serverstart.
     * 
     * @param stub Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     */
    private static void getSize(DataFunctionGrpc.DataFunctionBlockingStub stub) {
        try {
            ComData request = ComData.newBuilder().build();

            ComData response = stub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getSize(request);

            System.out.println("[Info] Gespeicherte Datensätz/e: " + response.getIndex());
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
        }
    }

    /**
     * Stellt eine Speicheranfrage auf den Server.
     * Antwortet bei Erfolg mit [OK], ansosnten mit [FEHLER]
     * 
     * @param stub  Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     * @param index Der Indexeintrag für den Datensatz
     * @param city  Der Stadteintrag für den Datensatz
     */
    private static void addRecord(DataFunctionGrpc.DataFunctionBlockingStub stub, int index, String city) {
        try {
            ComData request = ComData.newBuilder()
                    .setIndex(index)
                    .setCity(city)
                    .build();

            ComData response = stub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .addRecord(request);

            if (response.getSucces()) {
                System.out.println(
                        "[OK] Stadt gespeichert: " + response.getIndex() + " -> "
                                + response.getCity());
            } else {
                System.out.println("[FEHLER] Der Index ist bereits in der Datenbank enthalten.");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed " + e.getStatus());
        }
    }

    /**
     * Stellt eine Indexanfrage an den Server.
     * 
     * @param stub  Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     * @param index Der Index für die Suchanfrage.
     */
    private static void getRecord(DataFunctionGrpc.DataFunctionBlockingStub stub, int index) {
        try {
            ComData request = ComData.newBuilder()
                    .setIndex(index)
                    .build();

            ComData response = stub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .getRecord(request);
            if (response.getSucces()) {
                System.out.println("[INFO] Stadt mit Index " + response.getIndex() + ": "
                        + response.getCity());
            } else {
                System.out.println("[Fehler] Kein Eintrag mit Index " + index + " gefunden.");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed " + e.getStatus());
        }
    }

    /**
     * Löscht den Inhalt der Datenbank
     * 
     * @param stub Verbindung zu dem auf dem Server hinterlegten PROTO Funktionen
     */
    public static void deleteData(DataFunctionGrpc.DataFunctionBlockingStub stub) {
        ComData request = ComData.newBuilder().build();

        ComData response = stub
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .deleteData(request);

        if (response.getSucces()) {
            System.out.println("[INFO] Der Inhalt der Datenbank wurde gelöscht");
        } else {
            System.out.println("[FEHLER] Die Löschung ist fehlgeschlagen");
        }
    }

    public static void main(String[] args) throws InterruptedException {

        // String host = args[0];
        String host = "localhost";

        // int port = Integer.parseInt(args[1]);
        int port = 50051;

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        DataFunctionGrpc.DataFunctionBlockingStub stub = DataFunctionGrpc.newBlockingStub(channel);

        try (Scanner input = new Scanner(System.in)) {

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

            while (running) {
                System.out.println();
                System.out.print("> ");

                if (!input.hasNextLine()) {
                    break;
                }

                String[] command = input.nextLine().trim().split("\\s+");
                int commandLength = command.length;

                switch (command[0]) {
                    case "getSize":
                        if (commandLength == 1) {
                            getSize(stub);
                        }
                        break;

                    case "addRecord":
                        if (commandLength == 3) {
                            int index = Integer.parseInt(command[1]);
                            addRecord(stub, index, command[1]);

                        }
                        break;

                    case "getRecord":

                        if (commandLength == 2) {
                            int index = Integer.parseInt(command[1]);
                            getRecord(stub, index);
                        }
                        break;

                    case "tutorialTask":

                        if (commandLength == 1) {
                            String[] cities = { "Appen", "Ahrensburg", "Wedel", "Aumühle", "Seevetal", "Quickborn" };

                            Integer[] indexe = { 4101, 4102, 4103, 4104, 4105, 4106 };
                            int length = cities.length;

                            for (int i = 0; i < length; i++) {
                                addRecord(stub, indexe[i], cities[i]);
                            }

                            Integer[] getNumbers = { 4103, 4107 };
                            length = getNumbers.length;

                            for (int i = 0; i < length; i++) {
                                getRecord(stub, getNumbers[i]);
                            }

                            getSize(stub);
                        }

                        break;

                    case "deleteData":
                        if (commandLength == 1) {
                            deleteData(stub);
                        }
                        break;

                    case "exit":
                        running = false;
                        break;

                    default:
                        System.out.println("[Fehler] Eingabefehler. Bitte auf die Eingabeform achten");
                        break;
                }
            }
        } finally {
            channel.shutdownNow();
            channel.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

}
