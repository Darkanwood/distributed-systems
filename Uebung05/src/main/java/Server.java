import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Server {


    public static void main(String[] args) throws Exception {
        // int portNumber = Integer.parseInt(args[0]);
        int portNumber = 50051;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

            GRCP_Database database = new GRCP_Database(
                Path.of("Uebung05", "GRCP_Database.json"));

            System.out.println("Server wartet auf Port " + portNumber);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Client verbunden: " + clientSocket.getRemoteSocketAddress());

                Thread socketThread = new Thread(() -> {

                    try (Socket socket = clientSocket;
                            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    socket.getInputStream(), StandardCharsets.UTF_8));

                            OutputStream out = socket.getOutputStream()) 
                    {
                        Gson gson = new Gson();
                        String requestText;

                        boolean connection = true;

                        while ( (connection) && ((requestText = in.readLine()) != null) ){
                            JsonObject request = JsonParser
                                        .parseString(requestText)
                                        .getAsJsonObject();
                        
                        
                        String method = request.get("method").getAsString();
                        JsonObject params = request.getAsJsonObject("params");
                        
                        JsonObject response;
                    

                        switch(method){
                            case "getSize": 
                            {
                                int size = database.getSize();

                                response = new JsonObject();
                                response.addProperty("jsonrpc", "2.0");
                                response.addProperty("result", size);

                                break;
                            }
                            case "getRecord": 
                            { 
                                int index = params.get("index").getAsInt();
                                String city = database.getRecord(index);

                                    response = new JsonObject();
                                    response.addProperty("jsonrpc", "2.0");
                                    response.addProperty("result", city);
   
                                break;
                            }
                            case "addRecord": 
                            {
                                int index = params.get("index").getAsInt();
                                String city = params.get("city").getAsString();

                                boolean added = database.addRecord(index, city);

                                response = new JsonObject();
                                response.addProperty("jsonrpc", "2.0");
                                response.addProperty("result", added);

                                break;
                            }
                            case "deleteData": 
                            {
                                boolean deleted = database.deleteData();

                                response = new JsonObject();
                                response.addProperty("jsonrpc", "2.0");

                                response.addProperty("result", deleted);

                                break;
                            }
                            case "quit": 
                            {   
                                connection = false;

                                response = new JsonObject();
                                response.addProperty("jsonrpc", "2.0");
                                response.addProperty("result", connection);

                                break;
                            }
                            default: 
                            {   
                                JsonObject error = new JsonObject();
                                error.addProperty("code", -1);
                                error.addProperty("message", "Method not found");

                                response = new JsonObject();
                                response.addProperty("jsonrpc", "2.0");
                                response.add("error", error);

                            }
                        }

                        if(request.has("id")){
                            response.add("id", request.get("id"));
                            String responseText = gson.toJson(response);

                            out.write((responseText + "\n").getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                });
                socketThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

}
