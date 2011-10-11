/*
 * Copyright (c) 2010 SimpleServer authors (see CONTRIBUTORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dumptruckman.pail.proxyserver.stream;

import com.dumptruckman.pail.proxyserver.Coordinate;
import com.dumptruckman.pail.proxyserver.Coordinate.Dimension;
import com.dumptruckman.pail.proxyserver.Player;
import com.dumptruckman.pail.proxyserver.ProxyServer;

import java.io.*;

//import java.util.IllegalFormatException;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class StreamTunnel {
    private static final boolean EXPENSIVE_DEBUG_LOGGING = Boolean.getBoolean("EXPENSIVE_DEBUG_LOGGING");
    //private static final boolean EXPENSIVE_DEBUG_LOGGING = true;
    private static final int IDLE_TIME = 30000;
    private static final int BUFFER_SIZE = 1024;
    private static final byte BLOCK_DESTROYED_STATUS = 2;
    //private static final Pattern MESSAGE_PATTERN = Pattern.compile("^<([^>]+)> (.*)$");
    //private static final Pattern COLOR_PATTERN = Pattern.compile("\u00a7[0-9a-f]");
    //private static final String CONSOLE_CHAT_PATTERN = "\\(CONSOLE:.*\\)";
    private static final int MESSAGE_SIZE = 60;
    private static final int MAXIMUM_MESSAGE_SIZE = 119;

    private final boolean isServerTunnel;
    private final String streamType;
    private final Player player;
    private final ProxyServer server;
    private final byte[] buffer;
    private final Tunneler tunneler;

    private DataInput in;
    private DataOutput out;
    private StreamDumper inputDumper;
    private StreamDumper outputDumper;

    private int motionCounter = 0;
    private boolean inGame = false;

    private volatile long lastRead;
    private volatile boolean run = true;
    private Byte lastPacket;

    public StreamTunnel(InputStream in, OutputStream out, boolean isServerTunnel,
            Player player) {
        this.isServerTunnel = isServerTunnel;
        if (isServerTunnel) {
            streamType = "ServerStream";
        } else {
            streamType = "PlayerStream";
        }

        this.player = player;
        server = player.getServer();

        DataInputStream dIn = new DataInputStream(new BufferedInputStream(in));
        DataOutputStream dOut = new DataOutputStream(new BufferedOutputStream(out));
        if (EXPENSIVE_DEBUG_LOGGING) {
            try {
                OutputStream dump = new FileOutputStream(streamType + "Input.debug");
                InputStreamDumper dumper = new InputStreamDumper(dIn, dump);
                inputDumper = dumper;
                this.in = dumper;
            } catch (FileNotFoundException e) {
                System.out.println("Unable to open input debug dump!");
                throw new RuntimeException(e);
            }

            try {
                OutputStream dump = new FileOutputStream(streamType + "Output.debug");
                OutputStreamDumper dumper = new OutputStreamDumper(dOut, dump);
                outputDumper = dumper;
                this.out = dumper;
            } catch (FileNotFoundException e) {
                System.out.println("Unable to open output debug dump!");
                throw new RuntimeException(e);
            }
        } else {
            this.in = dIn;
            this.out = dOut;
        }

        buffer = new byte[BUFFER_SIZE];

        tunneler = new Tunneler();
        tunneler.start();

        lastRead = System.currentTimeMillis();
    }

    public void stop() {
        run = false;
    }

    public boolean isAlive() {
        return tunneler.isAlive();
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastRead < IDLE_TIME/* || player.isRobot()*/;
    }

    private void handlePacket() throws IOException {
        Byte packetId = in.readByte();
        int x;
        byte y;
        int z;
        byte dimension;
        Coordinate coordinate;
        switch (packetId) {
            case 0x00: // Keep Alive
                write(packetId);
                write(in.readInt()); // random number that is returned form server
                break;
            case 0x01: // Login Request/Response
                write(packetId);
                if (isServerTunnel) {
                    player.setEntityId(write(in.readInt()));
                    write(readUTF16());
                    write(in.readLong());
                } else {
                    write(in.readInt());
                    readUTF16(); // and throw away
                    write(player.getName());
                    write(in.readLong());
                }

                write(in.readInt());

                dimension = in.readByte();
                if (isServerTunnel) {
                    player.setDimension(Dimension.get(dimension));
                }
                write(dimension);
                // added in 1.8
                write(in.readByte());
                write(in.readByte());
                //if (isServerTunnel) {
                //    in.readByte();
                //    write((byte) server.config.properties.getInt("maxPlayers"));
                //} else {
                    write(in.readByte());
                //}

                break;
            case 0x02: // Handshake
                String name = readUTF16();
                if (isServerTunnel || player.setName(name)) {
                    tunneler.setName(streamType + "-" + player.getName());
                    write(packetId);
                    write(name);
                }
                break;
            case 0x03: // Chat Message
                String message = readUTF16();
                if (!isServerTunnel) {
                    if (message.startsWith(server.pail.config.getCommandPrefix())) {
                        if (player.parseCommand(message)) {
                            break;
                        }
                    }
                }
                write(packetId);
                write(message);
                break;
            case 0x04: // Time Update
                write(packetId);
                long time = in.readLong();
                //server.setTime(time);
                write(time);
                break;
            case 0x05: // Player Inventory
                write(packetId);
                write(in.readInt());
                write(in.readShort());
                write(in.readShort());
                write(in.readShort());
                break;
            case 0x06: // Spawn Position
                write(packetId);
                copyNBytes(12);
                break;
            case 0x07: // Use Entity?
                int user = in.readInt();
                int target = in.readInt();
                write(packetId);
                write(user);
                write(target);
                //write(in.readBoolean());
                copyNBytes(1);
                break;
            case 0x08: // Update Health
                write(packetId);
                write(in.readShort());
                write(in.readShort());
                write(in.readFloat());
                break;
            case 0x09: // Respawn
                write(packetId);
                player.setDimension(Dimension.get(write(in.readByte())));
                write(in.readByte());
                write(in.readByte());
                write(in.readShort());
                write(in.readLong());
                break;
            case 0x0a: // Player
                write(packetId);
                copyNBytes(1);
                /*
                if (!inGame && !isServerTunnel) {
                    player.sendMOTD();

                    if (server.options.getBoolean("showListOnConnect")) {
                        // display player list if enabled in config
                        player.execute(PlayerListCommand.class);
                    }

                    inGame = true;
                }
                 * 
                 */
                break;
            case 0x0b: // Player Position
                write(packetId);
                copyPlayerLocation();
                copyNBytes(1);
                break;
            case 0x0c: // Player Look
                write(packetId);
                copyPlayerLook();
                copyNBytes(1);
                break;
            case 0x0d: // Player Position & Look
                write(packetId);
                copyPlayerLocation();
                copyPlayerLook();
                copyNBytes(1);
                break;
            case 0x0e: // Player Digging
                /*
                if (!isServerTunnel) {
                    byte status = in.readByte();
                    x = in.readInt();
                    y = in.readByte();
                    z = in.readInt();
                    byte face = in.readByte();

                    coordinate = new Coordinate(x, y, z, player);
                    boolean[] perms = server.permissions.getPlayerBlockPermissions(player, coordinate, 0);
                    if (!perms[2] && status == 0) {
                        player.addMessage("\u00a7c " + server.l.get("USE_FORBIDDEN"));
                        break;
                    }
                    if (!perms[1] && status == 2) {
                        player.addMessage("\u00a7c " + server.l.get("DESTROY_FORBIDDEN"));
                        break;
                    }

                    boolean locked = server.chests.isLocked(coordinate);

                    if (!locked || player.isAdmin()) {
                        if (locked && status == BLOCK_DESTROYED_STATUS) {
                        server.chests.releaseLock(coordinate);
                    }

                    write(packetId);
                    write(status);
                    write(x);
                    write(y);
                    write(z);
                    write(face);

                    if (player.instantDestroyEnabled()) {
                        packetFinished();
                        write(packetId);
                        write(BLOCK_DESTROYED_STATUS);
                        write(x);
                        write(y);
                        write(z);
                        write(face);
                    }

                    if (status == BLOCK_DESTROYED_STATUS) {
                    player.destroyedBlock();
                    }
                    }
                } else {
                    write(packetId);
                    copyNBytes(11);
                }
                 */
                write(packetId);
                copyNBytes(11);
                break;
            case 0x0f: // Player Block Placement
                x = in.readInt();
                y = in.readByte();
                z = in.readInt();
                coordinate = new Coordinate(x, y, z, player);
                final byte direction = in.readByte();
                final short dropItem = in.readShort();
                byte itemCount = 0;
                short uses = 0;
                if (dropItem != -1) {
                    itemCount = in.readByte();
                    uses = in.readShort();
                }

                boolean writePacket = true;
                boolean drop = false;

                /*
                boolean[] perms = server.permissions.getPlayerBlockPermissions(player, coordinate, dropItem);

                if (isServerTunnel || server.chests.isChest(coordinate)) {
                    // continue
                } else if ((dropItem != -1 && !perms[0]) || (dropItem == -1 && !perms[2])) {
                    if (dropItem == -1) {
                    player.addMessage("\u00a7c " + server.l.get("USE_FORBIDDEN"));
                    } else {
                    player.addMessage("\u00a7c " + server.l.get("PLACE_FORBIDDEN"));
                    }

                    writePacket = false;
                    drop = true;
                } else if (dropItem == 54) {
                    int xPosition = x;
                    byte yPosition = y;
                    int zPosition = z;
                    switch (direction) {
                        case 0:
                            --yPosition;
                            break;
                        case 1:
                            ++yPosition;
                            break;
                        case 2:
                            --zPosition;
                            break;
                        case 3:
                            ++zPosition;
                            break;
                        case 4:
                            --xPosition;
                            break;
                        case 5:
                            ++xPosition;
                            break;
                    }

                    Coordinate targetBlock = new Coordinate(xPosition, yPosition, zPosition, player);

                    Chest adjacentChest = server.chests.adjacentChest(targetBlock);

                    if (adjacentChest != null && !adjacentChest.isOpen() && !adjacentChest.ownedBy(player)) {
                        player.addMessage("\u00a7c " + server.l.get("ADJ_CHEST_LOCKED"));
                        writePacket = false;
                        drop = true;
                    } else {
                        player.placingChest(targetBlock);
                    }
                }
                 * 
                 */

                if (writePacket) {
                    write(packetId);
                    write(x);
                    write(y);
                    write(z);
                    write(direction);
                    write(dropItem);

                    if (dropItem != -1) {
                        write(itemCount);
                        write(uses);

                        if (dropItem <= 94 && direction >= 0) {
                            //player.placedBlock();
                        }
                    }

                    //player.openingChest(coordinate);

                } else if (drop) {
                    // Drop the item in hand. This keeps the client state in-sync with the
                    // server. This generally prevents empty-hand clicks by the client
                    // from placing blocks the server thinks the client has in hand.
                    write((byte) 0x0e);
                    write((byte) 0x04);
                    write(x);
                    write(y);
                    write(z);
                    write(direction);
                }

                break;
            case 0x10: // Holding Change
                write(packetId);
                copyNBytes(2);
                break;
            case 0x11: // Use Bed
                write(packetId);
                copyNBytes(14);
                break;
            case 0x12: // Animation
                write(packetId);
                copyNBytes(5);
                break;
            case 0x13: // ???
                write(packetId);
                write(in.readInt());
                write(in.readByte());
                break;
            case 0x14: // Named Entity Spawn
                int eid = in.readInt();
                name = readUTF16();
                write(packetId);
                write(eid);
                write(name);
                copyNBytes(16);
                break;
            case 0x15: // Pickup spawn
                write(packetId);
                copyNBytes(24);
                break;
            case 0x16: // Collect Item
                write(packetId);
                copyNBytes(8);
                break;
            case 0x17: // Add Object/Vehicle
                write(packetId);
                write(in.readInt());
                write(in.readByte());
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                int flag = in.readInt();
                write(flag);
                if (flag > 0) {
                    write(in.readShort());
                    write(in.readShort());
                    write(in.readShort());
                }
                break;
            case 0x18: // Mob Spawn
                write(packetId);
                write(in.readInt());
                write(in.readByte());
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                write(in.readByte());
                write(in.readByte());

                copyUnknownBlob();
                break;
            case 0x19: // Painting
                write(packetId);
                write(in.readInt());
                write(readUTF16());
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                break;
            case 0x1a: // 1.8p3, experience Orb spawn
                write(packetId);
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                write(in.readInt());
                write(in.readShort());
                break;
            case 0x1b: // ???
                write(packetId);
                copyNBytes(18);
                break;
            case 0x1c: // Entity Velocity?
                write(packetId);
                copyNBytes(10);
                break;
            case 0x1d: // Destroy Entity
                write(packetId);
                copyNBytes(4);
                break;
            case 0x1e: // Entity
                write(packetId);
                copyNBytes(4);
                break;
            case 0x1f: // Entity Relative Move
                write(packetId);
                copyNBytes(7);
                break;
            case 0x20: // Entity Look
                write(packetId);
                copyNBytes(6);
                break;
            case 0x21: // Entity Look and Relative Move
                write(packetId);
                copyNBytes(9);
                break;
            case 0x22: // Entity Teleport
                write(packetId);
                copyNBytes(18);
                break;
            case 0x26: // Entity status?
                write(packetId);
                copyNBytes(5);
                break;
            case 0x27: // Attach Entity?
                write(packetId);
                copyNBytes(8);
                break;
            case 0x28: // Entity Metadata
                write(packetId);
                write(in.readInt());

                copyUnknownBlob();
                break;
            case 0x29: // new in 1.8, add status effect (41)
                write(packetId);
                write(in.readInt());
                write(in.readByte());
                write(in.readByte());
                write(in.readShort());
                break;
            case 0x2a: // new in 1.8, remove status effect (42)
                write(packetId);
                write(in.readInt());
                write(in.readByte());
                break;
            case 0x2b: // new in 1.8 (43)
                write(packetId);
                write(in.readByte());
                write(in.readByte());
                write(in.readShort());
                break;
            case 0x32: // Pre-Chunk
                write(packetId);
                copyNBytes(9);
                break;
            case 0x33: // Map Chunk
                write(packetId);
                copyNBytes(13);
                int chunkSize = in.readInt();
                write(chunkSize);
                copyNBytes(chunkSize);
                break;
            case 0x34: // Multi Block Change
                write(packetId);
                copyNBytes(8);
                short arraySize = in.readShort();
                write(arraySize);
                copyNBytes(arraySize * 4);
                break;
            case 0x35: // Block Change
                write(packetId);
                x = in.readInt();
                y = in.readByte();
                z = in.readInt();
                byte blockType = in.readByte();
                byte metadata = in.readByte();
                coordinate = new Coordinate(x, y, z, player);

                /*
                if (blockType == 54 && player.placedChest(coordinate)) {
                    lockChest(coordinate);
                    player.placingChest(null);
                }
                 * 
                 */

                write(x);
                write(y);
                write(z);
                write(blockType);
                write(metadata);

                break;
            case 0x36: // ???
                write(packetId);
                copyNBytes(12);
                break;
            case 0x3c: // Explosion
                write(packetId);
                copyNBytes(28);
                int recordCount = in.readInt();
                write(recordCount);
                copyNBytes(recordCount * 3);
                break;
            case 0x3d: // Unknown
                write(packetId);
                write(in.readInt());
                write(in.readInt());
                write(in.readByte());
                write(in.readInt());
                write(in.readInt());
                break;
            case 0x46: // (70) Invalid state, changed in 1.8 (added serverMode byte)
                write(packetId);
                write(in.readByte());
                write(in.readByte());
                break;
            case 0x47: // Thunder
                write(packetId);
                copyNBytes(17);
                break;
            case 0x64: // Open window
                boolean allow = true;
                byte id = in.readByte();
                byte invtype = in.readByte();
                String typeString = readUTF16();
                byte unknownByte = in.readByte();
                //byte id = in.readByte();
                //byte invtype = in.readByte();
                //String typeString = in.readUTF();
                /*
                if (invtype == 0) {
                    if (server.chests.canOpen(player, player.openedChest()) || player.isAdmin()) {
                        if (server.chests.isLocked(player.openedChest())) {
                            if (player.isAttemptingUnlock()) {
                                server.chests.unlock(player.openedChest());
                                player.setAttemptedAction(null);
                                player.addMessage("\u00a77 " + server.l.get("CHEST_UNLOCKED"));
                                typeString = "Open Chest";
                            } else {
                                typeString = server.chests.chestName(player.openedChest());
                            }
                        } else {
                            typeString = "Open Chest";
                            if (player.isAttemptLock()) {
                                lockChest(player.openedChest());
                                typeString = player.nextChestName();
                            }
                        }
                    } else {
                        player.addMessage("\u00a7c " + server.l.get("CHEST_LOCKED"));
                        in.readByte();
                        break;
                    }
                }
                 * 
                 */
                // the following may be necessary
                //typeString = "Open Chest";
                write(packetId);
                write(id);
                write(invtype);
                write(typeString);
                write(unknownByte);
                break;
            case 0x65:
                write(packetId);
                write(in.readByte());
                break;
            case 0x66: // Inventory Item Move
                byte typeFrom = in.readByte();
                short slotFrom = in.readShort();
                byte typeTo = in.readByte();
                short slotTo = in.readShort();

                write(packetId);
                write(typeFrom);
                write(slotFrom);
                write(typeTo);
                write(slotTo);
                write(in.readBoolean());
                short moveItem = in.readShort();
                write(moveItem);
                if (moveItem != -1) {
                    write(in.readByte());
                    write(in.readShort());
                }
                break;
            case 0x67: // Inventory Item Update
                byte type67 = in.readByte();
                short slot = in.readShort();
                short setItem = in.readShort();
                write(packetId);
                write(type67);
                write(slot);
                write(setItem);
                if (setItem != -1) {
                    write(in.readByte());
                    write(in.readShort());
                }
                break;
            case 0x68: // Inventory
                byte type = in.readByte();
                write(packetId);
                write(type);
                short count = in.readShort();
                write(count);
                for (int c = 0; c < count; ++c) {
                    short item = in.readShort();
                    write(item);

                    if (item != -1) {
                        write(in.readByte());
                        write(in.readShort());
                    }
                }
                break;
            case 0x69:
                write(packetId);
                write(in.readByte());
                write(in.readShort());
                write(in.readShort());
                break;
            case 0x6a:
                write(packetId);
                write(in.readByte());
                write(in.readShort());
                write(in.readByte());
                break;
            case 0x6b: // 1.8 (107)
                write(packetId);
                write(in.readShort());
                write(in.readShort());
                write(in.readShort());
                write(in.readShort());
                break;
            case (byte) 0x82: // Update Sign
                write(packetId);
                write(in.readInt());
                write(in.readShort());
                write(in.readInt());
                write(readUTF16());
                write(readUTF16());
                write(readUTF16());
                write(readUTF16());
                break;
            case (byte) 0x83: // Map data
                write(packetId);
                write(in.readShort());
                write(in.readShort());
                byte length = in.readByte();
                write(length);
                copyNBytes(0xff & length);
                break;
            case (byte) 0xc3: // BukkitContrib
                write(packetId);
                write(in.readInt());
                copyNBytes(write(in.readInt()));
                break;
            case (byte) 0xc8: // Statistic
                write(packetId);
                copyNBytes(5);
                break;
            case (byte) 0xc9: // (201) 1.8, playerList
                write(packetId);
                write(readUTF16());
                write(in.readByte());
                write(in.readShort());
                break;
            case (byte) 0xd3: // Red Power (mod by Eloraam)
                write(packetId);
                copyNBytes(1);
                copyVLC();
                copyVLC();
                copyVLC();
                copyNBytes((int) copyVLC());
                break;
            case (byte) 0xe6: // ModLoaderMP by SDK
                write(packetId);
                write(in.readInt()); // mod
                write(in.readInt()); // packet id
                copyNBytes(write(in.readInt()) * 4); // ints
                copyNBytes(write(in.readInt()) * 4); // floats
                int sizeString = write(in.readInt()); // strings
                for (int i = 0; i < sizeString; i++) {
                  copyNBytes(write(in.readInt()));
                }
                break;
            case (byte) 0xfe: // 1.8, poll server status (254)
                write(packetId);
                break;
            case (byte) 0xff: // Disconnect/Kick
                write(packetId);
                String reason = readUTF16();
                write(reason);
                if (reason.startsWith("Took too long")) {
                    System.err.println("MAY NEED ROBOTS!!");
                    //server.addRobot(player);
                }
                player.close();
                break;
            default:
                if (EXPENSIVE_DEBUG_LOGGING) {
                    while (true) {
                        skipNBytes(1);
                        flushAll();
                    }
                } else {
                    if (lastPacket != null) {
                        throw new IOException("Unable to parse unknown " + streamType
                                + " packet 0x" + Integer.toHexString(packetId) + " for player "
                                + player.getName() + " (after 0x" + Integer.toHexString(lastPacket));
                    } else {
                        throw new IOException("Unable to parse unknown " + streamType
                                + " packet 0x" + Integer.toHexString(packetId) + " for player "
                                + player.getName());
                    }
                }
        }
        packetFinished();
        lastPacket = (packetId == 0x00) ? lastPacket : packetId;
    }

    private long copyVLC() throws IOException {
        long value = 0;
        int shift = 0;
        while (true) {
            int i = write(in.readByte());
            value |= (i & 0x7F) << shift;
            if ((i & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return value;
    }
    private String readUTF16() throws IOException {
        short length = in.readShort();
        byte[] bytes = new byte[length * 2 + 2];
        for (short i = 0; i < length * 2; i++) {
            bytes[i + 2] = in.readByte();
        }
        bytes[0] = (byte) 0xfffffffe;
        bytes[1] = (byte) 0xffffffff;
        return new String(bytes, "UTF-16");
    }

    /*
    private void lockChest(Coordinate coordinate) {
        Chest adjacentChest = server.chests.adjacentChest(coordinate);
        if (player.isAttemptLock() || adjacentChest != null && !adjacentChest.isOpen()) {
            if (adjacentChest != null && !adjacentChest.isOpen()) {
                server.chests.giveLock(adjacentChest.owner(), coordinate, false, adjacentChest.name());
            } else {
                if (adjacentChest != null) {
                    adjacentChest.lock(player);
                    adjacentChest.rename(player.nextChestName());
                }
                server.chests.giveLock(player, coordinate, false, player.nextChestName());
            }
            player.setAttemptedAction(null);
            player.addMessage("\u00a77This chest is now locked.");
        } else if (!server.chests.isChest(coordinate)) {
            server.chests.addOpenChest(coordinate);
        }
    }
     * 
     */

    private void copyPlayerLocation() throws IOException {
        if (!isServerTunnel) {
            motionCounter++;
        }
        if (!isServerTunnel && motionCounter % 8 == 0) {
            double x = in.readDouble();
            double y = in.readDouble();
            double stance = in.readDouble();
            double z = in.readDouble();
            player.updateLocation(x, y, z, stance);
            write(x);
            write(y);
            write(stance);
            write(z);
            copyNBytes(1);
        } else {
            copyNBytes(33);
        }
    }

    private void copyUnknownBlob() throws IOException {
        byte unknown = in.readByte();
        write(unknown);

        while (unknown != 0x7f) {
            int type = (unknown & 0xE0) >> 5;

            switch (type) {
                case 0:
                    write(in.readByte());
                    break;
                case 1:
                    write(in.readShort());
                    break;
                case 2:
                    write(in.readInt());
                    break;
                case 3:
                    write(in.readFloat());
                    break;
                case 4:
                    write(readUTF16());
                    break;
                case 5:
                    write(in.readShort());
                    write(in.readByte());
                    write(in.readShort());
                    break;
                case 6:
                    write(in.readInt());
                    write(in.readInt());
                    write(in.readInt());
            }

            unknown = in.readByte();
            write(unknown);
        }
    }

    private byte write(byte b) throws IOException {
        out.writeByte(b);
        return b;
    }

    private short write(short s) throws IOException {
        out.writeShort(s);
        return s;
    }

    private int write(int i) throws IOException {
        out.writeInt(i);
        return i;
    }

    private long write(long l) throws IOException {
        out.writeLong(l);
        return l;
    }

    private float write(float f) throws IOException {
        out.writeFloat(f);
        return f;
    }

    private double write(double d) throws IOException {
        out.writeDouble(d);
        return d;
    }

    private String write(String s) throws IOException {
        byte[] bytes = s.getBytes("UTF-16");
        if (s.length() == 0) {
            write((byte) 0x00);
            write((byte) 0x00);
            return s;
        }
        bytes[0] = (byte) ((s.length() >> 8) & 0xFF);
        bytes[1] = (byte) ((s.length() & 0xFF));
        for (byte b : bytes) {
            write(b);
        }
        return s;
    }

    private String write8(String s) throws IOException {
        out.writeUTF(s);
        return s;
    }

    private boolean write(boolean b) throws IOException {
        out.writeBoolean(b);
        return b;
    }

    private void skipNBytes(int bytes) throws IOException {
        int overflow = bytes / buffer.length;
        for (int c = 0; c < overflow; ++c) {
            in.readFully(buffer, 0, buffer.length);
        }
        in.readFully(buffer, 0, bytes % buffer.length);
    }

    private void copyNBytes(int bytes) throws IOException {
        int overflow = bytes / buffer.length;
        for (int c = 0; c < overflow; ++c) {
            in.readFully(buffer, 0, buffer.length);
            out.write(buffer, 0, buffer.length);
        }
        in.readFully(buffer, 0, bytes % buffer.length);
        out.write(buffer, 0, bytes % buffer.length);
    }

    private void kick(String reason) throws IOException {
        write((byte) 0xff);
        write(reason);
        packetFinished();
    }

    private String getLastColorCode(String message) {
        String colorCode = "";
        int lastIndex = message.lastIndexOf('\u00a7');
        if (lastIndex != -1 && lastIndex + 1 < message.length()) {
            colorCode = message.substring(lastIndex, lastIndex + 2);
        }

        return colorCode;
    }

    private void sendMessage(String message) throws IOException {
        if (message.length() > MESSAGE_SIZE) {
            int end = MESSAGE_SIZE - 1;
            while (end > 0 && message.charAt(end) != ' ') {
                end--;
            }
            if (end == 0) {
                end = MESSAGE_SIZE;
            } else {
                end++;
            }

            if (message.charAt(end) == '\u00a7') {
                end--;
            }

            String firstPart = message.substring(0, end);
            sendMessagePacket(firstPart);
            sendMessage(getLastColorCode(firstPart) + message.substring(end));
        } else {
            sendMessagePacket(message);
        }
    }

    private void sendMessagePacket(String message) throws IOException {
        if (message.length() > MESSAGE_SIZE) {
            System.out.println("[MC Server Pail] Invalid message size: " + message);
            return;
        }
        write(0x03);
        write(message);
        packetFinished();
    }

    private void packetFinished() throws IOException {
        if (EXPENSIVE_DEBUG_LOGGING) {
            inputDumper.packetFinished();
            outputDumper.packetFinished();
        }
    }

    private void flushAll() throws IOException {
        try {
            ((OutputStream) out).flush();
        } finally {
            if (EXPENSIVE_DEBUG_LOGGING) {
                inputDumper.flush();
            }
        }
    }

    private void copyPlayerLook() throws IOException {
    float yaw = in.readFloat();
    float pitch = in.readFloat();
    write(yaw);
    write(pitch);
  }

    private final class Tunneler extends Thread {
        @Override public void run() {
            try {
                while (run) {
                    lastRead = System.currentTimeMillis();

                    try {
                        handlePacket();

                        if (isServerTunnel) {
                            while (player.hasMessages()) {
                                sendMessage(player.getMessage());
                            }
                        }

                        flushAll();
                    } catch (IOException e) {
                        if (run/* && !player.isRobot()*/) {
                            System.err.println("[MC Server Pail] " + e);
                            System.err.println("[MC Server Pail] " + streamType
                                    + " error handling traffic for "
                                    + player.getIPAddress());
                        }
                        break;
                    }
                }

                try {
                    if (player.isKicked()) {
                        kick(player.getKickMsg());
                    }
                    flushAll();
                } catch (IOException e) {
                }
            } finally {
                if (EXPENSIVE_DEBUG_LOGGING) {
                    inputDumper.cleanup();
                    outputDumper.cleanup();
                }
            }
        }
    }
}