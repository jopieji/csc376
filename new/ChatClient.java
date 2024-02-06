import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

// ChatClient class
public class ChatClient {
  // fields for reader/writers and sockets
  private BufferedReader reader;
  private BufferedWriter writer;
  private Socket socket;

  // constructor to setup each client
  public ChatClient(String username, String serverAddress, int serverPort) {
    try {
      // instantiate socket, reader and writer
      socket = new Socket(serverAddress, serverPort);
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      // write username to server so we can keep track of clients by username
      writer.write(username);
      writer.newLine();
      writer.flush();
      // start new thread with receiveMessages as the de-facto run() method
      new Thread(this::receiveMessages).start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  // sendMessage method to send messages to server
  private void sendMessage() {
    try {
      // setup scanner to read messages from StdIn
      Scanner scanner = new Scanner(System.in);
      String message = scanner.nextLine();
      // write messages to server to broadcast to other clients
      writer.write(message);
      writer.newLine();
      writer.flush();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  // shutdowns all resources
  private void shutdown(Socket socket) {
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
      // exit client process once all resources are closed
      System.exit(0);
    } catch (IOException io) {
      io.printStackTrace();
    }
  }

  // receive messages method to read in another thread
  private void receiveMessages() {
    try {
      // accept messages while input isn't null
      String message;
      while ((message = reader.readLine()) != null) {
        // handle shutdown condition
        if (message.equals("SHUTDOWN")) {
          shutdown(socket);
        }
        else {
          // otherwise print message to output for each client to see
          System.out.println(message);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // main method to start client and read username
  public static void main(String[] args) {
    String serverAddress = "localhost";
    int serverPort = Integer.parseInt(args[0]);
    System.out.println("Enter your username: ");
    Scanner sc = new Scanner(System.in);
    String username = sc.nextLine();
    // instantiation of client starts receiving message thread
    ChatClient client = new ChatClient(username, serverAddress, serverPort);
    // wait to send messages while process is running
    while (true) {
      client.sendMessage();
    }
  }
}
