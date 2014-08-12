package com.drtshock.obsidiandestroyer.managers;

import java.util.logging.Level;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public class HookManager {

    private static HookManager instance;
    private boolean isCannonsHooked = false;
    private boolean isFactionFound = false;
    private boolean isMassiveCoreFound = false;

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
        return isFactionFound && isMassiveCoreFound;
    }

    /**
     * Checks to see if the Factions plugin is active.
     */
    private void checkFactions() {
        Plugin massivecore = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("MassiveCore");
        Plugin mcore = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("mcore");
        Plugin factions = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Factions");
        ConsoleCommandSender console = ObsidianDestroyer.getInstance().getServer().getConsoleSender();

        if (massivecore != null && massivecore.isEnabled()) {
            console.sendMessage(Util.header() + "MassiveCore Found! Version: " + massivecore.getDescription().getVersion());
            isMassiveCoreFound = true;
        } else if (mcore != null && mcore.isEnabled()) {
            console.sendMessage(Util.header() + "mcore Found! Version: " + mcore.getDescription().getVersion());
            isMassiveCoreFound = true;
        }

        if (factions != null && factions.isEnabled()) {
            String[] ver = factions.getDescription().getVersion().split("\\.");
            int v = 0;
            try {
                v = Integer.parseInt(ver[0]);
            } catch (NumberFormatException e) {
                ObsidianDestroyer.LOG.log(Level.SEVERE, e.getMessage());
            }
            if (v == 2) {
                console.sendMessage(Util.header() + "Factions Found! Version: " + factions.getDescription().getVersion());
                isFactionFound = true;
            } else {
                console.sendMessage(Util.header() + "Factions found, but version " + factions.getDescription().getVersion() + " is not supported! " + ChatColor.RED + ":(");
            }
        }

        if (isFactionFound && isMassiveCoreFound) {
            console.sendMessage(Util.header() + ChatColor.GREEN + "Factions - MassiveCore link established.");
        } else {
            console.sendMessage(Util.header() + ChatColor.RED + "Factions - MassiveCore link failed!");
        }
    }

}
