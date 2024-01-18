// must behave as either client or server
//
// -s on command line will make it act as server
// 
// when acting as a client, program needs a port number and host name of the server as command line args, then connects to server on given port
// localhost
// client repeatedly reads a line of input from standard input; if it reads a line, it prints the line and sends it to the server
// client terminates on Ctrl-d
// server takes -s option (first) then port num as command line args, then listens on the port (accept())
// after receiving messages from a client, a server must reverse the message and print the message to standard output
// jake -> 'ekaj'
// -s, if used, comes as the first arg, before the port number
// if acting as server, terminates if client closes the connection

import java.net.*;
import java.io.*;

public class ClientServer {
    public static void main(String[] args) {
	if (args.length < 2 || args.length > 3) {
	    System.out.println("Usage: <-s> hostname port");
	    return;
	}
  if (!args[0].equals("-s")) {
    // case for client
    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    Socket socket = null;
    try {
      socket = new Socket(hostname, port);
      socket.setSoTimeout(15000);
      Writer out = new OutputStreamWriter(socket.getOutputStream());
      while (true) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = reader.readLine();
        if (str == null)
          break;
        out.write(str + "\r\n");
        out.flush();
      }
    } catch(IOException ex) {
        System.err.println(ex);
      } finally {
          try {
            if (socket != null) socket.close();
          } catch(IOException ex) {}
      }
  }
  else if (args[0].equals("-s")){
    // case for server
    int port = Integer.parseInt(args[1]);
	  try (ServerSocket server = new ServerSocket(port)) {
		  try (Socket connection = server.accept()) {
		      InputStreamReader reader = new InputStreamReader(connection.getInputStream());
	    	      while (true) {
		          StringBuilder s = new StringBuilder();
		          int c;
		          for (c = reader.read(); c != '\n' && c != -1; c = reader.read()) {
		             s.append((char)c);
		          }
		          if (c == -1)
			          break;
              System.out.println(s.reverse());
	    	      }
		  }  catch (IOException ex) {
		      System.err.println(ex);
		  }
	  } catch (IOException ex) {
	      System.err.println(ex);
	  }
      }
 }
}
