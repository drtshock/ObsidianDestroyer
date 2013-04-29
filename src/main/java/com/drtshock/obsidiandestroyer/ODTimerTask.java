package com.pandemoneus.obsidianDestroyer;

import java.util.HashMap;
import java.util.TimerTask;

public final class ODTimerTask extends TimerTask {

    private ObsidianDestroyer plugin;
    private final Integer duraID;

    public ODTimerTask(ObsidianDestroyer plugin, Integer duraID) {
        this.plugin = plugin;
        this.duraID = duraID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        resetDurability(duraID);		
    }

    private void resetDurability(Integer id) {
        if (id == null) {
            return;
        }

        HashMap<Integer, Integer> map = plugin.getListener().getObsidianDurability();

        if (map == null) {
            return;
        }

        map.remove(id);
    }
}
