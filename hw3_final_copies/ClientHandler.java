import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	// need list of clientHandlers so we can propogate messages to all clients except for self
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // blocks and gets username to identify clientHandler in list (for not sending messages to self)
            this.username = reader.readLine();
            // add clientHandler to list
            clientHandlers.add(this);
        } catch (IOException ex) {
        	// close all resources on error
            closeAll(socket, reader, writer);
        }
    }

    // override run() for Runnable interface
    @Override
    public void run() {
        String msg;
        // read user input while socket is connected
        while (socket.isConnected()) {
            try {
                msg = reader.readLine();
                // if null, either client or server has disconnected and we need to deallocate resources
                if (msg == null) {
                    closeAll(socket, reader, writer);
                    break;
                }
                // send message to other users with newline char
                sendMessageToAll(username + ": " + msg + "\n");
            } catch (IOException ex) {
            	// close resources if socket disconnects
                closeAll(socket, reader, writer);
                break;
            }
        }
        // close resources at end
        closeAll(socket, reader, writer);
    }

    public void sendMessageToAll(String msg) {
    	// iterates thru clientHandlers, and if not sender client, write the message
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.username.equals(username)) {
                try {
                    clientHandler.writer.write(msg);
                    clientHandler.writer.flush();
                } catch (IOException e) {
                    // close relevant socket, reader, and writer if there's an error
                    closeAll(clientHandler.socket, clientHandler.reader, clientHandler.writer);
                }
            }
        }
    }

    // check if resources are null before closing them
    public void closeAll(Socket socket, BufferedReader reader, BufferedWriter writer) {
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
