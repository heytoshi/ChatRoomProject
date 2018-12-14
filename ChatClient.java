/*
 * Homework 5: Chat Room
 * 
 * @author: Aidan Crump, Tsogt Enkhbat
 * 
 * Class: ChatClient
 * Purpose: Client side of the chat room. Allows user to send and receive messages
 * from other active users connected to the server.
 */

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {

  // The client socket
  private static Socket clientSocket = null;
  // The output stream
  private static PrintStream os = null;
  // The input stream
  private static DataInputStream is = null;

  private static BufferedReader inputLine = null;
  private static BufferedReader in = null;
  private static boolean closed = false;
  
  
  public static void main(String[] args) {

    // The default port.
    int portNumber = 9889;
    // The default host.
    String host = "localhost";

    if (args.length < 2) {
      System.out.println("Usage: java ChatClient <host> <portNumber>\n"
              + "Now using host=" + host + ", portNumber=" + portNumber);
    } else {
      host = args[0];
      portNumber = Integer.valueOf(args[1]).intValue();
    }

    //Open a socket on a given host and port. Open input and output streams.
    try {
      clientSocket = new Socket(host, portNumber);
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new DataInputStream(clientSocket.getInputStream());
      in = new BufferedReader(new InputStreamReader(is));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }

    /* If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber. */
    if (clientSocket != null && os != null && is != null) {
      try {

        // Create a thread to read from the server.
        new Thread(new ChatClient()).start();
        
        while (!closed) {
          os.println(inputLine.readLine().trim());
        }
        
         //Close the output stream, close the input stream, close the socket.
        os.close();
        is.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println("IOException:  " + e);
      }
    }
  }

  //Create a thread to read from the server.
  public void run() {
     /* Keep on reading from the socket till we receive "Bye" from the 
      * server. Once we received that then we want to break. */
    String responseLine;
    try {
      while ((responseLine = in.readLine()) != null) {
        System.out.println(responseLine);
        if (responseLine.indexOf("*** Goodbye") != -1)
          break;
      }
      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}