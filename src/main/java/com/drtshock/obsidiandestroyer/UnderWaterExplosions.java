package com.drtshock.obsidiandestroyer;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;

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
                	
                	if (fluidBlocks.contains(targetLoc.getBlock().getTypeId()) && targetLoc.getBlock().isLiquid()) {             		
                		targetLoc.getBlock().setType(Material.AIR);
                		if (!bBoom)
                			bBoom = true;
                	}
                }
        
        // Creates a new explosion at the cleared location
        if (bBoom) {
        	event.getLocation().getBlock().setType(Material.AIR);
			Float f = Float.valueOf(4);
			entity.getWorld().createExplosion(event.getLocation(), f);
        }
	}
}
