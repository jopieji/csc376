import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessengerWithFiles {
    Socket client_socket;
    static int port;
    static String host;
    static String flag;
    DataInputStream input;
    DataOutputStream output;


    public MessengerWithFiles( int port_number ) throws IOException {
        client_socket= new Socket("localhost", port_number);
        input= new DataInputStream( client_socket.getInputStream() );
        output= new DataOutputStream( client_socket.getOutputStream() );
    }

    public long getFile( String file_name ) throws IOException {
        output.writeUTF( file_name );
        long file_size= input.readLong();
        if ( file_size == 0 )
            return 0;
        FileOutputStream file_out= new FileOutputStream( file_name );
        int number_read;
        byte[] buffer= new byte[1500];
        while( (number_read= input.read( buffer)) != -1 )
            file_out.write( buffer, 0, number_read );
        file_out.close();
        return file_size;
    }

    public void close() throws IOException {
        client_socket.close();
    }

    public static void parseArgs(String[] args) {
        if ( args.length != 2 || args.length != 5 || args.length != 7 )
        {
            System.out.println( "Usage: java MessengerWithFiles [-l] <listening port number> <file name> \\ [-p] <connect server port> [-s] [connect server address]" );
            System.exit(0);
        }
        else {
            if (args.length == 2) {
                parseArgsServer(args);
            } else if (args.length == 5 || args.length == 7) {
                parseArgsClient(args);
            }
        }
    }

    public static void parseArgsServer(String[] args) {
        port = Integer.valueOf(args[1]);
        host = "localhost";
        flag = "s";
    }

    public static void parseArgsClient(String[] args) {
        port = Integer.valueOf(args[1]);
        if (args.length == 7) {
            host = args[6];
        } else {
            host = "localhost";
        }
        flag = "c";
    }

    public static void main(String[] args) {
        // get port number and file name from command line
        // error checking also in function
        parseArgs(args);

        try {
            if (flag.equals("c")) {
                MessengerWithFiles client= new MessengerWithFiles(port);
                long file_size= client.getFile(args[1]);
                if ( file_size > 0 )
                    System.out.println( "File length: " + file_size );
                else
                    System.out.println( "No file received" );
            } else {
                // TODO: Server logic
            }
        }
        catch ( Exception e ){
            System.out.println( e.getMessage() );
        }
    }

}
