package com.drtshock.obsidiandestroyer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 
 * @author drtshock
 */
public final class ODEntityListener implements Listener {

    private ObsidianDestroyer plugin;
    public ODConfig config;
    public HashMap<Integer, Integer> obsidianDurability = new HashMap<Integer, Integer>();
    private HashMap<Integer, Timer> obsidianTimer = new HashMap<Integer, Timer>();
    private Random _random = new Random();
    private HashMap<Integer, Float> _entityPowerMap;
    private boolean DisplayWarning = true;

    public ODEntityListener(ObsidianDestroyer plugin) {
        this.plugin = plugin;
        this.config = plugin.getODConfig();
        this._entityPowerMap = new HashMap<Integer, Float>();
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {

        if (!event.isCancelled())
            this._entityPowerMap.put(Integer.valueOf(event.getEntity().getEntityId()), Float.valueOf(event.getRadius()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent event) {

        if (((event == null) || (event.isCancelled())) && (!this.config.getIgnoreCancel())) {
            return;
        }

        int radius = this.config.getRadius();

        if (radius < 0) {
            plugin.getLogger().log(Level.WARNING, "Explosion radius is less than zero. Current value: {0}", radius);
            return;
        }

        Entity detonator = event.getEntity();

        if (detonator == null) {
            return;
        }

        Location detonatorLoc = detonator.getLocation();
        String eventTypeRep = event.getEntity().toString();

        if ((!eventTypeRep.equals("CraftTNTPrimed")) && (!eventTypeRep.equals("CraftCreeper"))
                && (!eventTypeRep.equals("CraftFireball")) && (!eventTypeRep.equals("CraftGhast"))
                && (!eventTypeRep.equals("CraftSnowball"))) {
            return;
        }

        if ((eventTypeRep.equals("CraftTNTPrimed")) && (!this.config.getTntEnabled())) {
            return;
        }

        if ((eventTypeRep.equals("CraftSnowball")) && (!this.config.getCannonsEnabled())) {
            return;
        }

        if ((eventTypeRep.equals("CraftCreeper")) && (!this.config.getCreepersEnabled())) {
            return;
        }

        if (((eventTypeRep.equals("CraftFireball")) || (eventTypeRep.equals("CraftGhast"))) && (!this.config.getGhastsEnabled())) {
            return;
        }

        if (eventTypeRep.equals("CraftSnowball")) {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block block = (org.bukkit.block.Block)iter.next();
                blowBlockUp(block.getLocation());
            }
            return;
        }

        if ((!event.isCancelled()) && (event.getEntityType() != EntityType.ENDER_DRAGON) 
                && (this._entityPowerMap.containsKey(Integer.valueOf(event.getEntity().getEntityId()))) 
                && !this.config.getWaterProtection()) {

            correctExplosion(event, ((Float)this._entityPowerMap.get(Integer.valueOf(event.getEntity().getEntityId()))).floatValue());
            this._entityPowerMap.remove(Integer.valueOf(event.getEntity().getEntityId()));
        }
        
        if (config.getExplodeInLiquids())
        	UnderWaterExplosions.Handle(event, this.plugin);

        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++) {
                    Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);

                    if (detonatorLoc.distance(targetLoc) <= radius) {
                        if ((detonatorLoc.getBlock().isLiquid()) && (this.config.getWaterProtection())) {
                            return;
                        }
                        blowBlockUp(targetLoc);
                    }
                }
    }

    private void blowBlockUp(Location at) {

        if (at == null) {
            return;
        }

        Block b = at.getBlock();

        if (b.getTypeId() == 49) {
            applyDurability(at, this.config.getoDurability());
        }

        if (b.getTypeId() == 116) {
            applyDurability(at, this.config.geteDurability());
        }

        if (b.getTypeId() == 130) {
            applyDurability(at, this.config.getecDurability());
        }

        if (b.getTypeId() == 145) {
            applyDurability(at, this.config.getaDurability());
        }

        if (b.getTypeId() == 7 && this.config.getBedrockEnabled()) {
            applyDurability(at, this.config.getbDurability());
        }
    }

    private void applyDurability(Location at, int dura) {

        Integer representation = Integer.valueOf(at.getWorld().hashCode() + at.getBlockX() * 2389 + at.getBlockY() * 4027 + at.getBlockZ() * 2053);

        if ((this.config.getDurabilityEnabled()) && (dura > 1)) {
            if (this.obsidianDurability.containsKey(representation)) {
                int currentDurability = ((Integer)this.obsidianDurability.get(representation)).intValue();
                currentDurability++;

                if (checkIfMax(currentDurability, dura)) {
                    dropBlockAndResetTime(representation, at);
                }

                else {
                    this.obsidianDurability.put(representation, Integer.valueOf(currentDurability));

                    if (this.config.getDurabilityResetTimerEnabled()) {
                        startNewTimer(representation);
                    }
                }
            }

            else {
                this.obsidianDurability.put(representation, Integer.valueOf(1));

                if (this.config.getDurabilityResetTimerEnabled()) {
                    startNewTimer(representation);
                }

                if (checkIfMax(1, dura)) {
                    dropBlockAndResetTime(representation, at);
                }
            }
        } else {
            destroyBlockAndDropItem(at);
        }
    }

    protected void correctExplosion(EntityExplodeEvent event, float power) {

        World world = event.getEntity().getWorld();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    if ((i == 0) || (i == 15) || (j == 0) || (j == 15) || (k == 0) || (k == 15)) {

                        double d3 = i / 15.0F * 2.0F - 1.0F;
                        double d4 = j / 15.0F * 2.0F - 1.0F;
                        double d5 = k / 15.0F * 2.0F - 1.0F;
                        double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

                        d3 /= d6;
                        d4 /= d6;
                        d5 /= d6;
                        float f1 = power * (0.7F + this._random.nextFloat() * 0.6F);

                        double d0 = event.getLocation().getX();
                        double d1 = event.getLocation().getY();
                        double d2 = event.getLocation().getZ();

                        for (float f2 = 0.3F; f1 > 0.0F; f1 -= f2 * 0.75F) {
                            int l = (int) Math.floor(d0);
                            int i1 = (int) Math.floor(d1);
                            int j1 = (int) Math.floor(d2);
                            int k1 = world.getBlockTypeIdAt(l, i1, j1);

                            if ((k1 > 0) && (k1 != 8) && (k1 != 9) && (k1 != 10) && (k1 != 11)) {
                                f1 -= (net.minecraft.server.v1_5_R3.Block.byId[k1].a(((CraftEntity)event.getEntity()).getHandle()) + 0.3F) * f2;
                            }

                            if ((f1 > 0.0F) && (i1 < 256) && (i1 >= 0) && (k1 != 8) && (k1 != 9) && (k1 != 10) && (k1 != 11)) {
                                Block block = world.getBlockAt(l, i1, j1);

                                if ((block.getType() != Material.AIR) && (!event.blockList().contains(block))) {
                                    event.blockList().add(block);
                                }

                                d0 += d3 * f2;
                                d1 += d4 * f2;
                                d2 += d5 * f2;
                            }
                        }
                    }
                }
            }
        }
    }

    private void destroyBlockAndDropItem(Location at) {
        if (at == null) {
            return;
        }

        Block b = at.getBlock();

        if ((!b.getType().equals(Material.OBSIDIAN)) && (!b.getType().equals(Material.ENCHANTMENT_TABLE)) && (!b.getType().equals(Material.ENDER_CHEST)) && 
                (!b.getType().equals(Material.ANVIL)) && (!b.getType().equals(Material.MOB_SPAWNER)) && (!b.getType().equals(Material.BEDROCK))) {
            return;
        }

        double chance = this.config.getChanceToDropBlock();

        if (chance > 1.0D) {
            chance = 1.0D;
        }

        if (chance < 0.0D) {
            chance = 0.0D;
        }

        double random = Math.random();

        if ((chance == 1.0D) || (chance <= random)) {
            ItemStack is = new ItemStack(b.getType(), 1, Short.valueOf(b.getData()).shortValue());

            at.getWorld().dropItemNaturally(at, is);
        }

        b.setTypeId(Material.AIR.getId());
    }

    private boolean checkIfMax(int value, int Dura) {
        return value == Dura;
    }

    private void startNewTimer(Integer representation) {
        if (this.obsidianTimer.get(representation) != null) {
            ((Timer)this.obsidianTimer.get(representation)).cancel();
        }

        // EXPERIMENTAL: Some safety just in case the server is running low on memory.
        // This will prevent a new timer from being created. However, durability will not regenerate
        if (config.getDurabilityTimerSafey())
        {
            if (((float) Runtime.getRuntime().freeMemory() + (1024 * 1024 * config.getMinFreeMemoryLimit())) >= Runtime.getRuntime().maxMemory()) {
                if (DisplayWarning) {
                    plugin.LOG.info("Server Memory: " + ((Runtime.getRuntime().freeMemory() / 1024) / 1024) + "MB free out of " + ((Runtime.getRuntime().maxMemory() / 1024) / 1024) + "MB available.");
                    plugin.LOG.info("Server is running low on resources.. Let's not start a new timer, there are " + this.obsidianTimer.size() + " other timers running!");
                    DisplayWarning = false;
                }
                return;
            }
            else
                DisplayWarning = true;
        }

        Timer timer = new Timer();
        timer.schedule(new ODTimerTask(this.plugin, representation), this.config.getDurabilityResetTime());

        this.obsidianTimer.put(representation, timer);
    }

    private void dropBlockAndResetTime(Integer representation, Location at) {
        this.obsidianDurability.remove(representation);
        destroyBlockAndDropItem(at);

        if (this.config.getDurabilityResetTimerEnabled()) {
            if (this.obsidianTimer.get(representation) != null) {
                ((Timer)this.obsidianTimer.get(representation)).cancel();
            }

            this.obsidianTimer.remove(representation);
        }
    }

    public HashMap<Integer, Integer> getObsidianDurability() {
        return this.obsidianDurability;
    }

    public void setObsidianDurability(HashMap<Integer, Integer> map) {
        if (map == null) {
            return;
        }

        this.obsidianDurability = map;
    }

    public HashMap<Integer, Timer> getObsidianTimer() {
        return this.obsidianTimer;
    }

    public void setObsidianTimer(HashMap<Integer, Timer> map) {
        if (map == null) {
            return;
        }

        this.obsidianTimer = map;
    }
}