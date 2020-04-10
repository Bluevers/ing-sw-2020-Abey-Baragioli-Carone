package it.polimi.ingsw.server.model.godPowers;

import it.polimi.ingsw.server.model.*;

import java.util.List;

import static it.polimi.ingsw.server.model.Phase.BUILD;

public class AddBuildNotEdgePower implements BuildModifier {

    @Override
    public void changePossibleOptions(Player player, ActionController actionController, Map map) {

    }

    @Override
    public void executeAction(Player player, CommunicationController communicationController, ActionController actionController, Map map, List<Player> opponents, List<WinCondition> winConditions) {
        //buildPower-Hestia
        boolean usePower = communicationController.chooseToUsePower();
        if(usePower){
            actionController.verifyWinCondition(BUILD, winConditions, player, map, opponents);
            if(actionController.currentPlayerHasWon(player)){
                return;
            }
            actionController.initialisePossibleBuilds(player.turnSequence(), map);
            actionController.applyOpponentsCondition(player, opponents, 2, map);
            for(Box box : player.turnSequence().possibleBuilds()){
                if(box.isOnEdge())
                    player.turnSequence().possibleBuilds().remove(box);
            }
            if(player.turnSequence().possibleBuilds().isEmpty()){
                //todo comunicare all'utente che non può usare il suo potere aggiuntivo
                return;
            }
            Box chosenBox = communicationController.chooseBox(map);
            player.turnSequence().setChosenBox(chosenBox);
            actionController.updateBuiltOnBox(player.turnSequence());
        }
    }
}
