import java.net.*;
import java.io.*;
import java.util.*;

public class Client {

    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter username: ");
        String username = sc.nextLine();
        Socket socket = new Socket("localhost", port);
        Client client = new Client(socket, username);
        client.listenForOtherClientMessages();
        client.waitToSendMessage();
    }

    public void waitToSendMessage() throws IOException {
        try {
            writer.write(username);
            writer.newLine();
            writer.flush();

            Scanner sc = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = sc.nextLine();
                writer.write(username + ": " + message);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            closeAllSocketsAndStreams();
        }
    }

    public void closeAllSocketsAndStreams() throws IOException {
        if (this.socket != null) {
            this.socket.close();
        }
        if (this.writer != null) {
            this.writer.close();
        }
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public void listenForOtherClientMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        String message = reader.readLine();
                        System.out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
