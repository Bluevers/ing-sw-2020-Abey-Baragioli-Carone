package it.polimi.ingsw.server.socket;

import it.polimi.ingsw.server.controller.Lobby;
import it.polimi.ingsw.server.model.User;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public class UserManager extends Thread{
    private SocketServer socketServer;
    private List<String> userNames;
    private Map<String, User> users;
    private List<Lobby> lobbies;
    private List<Lobby> completeLobbies;

    public UserManager(Socket socket, List<String> userNames, Map<String, User> users, List<Lobby> lobbies, List<Lobby> completeLobbies) {
        this.socketServer = new SocketServer(socket);
        this.userNames = userNames;
        this.users = users;
        this.lobbies = lobbies;
        this.completeLobbies = completeLobbies;
        socketServer.start();
    }

    public SocketServer socketServer() {
        return socketServer;
    }

    public List<String> userNames() {
        return userNames;
    }

    public Map<String, User> users() {
        return users;
    }

    public List<Lobby> lobbies() {
        return lobbies;
    }

    public List<Lobby> completeLobbies() {
        return completeLobbies;
    }

    /**
     * This method tells if a userName is contained in the userNames List
     * @param username String
     * @return boolean
     */
    public boolean userNameExists(String username) {
        return userNames().contains(username);
    }

    /**
     * This method finds a user related to an existing userName
     * @param username String
     * @return User
     */
    public User findUser(String username) {
        if (users().containsKey(username))
            return users().get(username);
        return null;
    }

    /**
     * This method moves the lobby from the incomplete lobbies list to the complete ones
     * @param lobby the lobby created by this user manager
     */
    private synchronized void registerCompleteLobby(Lobby lobby) {
        lobbies().remove(lobby);
        completeLobbies().add(lobby);
    }

    /**
     * This method puts a user in the queue chosen by him
     * @param user User
     */
    public synchronized void assignUserToLobby(User user) {
        boolean found = false;
        int nPlayers = user.askTwoOrThreePlayerMatch();
        for (int i = 0; i < lobbies().size() && !found; i++) {
            Lobby lobby = lobbies().get(i);
            if (lobby.isFree() && lobby.nPlayers()==nPlayers) {
                found = true;
                lobby.addUser(user);
            }
        }
        if (!found) {
            Lobby lobby = new Lobby(user, nPlayers);
            lobbies().add(lobby);
            lobby.start();
            try {
                lobby.join();
                registerCompleteLobby(lobby);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("La lobby non joina");
            }
        }
    }

    /**
     * This method
     */
    public synchronized void run() {
        socketServer().write("USERNAME");
        String message = socketServer().read();
        boolean valid = false;
        while (!valid) {
            if (userNameExists(message) && findUser(message).hasSocket()) {
                socketServer().write("Your username must be unique to login.\n" + "Enter another username:");
                message = socketServer().read();
            }
            else
                valid = true;
        }
        if (!userNameExists(message)) {
            userNames().add(message);
            users().put(message, new User(message, socketServer()));
        }
        else
            findUser(message).setSocket(socketServer());
        assignUserToLobby(findUser(message));
    }
}
