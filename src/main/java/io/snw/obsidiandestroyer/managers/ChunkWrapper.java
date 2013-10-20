package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.Key;
import io.snw.obsidiandestroyer.io.ASRFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public class ChunkWrapper {

    private Map<Integer, Key> durabilities = new HashMap<Integer, Key>();
    private final int chunkX, chunkZ;
    private final String world;
    private final File durabilitiesDir;

    ChunkWrapper(Chunk chunk, File durabilitiesDir) {
        this.chunkX = chunk.getX();
        this.chunkZ = chunk.getZ();
        this.world = chunk.getWorld().getName();
        this.durabilitiesDir = durabilitiesDir;
    }

    public String getWorldName() {
        return world;
    }

    public int getDurability(int rep) {
        return durabilities.get(rep).durabilityAmount;
    }

    public void addBlock(int durability, Block block) {
        Key key = new Key(block.getLocation(), durability);
        durabilities.put(key.hashCode(), key);
    }
    
    public void addTimer(int durability, long time, Block block) {
        Key key = new Key(block.getLocation(), durability, time);
        durabilities.put(key.hashCode(), key);
    }

    /**
     * Saves the chunk information
     * 
     * @param load set to true to load data after saving
     * @param clear set to true to clear self after saving
     */
    public void save(boolean load, boolean clear) {
        File durabilityFile = new File(durabilitiesDir, chunkX + "." + chunkZ + "." + world + ".asr");
        // Used for sane file creation
        boolean noDuraFile = false;
        if (this.durabilities.size() <= 0) {
            if (durabilityFile.exists()) {
                durabilityFile.delete();
            }
            noDuraFile = true;
        }
        if (!noDuraFile) {
            ASRFile region = new ASRFile();
            try {
                region.prepare(durabilityFile, true);
                for(Key key : this.durabilities.values()) {
                    region.write(key.x, key.y, key.z, key.durabilityAmount, key.durabilityTime);
                }
                region.close();
            } catch(IOException e) {
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
     * Loads a specific directory
     * 
     * @param isTimer set to true if loading block information
     */
    public void load() {
        File file = new File(durabilitiesDir, chunkX + "." + chunkZ + "." + world + ".asr");
        if (!file.exists()) {
             return;
        }
        String[] fileParts = file.getName().split("\\.");
        if (fileParts.length < 3) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "Failed loading chunk durabilites! " + chunkX + " " + chunkZ);
            return;
        }
        String w = fileParts[2]; // To see if world == file name world
        World bWorld = Bukkit.getWorld(w);
        if (bWorld == null) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "World is null!");
            return;
        }
        if (!w.equals(world)) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "Wrong world.." );
            return;
        }
        ASRFile region = new ASRFile();
        try {
            region.prepare(file, false);
            Key info = null;
            while((info = region.getNext(bWorld)) != null) {
                if (System.currentTimeMillis() > info.durabilityTime && MaterialManager.getInstance().getDurabilityResetTimerEnabled(info.toLocation().getBlock().getType().name())) {
                    continue;
                }
                durabilities.put(info.hashCode(), info);
            }
            region.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
