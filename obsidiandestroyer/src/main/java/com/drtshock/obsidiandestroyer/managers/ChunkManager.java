package com.drtshock.obsidiandestroyer.managers;

import at.pavlov.cannons.event.ProjectileImpactEvent;
import at.pavlov.cannons.event.ProjectilePiercingEvent;
import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.datatypes.EntityData;
import com.drtshock.obsidiandestroyer.enumerations.DamageResult;
import com.drtshock.obsidiandestroyer.enumerations.TimerState;
import com.drtshock.obsidiandestroyer.events.DurabilityDamageEvent;
import com.drtshock.obsidiandestroyer.events.xEntityExplodeEvent;
import com.drtshock.obsidiandestroyer.managers.factions.FactionsIntegration;
import com.drtshock.obsidiandestroyer.util.Util;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

public class ChunkManager {

    private static ChunkManager instance;
    private final File durabilityDir;
    private ConcurrentMap<String, ChunkWrapper> chunks = new ConcurrentHashMap<String, ChunkWrapper>();
    private boolean doneSave = false;
    private List<String> disabledWorlds;

    /**
     * Creates wrappers around chunks and sets up the material block tracking
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
     * Gets the instance
     *
     * @return instance
     */
    public static ChunkManager getInstance() {
        return instance;
    }

    /**
     * Handles the entity explosion event
     *
     * @param event the entity explosion event to handle
     */
    public void handleExplosion(EntityExplodeEvent event) {
        handleExplosion(event, event.getEntity().getLocation());
    }

    /**
     * Handles the entity explosion event
     *
     * @param event        the entity explosion event to handle
     * @param detonatorLoc the location. Necessary for BlockExplodeEvents.
     */
    public void handleExplosion(final EntityExplodeEvent event, final Location detonatorLoc) {
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

        // Detonator
        final Entity detonator = event.getEntity();
        // Detonation location of the explosion

        if (detonator != null) {
            // Check for handled explosion types, with option to ignore
            final EntityType eventTypeRep = detonator.getType();
            if (!ConfigManager.getInstance().getIgnoreUnhandledExplosionTypes() && !(eventTypeRep.equals(EntityType.PRIMED_TNT) || eventTypeRep.equals(EntityType.MINECART_TNT) || eventTypeRep.equals(EntityType.CREEPER) || eventTypeRep.equals(EntityType.WITHER) || eventTypeRep.equals(EntityType.WITHER_SKULL) || eventTypeRep.equals(EntityType.GHAST) || eventTypeRep.equals(EntityType.FIREBALL) || eventTypeRep.equals(EntityType.SMALL_FIREBALL))) {
                return;
            }
        }

        // List of blocks that will be removed from the blocklist
        List<Block> blocksIgnored = new ArrayList<Block>();
        // List of blocks handled by this event
        LinkedList<Block> blocksDestroyed = new LinkedList<Block>();

        LinkedList<Block> xblocksDestroyed = new LinkedList<Block>();
        // Bleeding Damage blocked blocks
        List<Location> blockedBlockLocations = new ArrayList<Location>();

        // Liquid overrides
        if (ConfigManager.getInstance().getBypassAllFluidProtection()) {
            // Protects TNT cannons from exploding themselves
            if (ConfigManager.getInstance().getProtectTNTCannons()) {
                int redstoneCount = 0;
                final int cannon_radius = 2;
                for (int x = -cannon_radius; x <= cannon_radius; x++) {
                    for (int y = -cannon_radius; y <= cannon_radius; y++) {
                        for (int z = -cannon_radius; z <= cannon_radius; z++) {
                            Location targetLoc = new Location(detonatorLoc.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
                            if (targetLoc.getBlock().getType().equals(Material.REDSTONE_WIRE) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_ON) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF)) {
                                redstoneCount++;
                            }
                        }
                    }
                }
                if (redstoneCount >= 6) {
                    // Explode normal, with explosion dampened via the fluid
                    return;
                }
            }

            // metadata for future tracking of liquid handling
            if (detonator != null) {
                detonator.setMetadata("ObbyLiquidEntity", new FixedMetadataValue(ObsidianDestroyer.getInstance(), new EntityData(event.getEntityType())));
            }

        } else if ((detonatorLoc.getBlock().isLiquid()) && (ConfigManager.getInstance().getFluidsProtectIndustructables())) {
            // handle the liquid explosion normally
            return;
        }

        // Check explosion blocks and their distance from the detonation.
        for (Block block : event.blockList()) {
            // location corrections...
            Location blockLocation = block.getLocation();
            blockLocation.setY(blockLocation.getBlockY() + 0.5);
            if (blockLocation.getBlockX() > 0) {
                blockLocation.setX(blockLocation.getBlockX() + 0.5);
            } else {
                blockLocation.setX(blockLocation.getBlockX() + -0.5);
            }
            if (blockLocation.getBlockZ() > 0) {
                blockLocation.setZ(blockLocation.getBlockZ() + 0.5);
            } else {
                blockLocation.setZ(blockLocation.getBlockZ() + -0.5);
            }
            // distance from detonator to the target block
            final double dist = detonatorLoc.distance(blockLocation);

            // Damage bleeding fix
            if (ConfigManager.getInstance().getDisableDamageBleeding() && !detonator.getType().equals(EntityType.WITHER)) {
                // Attempt to prevent bleeding of damage to materials behind blocks not destroyed
                if (MaterialManager.getInstance().contains(block.getType().name())) {
                    // distance checks: if max ignore; if not too close check sight; else apply damage
                    if (dist > Util.getMaxDistance(block.getType().name(), radius) + 0.4) {
                        blocksIgnored.add(block);
                        continue;
                    } else if (dist > 1.8) {
                        // if greater than target distance apply hit check
                        // Radial hitscan check for blocking blocks, returns the blocking path
                        final List<Location> path = Util.getTargetsPathBlocked(blockLocation, detonatorLoc, false);

                        //if (Util.isTargetsPathBlocked(blockLocation, detonatorLoc, false)) {
                        if (path.size() > 0) {
                            // Add to blocked locations and ignore damage
                            blockedBlockLocations.add(blockLocation);
                            blocksIgnored.add(block);
                            ObsidianDestroyer.vdebug("[E] Blocked Bleeding Damage!! " + blockLocation.toString() + " -dist " + dist);
                            continue;
                        }
                    }

                    // Apply damage to block material
                    DamageResult result = damageBlock(blockLocation, detonator);
                    if (result == DamageResult.DESTROY) {
                        blocksDestroyed.add(block);
                    } else if (result == DamageResult.DAMAGE || result == DamageResult.CANCELLED) {
                        blocksIgnored.add(block);
                    }
                    ObsidianDestroyer.vdebug("Event Block Damage!! " + blockLocation.toString() + " -dist " + dist);
                } else {
                    // handle non tracked materials blocked and ignore non solids
                    final List<Location> path = Util.getTargetsPathBlocked(blockLocation, detonatorLoc, true);
                    if (path.size() > 0) {
                        // Add to blocked locations and ignore damage
                        blockedBlockLocations.add(blockLocation);
                        blocksIgnored.add(block);
                        ObsidianDestroyer.vdebug("[E] Blocked Bleeding Damage!!! " + blockLocation.toString() + " -dist " + dist);
                    } else {
                        xblocksDestroyed.add(block);
                    }
                }
            } else if (MaterialManager.getInstance().contains(block.getType().name())) {
                // Original handling
                if (dist > Util.getMaxDistance(block.getType().name(), radius) + 0.4) {
                    blocksIgnored.add(block);
                } else {
                    // Apply damage to block material
                    DamageResult result = damageBlock(blockLocation, detonator);
                    if (result == DamageResult.DESTROY) {
                        // Destroy the block
                        blocksDestroyed.add(block);
                    } else if (result == DamageResult.DAMAGE || result == DamageResult.CANCELLED) {
                        // Don't destroy
                        blocksIgnored.add(block);
                    }
                }
            }
        }

        // Bedrock override bypass
        final boolean enabledBedrock = MaterialManager.getInstance().contains(Material.BEDROCK.name());

        // =================================================
        // Material Explosion radius check for all materials
        // Loop through all blocks within the applied radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = radius; y >= -radius; y--) {
                for (int z = -radius; z <= radius; z++) {

                    // Target location around the detonator
                    Location targetLoc = new Location(detonatorLoc.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);

                    // location corrections...
                    targetLoc.setY(targetLoc.getBlockY() + 0.5);
                    if (targetLoc.getBlockX() > 0) {
                        targetLoc.setX(targetLoc.getBlockX() + 0.5);
                    } else {
                        targetLoc.setX(targetLoc.getBlockX() + -0.5);
                    }
                    if (targetLoc.getBlockZ() > 0) {
                        targetLoc.setZ(targetLoc.getBlockZ() + 0.5);
                    } else {
                        targetLoc.setZ(targetLoc.getBlockZ() + -0.5);
                    }

                    // ignore if air or ignored
                    if ((blocksIgnored.contains(targetLoc.getBlock())) || targetLoc.getBlock().getType() == Material.AIR) {
                        continue;
                    }
                    // Bedrock check
                    if (targetLoc.getBlock().getType() == Material.BEDROCK && !enabledBedrock) {
                        continue;
                    }

                    // Radius of effect of the handled explosion that is recreated
                    final double radiuz = Math.min(radius, Util.getMaxDistance(targetLoc.getBlock().getType().name(), radius)) + 0.6;
                    // Distance of detonator to this blocks location
                    final double distance = detonatorLoc.distance(targetLoc);

                    if (blocksDestroyed.contains(targetLoc.getBlock())) {
                        // if already tracked this block...
                        continue;
                    }

                    // Liquid overrides
                    if (ConfigManager.getInstance().getBypassAllFluidProtection()) {
                        // Special handling for fluids is enabled
                        if (distance < radiuz - 0.1 && (Util.isNearLiquid(targetLoc) || targetLoc.getBlock().isLiquid())) {
                            // if within radius and is a near or a fluid
                            if (distance > radiuz - 0.6 && Math.random() <= 0.4) {
                                // semi random radius calculation for edges
                                blocksIgnored.add(targetLoc.getBlock());
                                continue;
                            }

                            if (MaterialManager.getInstance().contains(targetLoc.getBlock().getType().name())) {
                                // if this material is being handled for durability
                                double damper = MaterialManager.getInstance().getFluidDamperAmount(targetLoc.getBlock().getType().name());
                                if (!targetLoc.getBlock().isLiquid() && damper > 0 && damper >= Math.random()) {
                                    // Ignore the block if the explosion has been absorbed
                                    ObsidianDestroyer.vdebug("Nearby Fluid Absorbed Explosion Damage to Block! " + targetLoc.toString());
                                    blocksIgnored.add(targetLoc.getBlock());
                                    continue;
                                }

                                // Apply damage to block material
                                DamageResult result = damageBlock(targetLoc, detonator);
                                if (result == DamageResult.DESTROY) {
                                    // Add block to list to destroy
                                    blocksDestroyed.add(targetLoc.getBlock());
                                    continue;
                                } else if (result == DamageResult.DAMAGE || result == DamageResult.CANCELLED || result == DamageResult.NONE) {
                                    // Add block to ignore list to not destroy
                                    blocksIgnored.add(targetLoc.getBlock());
                                    continue;
                                }
                            } else {
                                // add block or fluid to list to destroy
                                blocksDestroyed.add(targetLoc.getBlock());
                                continue;
                            }
                        }
                    }

                    // Check if handling
                    if (!MaterialManager.getInstance().contains(targetLoc.getBlock().getType().name()) && !blockedBlockLocations.contains(targetLoc)) {
                        // ignore material if not being handled
                        continue;
                    }

                    // Radius
                    if (distance <= radiuz) {
                        // Block damage within the radius
                        if (distance > radiuz - 0.4 && Math.random() <= 0.2) {
                            // semi random edge radius calculation
                            blocksIgnored.add(targetLoc.getBlock());
                            continue;
                        }

                        // Damage bleeding fix
                        if (ConfigManager.getInstance().getDisableDamageBleeding() && distance > 1.8 && !detonator.getType().equals(EntityType.WITHER)) {
                            // Radial hitscan check for blocking blocks, returns the blocking path
                            final List<Location> path = Util.getTargetsPathBlocked(targetLoc, detonatorLoc, false);

                            if (path.size() > 0) {
                                // the blocks protected path size is 1 or more
                                if (!(Util.matchBlocksToLocations(path, blocksDestroyed) && !Util.matchBlocksToLocations(path, xblocksDestroyed)) || Util.matchLocationsToLocations(path, blockedBlockLocations) || Util.matchBlocksToLocations(path, blocksIgnored)) {
                                    // the block is protected via its path
                                    blockedBlockLocations.add(targetLoc);
                                    blocksIgnored.add(targetLoc.getBlock());
                                    ObsidianDestroyer.vdebug("[L] Blocked Bleeding Path Damage!! Blocked: " + targetLoc.toString() + " -dist " + distance + " -size " + path.size());
                                    continue;
                                } else if (path.size() > 2 || distance >= 3) {
                                    // the block is too far away and or its protected path is too long
                                    blocksIgnored.add(targetLoc.getBlock());
                                    ObsidianDestroyer.vdebug("[L] Blocked Bleeding Path Damage!! Over: " + targetLoc.toString() + " -dist " + distance + " -size " + path.size());
                                    continue;
                                }
                            }
                        }

                        // Apply damage to block material
                        DamageResult result = damageBlock(targetLoc, detonator);
                        if (result == DamageResult.DESTROY) {
                            // Add block to list to destroy
                            blocksDestroyed.add(targetLoc.getBlock());
                        } else if (result == DamageResult.DAMAGE || result == DamageResult.CANCELLED || result == DamageResult.NONE) {
                            // Add block to ignore list to not destroy
                            blocksIgnored.add(targetLoc.getBlock());
                        } else if (result == DamageResult.DISABLED) {
                            // This shouldn't really happen...
                            blocksDestroyed.add(targetLoc.getBlock());
                        }
                        ObsidianDestroyer.vdebug("Block Damage!! " + targetLoc.toString() + " -dist " + distance);
                    } else if (event.blockList().contains(targetLoc.getBlock())) {
                        // Ignore blocks outside of radius
                        blocksIgnored.add(targetLoc.getBlock());
                    }
                }
            }
        }

        // Apply effects with factions
        final boolean factionsApplied = FactionsIntegration.isUsing() && ConfigManager.getInstance().getHandleOfflineFactions();

        // Bypass list for special handling's
        final List<Block> bypassBlockList = new ArrayList<Block>();

        // Remove managed blocks
        for (Block block : blocksDestroyed) {
            event.blockList().remove(block);

            // Factions bypasses
            if (factionsApplied && !blockedBlockLocations.contains(block.getLocation())) {
                if (FactionsIntegration.get().isFactionOffline(block.getLocation())) {
                    // Add block to bypass list to override
                    bypassBlockList.add(block);
                }
            }
        }
        // Remove ignored blocks
        for (Block block : blocksIgnored) {
            event.blockList().remove(block);
            blocksDestroyed.remove(block);
        }
        // Remove blocked blocks
        for (Location location : blockedBlockLocations) {
            event.blockList().remove(location.getBlock());
        }

        if (detonator != null) {
            // Set metadata for run once tracking
            detonator.setMetadata("ObbyEntity", new FixedMetadataValue(ObsidianDestroyer.getInstance(), new EntityData(event.getEntity().getType())));
        }

        // ==========================
        // Create a new explosion event from the custom block lists
        xEntityExplodeEvent explosionEvent = new xEntityExplodeEvent(detonator, detonatorLoc, blocksDestroyed, bypassBlockList, blockedBlockLocations, event.getYield());
        // Call the new explosion event
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(explosionEvent);

        if (detonator != null) {
            // Remove metadata since it is no longer needed
            detonator.removeMetadata("ObbyEntity", ObsidianDestroyer.getInstance());
        }
        // ==========================
        // Ignore if event is cancelled and not bypassed.
        if (explosionEvent.isCancelled()) {
            if (explosionEvent.bypassBlockList().size() == 0) {
                ObsidianDestroyer.debug("Explosion Event Cancelled");
                return;
            } else {
                // Bypass through factions cancel
                ObsidianDestroyer.debug("Explosion Event Cancellation Bypassed");
            }
        }

        // Iterate through the total block list from the events bypass and destroy needed blocks
        for (Block block : explosionEvent.totalBlockList()) {
            if (contains(block.getLocation())) {
                // drops block and reset the durability of the location
                dropBlockAndResetDurability(block.getLocation());
            } else if (MaterialManager.getInstance().contains(block.getType().name()) && MaterialManager.getInstance().getDurability(block.getType().name()) <= 1) {
                // destroy block and reset the durability of the location
                destroyBlockAndDropItem(block.getLocation());
            } else if (block.isLiquid() && (detonator != null && detonator.hasMetadata("ObbyLiquidEntity"))) {
                // remove liquid and set to air
                block.setType(Material.AIR);
            } else if (block.getType() == Material.TNT) {
                // Chance to ignite nearby tnt, else ignore
                if (Math.random() < 0.8) {
                    block.setType(Material.AIR);
                    TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(Util.getRandomNumberFrom(0, 2));
                }
            } else {
                // break the block naturally
                block.breakNaturally();
            }
        }

        // Damage bleeding fix
        if (ConfigManager.getInstance().getDisableDamageBleeding()) {

            // Iterate through the blocks that had an explosion blocked to check if the path was cleared by a current explosion
            for (Location location : explosionEvent.blockedLocationList()) {
                if (Math.random() < ConfigManager.getInstance().getNextLayerDamageChance() && !Util.isTargetsPathBlocked(location, explosionEvent.getLocation(), false)) {

                    // Apply damage to block material
                    // Entity might be null but damageBlock handles that.
                    DamageResult result = damageBlock(location, explosionEvent.getEntity());
                    ObsidianDestroyer.vdebug("Blocking Damage passed!! " + location.toString() + "  DamageResult: " + result.name());
                    if (result == DamageResult.DESTROY) {
                        // Destroy the block
                        destroyBlockAndDropItem(location);
                    } else if (result == DamageResult.DISABLED) {
                        // Break block naturally
                        location.getBlock().breakNaturally();
                    }
                }
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
     * @param at     the location of the block
     * @param entity the entity that triggered the event
     *
     * @return DamageResult result of damageBlock attempt
     */
    public DamageResult damageBlock(final Location at, Entity entity) {
        if (at == null || entity == null) {
            return DamageResult.NONE;
        }

        // Null and Air checks
        EntityType eventTypeRep = entity.getType();
        Block block = at.getBlock();
        if (block == null || block.getType() == Material.AIR) {
            return DamageResult.NONE;
        }

        String blockTypeName = block.getType().name();

        // ==========================
        // Create a new Durability Damage Event
        DurabilityDamageEvent durabilityDamageEvent = new DurabilityDamageEvent(blockTypeName, eventTypeRep);
        // Call event on blocks material durability damage
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(durabilityDamageEvent);

        // ==========================
        if (durabilityDamageEvent.isDisposed()) {
            // Return a new damage result if set
            return durabilityDamageEvent.getDamageResult();
        }
        if (durabilityDamageEvent.isCancelled()) {
            // Return no damage if even is cancelled.
            return DamageResult.CANCELLED;
        }

        // Check bedrock and env
        if (block.getType() == Material.BEDROCK && ConfigManager.getInstance().getProtectBedrockBorders()) {
            if (block.getY() <= ConfigManager.getInstance().getBorderToProtectNormal() && block.getWorld().getEnvironment() != Environment.THE_END) {
                return DamageResult.NONE;
            } else if (block.getY() >= ConfigManager.getInstance().getBorderToProtectNether() && block.getWorld().getEnvironment() == Environment.NETHER) {
                return DamageResult.NONE;
            }
        }

        // The handled materials listing
        MaterialManager materials = MaterialManager.getInstance();

        // Just in case the material is in the list and not enabled...
        if (!materials.getDurabilityEnabled(blockTypeName)) {
            return DamageResult.DISABLED;
        }

        if (!materials.isDestructible(blockTypeName)) {
            return DamageResult.NONE;
        }

        ObsidianDestroyer.vdebug("eventTypeRep= " + eventTypeRep);

        // Check explosion types
        if (eventTypeRep.equals(EntityType.PRIMED_TNT) && !MaterialManager.getInstance().getTntEnabled(blockTypeName)) {
            return DamageResult.NONE;
        }
        if (eventTypeRep.equals(EntityType.CREEPER) && !MaterialManager.getInstance().getCreepersEnabled(blockTypeName)) {
            return DamageResult.NONE;
        }
        if (eventTypeRep.equals(EntityType.WITHER) || eventTypeRep.equals(EntityType.WITHER_SKULL) && !MaterialManager.getInstance().getWithersEnabled(blockTypeName)) {
            return DamageResult.NONE;
        }
        if (eventTypeRep.equals(EntityType.MINECART_TNT) && !MaterialManager.getInstance().getTntMinecartsEnabled(blockTypeName)) {
            return DamageResult.NONE;
        }
        if ((eventTypeRep.equals(EntityType.FIREBALL) || eventTypeRep.equals(EntityType.SMALL_FIREBALL) || eventTypeRep.equals(EntityType.GHAST)) && !MaterialManager.getInstance().getGhastsEnabled(blockTypeName)) {
            return DamageResult.NONE;
        }

        // Durability multiplier hook for Factions
        double durabilityMultiplier = 1D;
        if (FactionsIntegration.isUsing()) {
            if (!FactionsIntegration.get().isExplosionsEnabled(at)) {
                return DamageResult.NONE;
            }
            if (ConfigManager.getInstance().getProtectOfflineFactions()) {
                if (FactionsIntegration.get().isFactionOffline(block.getLocation())) {
                    return DamageResult.NONE;
                }
            }
            durabilityMultiplier = Util.getMultiplier(at);
        }

        // Handle block if the materials durability is greater than one, else destroy the block
        if ((materials.getDurability(blockTypeName) * durabilityMultiplier) >= 2) {
            // durability is greater than one, get last state of the material location
            TimerState state = checkDurabilityActive(block.getLocation());
            if (ConfigManager.getInstance().getEffectsEnabled()) {
                // display particles effects on damage
                final double random = Math.random();
                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                    block.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
            // If timer is running or not active...
            if (state == TimerState.RUN || state == TimerState.INACTIVE) {
                // Check if current is over the max, else increment damage to durability
                int currentDurability = getMaterialDurability(block);
                if (Util.checkIfOverMax(currentDurability, blockTypeName, durabilityMultiplier)) {
                    currentDurability = (int) Math.round(materials.getDurability(blockTypeName) * 0.50);
                } else {
                    currentDurability += materials.getDamageTypeAmount(entity, blockTypeName);
                }
                // check if at max, else setup and track the material location
                if (Util.checkIfMax(currentDurability, blockTypeName, durabilityMultiplier)) {
                    // counter has reached max durability, remove and drop an item
                    return DamageResult.DESTROY;
                } else {
                    // counter has not reached max durability damage yet
                    if (!materials.getDurabilityResetTimerEnabled(blockTypeName)) {
                        // adds a block to be track
                        addBlock(block, currentDurability);
                    } else {
                        // adds a block to be tracked and starts a new durabilityTime with last state
                        startNewTimer(block, currentDurability, state);
                    }
                }
            } else {
                // No timers or tracked location, add a new material location
                if (!materials.getDurabilityResetTimerEnabled(blockTypeName)) {
                    addBlock(block, materials.getDamageTypeAmount(entity, blockTypeName));
                } else {
                    startNewTimer(block, materials.getDamageTypeAmount(entity, blockTypeName), state);
                }
                // Check if damage is at max for durability
                if (Util.checkIfMax(materials.getDamageTypeAmount(entity, blockTypeName), blockTypeName, durabilityMultiplier)) {
                    return DamageResult.DESTROY;
                }
            }
        } else {
            // durability is < 1, destroy the material location
            return DamageResult.DESTROY;
        }

        // Return damage
        return DamageResult.DAMAGE;
    }

    /**
     * Handles the cannons superbreaker projectile event
     *
     * @param event the ProjectilePiercingEvent to handle
     */
    public void handleCannonPiercing(ProjectilePiercingEvent event) {
        ObsidianDestroyer.debug("ProjectilePiercingEvent: " + event.getProjectile().getItemName());

        // Display effects on impact location
        if (ConfigManager.getInstance().getEffectsEnabled()) {
            event.getImpactLocation().getWorld().playEffect(event.getImpactLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }

        // List of blocks being handled
        LinkedList<Block> blocklist = new LinkedList<Block>();
        // Bypass list for special handlings
        final List<Block> bypassBlockList = new ArrayList<Block>();
        final boolean useFactions = FactionsIntegration.isUsing();
        final boolean applyFactions = useFactions && ConfigManager.getInstance().getHandleOfflineFactions();
        // Iterator through the events blocks
        Iterator<Block> iter = event.getBlockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            // Check if handled and not already checked
            if (MaterialManager.getInstance().contains(block.getType().name()) && !blocklist.contains(block)) {
                blocklist.add(block);
            }
            // Factions bypasses
            if (useFactions && applyFactions) {
                if (FactionsIntegration.get().isFactionOffline(block.getLocation())) {
                    bypassBlockList.add(block);
                }
            }
        }

        event.getBlockList().removeAll(blocklist);

        EntityExplodeEvent explosionEvent = new EntityExplodeEvent(null, event.getImpactLocation(), blocklist, 0.0f);
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(explosionEvent);

        // Repopulate the events blocklist with blocks through the bypass
        if (applyFactions && bypassBlockList.size() > 0) {
            explosionEvent.blockList().addAll(bypassBlockList);
        }

        // Do nothing if the event is cancelled (and not bypassed...)
        if (explosionEvent.isCancelled()) {
            if (!applyFactions || bypassBlockList.size() == 0) {
                ObsidianDestroyer.debug("Cannons Explosion Event Cancelled");
                return;
            } else {
                ObsidianDestroyer.debug("Cannons Explosion Event Cancellation Bypassed");
            }
        }

        // List of blocks that will be removed from the blocklist
        List<Block> blocksIgnored = new ArrayList<Block>();
        // Handle blocks that can only be broken with superbreaker
        iter = explosionEvent.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (MaterialManager.getInstance().contains(block.getType().name()) && !block.getType().equals(Material.AIR)) {
                DamageResult result = damageBlock(block.getLocation(), false);
                if (result != DamageResult.NONE && result != DamageResult.CANCELLED) {
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
    public void handleCannonImpact(ProjectileImpactEvent event) {
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
        final int radius = Math.round(event.getProjectile().getExplosionPower() + 0.1f);
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
                        DamageResult result = damageBlock(targetLoc.getBlock().getLocation(), true);
                        if (result != DamageResult.NONE && result != DamageResult.CANCELLED) {
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
     * @param at the location of the block
     * @return DamageResult result of damageBlock attempt
     */
    public DamageResult damageBlock(final Location at) {
        return damageBlock(at, true);
    }

    /**
     * Handles a block on an ProjectilePiercingEvent
     *
     * @param at     the location of the block
     * @param impact impact or piercing damage type
     * @return DamageResult result of damageBlock attempt
     */
    public DamageResult damageBlock(final Location at, boolean impact) {
        // Null and Air checks
        if (at == null) {
            return DamageResult.NONE;
        }
        Block block = at.getBlock();
        if (block == null) {
            return DamageResult.NONE;
        }
        if (block.getType() == Material.AIR) {
            return DamageResult.NONE;
        }

        String blockTypeName = block.getType().name();

        // ==========================
        // Create a new Durability Damage Event
        DurabilityDamageEvent durabilityDamageEvent = new DurabilityDamageEvent(blockTypeName);
        // Call event on blocks material durability damage
        ObsidianDestroyer.getInstance().getServer().getPluginManager().callEvent(durabilityDamageEvent);

        // ==========================
        if (durabilityDamageEvent.isDisposed()) {
            // Return a new damage result if set
            return durabilityDamageEvent.getDamageResult();
        }
        if (durabilityDamageEvent.isCancelled()) {
            // Return no damage if even is cancelled.
            return DamageResult.CANCELLED;
        }

        // Check bedrock and env
        if (block.getType() == Material.BEDROCK && ConfigManager.getInstance().getProtectBedrockBorders()) {
            if (block.getY() <= ConfigManager.getInstance().getBorderToProtectNormal() && block.getWorld().getEnvironment() != Environment.THE_END) {
                return DamageResult.NONE;
            } else if (block.getY() >= ConfigManager.getInstance().getBorderToProtectNether() && block.getWorld().getEnvironment() == Environment.NETHER) {
                return DamageResult.NONE;
            }
        }

        MaterialManager materials = MaterialManager.getInstance();
        // Just in case the material is in the list and not enabled...
        if (!materials.getDurabilityEnabled(blockTypeName)) {
            return DamageResult.DISABLED;
        } else if (!materials.getCannonsEnabled(blockTypeName)) {
            return DamageResult.DISABLED;
        }

        if (!materials.isDestructible(blockTypeName)) {
            return DamageResult.NONE;
        }

        // Durability multiplier hook for Factions
        double durabilityMultiplier = 1D;
        if (FactionsIntegration.isUsing()) {
            if (!FactionsIntegration.get().isExplosionsEnabled(at)) {
                return DamageResult.NONE;
            }
            if (ConfigManager.getInstance().getProtectOfflineFactions()) {
                if (FactionsIntegration.get().isFactionOffline(block.getLocation())) {
                    return DamageResult.NONE;
                }
            }
            durabilityMultiplier = Util.getMultiplier(at);
        }

        // Handle block if the materials durability is greater than one, else destroy the block
        if ((materials.getDurability(blockTypeName) * durabilityMultiplier) >= 2) {
            // durability is greater than one, get last state of the material location
            TimerState state = checkDurabilityActive(block.getLocation());
            if (ConfigManager.getInstance().getEffectsEnabled()) {
                // display particles effects on damage
                final double random = Math.random();
                if (random <= ConfigManager.getInstance().getEffectsChance()) {
                    block.getWorld().playEffect(at, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }

            int damageAmt = impact ? materials.getDamageTypeCannonsImpactAmount(blockTypeName) : materials.getDamageTypeCannonsPierceAmount(blockTypeName);

            ObsidianDestroyer.vdebug("Current TimerState= " + state);

            // If timer is running or not active...
            if (state == TimerState.RUN || state == TimerState.INACTIVE) {
                // Check if current is over the max, else increment damage to durability
                int currentDurability = getMaterialDurability(block);
                if (Util.checkIfOverMax(currentDurability, blockTypeName, durabilityMultiplier)) {
                    currentDurability = (int) Math.round(materials.getDurability(blockTypeName) * durabilityMultiplier);
                } else {
                    currentDurability += damageAmt;
                }
                // check if at max, else setup and track the material location
                if (Util.checkIfMax(currentDurability, blockTypeName, durabilityMultiplier)) {
                    // counter has reached max durability, remove and drop an item
                    dropBlockAndResetDurability(at);
                    return DamageResult.DESTROY;
                } else {
                    // counter has not reached max durability damage yet
                    if (!materials.getDurabilityResetTimerEnabled(blockTypeName)) {
                        // adds a block to be tracked
                        addBlock(block, currentDurability);
                    } else {
                        // adds a block to be tracked and starts a new durabilityTime with last state
                        startNewTimer(block, currentDurability, state);
                    }
                }
            } else {
                // No timers or tracked location, add a new material location
                if (!materials.getDurabilityResetTimerEnabled(blockTypeName)) {
                    addBlock(block, damageAmt);
                } else {
                    startNewTimer(block, damageAmt, state);
                }
                // Check if damage is at max for durability
                if (Util.checkIfMax(damageAmt, blockTypeName, durabilityMultiplier)) {
                    dropBlockAndResetDurability(at);
                    return DamageResult.DESTROY;
                }
            }
        } else {
            // durability is < 1, destroy the material location
            destroyBlockAndDropItem(at);
            return DamageResult.DESTROY;
        }

        // Return damage
        return DamageResult.DAMAGE;
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
    public void destroyBlockAndDropItem(final Location at) {
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

        // block drops
        final Collection<ItemStack> bd = at.getBlock().getDrops();

        final double random = Math.random();
        final double chance = MaterialManager.getInstance().getChanceToDropBlock(b.getType().name());

        // changes original block to Air block
        b.setType(Material.AIR);

        if (chance >= 1.0 || (chance >= random && chance > 0.0)) {
            if (bd.size() > 0) {
                // drop the blocks item drops
                for (ItemStack itemStack : bd) {
                    at.getWorld().dropItemNaturally(at, itemStack);
                }
            } else {
                // drop block as item
                at.getWorld().dropItemNaturally(at, is);
            }
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
     * @param block  the block to start a durability timer for
     * @param damage the damage done to the block
     */
    public void startNewTimer(Block block, int damage, TimerState state) {
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
     *
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
     *
     * @return the durability value
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
     *
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
            //this.percent = ((Double) (done / max)).intValue();
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

    public String chunkToString(Chunk chunk) {
        if (chunk == null) {
            return "";
        }

        return chunk.getX() + "." + chunk.getZ() + "." + chunk.getWorld().getName();
    }

    /**
     * Adds a block to the chunk
     *
     * @param block  the block to be added
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
     * @param block  the block to be added
     * @param damage the damage value of the block
     * @param time   the time value of the block
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
     *
     * @return true if block found within chunk
     */
    public boolean contains(Block block) {
        return block != null && contains(block.getLocation());
    }

    /**
     * Does the chunk contain this location
     *
     * @param location the location to check the chunk for
     *
     * @return true if location found within chunk
     */
    public boolean contains(Location location) {
        if (location == null) {
            return false;
        }
        String c = chunkToString(location.getChunk());
        ChunkWrapper chunk = chunks.get(c);

        return chunk != null && chunk.contains(location);
    }

    /**
     * Gets the chunk wrapper from a chunk
     *
     * @param chunk the chunk to get a wrapper from
     *
     * @return the ChunkWrapper that belongs to the chunk.
     */
    public ChunkWrapper getWrapper(Chunk chunk) {
        if (chunk == null) {
            return null;
        }
        String c = chunkToString(chunk);
        if (!chunks.containsKey(c)) {
            loadChunk(chunk);
        }
        return chunks.get(c);
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

}