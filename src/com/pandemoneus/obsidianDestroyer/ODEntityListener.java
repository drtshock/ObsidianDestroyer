package com.pandemoneus.obsidianDestroyer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Custom Entity Listener for the ObsidianDestroyer plugin.
 * 
 * @author Pandemoneus
 * 
 */
public final class ODEntityListener implements Listener {

	private ObsidianDestroyer plugin;
	private ODConfig config;
	private HashMap<Integer, Integer> obsidianDurability = new HashMap<Integer, Integer>();
	private HashMap<Integer, Timer> obsidianTimer = new HashMap<Integer, Timer>();

	public ODEntityListener(ObsidianDestroyer plugin) {
		this.plugin = plugin;
		config = plugin.getODConfig();
	}

	/**
	 * Destroys obsidian blocks in a radius around TNT, Creepers and/or Ghast
	 * Fireballs.
	 * 
	 * @param event
	 *            event data
	 */
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event) {
		// do not do anything in case explosions get canceled
		if (event == null || event.isCancelled()) {
			return;
		}

		final int radius = config.getRadius();

		// cancel if radius is < 0
		if (radius < 0) {
			Log.warning("Explosion radius is less than zero. Current value: " + radius);
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

		// cancel if detonator was neither TNT, a creeper nor a ghast
		if (!(eventTypeRep.equals("CraftTNTPrimed") || eventTypeRep.equals("CraftCreeper") || eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast") || eventTypeRep.equals("CraftSnowball"))) {
			return;
		}

		// cancel if detonator was TNT, but TNT not allowed to destroy obsidian
		if (eventTypeRep.equals("CraftTNTPrimed") && !config.getTntEnabled()) {
			return;
		}
		
		// cancel if detonator was Snowball, but Cannons not allowed to destroy obsidian
		if (eventTypeRep.equals("CraftSnowball") && !config.getCannonsEnabled()) {
			return;
		}

		// cancel if detonator was a creeper, but creepers not allowed to
		// destroy obsidian
		if (eventTypeRep.equals("CraftCreeper") && !config.getCreepersEnabled()) {
			return;
		}

		// cancel if detonator was a ghast, but ghasts not allowed to destroy
		// obsidian
		if ((eventTypeRep.equals("CraftFireball") || eventTypeRep.equals("CraftGhast")) && !config.getGhastsEnabled()) {
			return;
		}
		
		if (eventTypeRep.equals("CraftSnowball")) {
			Iterator<Block> iter = event.blockList().iterator();
			while (iter.hasNext()){
				Block block = iter.next();
				blowBlockUp(block.getLocation());
			}
			return;
		}

		// calculate sphere around detonator
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Location targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
					
					if (detonatorLoc.distance(targetLoc) <= radius) {
						if (detonatorLoc.getBlock().isLiquid())
							return;

						blowBlockUp(targetLoc);
					}
				}
			}
		}
	}

	private void blowBlockUp(final Location at) {
		if (at == null) {
			return;
		}

		final Block b = at.getBlock();

		if (b.getTypeId() == 49 || b.getTypeId() == 116) {
			// random formula to create unique integers
			Integer representation = at.getWorld().hashCode() + at.getBlockX() * 2389 + at.getBlockY() * 4027 + at.getBlockZ() * 2053;

			if (config.getDurabilityEnabled() && config.getDurability() > 1) {
				if (obsidianDurability.containsKey(representation)) {

					int currentDurability = (int) obsidianDurability.get(representation);
					currentDurability++;

					if (checkIfMax(currentDurability)) {
						// counter has reached max durability, so remove the
						// block and drop an item
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
				destroyBlockAndDropItem(at);
			}
		}
	}

	private void destroyBlockAndDropItem(final Location at) {
		if (at == null) {
			return;
		}

		final Block b = at.getBlock();

		if (!b.getType().equals(Material.OBSIDIAN) && !b.getType().equals(Material.ENCHANTMENT_TABLE)) {
			return;
		}

		double chance = config.getChanceToDropBlock();

		if (chance > 1.0)
			chance = 1.0;
		if (chance < 0.0)
			chance = 0.0;

		final double random = Math.random();

		if (chance == 1.0 || chance <= random) {
			ItemStack is = new ItemStack(b.getType(), 1, (byte) 0, b.getData());

			// drop item
			at.getWorld().dropItemNaturally(at, is);
		}

		// changes original block to Air block
		b.setTypeId(Material.AIR.getId());
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
		destroyBlockAndDropItem(at);

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
	 * @param map
	 *            the HashMap containing all saved durabilities
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
	 * @param map
	 *            the HashMap containing all saved durability timers
	 */
	public void setObsidianTimer(HashMap<Integer, Timer> map) {
		if (map == null) {
			return;
		}

		obsidianTimer = map;
	}
}
