package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.BlockTimer;
import io.snw.obsidiandestroyer.enumerations.TimerState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class BlockManager {
    private static BlockManager instance;

    @Deprecated
    private HashMap<Integer, Integer> durability = new HashMap<Integer, Integer>();
    @Deprecated
    private HashMap<Integer, BlockTimer> timer = new HashMap<Integer, BlockTimer>();

    private final File durabilityDir;
    private ConcurrentMap<String, ChunkWrapper> chunks =  new ConcurrentHashMap<String, ChunkWrapper>();

    public BlockManager() {
        instance = this;
 
        durabilityDir = new File(ObsidianDestroyer.getInstance().getDataFolder(), "data" + File.separator + "entities");
    }

    /**
     * Handles a block on an EntityExplodeEvent
     * 
     * @param block in EntityExplodeEvent
     * @return block to remove from Explosion blocklist
     */
    public Block blowBlockUp(final Block block, EntityExplodeEvent event) {
        if (block == null) {
            return null;
        }
        if (block.getType() == Material.AIR) {
            return null;
        }
        Location at = block.getLocation();
        if (at == null) {
            return null; 
        }

        if (!MaterialManager.getInstance().contains(block.getType().name())) {
            return null;
        }

        final String eventTypeRep = event.getEntity().toString();

        if (eventTypeRep.equals("CraftTNTPrimed") && !MaterialManager.getInstance().getTntEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftSnowball") && !MaterialManager.getInstance().getCannonsEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftCreeper") && !MaterialManager.getInstance().getCreepersEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftWither") && !MaterialManager.getInstance().getWithersEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftMinecartTNT") && !MaterialManager.getInstance().getTntMinecartsEnabled(block.getType().name())) {
            return null;
        }
        if ((eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast")) && !MaterialManager.getInstance().getGhastsEnabled(block.getType().name())) {
            return null;
        }

        Block returnedBlock = null;
        //ObsidianDestroyer.LOG.info("Protecting Block..!");
        returnedBlock = block;
        Integer representation = at.getWorld().hashCode() + at.getBlockX() * 2389 + at.getBlockY() * 4027 + at.getBlockZ() * 2053;
        if (MaterialManager.getInstance().getDurabilityEnabled(block.getType().name()) && MaterialManager.getInstance().getDurability(block.getType().name()) > 1) {
            if (durability.containsKey(representation) && checkDurabilityActive(representation) == TimerState.RUN) {
                int currentDurability = (int) durability.get(representation);
                currentDurability++;
                if (checkIfMax(currentDurability, block.getType().name())) {
                    // counter has reached max durability, remove and drop an item
                    dropBlockAndResetTime(representation, at, block.getType().name());
                } else {
                    // counter has not reached max durability yet
                    durability.put(representation, currentDurability);
                    if (MaterialManager.getInstance().getDurabilityResetTimerEnabled(block.getType().name())) {
                        startNewTimer(representation, block.getType().name());
                    }
                }
            } else {
                durability.put(representation, 1);
                if (MaterialManager.getInstance().getDurabilityResetTimerEnabled(block.getType().name())) {
                    startNewTimer(representation, block.getType().name());
                }
                if (checkIfMax(1, block.getType().name())) {
                    dropBlockAndResetTime(representation, at, block.getType().name());
                }
            }
        } else {
            destroyBlockAndDropItem(at);
        }
        return returnedBlock;
    }

    private void destroyBlockAndDropItem(final Location at) {
        if (at == null) {
            return;
        }

        final Block b = at.getBlock();

        if (!MaterialManager.getInstance().contains(b.getType().name())) {
            return;
        }

        //ObsidianDestroyer.LOG.info("Destroying Block!!");
        double chance = MaterialManager.getInstance().getChanceToDropBlock(b.getType().name());

        if (chance > 1.0)
            chance = 1.0;
        if (chance < 0.0)
            chance = 0.0;

        final double random = Math.random();

        if (chance == 1.0 || chance <= random) {
            ItemStack is = new ItemStack(b.getType(), 1);

            if (is.getType() == Material.AIR) {
                return;
            }

            // drop item
            at.getWorld().dropItemNaturally(at, is);
        }

        // changes original block to Air block
        b.setType(Material.AIR);
    }

    private boolean checkIfMax(int value, String id) {
        return value == MaterialManager.getInstance().getDurability(id);
    }

    private void dropBlockAndResetTime(Integer representation, Location at, String key) {
        durability.remove(representation);
        destroyBlockAndDropItem(at);

        if (MaterialManager.getInstance().getDurabilityResetTimerEnabled(key)) {
            if (timer.get(representation) != null) {
                timer.remove(representation);
            }
        }
    }

    private void startNewTimer(Integer representation, String material) {
        if (checkDurabilityActive(representation) == TimerState.END) {
            
        } else if (timer.get(representation) != null) {
            timer.remove(representation);
        }

        timer.put(representation, new BlockTimer(MaterialManager.getInstance().getDurabilityResetTime(material)));
    }

    @Deprecated
    public void checkDurability() {
        List<Integer> timersExpired = new ArrayList<Integer>();
        for (Entry<Integer, BlockTimer> blockTimer : timer.entrySet()) {
            if (System.currentTimeMillis() > blockTimer.getValue().getTimeToLive()) {
                timersExpired.add(blockTimer.getKey());
            }
        }
        for (Integer timerExpired : timersExpired) {
            removeMaterial(timerExpired);
        }
    }

    public TimerState checkDurabilityActive(Integer representation) {
        if (!timer.containsKey(representation)) {
            return TimerState.DEAD;
        }
        if (System.currentTimeMillis() > timer.get(representation).getTimeToLive()) {
            removeMaterial(representation);
            return TimerState.END;
        }
        return TimerState.RUN;
    }

    public void removeMaterial(int representation) {
        timer.remove(representation);
        durability.remove(representation);
    }

    public Integer getMaterialDurability(Integer representation) {
        if (checkDurabilityActive(representation) != TimerState.RUN && !durability.containsKey(representation)) {
            return 0;
        } else {
            return durability.get(representation);
        }
    }

    /**
     * Loads a chunk into the block manager
     * 
     * @param chunk the chunk to load
     */
    public void loadChunk(Chunk chunk){
            String str = chunkToString(chunk);
            ChunkWrapper wrapper = new ChunkWrapper(chunk, durabilityDir);
            wrapper.load();
            chunks.put(str, wrapper);
    }

    /**
     * Unloads a chunk from the block manager
     * 
     * @param chunk the chunk to unload
     */
    public void unloadChunk(Chunk chunk){
            String key = chunkToString(chunk);
            ChunkWrapper wrapper = chunks.get(key);
            if(wrapper != null){
                    wrapper.save(false, false);
                    chunks.remove(wrapper);
            }
    }

    private String chunkToString(Chunk chunk){
        return chunk.getX() + "." + chunk.getZ() + "." + chunk.getWorld().getName();
    }

    /**
     * Returns the HashMap containing all saved durabilities.
     * 
     * @return the HashMap containing all saved durabilities
     */
    @Deprecated
    public HashMap<Integer, Integer> getMaterialDurability() {
        return durability;
    }

    /**
     * Sets the HashMap containing all saved durabilities.
     * 
     * @param map containing all saved durabilities
     */
    @Deprecated
    public void setMaterialDurability(HashMap<Integer, Integer> map) {
        if (map == null) {
            return;
        }

        durability = map;
    }

    /**
     * Returns the HashMap containing all saved durability timers.
     * 
     * @return the HashMap containing all saved durability timers
     */
    @Deprecated
    public HashMap<Integer, BlockTimer> getMaterialTimer() {
        return timer;
    }

    /**
     * Sets the HashMap containing all saved durability timers.
     * 
     * @param map containing all saved durability timers
     */
    @Deprecated
    public void setMaterialTimer(HashMap<Integer, BlockTimer> map) {
        if (map == null) {
            return;
        }

        timer = map;
    }

    /**
     * Gets the instance
     * 
     * @return instance
     */
    public static BlockManager getInstance() {
        return instance;
    }
}
