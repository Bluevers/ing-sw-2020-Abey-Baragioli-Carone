package it.polimi.ingsw.server.socket;

import it.polimi.ingsw.server.controller.DataBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    final private int port;

    /**
     * constructor for server class, asks port to the user
     */
    public Server() {
        int port = 0;
        boolean valid = false;
        BufferedReader in   = new BufferedReader(new InputStreamReader(System.in));
        while (!valid) {
            System.out.println("Write port:");
            try {
                port = Integer.parseInt(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                port = 0;
            }
            if (port > 1023)
                valid = true;
            else
                System.out.println("Not valid port. Try again");
        }
        this.port = port;
    }

    /**
     * constructor for server class, takes the port number in the string parameter
     * @param parameter, contains port number in String format
     */
    public Server(String parameter) {
        int port;
        try {
            port = Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            port = 0;
        }
        this.port = port;
    }

    /**
     * constructor for server class, takes port number from parameter args, if there is not a port number in the arguments, asks the user for a port number
     * @param args arguments entered by the user launching the jar
     */
    public Server(String[] args) {
        int port = 0;
        boolean valid = false;

        if (args.length > 2 && args[1].equals("-port")) {
            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                port = 0;
            }
            if (port > 1023)
                valid = true;
            else
                System.out.println("Not valid port.");
        }

        BufferedReader in   = new BufferedReader(new InputStreamReader(System.in));
        while (!valid) {
            System.out.println("Write port:");
            try {
                port = Integer.parseInt(in.readLine());
            } catch (IOException e) {
                e.printStackTrace();
                port = 0;
            } catch (NumberFormatException e) {
                port = 0;
            }
            if (port > 1023)
                valid = true;
            else
                System.out.println("Not valid port. Try again");
        }

        this.port = port;
    }

    /**
     * establishes the socket connection to the users and creates a database for lobbies and matches
     */
    public void run() {
        ExecutorService executor = Executors.newCachedThreadPool();
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getMessage()); // Porta non disponibile
            return;
        }

        DataBase dataBase = new DataBase();

        System.out.println("Server ready");
        boolean closed = false;

        while (!closed) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch(IOException e) {
                closed = true;
                e.printStackTrace();
                System.err.println("ServerSocket is not accepting");
            }
            if(socket!=null) {
                System.out.println(socket + " tried to connect");
                executor.submit(new ClientHandler(dataBase, socket));
            }
        }
        executor.shutdown();
    }
}
