import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    public static List<ClientHandler> listOfClients = new ArrayList<>();

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        listOfClients.add(this);
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }
                sendMessageToClients(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                closeAllSocketsAndStreams();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void closeAllSocketsAndStreams() throws IOException {
        removeClientFromList();
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.shutdownInput();
            this.socket.shutdownOutput();
            this.socket.close();
        }
        if (this.writer != null) {
            this.writer.close();
        }
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public void sendMessageToClients(String message) throws IOException {
        for (ClientHandler client : listOfClients) {
            if (client != this) {
                client.writer.write(message);
                client.writer.newLine();
                client.writer.flush();
            }
        }
    }

    public void removeClientFromList() {
        listOfClients.remove(this);
    }

    public void notifyServerShutdown() {
        try {
            writer.write("Server is shutting down. Closing connection.");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

