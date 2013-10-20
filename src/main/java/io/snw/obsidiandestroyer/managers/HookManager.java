package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;

import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class HookManager {
    private static HookManager instance;
    private boolean isFactionHooked = false;
    private boolean isTownyHooked = false;
    private boolean isWorldGuardHooked = false;

    /**
     * Manages the hooks into other plugins
     */
    public HookManager() {
        instance = this;
        checkFactionsHook();
        checkTownyHook();
        checkWorldGuardGHook();
    }

    public static HookManager getInstance() {
        return instance;
    }

    /**
     * Checks to see if the Factions plugin is active and sets flag.
     */
    private void checkFactionsHook() {
        Plugin plug = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Factions");

        if (plug != null) {
            String[] ver = plug.getDescription().getVersion().split("\\.");
            String version = ver[0] + "." + ver[1];
            if (ver[0] == "2") {
                ObsidianDestroyer.LOG.info("Factions 2.x Found, but not supported at this time.");
            } else if (version.equalsIgnoreCase("1.8")) {
                ObsidianDestroyer.LOG.info("Factions 1.8.x Found! Enabling hook..");
                isFactionHooked = true;
            } else if (version.equalsIgnoreCase("1.6")) {
                ObsidianDestroyer.LOG.info("Factions found, but v1.6.x is not supported!");
            }
        }
    }

    /**
     * Gets the state of the Factions hook.
     *
     * @return Factions hook state
     */
    public boolean isHookedFactions() {
        return isFactionHooked;
    }

    /**
     * Checks to see if the Towny plugin is active and sets flag.
     */
    private void checkTownyHook() {
        Plugin plug = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Towny");

        if (plug != null) {
            ObsidianDestroyer.LOG.info("Towny Found! Enabling hook..");
            isTownyHooked = true;
        }
    }

    /**
     * Gets the state of the Towny hook.
     *
     * @return Towny hook state
     */
    public boolean isHookedTowny() {
        return isTownyHooked;
    }

    /**
     * Checks to see if the WorldGuard plugin is active and sets flag.
     */
    private void checkWorldGuardGHook() {
        Plugin plug = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        if (plug != null) {
            ObsidianDestroyer.LOG.info("WorldGuard Found! Enabling hook..");
            isWorldGuardHooked = true;
        }
    }

    /**
     * Gets the state of the WorldGuard hook.
     *
     * @return WorldGuard hook state
     */
    public boolean isHookedWorldGuard() {
        return isWorldGuardHooked;
    }

    /**
     * Gets the WorldGuard plugin
     *
     * @return WorldGuardPlugin
     * @throws Exception
     */
    public WorldGuardPlugin getWorldGuard() throws Exception {
        Plugin plugin = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            throw new Exception("WorldGuard could not be reached!");
        }

        return (WorldGuardPlugin) plugin;
    }
}
