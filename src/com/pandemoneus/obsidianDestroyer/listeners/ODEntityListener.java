package com.pandemoneus.obsidianDestroyer.listeners;

import java.util.HashMap;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import com.pandemoneus.obsidianDestroyer.ODTimerTask;
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
	
	private ObsidianDestroyer plugin;
	private ODConfig config;
	private HashMap<Integer, Integer> obsidianDurability = new HashMap<Integer, Integer>();
	private HashMap<Integer, Timer> obsidianTimer = new HashMap<Integer, Timer>();
	
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
						// counter has reached max durability, so remove the block and drop an item
						dropBlockAndResetTime(representation, at);
					} else {
						// counter has not reached max durability yet
						obsidianDurability.put(representation, currentDurability);
						
						if (config.getDurabilityResetTimerEnabled()) {
							startNewTimer(representation);
						}
					}
				} else {
					obsidianDurability.put(representation, 1);
					
					if (config.getDurabilityResetTimerEnabled()) {
						startNewTimer(representation);
					}
					
					if (checkIfMax(1)) {
						dropBlockAndResetTime(representation, at);
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
	
	private void startNewTimer(Integer representation) {
		if (obsidianTimer.get(representation) != null) {
			obsidianTimer.get(representation).cancel();
		}
		
		Timer timer = new Timer();
		timer.schedule(new ODTimerTask(plugin, representation), config.getDurabilityResetTime());
		
		obsidianTimer.put(representation, timer);
	}
	
	private void dropBlockAndResetTime(Integer representation, Location at) {
		obsidianDurability.remove(representation);
		dropItem(at);
		
		if (config.getDurabilityResetTimerEnabled()) {
			if (obsidianTimer.get(representation) != null) {
				obsidianTimer.get(representation).cancel();
			}
		
			obsidianTimer.remove(representation);
		}
	}
	
	/**
	 * Returns the HashMap containing all saved durabilities.
	 * 
	 * @return the HashMap containing all saved durabilities
	 */
	public HashMap<Integer, Integer> getObsidianDurability() {
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
	
	/**
	 * Returns the HashMap containing all saved durability timers.
	 * 
	 * @return the HashMap containing all saved durability timers
	 */
	public HashMap<Integer, Timer> getObsidianTimer() {
		return obsidianTimer;
	}
	
	/**
	 * Sets the HashMap containing all saved durability timers.
	 * 
	 * @param map the HashMap containing all saved durability timers
	 */
	public void setObsidianTimer(HashMap<Integer, Timer> map) {
		if (map == null) {
			return;
		}
		
		obsidianTimer = map;
	}
}
