package it.polimi.ingsw.server.model.godPowers;

import it.polimi.ingsw.server.model.*;

import java.util.List;

public class PushAdjacentOpponentPower implements MoveModifier {
    @Override
    public void changePossibleOptions(Player player, ActionController actionController, Map map) {
        Box workerBox = player.turnSequence().chosenWorker().position();
        for(Box box : map.adjacent(workerBox)){
            if(box.isOccupiedByWorkers() && !player.workers().contains(box.occupier()) && map.levelDifference(workerBox, box)<=player.turnSequence().allowedLevelDifference() && map.boxesSameDirection(workerBox,box)!=null && map.boxesSameDirection(workerBox,box).get(0).isFree())
                player.turnSequence().addPossibleDestination(box);
        }
    }

    @Override
    public void executeAction(Player player, CommunicationController communicationController, ActionController actionController, Map map, List<Player> opponents, List<WinCondition> winConditions) {
        //movePower - Minotaur
        if(player.turnSequence().chosenBox().isOccupiedByWorkers()){
            player.turnSequence().recordNewPosition(player.turnSequence().chosenBox().occupier(), map.boxesSameDirection(player.turnSequence().chosenWorker().position(), player.turnSequence().chosenBox().occupier().position()).get(0));
        }
    }
}
