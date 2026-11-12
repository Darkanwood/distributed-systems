import java.io.IOException;
import java.nio.file.Path;

import io.grpc.examples.routeguide.ComData;
import io.grpc.examples.routeguide.DataFunctionGrpc;
import io.grpc.stub.StreamObserver;

public class Server {

    public static void main(String[] args) throws Exception {
        // int portNumber = Integer.parseInt(args[0]);
        int portNumber = 50051;

        GRCP_Database database = new GRCP_Database(
                Path.of("distributed-systems", "Uebung04", "GRCP_Database.json"));

        io.grpc.Server server = io.grpc.ServerBuilder
                .forPort(portNumber)
                .addService(new DataFunctionService(database))
                .build()
                .start();

        System.out.println("gRPC-Server läuft auf Port " + portNumber);

        server.awaitTermination();
    }

    // Funktion für die in der PROTO Datei definierten Funktionen
    private static class DataFunctionService extends DataFunctionGrpc.DataFunctionImplBase {

        private final GRCP_Database database;

        DataFunctionService(GRCP_Database database) {
            this.database = database;
        }

        @Override
        public void getRecord(ComData request, StreamObserver<ComData> responseObserver) {

            int index = request.getIndex();
            System.out.println(index);
            String city = database.getRecord(index);

            ComData response;
            System.out.println(city);

            if (city != null) {
                System.out.println("Eine Stadt ist " + city);
                response = ComData.newBuilder()
                        .setCity(city)
                        .setIndex(index)
                        .setSucces(true)
                        .build();
            } else {

                response = ComData.newBuilder()
                        .setSucces(false)
                        .build();
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getSize(ComData request, StreamObserver<ComData> responseObserver) {

            ComData response = ComData.newBuilder()
                    .setIndex(database.getSize())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void addRecord(ComData request, StreamObserver<ComData> responseObserver) {

            String city = request.getCity();
            int index = request.getIndex();
            boolean inserted = true;

            try {
                inserted = database.addRecord(index, city);
            } catch (Exception e) {
                System.err.println("Failure " + e.getMessage());
            }

            ComData response;
            if (inserted) {
                response = ComData.newBuilder()
                        .setCity(city)
                        .setIndex(index)
                        .setSucces(inserted)
                        .build();
            } else {
                response = ComData.newBuilder()
                        .setSucces(inserted)
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }

        @Override
        public void deleteData(ComData request, StreamObserver<ComData> responseObserver) {
            try{
            boolean deleted = database.deleteData();

            ComData response = ComData.newBuilder()
                    .setSucces(deleted)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            }catch(IOException e){
                System.err.print(e.getMessage());
            }

        }
    }

}
