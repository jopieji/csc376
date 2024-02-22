import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MessengerWithFiles {
    Socket client_cmd_socket;
    Socket server_client_cmd_socket;
    ServerSocket server_cmd_socket;
    static int server_cmd_port;
    static int client_cmd_port;
    static String host;
    static String sender_flag;

    // Choosing to instantiate cmd data streams when client and server are created
    // the data data streams will be created when file transfer requests are made
    DataInputStream client_cmd_input;
    DataOutputStream client_cmd_output;
    DataInputStream server_cmd_input;
    DataOutputStream server_cmd_output;


    // TODO: implement functions to start in other threads
    /*
    new Thread(() -> {
    // code goes here.
    }).start();
     */

    // for acting as client
    public MessengerWithFiles(int port_number) throws IOException {
        client_cmd_socket = new Socket("localhost", port_number);
        client_cmd_input = new DataInputStream(client_cmd_socket.getInputStream());
        client_cmd_output = new DataOutputStream(client_cmd_socket.getOutputStream());
    }

    // for acting as server
    public MessengerWithFiles(int port_number, String f) throws IOException {
        server_cmd_socket = new ServerSocket(port_number);
        server_client_cmd_socket = server_cmd_socket.accept();
        // TODO: might be incorrect socket; need to see what data is being transported where
        server_cmd_input = new DataInputStream(server_client_cmd_socket.getInputStream());
        server_cmd_output = new DataOutputStream(server_client_cmd_socket.getOutputStream());
    }

    // TODO: send file size before transmitting bytes
    // use buffer array to partition bytes into multiple packets
    public void serviceRequest() throws IOException {
        client_cmd_input = new DataInputStream(client_cmd_socket.getInputStream());
        client_cmd_output = new DataOutputStream(client_cmd_socket.getOutputStream());
        String file_name = client_cmd_input.readUTF();
        File file = new File(file_name);
        if (file.exists() && file.canRead()) {
            // return the number of bytes in the file as a long int
            long file_size = file.length();
            if (file_size > 0)
                client_cmd_output.writeLong(file_size);
            else {
                client_cmd_output.writeLong(0L);
                return;
            }
        } else {
            client_cmd_output.writeLong(0L);
            return;
        }
        FileInputStream file_input = new FileInputStream(file);
        System.out.println("Transmitting file: " + file_name);
        byte[] file_buffer = new byte[1500];
        int number_read;
        while ((number_read = file_input.read(file_buffer)) != -1)
            client_cmd_output.write(file_buffer, 0, number_read);
        file_input.close();
    }

    public void closeServer() throws IOException {
        server_client_cmd_socket.close();
        server_cmd_socket.close();
    }

    public int getFile(String file_name) throws IOException {
        client_cmd_output.writeUTF(file_name);
        long file_size = client_cmd_input.readLong();
        if (file_size == 0)
            return 0;
        FileOutputStream file_out = new FileOutputStream(file_name);
        int number_read;
        byte[] buffer = new byte[10000];
        while ((number_read = client_cmd_input.read(buffer)) != -1)
            file_out.write(buffer, 0, number_read);
        file_out.close();
        return number_read;
    }

    public void closeClient() throws IOException {
        client_cmd_socket.close();
    }

    public String promptUser() {
        System.out.println("Enter an option ('m', 'f', 'x'):\n\t (M)essage (send)\n\t (F)ile (request)\n\te(X)it");
        Scanner sc = new Scanner(System.in);
        String opt = sc.nextLine();
        if (opt.equals("m") || opt.equals("f") || opt.equals("x")) return opt;
        else return "Error";
    }

    public static void parseArgs(String[] args) {
        if (args.length != 2 && args.length != 4) {
            System.out.println("Usage: java MessengerWithFiles [-l] <listening port number> <file name> \\ [-p] <connect server port> [-s] [connect server address]");
            System.exit(0);
        } else {
            if (args.length == 2) {
                parseArgsServer(args);
            } else {
                parseArgsClient(args);
            }
        }
    }

    public static void parseArgsServer(String[] args) {
        server_cmd_port = Integer.valueOf(args[1]);
        host = "localhost";
        sender_flag = "s";
    }

    public static void parseArgsClient(String[] args) {
        server_cmd_port = Integer.valueOf(args[1]);
        if (args.length == 6) {
            host = args[5];
        } else {
            host = "localhost";
        }
        sender_flag = "c";
        client_cmd_port = Integer.valueOf(args[3]);
    }

    public void executeAction(MessengerWithFiles client, String act) throws IOException {
        if (act.equals("m")) {
            client.readMessageAndSend();
        } else if (act.equals("f")) {
            System.out.println("Enter a file name: ");
            Scanner sc = new Scanner(System.in);
            String file_name = sc.nextLine();
            try {
                client.getFile(file_name);
            } catch (IOException io) {
                io.printStackTrace();
            }
        } else if (act.equals("x")) {
            System.exit(0);
            // TODO: send null message to close opposite client
            // closeAllResourcesAndSendNullMessage();
        }
    }

    public void readMessageAndSend() {
        // message read and send logic
        try {
            System.out.println("Enter your message:");
            // TODO: implement 'f' type messages to be sent
            // write as bytes and decode on receiving side
            // string builder to be converted to byte array
            // append t for text message
            // setup scanner to get message
            Scanner sc = new Scanner(System.in);
            String msg = sc.nextLine();
            client_cmd_output.writeByte('t');
            byte[] messageBytes = msg.getBytes(StandardCharsets.UTF_8);
            client_cmd_output.writeInt(messageBytes.length);
            client_cmd_output.write(messageBytes);
            client_cmd_output.flush();

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // TODO: Fix for handling file requests as well
    // TODO: run function in other thread
    public void receiveMessageFromClient() throws IOException {
        try {
            char header = (char) server_cmd_input.readByte();
            System.out.println(header);
            // text message case
            if (header == 't') {
                int messageLength = server_cmd_input.readInt(); // Read the length of the message
                byte[] messageBytes = new byte[messageLength];
                server_cmd_input.readFully(messageBytes); // Read the message bytes
                String message_received = new String(messageBytes, StandardCharsets.UTF_8);
                System.out.println(message_received);
            } else {
                System.out.println("implement correct file logic in function");
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // get port number and file name from command line
        // error checking also in function
        parseArgs(args);

        try {
            if (sender_flag.equals("c")) {
                MessengerWithFiles client = new MessengerWithFiles(client_cmd_port);
                boolean loop = true;
                while (loop) {
                    String act = client.promptUser();
                    client.executeAction(client, act);
                }
                long file_size = 0;
                if (file_size > 0)
                    System.out.println("File length: " + file_size);
                else
                    System.out.println("No file received");
            } else {
                // TODO: Server logic
                MessengerWithFiles server = new MessengerWithFiles(server_cmd_port, "y");
                // listen for messages
                //BufferedReader reader = new BufferedReader(new InputStreamReader(server.server_client_cmd_socket.getInputStream()));
                while (true) {
                    server.receiveMessageFromClient();
                    // TODO: server closing after first message?
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
