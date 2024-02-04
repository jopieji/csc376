import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private Socket socket;
    private BufferedWriter writer;
    private String username;

    public class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.username = reader.readLine();
                // Add clientHandler to the static list in ChatServer
                ChatServer.addClientHandler(this);
            } catch (IOException ex) {
                closeAll(socket, reader, writer);
            }
        }

        @Override
        public void run() {
            // Existing implementation remains unchanged
        }

        public void sendMessageToAll(String msg) {
            ChatServer.sendMessageToAllClients(msg, this);
        }

        // Existing implementation remains unchanged

    }

    public ChatClient(Socket socket, String username) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException io) {
            closeAll(socket, writer);
        }
    }

    public void send() {
        try {
            writer.write(username);
            writer.newLine();
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (socket.isConnected()) {
                String msg = reader.readLine();
                if (msg == null) {
                    break;
                }
                writer.write(msg);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            closeAll(socket, writer);
        }
    }

    public void listenForMessage() {
        // Existing implementation remains unchanged
    }

    public void closeAll(Socket socket, BufferedWriter writer) {
        // Existing implementation remains unchanged
    }

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(args[0]);
            Socket socket = new Socket("localhost", port);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter your username: ");
            String username = reader.readLine();

            ChatClient client = new ChatClient(socket, username);
            client.listenForMessage();
            client.send();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

