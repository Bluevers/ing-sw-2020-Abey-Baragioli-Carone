package it.polimi.ingsw.client;

import it.polimi.ingsw.server.model.Box;
import it.polimi.ingsw.server.model.GodCard;
import it.polimi.ingsw.server.model.Worker;

import java.util.List;

public class Gui implements View {
    @Override
    public int askBox(List<Box> boxes) {
        return 0;
    }

    @Override
    public int askWorker(List<Worker> workers) {
        return 0;
    }

    @Override
    public int askCards(List<GodCard> cards) {
        return 0;
    }

    @Override
    public boolean askConfirmation() {
        return false;
    }

    @Override
    public void prepareAdditionalCommunication(String message) {

    }

    @Override
    public void updateMap(List<Box> boxes) {

    }

    @Override
    public String askIp() {
        return null;
    }

    @Override
    public int askPort() {
        return 0;
    }
}
