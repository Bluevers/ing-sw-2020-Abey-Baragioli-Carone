package it.polimi.ingsw.network;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.network.exceptions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.polimi.ingsw.network.CommunicationProtocol.*;

public class CommunicationChannel {

    final private BufferedReader in;
    final private PrintWriter out;
    private List<String> buffer = new ArrayList<>();
    private boolean closed = false;
    private boolean ping = false;
    private static final String SEPARATOR = "_CONTENT_";
    private final int quit = -1;

    public CommunicationChannel(BufferedReader bufferedReader, PrintWriter printWriter) {
        in = bufferedReader;
        out = printWriter;
    }

    public boolean isClosed() {
        return closed;
    }

    boolean isPinged() {
        return ping;
    }

    private synchronized void ping() {
        ping = true;
    }

    synchronized void resetPing() {
        ping = false;
    }

    /**
     * This method blocks every input and output communication
     */
    public synchronized void close() {
        closed = true;
        notifyAll();
    }

    /**
     * This method reads in the input stream
     * @return string
     * @throws IOException when a network error occurs
     */
    public String read() throws IOException {
        synchronized (in) {
            return in.readLine();
        }
    }

    /**
     * This method adds a message to the buffer
     * @param message, the message saved
     */
    private synchronized void saveMessage(String message) {
        buffer.add(message);
        notifyAll();
    }

    /**
     * This method tells if the has a message containing the key isn't empty
     * @return false if not
     * @param key, key of communication
     */
    private synchronized boolean hasMessages(CommunicationProtocol key) {
        return buffer.stream().anyMatch(x -> getKey(x) == key);
    }

    /**
     * This method tells if the buffer contains a message
     * @return Boolean
     */
    public synchronized boolean isEmpty() {
        return buffer.isEmpty();
    }

    /**
     * This method returns the first key that was received from the ones contained in the buffer
     * @return CommunicationProtocol key
     * @throws ChannelClosedException if the connection is closed
     */
    public synchronized CommunicationProtocol popKey() throws ChannelClosedException {
        while (!isClosed()) {
            if (!isEmpty())
                return getKey(buffer.get(0));
            else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.err.println("Non waita");
                }
            }
        }
        throw new ChannelClosedException();
    }

    /**
     * This method pops the first message that was from the ones contained in the buffer
     * @return String
     * @throws ChannelClosedException if there's no connection
     */
    public synchronized String popMessage() throws ChannelClosedException {
        while (!isClosed()) {
            if (!isEmpty()) {
                String message = buffer.get(0);
                buffer.remove(0);
                return message;
            }
            else {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.err.println("Non waita");
                }
            }
        }
        throw new ChannelClosedException();
    }

    /**
     * This method pops the first message in the buffer
     * @param key, key of communication
     * @return The popped message
     * @throws ChannelClosedException if connection is lost
     */
    private synchronized String nextMessage(CommunicationProtocol key) throws ChannelClosedException {
        while (!isClosed()) {
            if (hasMessages(key)) {
                String message;
                Optional<String> optional = buffer.stream().filter(x -> getKey(x) == key).findFirst();
                if (optional.isPresent()) {
                    message = optional.get();
                    buffer.remove(message);
                    return message;
                }
            }
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("Non waita");
            }
        }
        throw new ChannelClosedException();
    }

    /**
     * This method pops the first message in the buffer if it arrives in time
     * @param key, key of communication
     * @return The popped message
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    private synchronized String nextGameMessage(CommunicationProtocol key) throws ChannelClosedException, TimeOutException {
        while (!isClosed()) {
            if(hasMessages(TIMEOUT)) {
                nextMessage(TIMEOUT);
                throw new TimeOutException();
            }
            if (hasMessages(key)) {
                return nextMessage(key);
            }
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new ChannelClosedException();
    }

    /**
     * This method converts a key to a string
     * @param key keyword
     * @return related string
     */
    private static String keyToString(CommunicationProtocol key) {
        Type type = new TypeToken<CommunicationProtocol>() {}.getType();
        return new Gson().toJson(key, type);
    }

    /**
     * This method recognizes a key from a string
     * @param string unknown key
     * @return converted key
     */
    private CommunicationProtocol stringToKey(String string) {
        Type type = new TypeToken<CommunicationProtocol>() {}.getType();
        CommunicationProtocol key;
        try {
            key = new Gson().fromJson(string, type);
        } catch (JsonSyntaxException e) {
            //non existing key
            key = INVALID;
        }
        return key;
    }

    /**
     * This method gets a key from a message
     * @param message string
     * @return recognized key
     */
    private CommunicationProtocol getKey(String message) {
        if (message != null) {
            String[] content = message.split(SEPARATOR);
            int keyIndex = 0;
            return stringToKey(content[keyIndex]);
        }
        return INVALID;
    }

    /**
     * This method gets the content from a message
     * @param message string
     * @return recognized content
     */
    public String getContent(String message) {
        if (message != null) {
            String[] content = message.split(SEPARATOR);
            int contentIndex = 1;
            if (content.length == 2)
                return content[contentIndex];
            if (content.length < 2)
                return keyToString(EMPTY);
        }
        return keyToString(INVALID);
    }

    /**
     * This method reads the key of the incoming message and saves it if it is neither a PING nor a QUIT
     * @return key
     * @throws IOException in case of network error
     * @throws ChannelClosedException if connection is lost
     */
    public CommunicationProtocol nextKey() throws IOException, ChannelClosedException {
        if(!closed) {
            String message = read();
            CommunicationProtocol key = getKey(message);

            if (key != PING && key != PONG)
                saveMessage(message);
            else if (key == PONG)
                ping();

            return key;
        }
        throw new ChannelClosedException();
    }

    /**
     * This method converts the content in a number
     * @param message String
     * @return content
     * @throws JsonSyntaxException in case the content is invalid
     */
    private int readNumber(String message) throws JsonSyntaxException {
        Type type = new TypeToken<Integer>() {}.getType();
        return new Gson().fromJson(getContent(message), type);
    }

    /**
     * This method converts the content in a boolean value
     * @param message String
     * @return content
     */
    private boolean readBoolean(String message) {
        int answer = readNumber(message);
        return answer == 0;
    }

    /**
     * This method send a string
     * @param message string
     * @throws ChannelClosedException if connection is lost
     */
    public void write(String message) throws ChannelClosedException {
        synchronized (out) {
            if (!isClosed()) {
                out.println(message);
                out.flush();
            } else
                throw new ChannelClosedException();
        }
    }

    /**
     * This method sends a single key
     * @param key key
     * @throws ChannelClosedException if connection is lost
     */
    public void writeKeyWord(CommunicationProtocol key) throws ChannelClosedException {
        write(keyToString(key));
        if(key==QUIT)
            close();
    }

    /**
     * This method sends the keyword USERNAME and waits for a reply
     * @return String
     * @throws ChannelClosedException if there's no connection
     */
    public String askUsername() throws ChannelClosedException {
        while (!isClosed()){
            writeKeyWord(USERNAME);
            String message = nextMessage(USERNAME);
            if (message != null && getKey(message) == USERNAME)
                return getContent(message);
        }
        throw new ChannelClosedException();
    }


    /**
     * This method sends the keyword UNIQUE_USERNAME and waits for a reply
     * @return String
     * @throws ChannelClosedException if there's no connection
     */
    public String askUniqueUsername() throws ChannelClosedException {
        while (!isClosed()){
            writeKeyWord(UNIQUE_USERNAME);
            String message = nextMessage(USERNAME);
            if (message != null && getKey(message) == USERNAME)
                return getContent(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method writes USERNAME followed by the content
     * @param userName string
     * @throws ChannelClosedException if there's no connection
     */
    public synchronized void writeUsername(String userName) throws ChannelClosedException {
        write(keyToString(USERNAME) + SEPARATOR + userName);
    }

    /**
     * This method sends the keyword MATCH_TYPE and waits for a reply
     * @return int
     * @throws ChannelClosedException if there's no connection
     */
    public int askMatchType() throws ChannelClosedException {
        if (!isClosed()){
            writeKeyWord(MATCH_TYPE);
            String message = nextMessage(MATCH_TYPE);
            return readNumber(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method writes a reply to a MATCH_TYPE request
     * @param matchType answer
     * @throws ChannelClosedException if there's no connection
     */
    public synchronized void writeMatchType(int matchType) throws ChannelClosedException {
        if (matchType == quit)
            writeKeyWord(QUIT);
        else
            write(keyToString(MATCH_TYPE) + SEPARATOR + matchType);
    }

    /**
     * This method sends a list of positions as json object and waits for a reply
     * @param workers Box coordinates
     * @return int list index
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    public int askWorker(String workers) throws TimeOutException, ChannelClosedException {
        while (!isClosed()) {
            write(keyToString(WORKER) + SEPARATOR + workers);
            String message = nextGameMessage(WORKER);
            if (message != null && getKey(message) == WORKER)
                return readNumber(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method checks if the other side received what was sent before
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean copy() throws ChannelClosedException {
        return getKey(nextMessage(RECEIVED)) == RECEIVED;
    }

    /**
     * This method sends a player and tells if it was received
     * @param player Sent player
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendMyPlayer(String player) throws ChannelClosedException {
        write(keyToString(MY_PLAYER) + SEPARATOR + player);
        return copy();
    }

    /**
     * This method sends to the user his player and tells if it was received
     * @param opponents Opponents
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendOpponents(String opponents) throws ChannelClosedException {
        write(keyToString(OPPONENTS) + SEPARATOR + opponents);
        return copy();
    }

    /**
     * This method sends a player and tells if it was received
     * @param currentPlayer Sent player
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendCurrentPlayer(String currentPlayer) throws ChannelClosedException {
        write(keyToString(CURRENT_PLAYER) + SEPARATOR + currentPlayer);
        return copy();
    }

    /**
     * This method sends the winner
     * @param winner Sent player
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendWinner(String winner) throws ChannelClosedException {
        write(keyToString(WINNER) + SEPARATOR + winner);
        return copy();
    }

    /**
     * This method sends a loser player
     * @param loser Sent player
     * @return boolean
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendLoser(String loser) throws ChannelClosedException {
        write(keyToString(LOSER) + SEPARATOR + loser);
        return copy();
    }

    /**
     * this method sends to the user the map and tells if it was received
     * @param map, map that has to be sent
     * @return boolean, indicates if the receiver has received the map
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendMap(String map)throws ChannelClosedException {
        write(keyToString(MAP) + SEPARATOR + map);
        return copy();
    }

    /**
     * this method sends to the user the map and tells if it was received
     * @param story String list
     * @return boolean, indicates if the receiver actually received the story
     * @throws ChannelClosedException if there's no connection
     */
    public boolean sendStory(String story) throws ChannelClosedException {
        write(keyToString(MATCH_STORY) + SEPARATOR + story);
        return copy();
    }

    /**
     * This method sends the user an index of a card list
     * @param key Type of list
     * @param index int
     * @throws ChannelClosedException if there's no connection
     */
    public void writeChoiceFromList(CommunicationProtocol key, int index) throws ChannelClosedException {
        write(keyToString(key) + SEPARATOR + index);
    }

    /**
     * This method sends the user an index of a card list
     * @param key Type of list
     * @param indexes int
     * @throws ChannelClosedException if there's no connection
     */
    public void writeChoicesFromList(CommunicationProtocol key, int[] indexes) throws ChannelClosedException{
        Type listType = new TypeToken<int[]>() {}.getType();
        write(keyToString(key) + SEPARATOR + new Gson().toJson(indexes, listType));
    }

    /**
     * This method sends a list of positions as json object and waits for a reply
     * @param positions Box coordinates
     * @return int list index
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    public int askStartPosition(String positions) throws ChannelClosedException, TimeOutException {
        while (!isClosed()) {
            write(keyToString(START_POSITION) + SEPARATOR + positions);
            String message = nextGameMessage(START_POSITION);
            if (message != null && getKey(message) == START_POSITION)
                return readNumber(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method sends a list of destinations as json object and waits for a reply
     * @param destinations Box coordinates
     * @return int list index
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    public int askDestination(String destinations) throws TimeOutException, ChannelClosedException {
        while (!isClosed()) {
            write(keyToString(DESTINATION) + SEPARATOR + destinations);
            String message = nextGameMessage(DESTINATION);
            if (message != null && getKey(message) == DESTINATION)
                return readNumber(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method sends a list of locations as json object and waits for a reply
     * @param builds Box coordinates
     * @return int list index
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    public int askBuild(String builds) throws TimeOutException, ChannelClosedException {
        while (!isClosed()) {
            write(keyToString(BUILD) + SEPARATOR + builds);
            String message = nextGameMessage(BUILD);
            if (message != null && getKey(message) == BUILD)
                return readNumber(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method sends a list of locations as json object and waits for a reply
     * @param removals Box coordinates
     * @return int list index
     * @throws TimeOutException if the time is out
     * @throws ChannelClosedException if there's no connection
     */
    public int askRemoval(String removals) throws TimeOutException, ChannelClosedException {
        if (!isClosed()) {
            write(keyToString(REMOVAL) + SEPARATOR + removals);
            String message = nextGameMessage(REMOVAL);
            return readNumber(message);
        }
        return -1;
    }

    /**
     * This method sends a list of cards and waits for a list index
     * @param cards List of cards
     * @return int
     * @throws TimeOutException if the answer is TIMEOUT
     * @throws ChannelClosedException if the connection is lost
     */
    public int askCard(String cards) throws TimeOutException, ChannelClosedException {
        if (!isClosed()) {
            write(keyToString(CARD) + SEPARATOR + cards);
            String message = nextGameMessage(CARD);
            return readNumber(message);
        }
        return -1;
    }

    /**
     * This method sends a list of cards and waits for an array of list indexes
     * @param deck List of cards
     * @return int[]
     * @throws TimeOutException if the answer is TIMEOUT
     * @throws ChannelClosedException if the connection is lost
     */
    public int[] askDeck(String deck) throws TimeOutException, ChannelClosedException {
        if (!isClosed()) {
            write(keyToString(DECK) + SEPARATOR + deck);
            String message = nextGameMessage(DECK);
            Type listType = new TypeToken<int[]>() {}.getType();
            return new Gson().fromJson(getContent(message), listType);
        }
        int[] quitting = new int[1];
        quitting[0] = -1;
        return quitting;
    }

    /**
     * This method asks for confirmation  and waits for a reply
     * @param key key of Communication Protocol
     * @return boolean value of confirmation
     * @throws ChannelClosedException if there's no connection
     * @throws TimeOutException Exception thrown when the time to do an action runs out
     */
    public boolean askConfirmation(CommunicationProtocol key) throws TimeOutException, ChannelClosedException {
        if (!isClosed()) {
            write(keyToString(key));
            String message = nextGameMessage(key);
            return readBoolean(message);
        }
        throw new ChannelClosedException();
    }

    /**
     * This method answers a confirmation request
     * @param key Request type
     * @param confirmation Answer
     * @throws ChannelClosedException if the connection is closed
     */
    public void writeConfirmation(CommunicationProtocol key, int confirmation) throws ChannelClosedException {
        if (confirmation == quit)
            writeKeyWord(QUIT);
        else
            write(keyToString(key) + SEPARATOR + confirmation);
    }
}
