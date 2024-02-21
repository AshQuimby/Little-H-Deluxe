package com.sab.littleh.net;

import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.entity.player.Player;
import com.sab.littleh.game.level.Level;

import java.awt.*;

public class NetPlayer extends Player {
    protected Connection connection;
    private LittleHServer server;
    private int netId;
    private int ticksSinceSync;
    private boolean waitingForInit;
    private boolean waitingForDisconnect;

    public NetPlayer(Point startPos) {
        super(startPos);
        controller = new ControlInput();
    }
    public NetPlayer() {
        this(new Point());
    }

    public void startConnection(Connection connection) {
        this.connection = connection;
    }

    public void setNetId(int netId) {
        this.netId = netId;
    }

    public int getNetId() {
        return netId;
    }

    public boolean isWaitingForInit() {
        return waitingForInit;
    }

    public boolean isWaitingForDisconnect() {
        return waitingForDisconnect;
    }

    @Override
    public void update(Level game) {
        super.update(game);
        ticksSinceSync++;
    }

    public void receivePacket(byte packetType, int data) {
        switch (packetType) {
            case 0x00:
                // Key down
                controller.pressControl(Controls.get(data));
                break;
            case 0x01:
                // Key up
                controller.releaseControl(Controls.get(data));
                break;
            case 0x02:
                // Re-sync x
                x = data;
                break;
            case 0x03:
                // Re-sync y
                y = data;
                break;
            case 0x04:
                // Re-sync netId
                netId = data;
                break;
            case 0x05:
                // Connect
                waitingForInit = true;
                break;
            case 0x06:
                // Disconnect
                waitingForDisconnect = true;
                break;
            case 0x07:
                // Set up player (This should not be received by NetPlayers)
        }
    }
}
