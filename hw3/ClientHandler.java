import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> listOfClients = new ArrayList<ClientHandler>();

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.username = reader.readLine();
        listOfClients.add(this);
    }

    @Override
    public void run() {
        // accept incoming messages
        while (socket.isConnected()) {
            String message = null;
            try {
                message = reader.readLine();
            } catch (IOException e) {
                // close clients?
                e.printStackTrace();
            }
            try {
                sendMessageToClients(message);
            } catch (IOException e) {
                // need to close clients?
                e.printStackTrace();
            }
        }
    }


    public void sendMessageToClients(String message) throws IOException {
        try {
            for (ClientHandler client : listOfClients) {
                if (!client.username.equals(this.username)) {
                    client.writer.write(message);
                    client.writer.newLine();
                    client.writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Remove ClientHandler from list if closed:
    public void removeClientFromList() {
        listOfClients.remove(this);
    }



}
