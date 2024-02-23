package com.sab.littleh.net;

import com.sab.littleh.LittleH;
import com.sab.littleh.controls.ControlInput;
import com.sab.littleh.controls.Controls;
import com.sab.littleh.game.level.Level;
import com.sab.littleh.mainmenu.GameMenu;
import com.sab.littleh.mainmenu.TitleMenu;
import com.sab.littleh.util.Cursors;
import com.sab.littleh.util.SoundEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OnlineLevelMenu extends GameMenu {
    private final Map<Integer, NetPlayer> netPlayers;
    private final Connection server;
    private int clientNetId;
    private int ticksUntilSync;
    public OnlineLevelMenu(Connection server, int clientNetId, Level level) {
        super(null, level, true);
        this.server = server;
        this.clientNetId = clientNetId;
        netPlayers = new HashMap<>();

        Thread receiver = new Thread(
            () -> {
                while (true) {
                    try {
                        byte packetType = server.readByte();

                        // Invalid packet type
                        if (packetType > LittleHServer.MAX_PACKET_TYPE) {
                            disconnect();
                            break;
                        }

                        receivedPacket(packetType, server.readInt(), server.readInt());
                    } catch (IOException e) {
                        disconnect();
                        break;
                    }
                }
            }
        );

        receiver.setName("Packet Handler");
        receiver.setDaemon(true);
        receiver.start();
    }

    public void disconnect() {
        try {
            server.writeByte(LittleHServer.PACKET_DISCONNECT);
            server.writeInt(clientNetId);
            server.writeInt(0);
            server.close();
        } catch (IOException ignored) {
        }
    }

    public void receivePlayer(int netId, int data) {
        NetPlayer netPlayer = new NetPlayer(level.getStartPos());
        netPlayer.setNetId(netId);
        netPlayer.pickUpPowerup(data);
        netPlayers.put(netId, netPlayer);
        level.addMiscGameObject(netPlayer);
    }

    @Override
    public void stop() {
        LittleH.program.switchMenu(TitleMenu.titleScreen);
        Cursors.switchCursor("cursor");
        SoundEngine.playMusic("menu/menu_theme.ogg");
    }

    @Override
    public void update() {
        for (String control : Controls.controlList) {
            if (ControlInput.localControls.isJustPressed(control)) {
                sendPacket(LittleHServer.PACKET_KEY_DOWN, clientNetId, Controls.controlList.indexOf(control));
            }
            if (ControlInput.localControls.isJustReleased(control)) {
                sendPacket(LittleHServer.PACKET_KEY_UP, clientNetId, Controls.controlList.indexOf(control));
            }
        }
        level.update();
        if (!level.inGame()) {
           stop();
        }
        if (--ticksUntilSync <= 0) {
            sendPacket(LittleHServer.PACKET_SYNC_X, clientNetId, (int) level.player.x);
            sendPacket(LittleHServer.PACKET_SYNC_Y, clientNetId, (int) level.player.y);
            ticksUntilSync = 30;
        }
    }

    public void sendPacket(byte packetType, int forPlayer, int data) {
        try {
            server.writeByte(packetType);
            server.writeInt(forPlayer);
            server.writeInt(data);
        } catch (IOException ignored) {
        }
    }

    public void receivedPacket(byte packetType, int forPlayer, int data) {
        switch (packetType) {
            case LittleHServer.PACKET_SET_UP_PLAYER:
                // Set up player
                receivePlayer(forPlayer, data);
                break;
            case LittleHServer.PACKET_SYNC_NET_ID:
                // Sync new netId
                if (forPlayer == clientNetId) {
                    this.clientNetId = data;
                    break;
                }
            default:
                // Packets for players
                NetPlayer netPlayer = netPlayers.get(forPlayer);
                System.out.println(netPlayer);
                if (netPlayer != null) netPlayer.receivePacket(packetType, data);
        }
    }
}
