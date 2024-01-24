import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    public static void main(String[] args) throws IOException {
        // get port number from args[]
        int port = Integer.parseInt(args[0]);

        try {
            // setup ServerSocket using port number
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            while (!serverSocket.isClosed()) {
                // setup Socket using ServerSocket.accept()
                Socket socket = serverSocket.accept();

                // if the above blocking code gets passed, a client has connected
                // we use it's port to create a client handler and add it to our client list

                // instantiate client handler
                ClientHandler handler = new ClientHandler(socket);

                // create ClientHandler thread, then start it
                // this will allow it to wait for input  without blocking other clients
                // when it receives a message, it will broadcast the message to other clients
                Thread thread = new Thread(handler);
                thread.start();

            }


        } catch (IOException e) {
            // shut down server?
            e.printStackTrace();
        }

    }

}
