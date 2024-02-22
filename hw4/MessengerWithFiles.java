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

    // TODO: send file size before transmitting bytes; Modify to open connections on new port
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
            client.sendPortToServer();
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

    public void sendPortToServer() {
        DataOutputStream output;
        if (client_cmd_output == null) output = server_cmd_output;
        else output = client_cmd_output;
        try {
            ServerSocket file_transfer_server_socket = new ServerSocket(6003);
            output.writeByte('p');
            output.writeInt(6003);
            Socket file_transfer_client_socket = file_transfer_server_socket.accept();
            output.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void readMessageAndSend() {
        DataOutputStream output;
        if (client_cmd_output == null) output = server_cmd_output;
        else output = client_cmd_output;
        // message read and send logic
        try {
            System.out.println("Enter your message:");
            // TODO: implement 'f' type messages to be sent
            Scanner sc = new Scanner(System.in);
            String msg = sc.nextLine();
            output.writeByte('t');
            byte[] messageBytes = msg.getBytes(StandardCharsets.UTF_8);
            output.writeInt(messageBytes.length);
            output.write(messageBytes);
            output.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // TODO: Fix for handling file requests as well
    public void receiveMessage() throws IOException {
        DataInputStream input;
        if (client_cmd_input == null) input = server_cmd_input;
        else input = client_cmd_input;
        try {
            char header = (char) input.readByte();

            // text message case
            if (header == 't') {
                // read length of message sent by client to instantiate byte array of correct size
                int message_length = input.readInt();
                byte[] message_bytes = new byte[message_length];
                input.readFully(message_bytes);
                String message_received = new String(message_bytes, StandardCharsets.UTF_8);
                System.out.println(message_received);
            } else {
                System.out.println("Step into file logic");
                // file transfer case
                int file_port = input.readInt();
                Socket file_connection = new Socket("localhost", file_port);

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
                new Thread(() -> {
                    while (true) {
                        try {
                            client.receiveMessage();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
                // new thread for prompting user and executing actions
                    new Thread(() -> {
                        while (true) {
                            try {
                                String act = client.promptUser();
                                client.executeAction(client, act);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).start();
            } else {
                MessengerWithFiles server = new MessengerWithFiles(server_cmd_port, "y");
                // listen for messages in another thread
                new Thread(() -> {
                    while (true) {
                        try {
                            server.receiveMessage();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
                // server thread for prompting input and sending to client
                new Thread(() -> {
                    while (true) {
                        try {
                            String act = server.promptUser();
                            server.executeAction(server, act);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
