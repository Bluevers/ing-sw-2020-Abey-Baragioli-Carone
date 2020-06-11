package it.polimi.ingsw.server.model.godPowers.fx;

import it.polimi.ingsw.network.exceptions.ChannelClosedException;
import it.polimi.ingsw.network.objects.MatchStory;
import it.polimi.ingsw.server.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static it.polimi.ingsw.server.model.Phase.BUILD;

public class AddBuildNotEdgePower extends BuildModifier {
    @Override
    public void executeAction(Player player, CommunicationController communicationController, ActionController actionController, Map map, List<Player> opponents, List<WinCondition> winConditions,  MatchStory matchStory) throws TimeoutException, ChannelClosedException {

        actionController.verifyWinCondition(BUILD, winConditions, player, map, opponents);
        if(actionController.currentPlayerHasWon(player)){
            return;
        }
        actionController.initialisePossibleBuilds(player.turnSequence(), map);
        actionController.applyOpponentsCondition(player, opponents, 2, map);
        List<Box> edgeBoxes = new ArrayList<>();
        for(Box box : player.turnSequence().possibleBuilds()){
            if(box.isOnEdge())
                edgeBoxes.add(box);
        }
        player.turnSequence().possibleBuilds().removeAll(edgeBoxes);
        if(!player.turnSequence().possibleBuilds().isEmpty()) {
            boolean usePower = communicationController.chooseToUsePower(player);
            usePower(player, communicationController, actionController,map, opponents, winConditions, usePower, matchStory);
        }

    }

    @Override
    public void executePower(Player player, ActionController actionController, Box chosenBox, MatchStory matchStory) {
        player.turnSequence().setChosenBox(chosenBox);
        matchStory.addEvent(player.turnSequence().workersCurrentPosition(player.turnSequence().chosenWorker()).position(), matchStory.build, player.turnSequence().chosenBox().position());
        actionController.updateBuiltOnBox(player.turnSequence());
    }

    @Override
    public void usePower(Player player, CommunicationController communicationController, ActionController actionController, Map map, List<Player> opponents, List<WinCondition> winConditions, boolean usePower, MatchStory matchStory) throws TimeoutException, ChannelClosedException {
        if (usePower){
            Box chosenBox = communicationController.chooseBuild(player, player.turnSequence().possibleBuilds());
            if(chosenBox!=null) {
                executePower(player, actionController, chosenBox, matchStory);
                communicationController.updateView(player, map.createProxy());
            }
        }
    }
}
