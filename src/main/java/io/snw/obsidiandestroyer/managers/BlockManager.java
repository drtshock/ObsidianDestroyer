package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.BlockTimer;
import io.snw.obsidiandestroyer.DurabilityBlock;
import io.snw.obsidiandestroyer.TimerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class BlockManager {
    private static BlockManager instance;
    private HashMap<Integer, Integer> durability = new HashMap<Integer, Integer>();
    private HashMap<Integer, BlockTimer> timer = new HashMap<Integer, BlockTimer>();
    private Map<String, DurabilityBlock> durabilityBlocks = new HashMap<String, DurabilityBlock>();

    public BlockManager() {
        instance = this;
        durabilityBlocks = ConfigManager.getInstance().getDurabilityBlocks();
    }

    public void setDuraBlocks(Map<String, DurabilityBlock> rBocks) {
        this.durabilityBlocks = rBocks;
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

        if (!contains(block.getType().name())) {
            return null;
        }

        final String eventTypeRep = event.getEntity().toString();

        if (eventTypeRep.equals("CraftTNTPrimed") && !getTntEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftSnowball") && !getCannonsEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftCreeper") && !getCreepersEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftWither") && !getWithersEnabled(block.getType().name())) {
            return null;
        }
        if (eventTypeRep.equals("CraftMinecartTNT") && !getTntMinecartsEnabled(block.getType().name())) {
            return null;
        }
        if ((eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast")) && !getGhastsEnabled(block.getType().name())) {
            return null;
        }

        Block returnedBlock = null;
        //ObsidianDestroyer.LOG.info("Protecting Block..!");
        returnedBlock = block;
        Integer representation = at.getWorld().hashCode() + at.getBlockX() * 2389 + at.getBlockY() * 4027 + at.getBlockZ() * 2053;
        if (getDurabilityEnabled(block.getType().name()) && getDurability(block.getType().name()) > 1) {
            if (durability.containsKey(representation) && checkDurabilityActive(representation) == TimerState.RUN) {
                int currentDurability = (int) durability.get(representation);
                currentDurability++;
                if (checkIfMax(currentDurability, block.getType().name())) {
                    // counter has reached max durability, remove and drop an item
                    dropBlockAndResetTime(representation, at, block.getType().name());
                } else {
                    // counter has not reached max durability yet
                    durability.put(representation, currentDurability);
                    if (getDurabilityResetTimerEnabled(block.getType().name())) {
                        startNewTimer(representation, block.getType().name());
                    }
                }
            } else {
                durability.put(representation, 1);
                if (getDurabilityResetTimerEnabled(block.getType().name())) {
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

        if (!contains(b.getType().name())) {
            return;
        }

        //ObsidianDestroyer.LOG.info("Destroying Block!!");
        double chance = getChanceToDropBlock(b.getType().name());

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
        return value == getDurability(id);
    }

    private void dropBlockAndResetTime(Integer representation, Location at, String key) {
        durability.remove(representation);
        destroyBlockAndDropItem(at);

        if (getDurabilityResetTimerEnabled(key)) {
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

        timer.put(representation, new BlockTimer(getDurabilityResetTime(material)));
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
     * Returns the HashMap containing all saved durabilities.
     * 
     * @return the HashMap containing all saved durabilities
     */
    public HashMap<Integer, Integer> getMaterialDurability() {
        return durability;
    }

    /**
     * Sets the HashMap containing all saved durabilities.
     * 
     * @param map containing all saved durabilities
     */
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
    public HashMap<Integer, BlockTimer> getMaterialTimer() {
        return timer;
    }

    /**
     * Sets the HashMap containing all saved durability timers.
     * 
     * @param map containing all saved durability timers
     */
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

    /**
     * Checks if the managed blocks contains an item
     * 
     * @param item to compare against
     * @return true if item equals managed block
     */
    public boolean contains(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether durability for block is enabled.
     * 
     * @return whether durability for block is enabled
     */
    public boolean getDurabilityEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getEnabled();
        }
        return false;
    }

    /**
     * Returns the max durability.
     * 
     * @return the max durability
     */
    public int getDurability(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getDurability();
        }
        return 0;
    }

    /**
     * Returns whether durability timer for block is enabled.
     * 
     * @return whether durability timer for block is enabled
     */
    public boolean getDurabilityResetTimerEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getResetEnabled();
        }
        return false;
    }

    /**
     * Returns the time in milliseconds after which the durability gets reset.
     * 
     * @return the time in milliseconds after which the durability gets reset
     */
    public long getDurabilityResetTime(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getResetTime();
        }
        return 100000L;
    }

    /**
     * Returns the chance to drop an item from a blown up block.
     * 
     * @return the chance to drop an item from a blown up block
     */
    public double getChanceToDropBlock(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getChanceTopDrop();
        }
        return 0.6D;
    }

    /**
     * Returns if Fireball damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getGhastsEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getGhastsEnabled();
        }
        return false;
    }

    /**
     * Returns if Creeper damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getCreepersEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getCreepersEnabled();
        }
        return false;
    }

    /**
     * Returns if Cannon damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getCannonsEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getCannonsEnabled();
        }
        return false;
    }

    /**
     * Returns if TNT damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getTntEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getTntEnabled();
        }
        return false;
    }

    /**
     * Returns if TNT minecart damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getTntMinecartsEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getTntMinecartsEnabled();
        }
        return false;
    }

    /**
     * Returns if Wither damage is enabled for block
     * 
     * @param key
     * @return
     */
    private boolean getWithersEnabled(String material) {
        if (durabilityBlocks.containsKey(material)) {
            return durabilityBlocks.get(material).getWithersEnabled();
        }
        return false;
    }
}
