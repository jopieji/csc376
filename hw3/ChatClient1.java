import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public ChatClient(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        int port = Integer.parseInt(args[0]);
        ChatClient client = new ChatClient(serverAddress, port);
        client.startListening();
        client.waitToSendMessage();
    }

    public void startListening() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void waitToSendMessage() throws IOException {
        try {
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                writer.write(message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void closeConnection() throws IOException {
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
}

