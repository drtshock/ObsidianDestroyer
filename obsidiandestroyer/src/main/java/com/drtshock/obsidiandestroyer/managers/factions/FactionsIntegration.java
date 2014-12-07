package com.drtshock.obsidiandestroyer.managers.factions;

import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.HookManager;

/**
 * Created by Squid on 12/6/2014.
 */
public class FactionsIntegration {
    public static boolean isUsing() {
        return ConfigManager.getInstance().getUsingFactions();
    }

    public static FactionsHook get() {
        return HookManager.getInstance().getFactionsManager().getFactions();
    }
}
