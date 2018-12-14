/*
 * Homework 5: Chat Room
 * 
 * @author: Aidan Crump, Tsogt Enkhbat
 * 
 * Class: ChatServer
 * Purpose: Creates a server which handles multiple clients in separate threads,
 * and allows each to communicate with any other active connection
 */

import java.io.*;
import java.net.*;
import java.util.*;
@SuppressWarnings("resource")

public class ChatServer {
	private static int port;

	private static List<String> clientList = new ArrayList<>();
	private static List<ClientThread> clientThreads = new ArrayList<>();

	private static final int max = 10;
	private static final ClientThread[] threads = new ClientThread[max];

	public static void main(String[] args) throws IOException {
		while (true) {
			try {
				port = Integer.parseInt(args[0]);
				ServerSocket sock = new ServerSocket(port);
				System.out.println("Using port " + port);
				Socket socket = sock.accept();
				System.out.println("New user connected");
				for (int i = 0; i < max; i++) {
					if (threads[i] == null) {
						(threads[i] = new ClientThread(socket, threads)).start();
						clientThreads.add(threads[i]);
						break;
					}
				}
			}
			catch (IOException ioe) {
			}
		}
	}

	public static void sendToALL(String message, ClientThread except) {
		for (ClientThread aUser : clientThreads) {
			if (aUser != except) {
				aUser.send(message);
			}
		}
	}

	public static void clientAdd(String userName) {
		clientList.add(userName);
	}

	public static void clientRemove(String name, ClientThread thread) {
		boolean r = clientList.remove(name);
		if (r) {
			clientThreads.remove(thread);
			System.out.println(name + "has been removed");
		}
	}

	public List<String> getUserNames() {
		return this.clientList;
	}
}