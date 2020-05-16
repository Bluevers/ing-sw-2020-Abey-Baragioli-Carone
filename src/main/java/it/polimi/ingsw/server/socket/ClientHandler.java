package it.polimi.ingsw.server.socket;

import it.polimi.ingsw.network.CommunicationChannel;
import it.polimi.ingsw.network.CommunicationProtocol;
import it.polimi.ingsw.server.controller.DataBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static it.polimi.ingsw.network.CommunicationProtocol.HI;

public class ClientHandler extends Thread {
    final private DataBase dataBase;
    final private Socket socket;

    public ClientHandler(DataBase dataBase, Socket socket) {
        this.dataBase = dataBase;
        this.socket = socket;
    }

    public void run() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get BufferReader Serverside");
            System.exit(1);
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get PrintWriter Serverside");
            System.exit(1);
        }

        CommunicationChannel communicationChannel = new CommunicationChannel(in, out);
        CommunicationProtocol key = null;
        try {
            key = communicationChannel.nextKey();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't receive the first key word from client");
            System.exit(1);
        }

        if (key == HI) {
            System.out.println("Starting registration of socket " + socket);
            new UserManager(dataBase, communicationChannel).run();
        }

        while (!communicationChannel.isClosed()) {
        }
            // Chiudo gli stream e il socket
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get close socket Serverside");
            System.exit(1);
        }
    }
}