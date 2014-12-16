package com.drtshock.obsidiandestroyer.managers;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.managers.factions.FactionsManager;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class HookManager {

    private static HookManager instance;
    private boolean isCannonsHooked = false;
    private boolean isFactionFound = false;

    private FactionsManager factions;

    /**
     * Manages the hooks into other plugins
     */
    public HookManager() {
        instance = this;
        checkCannonsHook();
        checkFactions();
    }

    public static HookManager getInstance() {
        return instance;
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
                try {
                    sv = Integer.parseInt(vr[0]);
                } catch (NumberFormatException e) {
                    ObsidianDestroyer.LOG.log(Level.SEVERE, e.getMessage());
                }
                if (vr.length > 1) {
                    r = vr[1];
                }
            }
            if (v > 2 || (v == 2 && (sv > 0 || (sv == 0 && r.equalsIgnoreCase("R7"))))) {
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
    public boolean isFactionsFound() {
        return isFactionFound;
    }

    /**
     * Check if the plugin should use factions.
     *
     * @return true if config is set to true and the factions manager is not null. Otherwise false.
     */
    public boolean isUsingFactions() {
        return ConfigManager.getInstance().getFactionHookEnabled() && factions != null && factions.getFactions() != null;
    }

    /**
     * Checks to see if the Factions plugin is active.
     */
    private void checkFactions() {
        if (ConfigManager.getInstance().getFactionHookEnabled()) {
            Plugin factions = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Factions");
            if (factions != null) {
                this.factions = new FactionsManager(ObsidianDestroyer.getInstance(), factions); // Loads the hooks internally, if nothing is found that's fine.
                if (this.factions.getFactions() == null) {
                    ObsidianDestroyer.LOG.info("Factions was found, but the version " + factions.getDescription().getVersion() + " you have is not supported.");
                } else {
                    isFactionFound = true;
                    ObsidianDestroyer.LOG.info("Factions hook for version " + factions.getDescription().getVersion() + " has been loaded!");
                }
            } else {
                ObsidianDestroyer.LOG.info("Factions hook enabled, but Factions wasn't found. What were you thinking O_o");
            }
        }
    }

    /**
     * Gets the Factions Manager.
     *
     * @return the FactionsManager.
     */
    public FactionsManager getFactionsManager() {
        return factions;
    }

}
