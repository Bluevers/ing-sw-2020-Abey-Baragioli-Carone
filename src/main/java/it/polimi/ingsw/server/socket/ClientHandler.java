package it.polimi.ingsw.server.socket;

import it.polimi.ingsw.network.CommunicationChannel;
import it.polimi.ingsw.network.CommunicationProtocol;
import it.polimi.ingsw.network.Pinger;
import it.polimi.ingsw.network.exceptions.ChannelClosedException;
import it.polimi.ingsw.server.controller.DataBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static it.polimi.ingsw.network.CommunicationProtocol.HI;
import static it.polimi.ingsw.network.CommunicationProtocol.QUIT;

public class ClientHandler extends Thread {

    final private DataBase dataBase;
    final private Socket socket;

    ClientHandler(DataBase dataBase, Socket socket) {
        this.dataBase = dataBase;
        this.socket = socket;
    }

    /**
     * This method stores in the buffer every message that reaches
     * the communication channel till the connection is closed
     */
    @Override
    public void run() {
        setPriority(1);
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get BufferReader Serverside");
            return;
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get PrintWriter Serverside");
            return;
        }

        CommunicationChannel communicationChannel = new CommunicationChannel(in, out);
        CommunicationProtocol key = null;
        try {
            key = communicationChannel.nextKey();
        } catch (IOException | ChannelClosedException e) {
            e.printStackTrace();
            System.err.println("IO Exception");
            return;
        }

        Pinger pinger = new Pinger(communicationChannel);
        pinger.start();

        if (key == HI) {
            System.out.println("Starting registration of socket " + socket);
            dataBase.addConnection(communicationChannel);
            new UserManager(dataBase, communicationChannel).start();
        }

        while (!communicationChannel.isClosed() && !pinger.isEnded()) {
            try {
                if (communicationChannel.nextKey() == QUIT) //metere come content del quit il nome del quitter o mandare in gson
                    communicationChannel.close();           //il client stesso
            } catch (IOException | ChannelClosedException e) {
                e.printStackTrace();
                System.err.println("Error in Client Handler");
                communicationChannel.close();
            }
        }
        dataBase.deleteConnection(communicationChannel);
        // Chiudo gli stream e il socket
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't get close socket Serverside");
        }
    }
}
