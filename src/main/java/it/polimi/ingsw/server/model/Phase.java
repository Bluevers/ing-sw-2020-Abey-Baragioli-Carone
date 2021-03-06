package it.polimi.ingsw.server.model;

public enum Phase {
    START('S'), MOVE('M'), BUILD('B'), END('E');

    private char abbreviation;

    Phase(char abbreviation) {
        this.abbreviation = abbreviation;
    }
}
