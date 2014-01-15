package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.DamageResult;
import io.snw.obsidiandestroyer.datatypes.EntityData;
import io.snw.obsidiandestroyer.datatypes.LiquidExplosion;
import io.snw.obsidiandestroyer.enumerations.TimerState;
import io.snw.obsidiandestroyer.util.Util;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
        if (event == null) {
            return;
        }

        // Debug time taken
        final long time = System.currentTimeMillis();
        final int radius = ConfigManager.getInstance().getRadius();

        // cancel if radius is < 0 or > 10
        if (radius < 0) {
            ObsidianDestroyer.LOG.log(Level.WARNING, "Explosion radius is less than zero. Current value: {0}", radius);
            return;
        } else if (radius > 10) {
            ObsidianDestroyer.LOG.log(Level.WARNING, "Explosion radius is greater than 10. Current value: {0}", radius);
            return;
        }

        final Entity detonator = event.getEntity();

        // Check for handled explosion types, with option to ignore
        final EntityType eventTypeRep = detonator.getType();
        if (!ConfigManager.getInstance().getIgnoreUnhandledExplosionTypes()
                && !(eventTypeRep.equals(EntityType.PRIMED_TNT)
                || eventTypeRep.equals(EntityType.MINECART_TNT)
                || eventTypeRep.equals(EntityType.CREEPER)
                || eventTypeRep.equals(EntityType.WITHER)
                || eventTypeRep.equals(EntityType.GHAST)
                || eventTypeRep.equals(EntityType.FIREBALL)
                || eventTypeRep.equals(EntityType.SMALL_FIREBALL))) {
            return;
        }

        // Detonation location of the explosion
        final Location detonatorLoc = detonator.getLocation();

        // List of blocks that will be removed from the blocklist
        List<Block> blocksIgnored = new ArrayList<Block>();
        // List of blocks handled by this event
        LinkedList<Block> blocklist = new LinkedList<Block>();

        // Liquid overrides
        if (ConfigManager.getInstance().getBypassAllFluidProtection()) {
            LiquidExplosion.handle(event, blocklist);
        } else if ((detonatorLoc.getBlock().isLiquid()) && (ConfigManager.getInstance().getFluidsProtectIndustructables())) {
            return;
        }

        // Check explosion blocks and their distance from the detonation.
        for (Block block : event.blockList()) {
            if (MaterialManager.getInstance().contains(block.getType().name())) {
                if (detonatorLoc.distance(block.getLocation()) > Util.getMaxDistance(block.getType().name(), radius) + 0.6) {
                    blocksIgnored.add(block);
                } else {
                    DamageResult result = blowBlockUp(block.getLocation(), detonator);
                    if (result.isDestroyed()) {
                        blocklist.add(block);
                        continue;
                    }
                    if (result.isDamage()) {
                        blocksIgnored.add(block);
                    }
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
                        DamageResult result = blowBlockUp(targetLoc.getBlock().getLocation(), detonator);
                        if (result.isDestroyed()) {
                            blocklist.add(targetLoc.getBlock());
                            continue;
                        }
                        if (result.isDamage()) {
                            blocksIgnored.add(targetLoc.getBlock());
                        }
                    } else if (event.blockList().contains(targetLoc.getBlock())) {
                        blocksIgnored.add(targetLoc.getBlock());
                    }
                }
            }
        }

        // Remove managed blocks
        for (Block block : blocklist) {
            event.blockList().remove(block);
        }
        for (Block block : blocksIgnored) {
            event.blockList().remove(block);
            blocklist.remove(block);
        }

        // Set metadata for run once tracking
        detonator.setMetadata("ObbyEntity", new FixedMetadataValue(ObsidianDestroyer.getInstance(), new EntityData(event.getEntity().getType())));
        // Call a new explosion event
        EntityExplodeEvent explosionEvent = new EntityExplodeEvent(detonator, detonator.getLocation(), blocklist, 3.0f);
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(explosionEvent);

        if (explosionEvent.isCancelled()) {
            ObsidianDestroyer.debug("OD explosionEvent Canceled");
            return;
        }

        // Remove and destroy needed blocks
        for (Block block : explosionEvent.blockList()) {
            if (contains(block.getLocation())) {
                dropBlockAndResetDurability(block.getLocation());
            } else if (MaterialManager.getInstance().contains(block.getType().name()) && MaterialManager.getInstance().getDurability(block.getType().name()) <= 1){ 
                destroyBlockAndDropItem(block.getLocation());
            } else if (block.isLiquid() && event.getEntity().hasMetadata("LiquidEntity")) {
                block.setType(Material.AIR);
            }
        }

        // Debug time for explosion
        if (ConfigManager.getInstance().getDebug()) {
            ObsidianDestroyer.debug("Taken " + (System.currentTimeMillis() - time) + " ms.  For explosion at [ " + detonatorLoc.toString() + " ]");
        }
    }

    /**
     * Handles a block on an EntityExplodeEvent
     *
     * @param at the location of the block
     * @param entity the entity that triggered the event
     * @return true if the blow is handled by the plugin
     */
    private DamageResult blowBlockUp(final Location at, Entity entity) {
        if (at == null || entity == null) {
            return new DamageResult(false, false);
        }

        EntityType eventTypeRep = entity.getType();
        Block block = at.getBlock();
        if (block == null) {
            return new DamageResult(false, false);
        }
        if (block.getType() == Material.AIR) {
            return new DamageResult(false, false);
        }

        if (block.getType() == Material.BEDROCK && ConfigManager.getInstance().getProtectBedrockBorders()) {
            if (block.getY() <= 5 && block.getWorld().getEnvironment() != Environment.THE_END) {
                return new DamageResult(true, false);
            } else if (block.getY() >= 123 && block.getWorld().getEnvironment() == Environment.NETHER) {
                return new DamageResult(true, false);
            }
        }

        if (eventTypeRep.equals(EntityType.PRIMED_TNT) && !MaterialManager.getInstance().getTntEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }
        if (eventTypeRep.equals(EntityType.CREEPER) && !MaterialManager.getInstance().getCreepersEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }
        if (eventTypeRep.equals(EntityType.WITHER) || eventTypeRep.equals(EntityType.WITHER_SKULL) && !MaterialManager.getInstance().getWithersEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }
        if (eventTypeRep.equals(EntityType.MINECART_TNT) && !MaterialManager.getInstance().getTntMinecartsEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }
        if ((eventTypeRep.equals(EntityType.FIREBALL) || eventTypeRep.equals(EntityType.SMALL_FIREBALL) || eventTypeRep.equals(EntityType.GHAST)) && !MaterialManager.getInstance().getGhastsEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }

        MaterialManager materials = MaterialManager.getInstance();
        // Just in case the material is in the list and not enabled...
        if (!materials.getDurabilityEnabled(block.getType().name())) {
            return new DamageResult(false, false);
        }
        boolean destroy = false;
        // Handle block if the materials durability is greater than one, else destroy the block
        if (materials.getDurability(block.getType().name()) > 1) {
            TimerState state = checkDurabilityActive(block.getLocation());
            if (ConfigManager.getInstance().getEffectsEnabled()) {
                final double random = Math.random();
                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                    block.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
            if (state == TimerState.RUN || state == TimerState.INACTIVE) {
                int currentDurability = getMaterialDurability(block);
                currentDurability += materials.getDamageTypeAmount(entity, block.getType().name());
                if (Util.checkIfMax(currentDurability, block.getType().name())) {
                    // counter has reached max durability, remove and drop an item
                    destroy = true;
                } else {
                    // counter has not reached max durability damage yet
                    if (!materials.getDurabilityResetTimerEnabled(block.getType().name())) {
                        addBlock(block, currentDurability);
                    } else {
                        startNewTimer(block, currentDurability, state);
                    }
                }
            } else {
                if (!materials.getDurabilityResetTimerEnabled(block.getType().name())) {
                    addBlock(block, materials.getDamageTypeAmount(entity, block.getType().name()));
                } else {
                    startNewTimer(block, materials.getDamageTypeAmount(entity, block.getType().name()), state);
                }
                if (Util.checkIfMax(materials.getDamageTypeAmount(entity, block.getType().name()), block.getType().name())) {
                    destroy = true;
                }
            }
        } else {
            destroy = true;
        }
        return new DamageResult(true, destroy);
    }

    /**
     * Handles the cannons superbreaker projectile event
     *
     * @param event the ProjectilePiercingEvent to handle
     */
    public void handlePiercing(ProjectilePiercingEvent event) {
        ObsidianDestroyer.debug("ProjectilePiercingEvent: " + event.getProjectile().getItemName());

        // Display effects on impact location
        if (ConfigManager.getInstance().getEffectsEnabled()) {
            event.getImpactLocation().getWorld().playEffect(event.getImpactLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }

        LinkedList<Block> blocklist = new LinkedList<Block>();
        Iterator<Block> iter = event.getBlockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (MaterialManager.getInstance().contains(block.getType().name()) && !blocklist.contains(block)) {
                blocklist.add(block);
            }
        }

        event.getBlockList().removeAll(blocklist);

        EntityExplodeEvent explosionEvent = new EntityExplodeEvent(null, event.getImpactLocation(), blocklist, 0.0f);
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(explosionEvent);

        if (explosionEvent.isCancelled()) {
            return;
        }

        // List of blocks that will be removed from the blocklist
        List<Block> blocksIgnored = new ArrayList<Block>();
        // Handle blocks that can only be broken with superbreaker
        iter = explosionEvent.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (MaterialManager.getInstance().contains(block.getType().name()) && !block.getType().equals(Material.AIR)) {
                if (blowBlockUp(block.getLocation())) {
                    blocksIgnored.add(block);
                }
            }
        }
        // Remove blocks from event blocklist
        for (Block block : blocksIgnored) {
            explosionEvent.blockList().remove(block);
        }
    }

    /**
     * Handles the cannons projectile event
     *
     * @param event the ProjectileImpactEvent to handle
     */
    public void handleImpact(ProjectileImpactEvent event) {
        ObsidianDestroyer.debug("ProjectileImpactEvent: " + event.getProjectile().getItemName());

        if (event.isCancelled()) {
            return;
        }
        if (!event.getProjectile().getPenetrationDamage()) {
            return;
        }

        Location location = event.getImpactLocation();
        // Display effects on impact location
        if (ConfigManager.getInstance().getEffectsEnabled()) {
            event.getImpactLocation().getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
        }

        LinkedList<Block> blocklist = new LinkedList<Block>();
        final int radius = 1;
        for (int x = -radius; x <= radius; x++) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = -radius; z <= radius; z++) {
                    Location targetLoc = new Location(location.getWorld(), location.getX() + x, location.getY() + y, location.getZ() + z);
                    if (blocklist.contains(targetLoc.getBlock()) || targetLoc.getBlock().getType() == Material.AIR) {
                        continue;
                    }
                    if (!MaterialManager.getInstance().contains(targetLoc.getBlock().getType().name())) {
                        continue;
                    }
                    if (location.distance(targetLoc) <= Math.min(radius, Util.getMaxDistance(targetLoc.getBlock().getType().name(), radius))) {
                        if (blowBlockUp(targetLoc.getBlock().getLocation())) {
                            if (ConfigManager.getInstance().getEffectsEnabled()) {
                                final double random = Math.random();
                                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                                    location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
                                }
                            }
                            // Cancel the event
                            if (!event.isCancelled()) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles a block on an ProjectilePiercingEvent
     *
     * @param at the location of the block
     * @return true if the blow is handled by the plugin
     */
    private boolean blowBlockUp(final Location at) {
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
            if (block.getY() <= 5 && block.getWorld().getEnvironment() != Environment.THE_END) {
                return true;
            } else if (block.getY() >= 123 && block.getWorld().getEnvironment() == Environment.NETHER) {
                return true;
            }
        }
        MaterialManager materials = MaterialManager.getInstance();
        // Just in case the material is in the list and not enabled...
        if (!materials.getDurabilityEnabled(block.getType().name())) {
            return false;
        }
        if (!materials.getCannonsEnabled(block.getType().name())) {
            return false;
        }
        // Handle block if the materials durability is greater than one, else destroy the block
        if (materials.getDurability(block.getType().name()) > 1) {
            TimerState state = checkDurabilityActive(block.getLocation());
            if (ConfigManager.getInstance().getEffectsEnabled()) {
                final double random = Math.random();
                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                    block.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
            if (state == TimerState.RUN || state == TimerState.INACTIVE) {
                int currentDurability = getMaterialDurability(block);
                currentDurability += materials.getDamageTypeCannonsAmount(block.getType().name());
                if (Util.checkIfMax(currentDurability, block.getType().name())) {
                    // counter has reached max durability, remove and drop an item
                    dropBlockAndResetDurability(at);
                } else {
                    // counter has not reached max durability damage yet
                    if (!materials.getDurabilityResetTimerEnabled(block.getType().name())) {
                        addBlock(block, currentDurability);
                    } else {
                        startNewTimer(block, currentDurability, state);
                    }
                }
            } else {
                if (!materials.getDurabilityResetTimerEnabled(block.getType().name())) {
                    addBlock(block, materials.getDamageTypeCannonsAmount(block.getType().name()));
                } else {
                    startNewTimer(block, materials.getDamageTypeCannonsAmount(block.getType().name()), state);
                }
                if (Util.checkIfMax(materials.getDamageTypeCannonsAmount(block.getType().name()), block.getType().name())) {
                    dropBlockAndResetDurability(at);
                }
            }
        } else {
            destroyBlockAndDropItem(at);
        }
        return true;
    }

    /**
     * Drops at block at a location
     *
     * @param at the location the drop the block at
     */
    public void dropBlockAndResetDurability(final Location at) {
        if (at == null) {
            return;
        }

        removeLocation(at);
        destroyBlockAndDropItem(at);
    }

    /**
     * Destroys a block and drops and item at a location
     *
     * @param at the location to destroy and drop
     */
    private void destroyBlockAndDropItem(final Location at) {
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

    /**
     * Reset all durabilites
     *
     * @return time taken in milliseconds
     */
    public long resetAllDurabilities() {
        final long time = System.currentTimeMillis();
        for (File odr : durabilityDir.listFiles()) {
            if (odr.getName().endsWith(".odr")) {
                if (!odr.delete()) {
                    ObsidianDestroyer.LOG.log(Level.WARNING, "Failed to remove file {0}", odr.getName());
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
     * @param block the block to start a durability timer for
     * @param damage the damage done to the block
     */
    private void startNewTimer(Block block, int damage, TimerState state) {
        if (block == null || state == null) {
            return;
        }

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
        if (location == null) {
            return TimerState.DEAD;
        }

        if (!contains(location)) {
            return TimerState.DEAD;
        }
        if (!MaterialManager.getInstance().getDurabilityResetTimerEnabled(location.getBlock().getType().name())) {
            return TimerState.INACTIVE;
        }
        final long currentTime = System.currentTimeMillis();
        final ChunkWrapper chunk = getWrapper(location.getChunk());
        if (chunk == null) {
            if (ConfigManager.getInstance().getVerbose() || ConfigManager.getInstance().getDebug()) {
                ObsidianDestroyer.LOG.severe("The requested chunk appears to be null  D:");
            }
            return TimerState.DEAD;
        }
        final long time = chunk.getDurabilityTime(location);
        if (currentTime > time) {
            if (ConfigManager.getInstance().getMaterialsRegenerateOverTime()) {
                int currentDurability = chunk.getDurability(location);
                final long regenTime = MaterialManager.getInstance().getDurabilityResetTime(location.getBlock().getType().name());
                final long result = currentTime - time;
                final int amount = Math.max(1, Math.round((float) result / regenTime));
                currentDurability -= amount;
                if (currentDurability <= 0) {
                    chunk.removeKey(location);
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
        if (block == null) {
            return 0;
        }

        return getMaterialDurability(block.getLocation());
    }

    /**
     * Gets the Material durability from a location
     *
     * @param location the location to checks durability
     * @return the durability value
     */
    public Integer getMaterialDurability(Location location) {
        if (location == null) {
            return 0;
        }

        if (checkDurabilityActive(location) != TimerState.RUN && !contains(location)) {
            return 0;
        } else {
            return getWrapper(location.getChunk()) != null ? getWrapper(location.getChunk()).getDurability(location) : 0;
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
        if (chunk == null) {
            return;
        }

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
        if (chunk == null) {
            return;
        }

        String key = chunkToString(chunk);
        ChunkWrapper wrapper = chunks.get(key);
        if (wrapper != null) {
            wrapper.save(false, false);
            chunks.remove(key);
        }
    }

    private String chunkToString(Chunk chunk) {
        if (chunk == null) {
            return "";
        }

        return chunk.getX() + "." + chunk.getZ() + "." + chunk.getWorld().getName();
    }

    /**
     * Adds a block to the chunk
     *
     * @param block the block to be added
     * @param damage the damage value of the block
     */
    public void addBlock(Block block, int damage) {
        if (block == null) {
            return;
        }

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
     * @param block the block to be added
     * @param damage the damage value of the block
     * @param time the time value of the block
     */
    public void addBlock(Block block, int damage, long time) {
        if (block == null) {
            return;
        }

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
        if (block == null) {
            return;
        }

        removeLocation(block.getLocation());
    }

    /**
     * Remove a location from the chunk
     *
     * @param location the location to be removed
     */
    public void removeLocation(Location location) {
        if (location == null) {
            return;
        }

        String c = chunkToString(location.getChunk());
        if (chunks.containsKey(c)) {
            ChunkWrapper chunk = chunks.get(c);
            chunk.removeKey(location);
        }
    }

    /**
     * Does the chunk contain this block
     *
     * @param block the block to check the chunk for
     * @return true if block found within chunk
     */
    public boolean contains(Block block) {
        if (block == null) {
            return false;
        }

        return contains(block.getLocation());
    }

    /**
     * Does the chunk contain this location
     *
     * @param location the location to check the chunk for
     * @return true if location found within chunk
     */
    public boolean contains(Location location) {
        if (location == null) {
            return false;
        }
        String c = chunkToString(location.getChunk());
        ChunkWrapper chunk = chunks.get(c);
        if (chunk == null) {
            return false;
        }
        return chunk.contains(location);
    }

    /**
     * Gets the chunk wrapper from a chunk
     *
     * @param chunk the chunk to get a wrapper from
     * @return the ChunkWrapper that belongs to the chunk.
     */
    private ChunkWrapper getWrapper(Chunk chunk) {
        if (chunk == null) {
            return null;
        }
        String c = chunkToString(chunk);
        if (!chunks.containsKey(c)) {
            loadChunk(chunk);
        }
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
