import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MessengerWithFiles {
    Socket client_cmd_socket;
    Socket server_client_cmd_socket;
    Socket client_file_connection;
    Socket server_file_connection;
    Socket file_connection;
    ServerSocket server_cmd_socket;
    ServerSocket file_transfer_server_socket;
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
        server_cmd_input = new DataInputStream(server_client_cmd_socket.getInputStream());
        server_cmd_output = new DataOutputStream(server_client_cmd_socket.getOutputStream());
    }

    // TODO: send file size before transmitting bytes; Modify to open connections on new port
    // TODO: reset file transfer sockets to null; they can switch roles dynamically with each transfer, can't have both populated
    // TODO: use buffer array to partition bytes into multiple packets if needed
    public void transmitFileToOutputStream(String file_name) throws IOException {
        DataOutputStream output = new DataOutputStream(file_connection.getOutputStream());
        /*
        if (client_file_connection == null) {
            output = new DataOutputStream(server_file_connection.getOutputStream());
        } else {
            output = new DataOutputStream(client_file_connection.getOutputStream());
        }
         */
        File file = new File(file_name);
        int file_size;
        if (file.exists() && file.canRead()) {
            // return the number of bytes in the file as a long int
            file_size = (int) file.length();
            if (file_size > 0)
                output.writeInt(file_size);
            else {
                output.writeInt(0);
                return;
            }
        } else {
            output.writeInt(0);
            return;
        }
        FileInputStream file_input = new FileInputStream(file);
        System.out.println("Transmitting file: " + file_name);
        int number_read;
        byte[] buffer = new byte[file_size];
        while ((number_read = file_input.read(buffer)) != -1)
            output.write(buffer, 0, number_read);

        file_input.close();
    }

    public void closeServer() throws IOException {
        server_client_cmd_socket.close();
        server_cmd_socket.close();
    }

    public int receiveFile(String file_name) throws IOException {
        try {
            DataInputStream input = new DataInputStream(file_connection.getInputStream());
            int file_size = input.readInt();
            if (file_size == 0) {
                return 0;
            }
            FileOutputStream file_out = new FileOutputStream(file_name);
            int number_read;
            byte[] buffer = new byte[1000];
            while ((number_read = input.read(buffer)) != -1)
                file_out.write(buffer, 0, number_read);
            file_out.close();
            System.out.println("Starting closing connections...");
            // find correct file broadcasting stream
            DataOutputStream output;
            if (client_cmd_output == null) output = server_cmd_output;
            else output = client_cmd_output;
            output.writeByte('z');

            closeFileConnections();
            return number_read;
        } catch (SocketException se) {
            System.out.println("Socket exception");
        }
        return 0;
    }

    public void closeClient() throws IOException {
        client_cmd_socket.close();
    }

    public String promptUser() throws IOException {
        System.out.println("Enter an option ('m', 'f', 'x'):\n\t (M)essage (send)\n\t (F)ile (request)\n\te(X)it");
        BufferedReader reader = new BufferedReader(new InputStreamReader((System.in)));
        String opt = reader.readLine();

        //Scanner sc = new Scanner(System.in);
        //String opt = sc.nextLine();
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
                sendFileName(file_name);
                new Thread(() -> {
                    try {
                        System.out.println("File size: (-1 at end?) " + client.receiveFile(file_name));
                        File f_test = new File(file_name);
                        long test_size = f_test.length();
                        System.out.println("Actual size client-side: " + test_size);
                        client.promptUser();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } catch (IOException io) {
                io.printStackTrace();
            }
        } else if (act.equals("x")) {
            System.exit(0);
            // TODO: send null message to close opposite client
            // closeAllResourcesAndSendNullMessage();
        }
    }

    public void sendFileName(String file_name) throws IOException {
        DataOutputStream output;
        if (client_cmd_output == null) output = server_cmd_output;
        else output = client_cmd_output;
        output.writeUTF(file_name);
    }

    public void closeFileConnections() throws IOException {
        if (file_connection != null) {
            System.out.println("Closing file connection...");
            file_connection.close();
        }
        if (file_transfer_server_socket != null) {
            System.out.println("Closing server socket...");
            file_transfer_server_socket.close();
        }
        System.out.println("All connections closed.");
    }

    public void sendPortToServer() {
        DataOutputStream output;
        if (client_cmd_output == null) output = server_cmd_output;
        else output = client_cmd_output;
        try {
            System.out.println("Opening server socket on 6003");
            file_transfer_server_socket = new ServerSocket(6003);
            output.writeByte('p');
            output.writeInt(6003);
            file_connection = file_transfer_server_socket.accept();
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

    public void receiveCommands() throws IOException {
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
            } else if (header == 'p') {
                // file transfer case
                int file_port = input.readInt();
                System.out.println("Opening socket on " + file_port);
                file_connection = new Socket("localhost", file_port);
                // call function to transmit file
                System.out.println("Socket open on " + file_port);
                String file_name = input.readUTF();
                System.out.println("File name received from requestor..." + file_name);
                File f_test = new File(file_name);
                long test_size = f_test.length();
                System.out.println("Supposed size: " + test_size);
                new Thread(() -> {
                    try {
                        this.transmitFileToOutputStream(file_name);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            } else if (header == 'z') {
                System.out.println("Picked up Z byte!");
                Thread.sleep(100);
            	closeFileConnections();
            }
        } catch (EOFException eof) {
            System.out.println("End of file! Terminating...");
            System.exit(0);
        } catch (IOException io) {
            io.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
                            client.receiveCommands();
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
                            server.receiveCommands();
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
