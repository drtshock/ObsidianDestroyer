package com.drtshock.obsidiandestroyer;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;

public class UnderWaterExplosions {
	private static ObsidianDestroyer OD;
	private static int radius = 1;
	private static int cannonRadius = 2;
	private static ArrayList<Integer> fluidBlocks = new ArrayList<Integer>();
	
	public static void Handle (EntityExplodeEvent event, ObsidianDestroyer plugin) {
		OD = plugin;
		
		fluidBlocks.add(8);
		fluidBlocks.add(9);
		fluidBlocks.add(10);
		fluidBlocks.add(11);
		
		explodeUnderWater(event);
	}
	
	private static void explodeUnderWater(EntityExplodeEvent event) {
		if (radius <= 0 || event.isCancelled())
			return;
		
		Entity entity = event.getEntity();
		
		boolean bBoom = false;
		int redstoneCount = 0;
		
		// Hook to prevent liquids from being destroyed if Towny has explosions disabled
		if (ObsidianDestroyer.hookedTowny()) {
			TownyWorld townyWorld;
			try {
				townyWorld = TownyUniverse.getDataSource().getWorld(event.getLocation().getWorld().getName());

				if (!townyWorld.isUsingTowny())
					return;

			} catch (NotRegisteredException e) {
				// failed to get world so abort
				return;
			}
			
			try {
				TownBlock townBlock = townyWorld.getTownBlock(Coord.parseCoord(event.getLocation()));
				if (!townBlock.getPermissions().explosion && !townyWorld.isForceExpl())
					return;
				if (townyWorld.isWarZone(Coord.parseCoord(event.getLocation())) && !TownyWarConfig.explosionsBreakBlocksInWarZone())
					return;
			} catch (NotRegisteredException e) {
				// Block not registered so continue
			}
			
			if (!townyWorld.isExpl())
				return;
		}
		
		// Protects TNT cannons from exploding themselves
		if (OD.getODConfig().getProtectTNTCannons()) {
	        for (int x = -cannonRadius; x <= cannonRadius; x++)
	            for (int y = -cannonRadius; y <= cannonRadius; y++)
	                for (int z = -cannonRadius; z <= cannonRadius; z++) {
	                	Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);
	                	
	                	if (targetLoc.getBlock().getType().equals(Material.REDSTONE_WIRE) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_ON) || targetLoc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF))
	                		redstoneCount++;
	                }
			
	        if (redstoneCount >= 6) {
	        	return;
            }
		}
		
        // Creates air where water used to be and sets up the boom if the explosion is from within a liquid
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++) {
                	Location targetLoc = new Location(entity.getWorld(), entity.getLocation().getX() + x, entity.getLocation().getY() + y, entity.getLocation().getZ() + z);
                	
                	// TODO: Check every block in the explosion for Towny..
                	
                	// Hook to prevent liquids from being destroyed in Faction territory that has explosions disabled
            		if (ObsidianDestroyer.hookedFactions()) {
            			Faction faction = Board.getFactionAt(event.getLocation());
            			if (faction.getFlag(FFlag.EXPLOSIONS) == false || faction.noExplosionsInTerritory())
            				return;
            		}
                	
            		// Replace any liquid blocks with air.
                	if (fluidBlocks.contains(targetLoc.getBlock().getTypeId()) && targetLoc.getBlock().isLiquid()) {             		
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
