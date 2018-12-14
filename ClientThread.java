/*
 * Homework 5: Chat Room
 * 
 * @author: Aidan Crump, Tsogt Enkhbat
 * 
 * Class: ClientThread
 * Purpose: Thread which handles all of the individual actions necessary for the chat room.
 * Opens input/output streams, asks names, etc.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ClientThread extends Thread {

	private String clientName = null;
	private DataInputStream is = null;
	private PrintStream os = null;
	private BufferedReader in = null;
	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	private String name;


	public ClientThread(Socket clientSocket, ClientThread[] threads) {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
	}
     
    public void send(String message) {
        os.println(message);
    }
	
	public void run() {
		int maxClientsCount = this.maxClientsCount;
		ClientThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 */
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(is));
			while (true) {
				os.println("Enter your name.");
				name = in.readLine().trim();
				if (name.indexOf('@') == -1) {
					break;
				} else {
					os.println("The name should not contain '@' character.");
				}
			}

			/* Welcome the new the client. */
			os.println("Welcome to the chat room, <" + name + ">" + "\n" + "To leave enter EXIT in a new line." + "\n");
			ChatServer.clientAdd(name);
			ChatServer.sendToALL("<" + name + "> has joined the chatroom!", this);
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] != null && threads[i] == this) {
						clientName = "@" + name;
						break;
					}
				}
			}
			
			/* Start the conversation. */
			while (true) {
				String line = in.readLine();
				if (line.startsWith("EXIT")) {
					break;
				}
				/* If the message is private send it to the given client. */
				if (line.startsWith("@")) {
					whisper(line);
				} else {
					/* The message is public, broadcast it to all other clients. */
					shoutout(line);
				}
			}
			synchronized (this) {
				ChatServer.sendToALL("*** " + name + " has left the chat room ***", this);
			}
			os.println("*** Goodbye " + name + " ***");
			//remove the client from the thread
			ChatServer.clientRemove(name, this);

			//Clean up. Set the current thread variable to null so that a new client
			//could be accepted by the server.
			synchronized (this) {
				for (int i = 0; i < maxClientsCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			//Close the output stream, close the input stream, close the socket.
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}

	//Helper method containing the code for sending a private message
	public void whisper(String line) {
		String[] words = line.split("\\s", 2);
		if (words.length > 1 && words[1] != null) {
			words[1] = words[1].trim();
			if (!words[1].isEmpty()) {
				synchronized (this) {
					for (int i = 0; i < maxClientsCount; i++) {
						if (threads[i] != null && threads[i] != this
								&& threads[i].clientName != null
								&& threads[i].clientName.equals(words[0])) {
							threads[i].os.println("<PRIVATE><" + name + ">" + words[1]);
							/*
							 * Echo this message to let the client know the private
							 * message was sent.
							 */
							this.os.println("<PRIVATE><" + name + "> " + words[1]);
						}
					}
				}
			}
		}
	}

	//Helper method containing the code to send any message that is not private
	public void shoutout(String input) {
		synchronized (this) {
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] != null && threads[i].clientName != null) {
					threads[i].os.println("<" + name + "> " + input);
				}
			}
		}
	}
}