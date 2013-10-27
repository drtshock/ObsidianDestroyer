package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.LiquidExplosion;
import io.snw.obsidiandestroyer.enumerations.TimerState;
import io.snw.obsidiandestroyer.util.Util;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class ChunkManager {
    private static ChunkManager instance;

    private final File durabilityDir;
    private ConcurrentMap<String, ChunkWrapper> chunks = new ConcurrentHashMap<String, ChunkWrapper>();
    private boolean doneSave = false;
    private int percent = 0;
    private List<String> disabledWorlds;

    /**
     * Creates wrappers around chunks
     */
    public ChunkManager() {
        instance = this;

        durabilityDir = new File(ObsidianDestroyer.getInstance().getDataFolder(), "data" + File.separator + "durabilities");
        if (!durabilityDir.exists()) {
            durabilityDir.mkdirs();
        }

        load();
    }

    /**
     * Handles the entity explosion event
     *
     * @param event the entity explosion event to handle
     */
    public void handleExplosion(EntityExplodeEvent event) {
        // Debug time taken
        final long time = System.currentTimeMillis();
        final int radius = ConfigManager.getInstance().getRadius();

        // cancel if radius is < 0 or > 10
        if (radius < 0) {
            ObsidianDestroyer.LOG.warning("Explosion radius is less than zero. Current value: " + radius);
            return;
        } else if (radius > 10) {
            ObsidianDestroyer.LOG.warning("Explosion radius is greater than 10. Current value: " + radius);
            return;
        }

        final Entity detonator = event.getEntity();

        if (detonator == null) {
            // some other plugins create new explosions passing 'null' as
            // Entity, so we need this here to fix it
            return;
        }

        final Location detonatorLoc = detonator.getLocation();
        final String eventTypeRep = event.getEntity().toString();
        //ObsidianDestroyer.debug("EventTypeRep: " + eventTypeRep);

        // List of blocks that will be removed from the blocklist
        List<Block> blocksIgnored = new ArrayList<Block>();

        // Hook into cannons... (somehow)
        // TODO: Hook into cannons again.
        if (eventTypeRep.equals("CraftSnowball")) {
            List<Location> hitLocs = new ArrayList<Location>();
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                hitLocs.add(block.getLocation());
                if (hitLocs.contains(block.getLocation())) {
                    continue;
                }
                if (MaterialManager.getInstance().contains(block.getType().name())) {
                    if (ChunkManager.getInstance().blowBlockUp(block.getLocation(), event.getEntity().toString())) {
                        blocksIgnored.add(block);
                    }
                }
            }
        }

        // Liquid overrides
        if (ConfigManager.getInstance().getBypassAllFluidProtection()) {
            LiquidExplosion.Handle(event);
        } else if ((detonatorLoc.getBlock().isLiquid()) && (ConfigManager.getInstance().getFluidsProtectIndustructables())) {
            return;
        }

        // Check explosion blocks and their distance from the detonation.
        for (Block block : event.blockList()) {
            if (MaterialManager.getInstance().contains(block.getType().name())) {
                if (detonatorLoc.distance(block.getLocation()) > Util.getMaxDistance(block.getType().name(), radius) + 0.6) {
                    blocksIgnored.add(block);
                } else if (ChunkManager.getInstance().blowBlockUp(block.getLocation(), event.getEntity().toString())) {
                    blocksIgnored.add(block);
                }
            }
        }

        // For materials that are not normally destructible.
        for (int x = -radius; x <= radius; x++) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = -radius; z <= radius; z++) {
                    Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
                    if (blocksIgnored.contains(targetLoc.getBlock()) || targetLoc.getBlock().getType() == Material.AIR) {
                        continue;
                    }
                    if (!MaterialManager.getInstance().contains(targetLoc.getBlock().getType().name())) {
                        continue;
                    }
                    // FIXME: Damage to blocks bleed between materials with mismatched blast radius
                    if (detonatorLoc.distance(targetLoc) <= Math.min(radius, Util.getMaxDistance(targetLoc.getBlock().getType().name(), radius))) {
                        if (ChunkManager.getInstance().blowBlockUp(targetLoc, event.getEntity().toString())) {
                            blocksIgnored.add(targetLoc.getBlock());
                        }
                    } else if (event.blockList().contains(targetLoc.getBlock())) {
                        blocksIgnored.add(targetLoc.getBlock());
                    }
                }
            }
        }

        // Remove managed blocks
        for (Block block : blocksIgnored) {
            event.blockList().remove(block);
        }
        // Debug time for explosion
        if (ConfigManager.getInstance().getDebug()) {
            ObsidianDestroyer.debug("Taken "  + (System.currentTimeMillis() - time) + " ms.  For explosion at [ " + detonatorLoc.toString() + " ]");
        }
    }

    /**
     * Handles a block on an EntityExplodeEvent
     *
     * @param at           the location of the block
     * @param eventTypeRep the entity that triggered the event
     * @return true if the blow is handled by the plugin
     */
    private boolean blowBlockUp(final Location at, String eventTypeRep) {
        if (at == null) {
            return false;
        }
        Block block = at.getBlock();
        if (block == null) {
            return false;
        }
        if (block.getType() == Material.AIR) {
            return false;
        }

        if (block.getType() == Material.BEDROCK && ConfigManager.getInstance().getProtectBedrockBorders()) {
            if (block.getY() <= 3 && block.getWorld().getEnvironment() != Environment.THE_END) {
                return true;
            } else if (block.getY() >= 125 && block.getWorld().getEnvironment() == Environment.NETHER) {
                return true;
            }
        }

        if (eventTypeRep.equals("CraftTNTPrimed") && !MaterialManager.getInstance().getTntEnabled(block.getType().name())) {
            return false;
        }
        if (eventTypeRep.equals("CraftSnowball") && !MaterialManager.getInstance().getCannonsEnabled(block.getType().name())) {
            return false;
        }
        if (eventTypeRep.equals("CraftCreeper") && !MaterialManager.getInstance().getCreepersEnabled(block.getType().name())) {
            return false;
        }
        if (eventTypeRep.equals("CraftWither") && !MaterialManager.getInstance().getWithersEnabled(block.getType().name())) {
            return false;
        }
        if (eventTypeRep.equals("CraftMinecartTNT") && !MaterialManager.getInstance().getTntMinecartsEnabled(block.getType().name())) {
            return false;
        }
        if ((eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast")) && !MaterialManager.getInstance().getGhastsEnabled(block.getType().name())) {
            return false;
        }

        if (MaterialManager.getInstance().getDurabilityEnabled(block.getType().name()) && MaterialManager.getInstance().getDurability(block.getType().name()) > 1) {
            TimerState state = checkDurabilityActive(block.getLocation());
            if (ConfigManager.getInstance().getEffectsEnabled()) {
                final double random = Math.random();
                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                    block.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
            if (state == TimerState.RUN || state == TimerState.INACTIVE) {
                int currentDurability = getMaterialDurability(block);
                currentDurability++;
                if (checkIfMax(currentDurability, block.getType().name())) {
                    // counter has reached max durability, remove and drop an item
                    dropBlockAndResetTime(at);
                } else {
                    // counter has not reached max durability damage yet
                    if (!MaterialManager.getInstance().getDurabilityResetTimerEnabled(block.getType().name())) {
                        addBlock(block, currentDurability);
                    } else {
                        startNewTimer(block, currentDurability, state);
                    }
                }
            } else {
                if (!MaterialManager.getInstance().getDurabilityResetTimerEnabled(block.getType().name())) {
                    addBlock(block, 1);
                } else {
                    startNewTimer(block, 1, state);
                }
                if (checkIfMax(1, block.getType().name())) {
                    dropBlockAndResetTime(at);
                }
            }
        } else {
            ObsidianDestroyer.getInstance().getServer().getScheduler().runTask(ObsidianDestroyer.getInstance(), new Runnable() {
                @Override
                public void run() {
                    destroyBlockAndDropItem(at);
                }
            });
        }
        return true;
    }

    /**
     * Destroys a block and drops and item at a location
     *
     * @param at the location to destroy and drop
     */
    private final void destroyBlockAndDropItem(final Location at) {
        if (at == null) {
            return;
        }

        final Block b = at.getBlock();

        if (!MaterialManager.getInstance().contains(b.getType().name())) {
            return;
        }

        final ItemStack is = new ItemStack(b.getType(), 1);
        if (is.getType() == Material.AIR) {
            return;
        }

        final double random = Math.random();
        final double chance = MaterialManager.getInstance().getChanceToDropBlock(b.getType().name());

        // changes original block to Air block
        b.setType(Material.AIR);

        if (chance >= 1.0 || (chance >= random && chance > 0.0)) {
            // drop item
            at.getWorld().dropItemNaturally(at, is);
        }
    }

    private boolean checkIfMax(int value, String id) {
        return value == MaterialManager.getInstance().getDurability(id);
    }

    /**
     * Drops at block at a location
     *
     * @param at the location the drop the block at
     */
    private void dropBlockAndResetTime(final Location at) {
        removeLocation(at);
        ObsidianDestroyer.getInstance().getServer().getScheduler().runTask(ObsidianDestroyer.getInstance(), new Runnable() {
            @Override
            public void run() {
                destroyBlockAndDropItem(at);
            }
        });
    }

    /**
     * Reset all durabilites
     *
     * @return time taken in milliseconds
     */
    public long resetAllDurabilities() {
        long time = System.currentTimeMillis();
        for (File odr : durabilityDir.listFiles()) {
            if (odr.getName().endsWith(".odr")) {
                if (!odr.delete()) {
                    ObsidianDestroyer.LOG.log(Level.WARNING, "Failed to remove file " + odr.getName());
                }
            }
        }
        for (ChunkWrapper chunk : chunks.values()) {
            chunk.removeKeys();
        }
        return time;
    }

    /**
     * Starts a new timer for a block
     *
     * @param block  the block to start a durability timer for
     * @param damage the damage done to the block
     */
    private void startNewTimer(Block block, int damage, TimerState state) {
        if (state == TimerState.RUN) {
            removeBlock(block);
        }

        addBlock(block, damage, MaterialManager.getInstance().getDurabilityResetTime(block.getType().name()));
    }

    /**
     * Check if there is an active durability reset
     *
     * @param location the location to check
     * @return the state of the durability timer object
     */
    public TimerState checkDurabilityActive(Location location) {
        if (!contains(location)) {
            return TimerState.DEAD;
        }
        if (!MaterialManager.getInstance().getDurabilityResetTimerEnabled(location.getBlock().getType().name())) {
            return TimerState.INACTIVE;
        }
        long currentTime = System.currentTimeMillis();
        long time = getWrapper(location.getChunk()).getDurabilityTime(location);
        if (currentTime > time) {
            if (ConfigManager.getInstance().getMaterialsRegenerateOverTime()) {
                int currentDurability = getWrapper(location.getChunk()).getDurability(location);
                long regenTime = MaterialManager.getInstance().getDurabilityResetTime(location.getBlock().getType().name());
                long result = currentTime - time;
                int amount = Math.max(1, Math.round((float) result / regenTime));
                currentDurability -= amount;
                if (currentDurability <= 0) {
                    getWrapper(location.getChunk()).removeKey(location);
                    return TimerState.END;
                } else {
                    startNewTimer(location.getBlock(), currentDurability, TimerState.RUN);
                    return TimerState.RUN;
                }
            } else {
                removeLocation(location);
                return TimerState.END;
            }
        }
        return TimerState.RUN;
    }

    /**
     * Gets the Material durability from a location
     *
     * @param block the block to the checks durability
     * @return the durabiltiy value
     */
    public Integer getMaterialDurability(Block block) {
        return getMaterialDurability(block.getLocation());
    }

    /**
     * Gets the Material durability from a location
     *
     * @param location the location to checks durability
     * @return the durability value
     */
    public Integer getMaterialDurability(Location location) {
        if (checkDurabilityActive(location) != TimerState.RUN && !contains(location)) {
            return 0;
        } else {
            return getWrapper(location.getChunk()).getDurability(location);
        }
    }

    /**
     * Loads the world that will be ignored
     */
    public void loadDisabledWorlds() {
        disabledWorlds = ConfigManager.getInstance().getDisabledWorlds();
    }

    /**
     * Loads the chunk manager
     */
    public void load() {
        loadDisabledWorlds();
        chunks.clear();
        for (World world : ObsidianDestroyer.getInstance().getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                loadChunk(chunk);
            }
        }
    }

    /**
     * Saves the chunk manager
     */
    public void save() {
        doneSave = false;
        Double max = ((Integer) chunks.size()).doubleValue();
        Double done = 0.0;
        for (String key : chunks.keySet()) {
            ChunkWrapper w = chunks.get(key);
            w.save(false, true);
            done++;
            this.percent = ((Double) (done / max)).intValue();
        }
        chunks.clear();
        doneSave = true;
    }

    /**
     * Loads a chunk into the chunk manager
     *
     * @param chunk the chunk to load
     */
    public void loadChunk(Chunk chunk) {
        String str = chunkToString(chunk);
        ChunkWrapper wrapper = new ChunkWrapper(chunk, durabilityDir);
        wrapper.load();
        chunks.put(str, wrapper);
    }

    /**
     * Unloads a chunk from the chunk manager
     *
     * @param chunk the chunk to unload
     */
    public void unloadChunk(Chunk chunk) {
        String key = chunkToString(chunk);
        ChunkWrapper wrapper = chunks.get(key);
        if (wrapper != null) {
            wrapper.save(false, false);
            chunks.remove(key);
        }
    }

    private String chunkToString(Chunk chunk) {
        return chunk.getX() + "." + chunk.getZ() + "." + chunk.getWorld().getName();
    }

    /**
     * Adds a block to the chunk
     *
     * @param block  the block to be added
     * @param damage the damage value of the block
     */
    public void addBlock(Block block, int damage) {
        String c = chunkToString(block.getChunk());
        if (!chunks.containsKey(c)) {
            loadChunk(block.getChunk());
        }
        ChunkWrapper chunk = chunks.get(c);
        chunk.addBlock(damage, block);
    }

    /**
     * Adds a block to the chunk
     *
     * @param block  the block to be added
     * @param damage the damage value of the block
     * @param time   the time value of the block
     */
    public void addBlock(Block block, int damage, long time) {
        String c = chunkToString(block.getChunk());
        if (!chunks.containsKey(c)) {
            loadChunk(block.getChunk());
        }
        ChunkWrapper chunk = chunks.get(c);
        time += System.currentTimeMillis();
        chunk.addBlockTimer(damage, time, block);
    }

    /**
     * Remove a block from the chunk
     *
     * @param block the block to be removed
     */
    public void removeBlock(Block block) {
        removeLocation(block.getLocation());
    }

    /**
     * Remove a location from the chunk
     *
     * @param location the location to be removed
     */
    public void removeLocation(Location location) {
        String c = chunkToString(location.getChunk());
        ChunkWrapper chunk = chunks.get(c);
        chunk.removeKey(location);
    }

    /**
     * Does the chunk contain this block
     *
     * @param block the block to check the chunk for
     * @return true if block found within chunk
     */
    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    /**
     * Does the chunk contain this location
     *
     * @param location the location to check the chunk for
     * @return true if location found within chunk
     */
    public boolean contains(Location location) {
        String c = chunkToString(location.getChunk());
        ChunkWrapper chunk = chunks.get(c);
        return chunk.contains(location);
    }

    /**
     * Gets the chunk wrapper from a chunk
     *
     * @param chunk the chunk to get a wrapper from
     * @return the ChunkWrapper that belongs to the chunk
     */
    private ChunkWrapper getWrapper(Chunk chunk) {
        String c = chunkToString(chunk);
        ChunkWrapper wrapper = chunks.get(c);
        return wrapper;
    }

    /**
     * Gets the percentage of the save done
     *
     * @return the percentage done
     */
    public int percentSaveDone() {
        if (isSaveDone()) {
            return 100;
        }
        return percent;
    }

    /**
     * Determines if the save has been completed
     *
     * @return true if completed, false otherwise
     */
    public boolean isSaveDone() {
        return this.doneSave;
    }

    /**
     * Gets a list of worlds the plugin will ignore
     *
     * @return list of world names to ignore
     */
    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    /**
     * Gets the instance
     *
     * @return instance
     */
    public static ChunkManager getInstance() {
        return instance;
    }
}
