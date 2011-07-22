package com.pandemoneus.obsidianDestroyer.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import com.pandemoneus.obsidianDestroyer.config.ODConfig;
import com.pandemoneus.obsidianDestroyer.logger.Log;

/**
 * Custom Entity Listener for the ObsidianDestroyer plugin.
 * 
 * @author Pandemoneus
 *
 */
public final class ODEntityListener extends EntityListener {
	
	private static HashMap<Location, Integer> obsidianDurability = new HashMap<Location, Integer>();
	
	/**
	 * Destroys obsidian blocks in a radius around TNT, Creepers and/or Ghast Fireballs.
	 * 
	 * @param event event data
	 */
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		// do not do anything in case explosions get canceled
		if (event.isCancelled()) {
			return;
		}
		
		int radius = ODConfig.getRadius();

		// cancel if radius is < 0
		if (radius < 0) {
			Log.warning("Explosion radius is less than zero. Current value: "
							+ radius);
			return;
		}

		Entity detonator = event.getEntity();
		Location detonatorLoc = detonator.getLocation();
		String eventTypeRep = event.getEntity().toString();

		// cancel if detonator was neither TNT, a creeper nor a ghast
		if (!(eventTypeRep.equals("CraftTNTPrimed")
				|| eventTypeRep.equals("CraftCreeper")
				|| eventTypeRep.equals("CraftFireball")
				|| eventTypeRep.equals("CraftGhast"))) {
			return;
		}

		// cancel if detonator was TNT, but TNT not allowed to destroy obsidian
		if (eventTypeRep.equals("CraftTNTPrimed") && !ODConfig.getTntEnabled()) {
			return;
		}

		// cancel if detonator was a creeper, but creepers not allowed to destroy obsidian
		if (eventTypeRep.equals("CraftCreeper") && !ODConfig.getCreepersEnabled()) {
			return;
		}

		// cancel if detonator was a ghast, but ghasts not allowed to destroy obsidian
		if ((eventTypeRep.equals("CraftFireball")
				|| eventTypeRep.equals("CraftGhast")) && !ODConfig.getGhastsEnabled()) {
			return;
		}

		// calculate sphere around detonator
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Location targetLoc = new Location(detonator.getWorld(),
							detonatorLoc.getX() + x, detonatorLoc.getY() + y,
							detonatorLoc.getZ() + z);

					if (detonatorLoc.distance(targetLoc) <= radius) {
						blowObsidianUp(targetLoc);
					}
				}
			}
		}
	}

	private static void blowObsidianUp(Location at) {
		Block b = at.getBlock();

		if (b.getTypeId() == 49) {
			Location atClone = new Location(at.getWorld(), (int) at.getX(), (int) at.getY(), (int) at.getZ());
			
			if (ODConfig.getDurabilityEnabled() && ODConfig.getDurability() > 1) {
				if (obsidianDurability.containsKey(atClone)) {
					int currentDurability = (int) obsidianDurability.get(atClone);
					currentDurability++;
					
					if (checkIfMax(currentDurability)) {
						obsidianDurability.remove(atClone);
						dropItem(at);
					} else {
						obsidianDurability.put(atClone, currentDurability);
					}
				} else {
					obsidianDurability.put(atClone, 1);
					
					if (checkIfMax(1)) {
						obsidianDurability.remove(atClone);
						dropItem(at);
					}
				}
			} else {
				dropItem(at);
			}
		}
	}
	
	private static void dropItem(Location at) {
		Block b = at.getBlock();
		
		Material mat = Material.getMaterial(49);
		ItemStack is = new ItemStack(mat, 1, (byte) 0, (byte) 0);

		// changes Obsidian block to Air block
		b.setTypeId(Material.AIR.getId());

		at.getWorld().dropItemNaturally(at, is);
	}
	
	private static boolean checkIfMax(int value) {
		return value == ODConfig.getDurability();
	}
}
