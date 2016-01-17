package com.drtshock.obsidiandestroyer.managers;

import com.drtshock.obsidiandestroyer.ObsidianDestroyer;
import com.drtshock.obsidiandestroyer.datatypes.DurabilityMaterial;
import com.drtshock.obsidiandestroyer.util.Util;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    private static ConfigManager instance;
    private YamlConfiguration tc;
    private YamlConfiguration tm;
    private YamlConfiguration config;
    private YamlConfiguration materials;
    private boolean loaded;

    /**
     * Initializes a new config manager
     *
     * @param loaded is the manager loaded
     */
    public ConfigManager(boolean loaded) {
        instance = this;
        this.loaded = false;
        if (!loaded) {
            loadFiles(false);
        }
    }

    /**
     * Gets the current instance of the config manager
     *
     * @return class instance
     */
    public static ConfigManager getInstance() {
        return instance;
    }

    public void reload() {
        this.loaded = false;
        loadFiles(false);
    }

    /**
     * Create a copy of the files in memory
     *
     * @param apply the copy in memory to the current instance
     */
    public void backup(boolean apply) {
        if (!apply) {
            tc = config;
            tm = materials;
        } else {
            config = tc;
            materials = tm;
        }
    }

    /**
     * Loads config and material file
     *
     * @param update for recursive check
     */
    private void loadFiles(boolean update) {
        File folder = ObsidianDestroyer.getInstance().getDataFolder();
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        File configFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            ObsidianDestroyer.debug("Creating config File.");
            createFile(configFile, "config.yml");
        } else {
            ObsidianDestroyer.debug("Loading config File.");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        String version = ObsidianDestroyer.getInstance().getDescription().getVersion();
        if (config != null && !config.getString("Version", version).equals(version)) {
            if (update) {
                ObsidianDestroyer.LOG.log(Level.SEVERE, "Loading failed on update check.  Aborting!");
                return;
            }
            ObsidianDestroyer.LOG.log(Level.WARNING, "Config File outdated, backing up old...");
            File configFileOld = new File(ObsidianDestroyer.getInstance().getDataFolder(), "config.yml.old-" + config.getString("Version", version).replace(".", "_"));
            try {
                config.save(configFileOld);
                ObsidianDestroyer.LOG.log(Level.WARNING, "Backed up old config as '" + configFileOld.getName() + "'");
            } catch (IOException e) {
                loaded = false;
                e.printStackTrace();
            }
            configFile.delete();
            loadFiles(true);
            return;
        } else if (config != null && "null".equals(config.getString("Version", "null"))) {
            loaded = false;
            return;
        }

        File materialsFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "materials.yml");
        if (!materialsFile.exists()) {
            ObsidianDestroyer.debug("Creating materials File.");
            createFile(materialsFile, "materials.yml");
        } else {
            ObsidianDestroyer.debug("Loading materials File.");
        }
        materials = YamlConfiguration.loadConfiguration(materialsFile);
        loaded = true;
    }

    /**
     * Create a file from a resource
     *
     * @param file     the file to be created
     * @param resource the resource to be used
     */
    private void createFile(File file, String resource) {
        file.getParentFile().mkdirs();

        InputStream inputStream = ObsidianDestroyer.getInstance().getResource(resource);

        if (inputStream == null) {
            ObsidianDestroyer.LOG.log(Level.SEVERE, "Missing resource file: ''{0}''", resource);
            return;
        }

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Gets verbose mode enabled
     *
     * @return verbose mode enabled
     */
    public boolean getVerbose() {
        return config.getBoolean("Verbose", config.getBoolean("verbose", false));
    }

    /**
     * Get debug mode enabled
     *
     * @return debug mode enabled
     */
    public boolean getDebug() {
        return config == null || config.getBoolean("Debug", config.getBoolean("Debug", config.getBoolean("debug", false)));
    }

    /**
     * Gets the list of disabled worlds from the config
     *
     * @return list of world names
     */
    public List<String> getDisabledWorlds() {
        return config.getStringList("DisabledOnWorlds");
    }

    /**
     * Translates the materials.yml from memory into a map for quick access
     *
     * @return map of materials keys and material durability data
     */
    public Map<String, DurabilityMaterial> getDurabilityMaterials() {
        ConfigurationSection section = materials.getConfigurationSection("HandledMaterials");
        Map<String, DurabilityMaterial> durabilityMaterials = new HashMap<String, DurabilityMaterial>();
        for (String durabilityMaterial : section.getKeys(false)) {
            try {
                ConfigurationSection materialSection = section.getConfigurationSection(durabilityMaterial);
                Material material = Material.matchMaterial(durabilityMaterial);
                if (material == null) {
                    if (Material.getMaterial(durabilityMaterial) == null) {
                        ObsidianDestroyer.LOG.log(Level.SEVERE, "Invalid Material Type: Unable to load ''{0}''", durabilityMaterial);
                        continue;
                    } else {
                        material = Material.getMaterial(durabilityMaterial);
                        ObsidianDestroyer.LOG.log(Level.SEVERE, "Semi-Valid Material Type: Loaded as ''{0}''", material.name());
                    }
                }
                if (!Util.isSolid(material)) {
                    ObsidianDestroyer.LOG.log(Level.WARNING, "Non-Solid Material Type: Did not load ''{0}''", durabilityMaterial);
                    continue;
                }
                final DurabilityMaterial durablock;
                if (materialSection.contains("MetaData")) {
                    durablock = new DurabilityMaterial(material, materialSection.getInt("MetaData"), materialSection);
                } else {
                    durablock = new DurabilityMaterial(material, materialSection);
                }
                if (durablock.getEnabled()) {
                    if (getVerbose() || getDebug()) {
                        ObsidianDestroyer.LOG.log(Level.INFO, "Loaded durability of ''{0}'' for ''{1}''", new Object[]{durablock.getDurability(), durablock.toString()});
                    }
                    durabilityMaterials.put(durablock.toString(), durablock);
                } else if (getDebug()) {
                    ObsidianDestroyer.debug("Disabled durability of '" + durablock.getDurability() + "' for '" + durablock.toString() + "'");
                }
            } catch (Exception e) {
                ObsidianDestroyer.LOG.log(Level.SEVERE, "Failed loading material ''{0}''", durabilityMaterial);
            }
        }
        return durabilityMaterials;
    }

    public int getRadius() {
        return config.getInt("Explosions.Radius", 3);
    }

    public boolean getMaterialsRegenerateOverTime() {
        return config.getBoolean("DurabilityGlobal.RegeneratesOverTime", false);
    }

    public boolean getFluidsProtectIndustructables() {
        return config.getBoolean("Explosions.FluidsProtectIndustructables", true);
    }

    public boolean getBypassAllFluidProtection() {
        return config.getBoolean("Explosions.BypassAllFluidProtection", false);
    }

    public boolean getProtectTNTCannons() {
        return config.getBoolean("Explosions.TNTCannonsProtected", true);
    }

    public boolean getEffectsEnabled() {
        return config.getBoolean("DurabilityGlobal.Effects.Enabled", true);
    }

    public double getEffectsChance() {
        double value = config.getDouble("DurabilityGlobal.Effects.Chance", 0.12);
        if (value > 0.6) {
            value = 0.6;
        }
        if (value <= 0) {
            value = 0.01;
        }
        return value;
    }

    public boolean getIgnoreUnhandledExplosionTypes() {
        return config.getBoolean("Explosions.IgnoreUnhandledTypes", false);
    }

    public Material getDurabilityCheckItem() {
        Material material = Material.matchMaterial(config.getString("DurabilityGlobal.CheckItem", "POTATO_ITEM"));
        if (material == null) {
            material = Material.POTATO_ITEM;
        }
        return material;
    }

    public boolean getHandleFactions() {
        return config.getBoolean("Factions.Enabled", false);
    }

    public boolean getHandleFactionsExplosions() {
        return config.getBoolean("Factions.HandleExplosions", true);
    }

    public boolean getUseFactionsPowerLevel() {
        return config.getBoolean("Factions.UseFactionPowerLevel", false);
    }

    public boolean getHandleOfflineFactions() {
        return config.getBoolean("Factions.HandleOffline", false);
    }

    public boolean getProtectOfflineFactions() {
        return config.getBoolean("Factions.ProtectOffline", false);
    }

    public boolean getUsingFactions() {
        return getHandleFactionsExplosions() && HookManager.getInstance().isUsingFactions();
    }

    public boolean getHandleOnlineFactions() {
        return config.getBoolean("Factions.HandleOnline", false);
    }

    public double getOfflineFactionsDurabilityMultiplier() {
        return config.getDouble("Factions.OfflineDurabilityMultiplier", 1.0);
    }

    public double getOnlineFactionsDurabilityMultiplier() {
        return config.getDouble("Factions.OnlineDurabilityMultiplier", 1.0);
    }

    public boolean getProtectBedrockBorders() {
        return config.getBoolean("WorldsEdge.ProtectBedrockBorders", true);
    }

    public int getBorderToProtectNormal() {
        return config.getInt("WorldsEdge.BorderToProtect.World", 5);
    }

    public int getBorderToProtectNether() {
        return config.getInt("WorldsEdge.BorderToProtect.Nether", 123);
    }

    public boolean getDisableDamageBleeding() {
        return config.getBoolean("Explosions.DisableDamageBleeding", true);
    }

    public double getNextLayerDamageChance() {
        return config.getDouble("Explosions.NextLevelDamageChance", 0.5);
    }
}
