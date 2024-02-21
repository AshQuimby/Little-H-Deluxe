package com.sab.littleh.net;

import com.sab.littleh.game.level.LevelLoader;
import com.sab.littleh.mainmenu.MainMenu;

import java.io.IOException;

public class Client {
    private Connection server;
    private OnlineLevelMenu onlineLevelMenu;
    public Client() {
        try {
            server = new Connection("localhost", 25565);
            int netId = server.readInt();
            onlineLevelMenu = new OnlineLevelMenu(server, netId, LevelLoader.readInternalLevel("meadows.map"));
        } catch (IOException e) {
            System.out.printf("Failed to connect to server. Are the ip and port correct? Error: %s", e);
        }
    }

    public OnlineLevelMenu getMenu() {
        return onlineLevelMenu;
    }
}
