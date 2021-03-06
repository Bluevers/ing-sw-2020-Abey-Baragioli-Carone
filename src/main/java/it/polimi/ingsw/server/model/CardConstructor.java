package it.polimi.ingsw.server.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.server.model.godPowers.fx.*;
import it.polimi.ingsw.server.model.godPowers.setUpConditions.*;
import it.polimi.ingsw.server.model.godPowers.winConditions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.server.model.godPowers.fx.GodFX.*;
import static it.polimi.ingsw.server.model.godPowers.setUpConditions.GodSetup.*;
import static it.polimi.ingsw.server.model.godPowers.winConditions.GodWin.*;
import static it.polimi.ingsw.server.model.godPowers.fx.GodFX.DO_NOTHING;

public class CardConstructor {
    private List<GodCard> cards = loadCards();

    public List<GodCard> cards() {
        return cards;
    }

    /**
     * This method loads the ProtoCard List from a file
     * @return List of ProtoCards
     */
    List<ProtoCard> loadProtoCardsFromFile(){
        List<ProtoCard> protoCards;

        JsonElement element = new JsonParser().parse(
                new BufferedReader(
                        new InputStreamReader(
                                getClass().getResourceAsStream("/GodCards.json")
                        )
                )
        );

        Type listType = new TypeToken<List<ProtoCard>>() {}.getType();
        protoCards = new Gson().fromJson(element, listType);

        return protoCards;
    }

    /**
     * This method loads the fx used by the protocards identifying them by their name
     * @return HashMap
     */
    Map<GodFX, TurnSequenceModifier> loadFX() {
        Map<GodFX, TurnSequenceModifier> fx = new EnumMap<>(GodFX.class);
        fx.put(DO_NOTHING, new DoNothing());
        fx.put(SWAP, new SwapPower());
        fx.put(ADD_MOVE_NOT_STARTING_BOX, new AddMoveNotStartingBoxPower());
        fx.put(OPPONENTS_CANT_MOVE_UP_IF_PLAYER_MOVES_UP, new OpponentsCantMoveUpIfPlayerMovesUpPower());
        fx.put(BUILD_DOME_EVERYWHERE, new BuildDomeEverywherePower());
        fx.put(ADD_BUILD_NOT_SAME_BOX, new AddBuildNotSameBoxPower());
        fx.put(ADD_BUILD_ON_SAME_BOX, new AddBuildOnSameBoxPower());
        fx.put(PUSH_ADJACENT_OPPONENT, new PushAdjacentOpponentPower());
        fx.put(ADD_BUILD_BEFORE_IF_NOT_MOVE_UP, new AddBuildBeforeMoveIfNotMoveUpPower());
        fx.put(REMOVE_ADJACENT_BLOCK, new RemoveAdjacentBlockPower());
        fx.put(ADD_BUILD_NOT_EDGE, new AddBuildNotEdgePower());
        fx.put(ADD_THREE_BUILDS_TO_UNMOVED_WORKER, new AddThreeBuildsToUnmovedWorkerIfOnGroundPower());
        fx.put(BUILD_UNDER_YOURSELF, new BuildUnderYourselfPower());

        return fx;
    }

    /**
     * This method returns the actions needed to create the card
     * @param protoCard The card I need to create
     * @return List of TurnSequenceModifiers
     */
    private List<TurnSequenceModifier> loadActions(ProtoCard protoCard) {
        List<TurnSequenceModifier> actions = new ArrayList<>();

        if (protoCard != null) {
            Map<GodFX, TurnSequenceModifier> powers = loadFX();
            for (GodFX power : protoCard.actions())
                actions.add(powers.get(power));
        }

        return actions;
    }

    /**
     * This method returns the WinCondition needed to create the card
     * @param protoCard The card I need to create
     * @return WinCondition (null if the input is null)
     */
    private WinCondition loadWinCondition(ProtoCard protoCard) {
        if (protoCard != null) {
            Map<GodWin, WinCondition> winConditions = new EnumMap<>(GodWin.class);
            winConditions.put(STANDARD, null);
            winConditions.put(MOVE_TWO_LEVELS_DOWN, new MoveTwoLevelsDownWin());
            winConditions.put(TOWER_COUNT, new TowerCountWin(protoCard.winParameter()));
            return winConditions.get(protoCard.winCondition());
        }
        return null;
    }

    /**
     * This method returns the SetUpCondition needed to create the card
     * @param protoCard The card I need to create
     * @return SetUpCondition (null if the input is null)
     */
    private SetUpCondition loadSetUpCondition(ProtoCard protoCard) {
        if (protoCard != null) {
            Map<GodSetup, SetUpCondition> setUpConditions = new EnumMap<>(GodSetup.class);
            setUpConditions.put(NO_SETUP, new NoSetUpCondition());
            return setUpConditions.get(protoCard.setUpCondition());
        }
        return null;
    }

    /**
     * This method returns the opponent actions needed to create the card
     * @param protoCard The card I need to create
     * @return List of TurnSequenceModifiers
     */
    private List<TurnSequenceModifier> loadFXOnOpponents(ProtoCard protoCard) {
        List<TurnSequenceModifier> fx = new ArrayList<>();

        if (protoCard != null) {
            Map<GodFX, TurnSequenceModifier> effects = loadFX();
            for (GodFX power : protoCard.fxOnOpponent())
                fx.add(effects.get(power));
        }

        return fx;
    }

    /**
     * This method generates a GodCard from a Protocard
     * @param protoCard The Protocard I want to create
     * @return GodCard (null if the input is null)
     */
    private GodCard createCard(ProtoCard protoCard) {
        if (protoCard != null) {
            return new GodCard(protoCard.name(), protoCard.id(), loadActions(protoCard), loadWinCondition(protoCard), loadSetUpCondition(protoCard), loadFXOnOpponents(protoCard), protoCard.description(), protoCard.winDescription(), protoCard.setUpDescription(), protoCard.opponentsFxDescription());
        }
        return null;
    }

    /**
     * This method loads the GodCard deck from a file
     * @return List of GodCards
     */
    private List<GodCard> loadCards() {
        List<GodCard> deck = new ArrayList<>();
        List<ProtoCard> protoCards = loadProtoCardsFromFile();
        for (ProtoCard protoCard: protoCards)
            deck.add(createCard(protoCard));
        return deck;
    }
}
