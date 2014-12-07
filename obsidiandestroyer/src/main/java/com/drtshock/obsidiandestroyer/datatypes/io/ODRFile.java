package com.drtshock.obsidiandestroyer.datatypes.io;

import com.drtshock.obsidiandestroyer.datatypes.Key;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ODRFile {

    private FileOutputStream output;
    private FileInputStream input;
    private FileChannel channel;
    private ByteBuffer buffer = null;
    private boolean write = false;

    /**
     * Creates a new ODRegion file
     */
    public ODRFile() {
        buffer = ByteBuffer.allocateDirect(24);
    }

    /**
     * Prepares the file for read or write
     *
     * @param file  the file to prepare
     * @param write true to write to the file, false otherwise
     * @throws FileNotFoundException thrown if the file is missing
     */
    public void prepare(File file, boolean write) throws FileNotFoundException {
        if (write) {
            output = new FileOutputStream(file, false);
            channel = output.getChannel();
        } else {
            input = new FileInputStream(file);
            channel = input.getChannel();
        }
        this.write = write;
    }

    /**
     * Writes a block to file
     *
     * @param location   the location of the block
     * @param duraAmount material durability amount
     * @throws IOException thrown if something happens
     */
    public void write(Location location, int duraAmount) throws IOException {
        write(location.getBlockX(), location.getBlockY(), location.getBlockZ(), duraAmount, 0L);
    }

    /**
     * Writes a block location to file
     *
     * @param x          the x location
     * @param y          the y location
     * @param z          the z location
     * @param duraAmount material durability amount
     * @param duraTime   material durability reset time
     * @throws IOException thrown if something happens
     */
    public void write(int x, int y, int z, int duraAmount, long duraTime) throws IOException {
        buffer.clear();
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
        buffer.putInt(duraAmount);
        buffer.putLong(duraTime);
        buffer.flip();
        channel.write(buffer);
    }

    /**
     * Writes an entity to file
     *
     * @param location the location of the entity
     * @param duraTime material durability reset time
     * @param duraTime material durability reset time
     * @throws IOException thrown if something happens
     */
    public void write(Location location, int duraAmount, long duraTime) throws IOException {
        write(location.getBlockX(), location.getBlockY(), location.getBlockZ(), duraAmount, duraTime);
    }

    /**
     * Gets the next block in the file
     *
     * @param world the world for location creation/reading
     * @return the entry (a block) or null if EOF has been reached / nothing was read
     * @throws IOException thrown if something happens
     */
    public Key getNext(World world) throws IOException {
        int read = channel.read(buffer);
        if (read <= 0) {
            return null;
        }
        buffer.position(0);
        int x = buffer.getInt(), y = buffer.getInt(), z = buffer.getInt();
        int value = buffer.getInt();
        long time = buffer.getLong();
        buffer.clear();
        return new Key(world.getName(), x, y, z, value, time);
    }

    /**
     * Closes the ODRegion, saving it to disk if needed
     *
     * @throws IOException thrown if something goes wrong
     */
    public void close() throws IOException {
        if (write) {
            output.close();
        } else {
            input.close();
        }
    }
}
