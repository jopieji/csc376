import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
  // server socket field so we can close and check if its open/closed
  private ServerSocket serverSocket;
  // keep track of clients; socket for easy access to resources and username to check what client is sender/receiving
  private Map<Socket, String> clients;

  // constructor to instantiate server
  public ChatServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      clients = new HashMap<>();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  // start method to call in main
  public void start() {
    try {
      // if server socket is open...
      while (!serverSocket.isClosed()) {
        try {
          // accept client connections
          Socket clientSocket = serverSocket.accept();
          // setup and start thread for each client, calling handleClient with our client connection in that thread
          Thread clientThread = new Thread(() -> handleClient(clientSocket));
          clientThread.start();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } finally {
      closeAllClients();
    }
  }

  private void closeAllClients() {
    // iterate thru clients (all items in set)
    for (Map.Entry<Socket, String> entry : clients.entrySet()) {
      // grab socket to make checks
      Socket socket = entry.getKey();
      try {
        // if check if socket is null/closed before closing resources
        if (socket != null && !socket.isClosed()) {
          // close the input and output streams before closing the socket
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          reader.close();
          writer.close();
          socket.close();
        }
      } catch (IOException io) {
        io.printStackTrace();
      }
    }
    // remove all remaining clients from clients list
    clients.clear();
  }

  // shutdown hook for this runtime; we can make sure this code runs on exit
  public void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      // broadcast shutdown message to all clients so they exit: more graceful than looping to check connections on a timer
      broadcastMessage("server", "SHUTDOWN", null);
      closeAllClients();
    }));
  }

  // handleClient method to run in separate thread to setup client and send messages
  private void handleClient(Socket clientSocket) {
    String username = "";
    try {
      // setup reader and writer for each client to read messages and write to client
      BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

      // get the username from the client
      username = reader.readLine();

      // add each client to list of clients 
      clients.put(clientSocket, username);
      String message;
      // while input isn't null, broadcast any input to all other clients using broadcastMessage() method
      while ((message = reader.readLine()) != null) {
        broadcastMessage(username, message, writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // Remove the client when it disconnects
      try {
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      removeClient(clientSocket);
    }
  }

  // broadcastMessage to send messages to all clients
  private synchronized void broadcastMessage(String sender, String message, BufferedWriter writer) {
    // for each client in set of clients...
    for (Map.Entry<Socket, String> entry : clients.entrySet()) {
      // get socket and username for checking sender/recipient and closing resources
      Socket socket = entry.getKey();
      String username = entry.getValue();
      // skip iteration if broadcast is being sent to sender
      if (username.equals(sender)) {
        continue;
      }
      // otherwise...
      try {
        // setup writer to broadcast to each client
        BufferedWriter clientWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // shutdown logic; closes clients
        if (message.equals("SHUTDOWN") && sender.equals("server")) { clientWriter.write("SHUTDOWN"); }
        // otherwise, write message with username
        else { clientWriter.write(sender + ": " + message + "\n"); }
        clientWriter.flush();
      } catch (IOException io) {
        io.printStackTrace();
      }
    }
  }

  private synchronized void removeClient(Socket clientSocket) {
    clients.remove(clientSocket);
  }

  public static void main(String[] args) {
    // instantiate ChatServer wtih port from args
    int port = Integer.parseInt(args[0]);
    ChatServer server = new ChatServer(port);
    // shutdown hook needs to be attached to runtime
    server.addShutdownHook();
    // start the server
    server.start();
  }
}

