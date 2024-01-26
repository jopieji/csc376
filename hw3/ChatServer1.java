import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static List<ClientHandler> connectedClients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                connectedClients.add(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void notifyClientsAboutShutdown() {
        for (ClientHandler client : connectedClients) {
            client.notifyServerShutdown();
        }
    }
}

