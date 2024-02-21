package com.sab.littleh.net;

import com.badlogic.gdx.Net;
import com.sab.littleh.LittleH;
import com.sab.littleh.controls.Controls;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LittleHServer {
    public static final byte PACKET_KEY_DOWN = 0;
    public static final byte PACKET_KEY_UP = 1;
    public static final byte PACKET_SYNC_X = 2;
    public static final byte PACKET_SYNC_Y = 3;
    public static final byte PACKET_SYNC_NET_ID = 4;
    public static final byte PACKET_DISCONNECT = 5;
    public static final byte PACKET_CONNECT = 6;
    public static final byte PACKET_SET_UP_PLAYER = 7;
    public static final byte MAX_PACKET_TYPE = 7;
    public List<NetPlayer> netPlayers;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        new LittleHServer().start();
    }

    public LittleHServer() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setPerformancePreferences(0, 2, 1);
            serverSocket.bind(new InetSocketAddress(25565));
            netPlayers = new ArrayList<>();
        } catch (IOException e) {
            System.out.printf("Failed to open socket, are you connected to the internet? Error: %s", e);
        }
    }

    public void start() {
        Thread waitForConnections = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("Waiting for connection...");
                    Socket socket = serverSocket.accept();
                    System.out.println("Connection received!");
                    Connection connection = new Connection(socket);
                    System.out.println("Connection established.");
                    connect(connection);
                } catch (IOException e) {
                    System.out.printf("Failed to accept connection. Error: %s", e);
                }
            }
        });
        waitForConnections.start();
    }

    public void connect(Connection connection) throws IOException {
        int newNetId = netPlayers.size();
        connection.writeInt(newNetId);
        NetPlayer player = new NetPlayer();
        player.setNetId(newNetId);
        player.connection = connection;
        sendPacket(PACKET_CONNECT, newNetId, 0);
        sendPacket(PACKET_SET_UP_PLAYER, newNetId, 0);
        for (int i = 0; i < netPlayers.size(); i++) {
            if (netPlayers.get(i) != null) {
                connection.writeByte(PACKET_SET_UP_PLAYER);
                connection.writeInt(i);
                connection.writeInt(0);
            }
        }
        netPlayers.add(player);
        System.out.printf("NetPlayer with id %s established.\n", newNetId);

        Thread receiver = new Thread(
                () -> {
                    while (true) {
                        try {
                            byte packetType = connection.readByte();

                            // Invalid packet type
                            if (packetType > LittleHServer.MAX_PACKET_TYPE) {
                                netPlayers.set(player.getNetId(), null);
                                System.out.printf("Invalid packet from netId: %s. Packet of type %s not expected\n", player.getNetId(), packetType);
                                connection.close();
                                continue;
                            }

                            receivePacket(player.getNetId(), packetType, connection.readInt(), connection.readInt());
                        } catch (Exception e) {
                            System.out.printf("Connection closed while receiving packet from netId: %s. Error: %s \n", player.getNetId(), e);
                            netPlayers.set(player.getNetId(), null);
                            try {
                                connection.close();
                            } catch (IOException ex) { ex.printStackTrace(); }
                            break;
                        }
                    }
                }
        );
        receiver.setDaemon(true);
        receiver.start();

        System.out.printf("Now receiving packets from NetPlayer with id %s.\n", newNetId);
    }

    public void receivePacket(int fromNetId, byte packetType, int forNetId, int data) {
        // Check if packet should ONLY be sent by server or packet is trying to affect another player
        if (packetType == PACKET_SYNC_NET_ID || /*( true) &&(*/ fromNetId != forNetId) {
            System.out.printf("SUSPICIOUS action from netId: %s. \n", fromNetId);
            // Don't allow players to send SUS packets
            return;
        }
        for (int i = 0; i < netPlayers.size(); i++) {
            if (i == fromNetId) continue;

            Connection connection = getConnection(i);
            if (connection != null) {
                try {
                    if (packetType == PACKET_KEY_DOWN)
                        System.out.println("Key down from netId: " + fromNetId + ". Sent to netId: " + i);
                    connection.writeByte(packetType);
                    connection.writeInt(forNetId);
                    connection.writeInt(data);
                } catch (IOException e) {
                    netPlayers.set(i, null);
                    System.out.printf("Connection closed while trying to distribute packet to netId: %s. Error: %s \n", fromNetId, e);
                    try {
                        connection.close();
                    } catch (IOException ignored) {
                    }
                    break;
                }
            }
        }
    }

    public Connection getConnection(int netId) {
        if (netPlayers.get(netId) == null) {
//            cullNullPlayers();
            return null;
        }
        return netPlayers.get(netId).connection;
    }

    public void sendPacket(byte packetType, int targetPlayer, int data) {
        for (NetPlayer netPlayer : netPlayers) {
            if (netPlayer != null) {
                try {
                    netPlayer.connection.writeByte(packetType);
                    netPlayer.connection.writeInt(targetPlayer);
                    netPlayer.connection.writeInt(data);
                } catch (IOException e) {
                    System.out.printf("Failed to write to connection: %s.\n Error: %s \n", netPlayer.connection, e);
                }
            }
        }
    }

    public void cullNullPlayers() {
        List<NetPlayer> newNetPlayers = new ArrayList<>();
        for (int i = 0; i < netPlayers.size(); i++) {
            NetPlayer netPlayer = netPlayers.get(i);
            if (netPlayer != null) {
                // Send a packet to all clients telling them to change player with netId i to have the reassigned netId
                sendPacket(PACKET_SYNC_NET_ID, i, newNetPlayers.size());
                netPlayer.setNetId(newNetPlayers.size());
                newNetPlayers.add(netPlayer);
            }
        }
        netPlayers = newNetPlayers;
    }

    public int countNullPlayers() {
        int count = 0;
        for (NetPlayer player : netPlayers)
            if (player == null) count++;
        return count;
    }
}
