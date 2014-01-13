package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import org.bukkit.plugin.Plugin;

public class HookManager {

    private static HookManager instance;
    private boolean isCannonsHooked = false;

    /**
     * Manages the hooks into other plugins
     */
    public HookManager() {
        instance = this;
        checkCannonsHook();
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

}
