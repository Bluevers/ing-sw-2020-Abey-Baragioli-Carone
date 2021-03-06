package it.polimi.ingsw.server.model.phases;

import it.polimi.ingsw.network.exceptions.ChannelClosedException;
import it.polimi.ingsw.network.exceptions.TimeOutException;
import it.polimi.ingsw.network.objects.MatchStory;
import it.polimi.ingsw.server.model.*;
import it.polimi.ingsw.server.model.godPowers.fx.DoNothing;
import it.polimi.ingsw.server.model.godPowers.fx.OpponentsCantMoveUpIfPlayerMovesUpPower;
import it.polimi.ingsw.server.model.godPowers.setUpConditions.NoSetUpCondition;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.server.model.Phase.MOVE;
import static org.junit.Assert.*;

public class EndTest {
    @Test
    public void executePhase() throws ChannelClosedException, TimeOutException {
        //-------------------------- Test 1 -----------------------------------------------------
        //this test tries a End Phase for OpponentsCantMoveUpIfPlayerMovesUpPower
        Map map = new Map();
        ActionController actionController = new ActionController();
        CommunicationController communicationController = new CommunicationController();
        //player's workers
        Worker worker1 = new Worker(false, map.position(3, 3));
        Worker worker2 = new Worker(true, map.position(1, 2));
        //player card
        List<TurnSequenceModifier> actions = new ArrayList<>();
        actions.add(0, new DoNothing());
        actions.add(1, new DoNothing());
        actions.add(2, new DoNothing());
        actions.add(3, new OpponentsCantMoveUpIfPlayerMovesUpPower());
        List<TurnSequenceModifier> playerEffectsOnOpponents = new ArrayList<>();
        playerEffectsOnOpponents.add(0, new DoNothing());
        playerEffectsOnOpponents.add(1, new DoNothing());
        playerEffectsOnOpponents.add(2, new DoNothing());
        playerEffectsOnOpponents.add(3, new DoNothing());
        GodCard none = new GodCard("none", 0, actions, new StandardWin(), new NoSetUpCondition(), playerEffectsOnOpponents);
        //player
        Player currentPlayer = new Player("santoreene97", Colour.BLUE, none);
        currentPlayer.assignWorker(worker1);
        currentPlayer.assignWorker(worker2);
        //opponents
        List<TurnSequenceModifier> effectsOnOpponents = new ArrayList<>();
        effectsOnOpponents.add(0, new DoNothing());
        effectsOnOpponents.add(1, new DoNothing());
        effectsOnOpponents.add(2, new DoNothing());
        effectsOnOpponents.add(3, new DoNothing());
        Player opponent = new Player("opponent1", Colour.GREY, new GodCard("Artemis", 2, new ArrayList<TurnSequenceModifier>(), new StandardWin(), new NoSetUpCondition(), effectsOnOpponents));
        List<Player> opponents = new ArrayList<>();
        opponents.add(opponent);
        //setUp of turnSequence to use power

        currentPlayer.turnSequence().setChosenWorker(worker1);
        currentPlayer.turnSequence().addMovableWorker(worker1);
        currentPlayer.turnSequence().addMovableWorker(worker2);
        currentPlayer.turnSequence().setChosenBox(map.position(3, 4));
        currentPlayer.turnSequence().chosenBox().build();
        assertEquals( currentPlayer.turnSequence().chosenBox().level(), 1);
        currentPlayer.turnSequence().recordBuiltOnBox(currentPlayer.turnSequence().chosenBox());
        currentPlayer.turnSequence().recordNewPosition(worker1, currentPlayer.turnSequence().chosenBox());

        assertNull(currentPlayer.turnSequence().possibleWinner());
        assertEquals(1, currentPlayer.turnSequence().allowedLevelDifference());
        assertTrue(currentPlayer.turnSequence().builtOnBoxes().contains(currentPlayer.turnSequence().chosenBox()));
        assertEquals(currentPlayer.turnSequence().newPositions().get(worker1), currentPlayer.turnSequence().chosenBox());
        End end = new End();
        end.executePhase(currentPlayer, communicationController, actionController, map, opponents, new ArrayList<WinCondition>(), new MatchStory(currentPlayer));

        for(Player enemy: opponents){
            assertEquals(0,enemy.turnSequence().allowedLevelDifference());
        }
        assertNull(currentPlayer.turnSequence().possibleWinner());
        assertTrue(currentPlayer.turnSequence().builtOnBoxes().isEmpty());
        assertTrue(currentPlayer.turnSequence().movedWorkers().isEmpty());
        assertTrue(currentPlayer.turnSequence().newPositions().isEmpty());
        //-------------------------------------------------test 2----------------------------------------------------------
        // this test tries a End Phase for OpponentsCantMoveUpIfPlayerMovesUpPower with a winning move changing possible winner
        currentPlayer.turnSequence().setChosenWorker(worker1);
        currentPlayer.turnSequence().addMovableWorker(worker1);
        currentPlayer.turnSequence().addMovableWorker(worker2);
        currentPlayer.turnSequence().setChosenBox(map.position(3,3));
        assertEquals(worker1.position(), map.position(3, 4));
        currentPlayer.turnSequence().chosenBox().build();
        currentPlayer.turnSequence().chosenBox().build();
        assertEquals(2, currentPlayer.turnSequence().chosenBox().level());
        currentPlayer.turnSequence().recordNewPosition(currentPlayer.turnSequence().chosenWorker(),currentPlayer.turnSequence().chosenBox());
        currentPlayer.turnSequence().previousBox().build();
        currentPlayer.turnSequence().previousBox().build();
        assertEquals(currentPlayer.turnSequence().previousBox().level(),3);
        currentPlayer.turnSequence().recordNewPosition(currentPlayer.turnSequence().chosenWorker(),currentPlayer.turnSequence().previousBox());
        actionController.verifyWinCondition(MOVE, new ArrayList<WinCondition>(), currentPlayer, map, opponents);
        assertEquals(currentPlayer, currentPlayer.turnSequence().possibleWinner());
        end.executePhase(currentPlayer, communicationController, actionController, map, opponents, new ArrayList<WinCondition>(), new MatchStory(currentPlayer));
        assertEquals(currentPlayer, currentPlayer.turnSequence().possibleWinner());


    }
}