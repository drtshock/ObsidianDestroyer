package io.snw.obsidiandestroyer.util;

import io.snw.obsidiandestroyer.managers.ConfigManager;

import java.util.List;

public class GlobalVariables {
    public static List<String> disabledWorlds;

    public static void init() {
        disabledWorlds = ConfigManager.getInstance().getDisabledWorlds();
    }
}
