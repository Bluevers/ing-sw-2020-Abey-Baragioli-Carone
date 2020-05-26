package it.polimi.ingsw.network;

import it.polimi.ingsw.network.exceptions.ChannelClosedException;

import static it.polimi.ingsw.network.CommunicationProtocol.PING;

public class Pinger extends Thread {

    final public CommunicationChannel communicationChannel;

    public Pinger(CommunicationChannel communicationChannel) {
        this.communicationChannel = communicationChannel;
    }

    public void run() {
        while (!communicationChannel.isClosed()) {
            int countdown = 5;
            System.out.println("Check connessione");
            try {
                communicationChannel.writeKeyWord(PING);
            } catch (ChannelClosedException e) {
                communicationChannel.close();
                break;
            }
            while (countdown > 0 && !communicationChannel.isClosed() && !communicationChannel.isPinged()) {
                try {
                    sleep(1000);
                    countdown--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    countdown = 0;
                    communicationChannel.close();
                }
                System.out.println(countdown);
            }
            if (!communicationChannel.isClosed() && (countdown == 0 || !communicationChannel.isPinged())) {
                communicationChannel.close();
                System.out.println("Client non connesso");
            }
            else {
                communicationChannel.resetPing();
                System.out.println("Client connesso");
            }
        }
    }
}