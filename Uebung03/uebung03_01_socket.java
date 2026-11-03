import java.io.*;
import java.net.*;

public static void main(String[] args){

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        
        LogMessage.writeLogRepport.Builder builder =
            LogMessage.writeLogRepport.newBuilder();


        try (Socket socket = new Socket(hostName, portNumber)) {

            //Senden des LogIn Logs
            //Werte im vorhandenen Builder ändern
            builder.setTime((int) (System.currentTimeMillis() / 1000));
            builder.setWho("USER: " + System.getProperty("user.name"));
            builder.setWhere("Host: " + socket.getLocalAddress().getHostAddress());
            builder.setSeverityLevel(LogMessage.SeverityLevel.DEBUG);
            builder.setMessage("LogIn");
            LogMessage.writeLogRepport proto = builder.build();

            //Neue Nachricht erzeugen und Proto zuweisen. Vergleichbar mit einem SnapShot
            proto.writeDelimitedTo(socket.getOutputStream());

            //Nachricht senden
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput = stdIn.readLine();

            //Senden der User Nachricht
            builder.setTime((int) (System.currentTimeMillis() / 1000));
            builder.setWho("USER: " + System.getProperty("user.name"));
            builder.setWhere("Host: " + socket.getLocalAddress().getHostAddress());
            builder.setSeverityLevel(LogMessage.SeverityLevel.INFO);
            builder.setMessage(userInput);
            
            proto = builder.build();
            
            proto.writeDelimitedTo(socket.getOutputStream());

            //Senden des LogOut Logs
            builder.setTime((int) (System.currentTimeMillis() / 1000));
            builder.setWho("USER: " + System.getProperty("user.name"));
            builder.setWhere("Host: " + socket.getLocalAddress().getHostAddress());
            builder.setSeverityLevel(LogMessage.SeverityLevel.DEBUG);
            builder.setMessage("LogOut");
            
            proto = builder.build();
            
            proto.writeDelimitedTo(socket.getOutputStream());


            socket.getOutputStream().flush();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    
}