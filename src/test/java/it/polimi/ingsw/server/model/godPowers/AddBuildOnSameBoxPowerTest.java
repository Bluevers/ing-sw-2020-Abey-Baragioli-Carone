package it.polimi.ingsw.server.model.godPowers;

import it.polimi.ingsw.server.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AddBuildOnSameBoxPowerTest {

    @Test
    public void executeAction() {
        //-------------------------- Test 1 ---------
        Map map = new  Map();
        ActionController actionController = new ActionController();
        CommunicationController communicationController = new CommunicationController();
        Worker chosenWorker = new Worker(false, map.position(2,3));
        Worker worker = new Worker(true, map.position(0,0));
        Player player = new Player("player1", Colour.BLUE,new GodCard("Hephaestus", 6, new ArrayList<TurnSequenceModifier>(), new StandardWin(), new NoSetUpCondition(), new ArrayList<TurnSequenceModifier>()));
        player.assignWorker(chosenWorker);
        player.assignWorker(worker);
        player.turnSequence().setChosenWorker(chosenWorker);
        player.turnSequence().setChosenBox(map.position(1,3));

        TurnSequenceModifier hephaestusPower = new AddBuildOnSameBoxPower();

        actionController.initialisePossibleBuilds(player.turnSequence(), map);
        actionController.updateBuiltOnBox(player.turnSequence());
        assertEquals(1, map.position(1,3).level());
        hephaestusPower.executeAction(player, communicationController, actionController, map, new ArrayList<Player>(), new ArrayList<WinCondition>());
        assertEquals(2, map.position(1,3).level());
    }
}