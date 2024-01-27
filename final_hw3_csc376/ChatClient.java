import java.io.*;
import java.net.Socket;

public class ChatClient {
	// fields for socket, writer to output, and username for client
    private Socket socket;
    private BufferedWriter writer;
    private String username;

    public ChatClient(Socket socket, String username) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
        } catch (IOException io) {
        	// method to close resources, saves nested try/catch
            closeAll(socket, writer);
        }
    }

    public void send() {
        try {
            // start by sending username to server to add to list of clients
            writer.write(username);
            // newline to 
            writer.newLine();
            // flush buffer
            writer.flush();

            // opening reader here bypasses blocked code using System.in instead of socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            // while socket connected, read user input and write to socket output stream
            while (socket.isConnected()) {
                String msg = reader.readLine();
                // if msg is null, then other streams are likely closed
                if (msg == null) {
                    break;
                }
                writer.write(msg);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException io) {
            // need catch block
        } finally {
        	// close all resources once socket is disconnected
            closeAll(socket, writer);
        }
    }

    // need to listen on another thread or else readLine() blocks all other execution
    public void listenForMessage() {
        new Thread(() -> {
            String msg;
            // setup input stream and check if it is null
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while ((msg = reader.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException io) {
                // need catch block
            } finally {
            	// close all resources after disconnected
                closeAll(socket, writer);
            }
        }).start();
    }
    
    // checks if resources are null before closing them
    public void closeAll(Socket socket, BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // runs whenever we start new ChatClient sessions
    public static void main(String[] args) {
        try {
        	// grab port number from args[]
            int port = Integer.parseInt(args[0]);
            // open socket on localhost
            Socket socket = new Socket("localhost", port);

            // read username from standard input for instantiating new client
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter your username: ");
            String username = reader.readLine();

            // instantiate new client
            ChatClient client = new ChatClient(socket, username);
            // executes on another thread, so isn't blocking
            client.listenForMessage();
            // blocks and sends any messages inputted by user
            client.send();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
