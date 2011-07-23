package com.pandemoneus.obsidianDestroyer.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import com.pandemoneus.obsidianDestroyer.ObsidianDestroyer;
import com.pandemoneus.obsidianDestroyer.config.ODConfig;
import com.pandemoneus.obsidianDestroyer.logger.Log;

/**
 * Custom Entity Listener for the ObsidianDestroyer plugin.
 * 
 * @author Pandemoneus
 *
 */
public final class ODEntityListener extends EntityListener {
	
	@SuppressWarnings("unused")
	private ObsidianDestroyer plugin;
	private ODConfig config;
	private HashMap<Integer, Integer> obsidianDurability = new HashMap<Integer, Integer>();
	
	public ODEntityListener(ObsidianDestroyer plugin) {
		this.plugin = plugin;
		config = plugin.getConfig();
	}
	/**
	 * Destroys obsidian blocks in a radius around TNT, Creepers and/or Ghast Fireballs.
	 * 
	 * @param event event data
	 */
	@Override
	public void onEntityExplode(EntityExplodeEvent event) {
		// do not do anything in case explosions get canceled
		if (event == null || event.isCancelled()) {
			return;
		}
		
		int radius = config.getRadius();

		// cancel if radius is < 0
		if (radius < 0) {
			Log.warning("Explosion radius is less than zero. Current value: " + radius);
			return;
		}

		Entity detonator = event.getEntity();
		
		if (detonator == null) {
			// some other plugins create new explosions passing 'null' as Entity, so we need this here to fix it
			return;
		}
		
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
		if (eventTypeRep.equals("CraftTNTPrimed") && !config.getTntEnabled()) {
			return;
		}

		// cancel if detonator was a creeper, but creepers not allowed to destroy obsidian
		if (eventTypeRep.equals("CraftCreeper") && !config.getCreepersEnabled()) {
			return;
		}

		// cancel if detonator was a ghast, but ghasts not allowed to destroy obsidian
		if ((eventTypeRep.equals("CraftFireball")
				|| eventTypeRep.equals("CraftGhast")) && !config.getGhastsEnabled()) {
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

	private void blowObsidianUp(Location at) {
		Block b = at.getBlock();

		if (b.getTypeId() == 49) {
			// random formula to create unique integers
			Integer representation = at.getWorld().hashCode() + (int) at.getX() + (int) at.getY() + (int) at.getZ();
			
			if (config.getDurabilityEnabled() && config.getDurability() > 1) {
				if (obsidianDurability.containsKey(representation)) {
					
					int currentDurability = (int) obsidianDurability.get(representation);
					currentDurability++;
					
					if (checkIfMax(currentDurability)) {
						obsidianDurability.remove(representation);
						dropItem(at);
					} else {
						obsidianDurability.put(representation, currentDurability);
					}
				} else {
					obsidianDurability.put(representation, 1);
					
					if (checkIfMax(1)) {
						obsidianDurability.remove(representation);
						dropItem(at);
					}
				}
			} else {
				dropItem(at);
			}
		}
	}
	
	private void dropItem(Location at) {
		Block b = at.getBlock();
		
		Material mat = Material.getMaterial(49);
		ItemStack is = new ItemStack(mat, 1, (byte) 0, (byte) 0);

		// changes Obsidian block to Air block
		b.setTypeId(Material.AIR.getId());

		at.getWorld().dropItemNaturally(at, is);
	}
	
	private boolean checkIfMax(int value) {
		return value == config.getDurability();
	}
	
	/**
	 * Returns the HashMap containing all saved durabilities.
	 * 
	 * @return the HashMap containing all saved durabilities
	 */
	public HashMap<Integer, Integer> getObisidanDurability() {
		return obsidianDurability;
	}
	
	/**
	 * Sets the HashMap containing all saved durabilities.
	 * 
	 * @param map the HashMap containing all saved durabilities
	 */
	public void setObsidianDurability(HashMap<Integer, Integer> map) {
		if (map == null) {
			return;
		}
		
		obsidianDurability = map;
	}
}
