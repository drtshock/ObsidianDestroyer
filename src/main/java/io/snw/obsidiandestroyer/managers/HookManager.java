package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import org.bukkit.plugin.Plugin;

public class HookManager {

    private static HookManager instance;
    private boolean isCannonsHooked = false;
    private boolean isFactionsHooked = false;

    /**
     * Manages the hooks into other plugins
     */
    public HookManager() {
        instance = this;
        checkCannonsHook();
        checkFactionsHook();
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
                sv = Integer.parseInt(vr[0]);
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
    public boolean isHookedFactions() {
        return isFactionsHooked;
    }

    /**
     * Checks to see if the Factions plugin is active.
     */
    private void checkFactionsHook() {
        Plugin plug = ObsidianDestroyer.getInstance().getServer().getPluginManager().getPlugin("Factions");

        if (plug != null) {
            String[] ver = plug.getDescription().getVersion().split("\\.");
            int v = Integer.parseInt(ver[0]);
            int sv = 0;
            int svr = 0;
            if (ver.length > 1) {
                sv = Integer.parseInt(ver[1]);
            }
            if (ver.length > 2) {
                svr = Integer.parseInt(ver[2]);
            }
            if (v == 2) {
                ObsidianDestroyer.LOG.info("Factions " + v + "." + sv + "." + svr + " Found, Enabling features.");
                isFactionsHooked = true;
            } else {
                ObsidianDestroyer.LOG.info("Factions found, but version " + v + "." + sv + "." + svr + " is not supported! :(");
            }
        }
    }

}
