import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MessengerWithFiles {
    Socket client_socket;
    Socket server_client_socket;
    ServerSocket server_socket;
    static int server_port;
    static int client_port;
    static String host;
    static String flag;
    DataInputStream input;
    DataOutputStream output;

    // TODO: Edit to correctly transfer file, not file fize
    // use buffer array
    public void serviceRequest() throws IOException {
        input = new DataInputStream(client_socket.getInputStream());
        output = new DataOutputStream(client_socket.getOutputStream());
        String file_name = input.readUTF();
        File file = new File(file_name);
        if (file.exists() && file.canRead()) {
            // return the number of bytes in the file as a long int
            long file_size = file.length();
            if (file_size > 0)
                output.writeLong(file_size);
            else {
                output.writeLong(0L);
                return;
            }
        } else {
            output.writeLong(0L);
            return;
        }
        FileInputStream file_input = new FileInputStream(file);
        System.out.println("Transmitting file: " + file_name);
        byte[] file_buffer = new byte[1500];
        int number_read;
        while ((number_read = file_input.read(file_buffer)) != -1)
            output.write(file_buffer, 0, number_read);
        file_input.close();
    }

    public void closeServer() throws IOException {
        server_client_socket.close();
        server_socket.close();
    }


    public MessengerWithFiles(int port_number) throws IOException {
        client_socket = new Socket("localhost", port_number);
        input = new DataInputStream(client_socket.getInputStream());
        output = new DataOutputStream(client_socket.getOutputStream());
    }

    public MessengerWithFiles(int port_number, String f) throws IOException {
        server_socket = new ServerSocket(port_number);
        server_client_socket = server_socket.accept();
    }


    public byte[] getFile(String file_name) throws IOException {
        output.writeUTF(file_name);
        long file_size = input.readLong();
        if (file_size == 0)
            return null;
        FileOutputStream file_out = new FileOutputStream(file_name);
        int number_read;
        byte[] buffer = new byte[100000];
        while ((number_read = input.read(buffer)) != -1)
            file_out.write(buffer, 0, number_read);
        file_out.close();
        return buffer;
    }

    public void closeClient() throws IOException {
        client_socket.close();
    }

    public String promptUser() {
        System.out.println("Enter an option ('m', 'f', 'x'):\n\t(M)essage (send)\n\t(F)ile (request)\n  e(X)it");
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
        server_port = Integer.valueOf(args[1]);
        host = "localhost";
        flag = "s";
    }

    public static void parseArgsClient(String[] args) {
        server_port = Integer.valueOf(args[1]);
        if (args.length == 6) {
            host = args[5];
        } else {
            host = "localhost";
        }
        flag = "c";
        client_port = Integer.valueOf(args[3]);
    }

    public void executeAction(MessengerWithFiles client, String act) throws IOException {
        if (act.equals("m")) {
            client.readMessageAndSend();
        } else if (act.equals("f")) {
            //getFileAndSendBytes();
            // using old function for testing
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client_socket.getOutputStream()));
            String msg = reader.readLine();
            writer.write(msg);
            writer.newLine();
            writer.flush();
            reader.close();
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // get port number and file name from command line
        // error checking also in function
        parseArgs(args);

        try {
            if (flag.equals("c")) {
                MessengerWithFiles client = new MessengerWithFiles(client_port);
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
                MessengerWithFiles server = new MessengerWithFiles(server_port, "y");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
