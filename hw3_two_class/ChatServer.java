import java.net.ServerSocket;
import java.util.*;
import java.io.BufferedReader;
import java.net.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private List<ChatClient.ClientHandler> clientHandlers;

    public ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
    }
    
	public void startServer() {
		try {
			// while server socket is open, accept new connections
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				// when new connection received, we instantiate a ClientHandler
				ChatClient.ClientHandler clientHandler = new ChatClient.ClientHandler(socket);
 				// add the new ClientHandler to our list
				clientHandlers.add(clientHandler);
				// create new Thread wrapping clientHandler (which is Runnable)
				Thread clientThread = new Thread(clientHandler);
				// start clientHandler thread so server can accept other clients
				clientThread.start();
				
				
			}
		} catch (IOException ex) {
			// close server socket on exception
			closeServerSocket();
		}
		
	}
	// check that sockets are null before closing them; saves try/catch elsewhere
	public void closeServerSocket() {
                for (ChatClient.ClientHandler clientHandler : clientHandlers) {
			clientHandler.closeAll(clientHandler.getSocket(), clientHandler.getReader(), clientHandler.getWriter());
		}
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
	
    // on running of server, we setup the port to listen on and instantiate a ChatServer before listening for ChatClient connections
	public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = new ServerSocket(port);
    ChatServer server = new ChatServer(serverSocket);
		server.startServer();
	}
}
