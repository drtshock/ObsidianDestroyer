package com.drtshock.obsidiandestroyer.managers;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.datatypes.Key;
import com.drtshock.obsidiandestroyer.datatypes.io.ODRFile;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class ChunkWrapper {

    private final int chunkX, chunkZ;
    private final String world;
    private final File durabilitiesDir;
    private ConcurrentMap<Integer, Key> durabilities = new ConcurrentHashMap<Integer, Key>();

    /**
     * Wraps a chunk with a ChunkWrapper
     *
     * @param chunk           the chunk to wrap
     * @param durabilitiesDir the directory to store this wrapper in
     */
    ChunkWrapper(Chunk chunk, File durabilitiesDir) {
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
        this.world = chunk.getWorld().getName();
        this.durabilitiesDir = durabilitiesDir;
    }

    /**
     * Gets the world name
     *
     * @return the world name
     */
    public String getWorldName() {
        return world;
    }

    /**
     * Gets the Key of the location
     *
     * @param location the location to get the key from
     * @return the key from the location
     */
    public Key getKey(Location location) {
        final int prime = 31;
        int rep = 1;
        rep = prime * rep + location.hashCode();
        return durabilities.get(rep);
    }

    /**
     * Gets the durability of a hash representation
     *
     * @param representation the hash representation to check
     * @return the durability of the hash representation
     */
    public int getDurability(int representation) {
        return durabilities.get(representation).durabilityAmount;
    }

    /**
     * Gets the durability time of a hash representation
     *
     * @param representation the hash representation to check
     * @return the durability time of the hash representation
     */
    public long getDurabilityTime(int representation) {
        return durabilities.get(representation).durabilityTime;
    }

    /**
     * Gets the durability time of the location
     *
     * @param location the location to check
     * @return the durability of the location
     */
    public int getDurability(Location location) {
        return getKey(location).durabilityAmount;
    }

    /**
     * Gets the durability time of the location
     *
     * @param location the location to check
     * @return the durability time of the location
     */
    public long getDurabilityTime(Location location) {
        return getKey(location).durabilityTime;
    }

    /**
     * Adds a block to the chunk
     *
     * @param durability the damage done to the block
     * @param block      the block to add
     */
    public void addBlock(int durability, Block block) {
        Key key = new Key(block.getLocation(), durability);
        durabilities.put(key.hashCode(), key);
    }

    /**
     * Adds a block with a timer to the chunk
     *
     * @param durability the damage done to the block
     * @param time       the time value of the block
     * @param block      the block to be added
     */
    public void addBlockTimer(int durability, long time, Block block) {
        Key key = new Key(block.getLocation(), durability, time);
        durabilities.put(key.hashCode(), key);
    }

    /**
     * Removes a key from the chunk
     *
     * @param block the block to remove
     */
    public void removeKey(Block block) {
        removeKey(block.getLocation());
    }

    /**
     * Removes a key from the chunk
     *
     * @param location the location to remove
     */
    public void removeKey(Location location) {
        Key key = new Key(location, 0, 0);
        durabilities.remove(key.hashCode());
    }

    /**
     * Removes a key from the chunk
     *
     * @param representation the hash representation to remove
     */
    public void removeKey(int representation) {
        durabilities.remove(representation);
    }

    /**
     * Clear the durabilities loaded in the chunk
     */
    public void removeKeys() {
        durabilities.clear();
    }

    /**
     * Does the chunk contains this location key
     *
     * @param location the location to check the chunk for
     * @return true if the location is found within the chunk
     */
    public boolean contains(Location location) {
        final int prime = 31;
        int rep = 1;
        rep = prime * rep + location.hashCode();
        return contains(rep);
    }

    /**
     * Does the chunk contains this location key
     *
     * @param representation the hash representation to check the chunk for
     * @return true if the hash representation is found within the chunk
     */
    public boolean contains(int representation) {
        return durabilities.containsKey(representation);
    }

    /**
     * Saves the chunk information
     *
     * @param load  set to true to load data after saving
     * @param clear set to true to clear self after saving
     */
    public void save(boolean load, boolean clear) {
        File durabilityFile = new File(durabilitiesDir, chunkX + "." + chunkZ + "." + world + ".odr");
        // Used for sane file creation
        boolean noDuraFile = false;
        if (this.durabilities.size() > 0) {
            durabilities.remove(expiredDurabilities());
        }
        if (this.durabilities.size() <= 0) {
            if (durabilityFile.exists()) {
                durabilityFile.delete();
            }
            noDuraFile = true;
        }
        if (!noDuraFile) {
            ODRFile region = new ODRFile();
            try {
                region.prepare(durabilityFile, true);
                for (Key key : durabilities.values()) {
                    region.write(key.x, key.y, key.z, key.durabilityAmount, key.durabilityTime);
                }
                region.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clear) {
            this.durabilities.clear();
        }
        if (load) {
            load();
        }
    }

    /**
     * Gets a list of durability keys that are not worth saving
     *
     * @return list of expired durability keys
     */
    private List<Integer> expiredDurabilities() {
        List<Integer> expiredDurabilities = new ArrayList<Integer>();
        for (Key key : durabilities.values()) {
            if (MaterialManager.getInstance().getDurabilityResetTimerEnabled(key.toLocation().getBlock().getType().name(), key.toLocation().getBlock().getData())) {
                long currentTime = System.currentTimeMillis();
                if (currentTime > key.durabilityTime) {
                    if (ConfigManager.getInstance().getMaterialsRegenerateOverTime()) {
                        long regenTime = MaterialManager.getInstance().getDurabilityResetTime(key.toLocation().getBlock().getType().name(), key.toLocation().getBlock().getData());
                        int amount = Math.max(1, Math.round((float) (currentTime - key.durabilityTime) / regenTime));
                        int durability = key.durabilityAmount - amount;
                        if (durability <= 0) {
                            expiredDurabilities.add(key.hashCode());
                        }
                    } else {
                        expiredDurabilities.add(key.hashCode());
                    }
                }
            }
        }
        return expiredDurabilities;
    }

    /**
     * Loads a specific directory
     */
    public void load() {
        File file = new File(durabilitiesDir, chunkX + "." + chunkZ + "." + world + ".odr");
        if (!file.exists()) {
            return;
        }
        String[] fileParts = file.getName().split("\\.");
        if (fileParts.length < 3) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "Failed loading chunk durabilites! {0} {1}", new Object[]{chunkX, chunkZ});
            return;
        }
        String w = fileParts[2]; // To see if world == file name world
        World bWorld = Bukkit.getWorld(w);
        if (bWorld == null) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "World is null!");
            return;
        }
        if (!w.equals(world)) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "Wrong world..");
            return;
        }
        ODRFile region = new ODRFile();
        try {
            region.prepare(file, false);
            Key info = null;
            while ((info = region.getNext(bWorld)) != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime > info.durabilityTime && MaterialManager.getInstance().getDurabilityResetTimerEnabled(info.toLocation().getBlock().getType().name(), info.toLocation().getBlock().getData())) {
                    if (ConfigManager.getInstance().getMaterialsRegenerateOverTime()) {
                        long regenTime = MaterialManager.getInstance().getDurabilityResetTime(info.toLocation().getBlock().getType().name(), info.toLocation().getBlock().getData());
                        long result = currentTime - info.durabilityTime;
                        int amount = Math.max(1, Math.round((float) result / regenTime));
                        int durability = info.durabilityAmount - amount;
                        if (durability <= 0) {
                            continue;
                        }
                        info = new Key(info.toLocation(), durability, currentTime + regenTime);
                    } else {
                        continue;
                    }
                }
                durabilities.put(info.hashCode(), info);
            }
            region.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
