package it.polimi.ingsw;

import it.polimi.ingsw.client.view.cli.Cli;
import it.polimi.ingsw.client.view.gui.Gui;
import it.polimi.ingsw.server.socket.Server;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {

        if (args.length > 3) {
            String parameter = args[3];
            if (parameter.equals("-cli"))
                new Cli().run();
            else if (parameter.equals("-server"))
                new Server().run(args);
        }
        else {
                Application.launch(Gui.class, args);
        }
    }
}
