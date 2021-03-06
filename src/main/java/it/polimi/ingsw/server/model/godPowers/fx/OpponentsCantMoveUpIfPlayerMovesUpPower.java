package it.polimi.ingsw.server.model.godPowers.fx;

import it.polimi.ingsw.network.objects.MatchStory;
import it.polimi.ingsw.server.model.*;

import java.util.List;

public class OpponentsCantMoveUpIfPlayerMovesUpPower extends MoveModifier {

    /**
     * This method sets opponent's allowedLevelDifferent to 0 for next turn if current player moves up
     * @param player Player that has the card
     * @param communicationController Communication controller
     * @param actionController Action controller
     * @param map Map of the match
     * @param opponents Player's opponents
     * @param winConditions List of win conditions
     * @param matchStory Last turn story
     */
    @Override
    public void executeAction(Player player, CommunicationController communicationController, ActionController actionController, Map map, List<Player> opponents, List<WinCondition> winConditions, MatchStory matchStory) {
        //endPower - Athena
        Box previousPosition = player.turnSequence().previousBox();
        Box currentPosition = player.turnSequence().workersCurrentPosition(player.turnSequence().chosenWorker());

        int counter1 = 0;

        for (Box box: player.turnSequence().builtOnBoxes()) {
            if (box.equals(previousPosition))
                counter1++;
        }

        for (Box box: player.turnSequence().removedBlocks()) {
            if (box.equals(previousPosition))
                counter1--;
        }

        if (previousPosition.hasDome())
            counter1--;

        int previousLevel = previousPosition.level() - counter1;
        int currentLevel = currentPosition.level();

        if(currentLevel - previousLevel > 0){
            for(Player opponent : opponents) {
                opponent.turnSequence().setAllowedLevelDifference(0);
            }
        }
    }
}
