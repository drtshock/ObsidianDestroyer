package com.drtshock.obsidiandestroyer;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class ExplosionsInLiquid {
    private static ObsidianDestroyer OD;
    private static int RADIUS = 1;
    private static int CANNON_RADIUS = 2;
    private static ArrayList<Integer> FLUID_BLOCKS = new ArrayList<Integer>();
    
    /**
     * Handles an explosion if it occurs from within a liquid.
     * Cancels the explosion, removes any nearby liquids found, and creates a new explosion in the cleared location.
     * Hooks and checks explosion settings in: WorldGuard, Factions, and Towny
     * 
     * @param event EntityExplodeEvent
     * @param plugin ObsidianDestroyer
     */
    public static void Handle(EntityExplodeEvent event, ObsidianDestroyer plugin) {
        OD = plugin;
        
        FLUID_BLOCKS.add(8);
        FLUID_BLOCKS.add(9);
        FLUID_BLOCKS.add(10);
        FLUID_BLOCKS.add(11);
        
        explosionInLiquid(event);
    }
    
    /**
     * Creates a custom explosion in the liquid.
     * Checks with other plugins to see if it has to cancel the event.
     * 
     * @param event EntityExplodeEvent
     */
    private static void explosionInLiquid(EntityExplodeEvent event) {
        if (RADIUS <= 0 || event.isCancelled())
            return;
        
        Entity entity = event.getEntity();
        
        boolean bBoom = false;
        int redstoneCount = 0;
        
        // Hook to prevent liquids from being destroyed if Towny has explosions disabled
        if (ObsidianDestroyer.isHookedTowny()) {
            TownyWorld townyWorld;
            try {
                townyWorld = TownyUniverse.getDataSource().getWorld(event.getLocation().getWorld().getName());

                if (!townyWorld.isUsingTowny())
                    return;
            } catch (Exception e) {
                // failed to get world so abort
                return;
            }
            
            try {
                TownBlock townBlock = townyWorld.getTownBlock(Coord.parseCoord(event.getLocation()));
                if (!townBlock.getPermissions().explosion && !townyWorld.isForceExpl())
                    return;
                if (townyWorld.isWarZone(Coord.parseCoord(event.getLocation())) && !TownyWarConfig.explosionsBreakBlocksInWarZone())
                    return;
            } catch (Exception e) {
                // Block not registered so continue
            }
            
            if (!townyWorld.isExpl())
                return;
        }
        
        // Protects TNT cannons from exploding themselves
        if (OD.getODConfig().getProtectTNTCannons()) {
            for (int x = -CANNON_RADIUS; x <= CANNON_RADIUS; x++)
                for (int y = -CANNON_RADIUS; y <= CANNON_RADIUS; y++)
                    for (int z = -CANNON_RADIUS; z <= CANNON_RADIUS; z++) {
                        Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);
                        
                        if (targetLoc.getBlock().getType().equals(Material.REDSTONE_WIRE) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_ON) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF))
                            redstoneCount++;
                    }
            
            if (redstoneCount >= 6)
                return;
        }
        
        // Creates air where water used to be and sets up the boom if the explosion is from within a liquid
        for (int x = -RADIUS; x <= RADIUS; x++)
            for (int y = -RADIUS; y <= RADIUS; y++)
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);
                    
                    // TODO: Check every block in the explosion for Towny..
                    
                    // Hook to prevent liquids from being destroyed in Faction territory that has explosions disabled
                    if (ObsidianDestroyer.isHookedFactions()) {
                        Faction faction = Board.getFactionAt(event.getLocation());
                        if (faction.getFlag(FFlag.EXPLOSIONS) == false)
                            return;
                    }
                    
                    // Hook to prevent liquids from being destroyed in protected worldguard regions
                    if (ObsidianDestroyer.isHookedWorldGuard()) {
                        try {
                            RegionManager regionManager = OD.getWorldGuard().getRegionManager(targetLoc.getWorld());
                            ApplicableRegionSet set = regionManager.getApplicableRegions(targetLoc);
                            if (!set.allows(com.sk89q.worldguard.protection.flags.DefaultFlag.TNT))
                                return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    // Replace any liquid blocks with air.
                    if (FLUID_BLOCKS.contains(targetLoc.getBlock().getTypeId()) && targetLoc.getBlock().isLiquid()) {                     
                        targetLoc.getBlock().setType(Material.AIR);
                        if (!bBoom)
                            bBoom = true;
                    }
                }
        
        // Creates a new explosion at the cleared location
        if (bBoom) {
            event.setCancelled(true);
            event.getLocation().getBlock().setType(Material.AIR);
            Float f = Float.valueOf(3);
            entity.getWorld().createExplosion(event.getLocation(), f);
        }
    }
}
