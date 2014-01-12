package io.snw.obsidiandestroyer.managers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.snw.obsidiandestroyer.ObsidianDestroyer;
import org.bukkit.plugin.Plugin;

public class HookManager {

    private static HookManager instance;
    private boolean isFactionHooked = false;
    private boolean isTownyHooked = false;
    private boolean isWorldGuardHooked = false;
    private boolean isCannonsHooked = false;

    /**
     * Manages the hooks into other plugins
     */
    public HookManager() {
        instance = this;
        checkFactionsHook();
        checkTownyHook();
        checkWorldGuardGHook();
        checkCannonsHook();
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
            if (ver[0].equalsIgnoreCase("2") && ConfigManager.getInstance().getBypassAllFluidProtection()) {
                ObsidianDestroyer.LOG.info("Factions 2.x Found, but not supported at this time.");
                ObsidianDestroyer.LOG.warning("ObsidianDestroyer and Factions have conflicting options.");
                ObsidianDestroyer.LOG.warning("Set handleExploitTNTWaterlog to false in Factions in config.");
            } else if (version.equalsIgnoreCase("1.8")) {
                ObsidianDestroyer.LOG.info("Factions 1.8.x Found! Enabling hook..");
                isFactionHooked = true;
            } else if (version.equalsIgnoreCase("1.6")) {
                ObsidianDestroyer.LOG.info("Factions found, but v1.6.x is not supported!");
            }
        }
    }

    /**
     * Gets the state of the Cannons hook.
     *
     * @return Cannons hook state
     */
    public boolean isHookedCannons() {
        return isCannonsHooked;
    }

    /**
     * Checks to see if the Cannons plugin is active and sets flag.
     */
    private void checkCannonsHook() {
        Plugin plug = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Cannons");

        if (plug != null) {
            String[] ver = plug.getDescription().getVersion().split("\\.");
            int v = Integer.parseInt(ver[0]);
            int sv = 0;
            String r = "";
            //ObsidianDestroyer.LOG.info(ver[0] + " . " + (ver.length > 1 ? ver[1] : ""));
            if (ver.length > 1) {
                String[] vr = ver[1].split("\\-");
                //ObsidianDestroyer.LOG.info(vr[0] + " - " + (vr.length > 1 ? vr[1] : ""));
                sv = Integer.parseInt(vr[0]);
                if (vr.length >1) {
                    r = vr[1];
                }
            }
            if (v > 2 || (v == 2 && (sv > 0 || (sv == 0 && (r.equalsIgnoreCase("R7")))))) {
                ObsidianDestroyer.LOG.info("Cannons " + v + "." + sv + "-" + r + " Found, Enabling features.");
                isCannonsHooked = true;
            } else {
                ObsidianDestroyer.LOG.info("Cannons " + v + "." + sv + "-" + r + " Found, but only versions v2.0-R7 and above are supported!");
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
