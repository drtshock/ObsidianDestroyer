package com.drtshock.obsidiandestroyer;

import com.drtshock.obsidiandestroyer.commands.ODCommand;
import com.drtshock.obsidiandestroyer.listeners.BlockListener;
import com.drtshock.obsidiandestroyer.listeners.EntityExplodeListener;
import com.drtshock.obsidiandestroyer.listeners.EntityImpactListener;
import com.drtshock.obsidiandestroyer.listeners.PlayerListener;
import com.drtshock.obsidiandestroyer.managers.ChunkManager;
import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.drtshock.obsidiandestroyer.managers.HookManager;
import com.drtshock.obsidiandestroyer.managers.MaterialManager;
import com.drtshock.obsidiandestroyer.util.Metrics;
import com.drtshock.obsidiandestroyer.util.Updater;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObsidianDestroyer extends JavaPlugin {

    public static Logger LOG;
    private static ObsidianDestroyer instance;
    private boolean update = false;
    private String name = "";

    public static ObsidianDestroyer getInstance() {
        return instance;
    }

    public static void debug(String debug) {
        if (ConfigManager.getInstance() == null || ConfigManager.getInstance().getDebug()) {
            LOG.info(debug);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        LOG = getLogger();

        // Things..
        new ConfigManager(false);
        new HookManager();
        new MaterialManager();
        new ChunkManager();

        // Set command executor
        getCommand("od").setExecutor(new ODCommand());

        // Register Event listeners
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new EntityExplodeListener(), this);
        if (HookManager.getInstance().isHookedCannons()) {
            pm.registerEvents(new EntityImpactListener(), this);
        }
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new BlockListener(), this);

        // Check for updates
        checkUpdate();

        // Initialize metrics
        startMetrics();
    }

    @Override
    public void onDisable() {
        // Save persistant data
        if (ChunkManager.getInstance() != null) {
            ChunkManager.getInstance().save();
        }
    }

    protected void checkUpdate() {
        if (ConfigManager.getInstance().getCheckUpdate()) {
            final ObsidianDestroyer plugin = this;
            final File file = this.getFile();
            final Updater.UpdateType updateType = (ConfigManager.getInstance().getDownloadUpdate() ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD);
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    Updater updater = new Updater(plugin, 43718, file, updateType, false);
                    update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    name = updater.getLatestName();
                    if (updater.getResult() == Updater.UpdateResult.SUCCESS) {
                        getLogger().log(Level.INFO, "Successfully updated ObsidianDestroyer to version {0} for next restart!", updater.getLatestName());
                    } else if (updater.getResult() == Updater.UpdateResult.NO_UPDATE) {
                        getLogger().log(Level.INFO, "We didn't find an update!");
                    }
                }
            });
        }
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);

            Metrics.Graph graph = metrics.createGraph("Durability");
            graph.addPlotter(new Metrics.Plotter() {
                @Override
                public String getColumnName() {
                    int amt = MaterialManager.getInstance().getDurability("OBSIDIAN");
                    return amt > 0 ? "" + amt : "N/A";
                }

                @Override
                public int getValue() {
                    return 1;
                }
            });
            metrics.start();
        } catch (IOException ex) {
            getLogger().warning("Failed to load metrics :(");
        }
    }

    public String getLatestVersion() {
        return name;
    }

    public boolean getNeedsUpdate() {
        return update;
    }
}
