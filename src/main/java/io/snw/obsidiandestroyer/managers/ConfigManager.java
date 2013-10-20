package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.DurabilityMaterial;
import io.snw.obsidiandestroyer.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
    private static ConfigManager instance;
    private YamlConfiguration config;
    private YamlConfiguration materials;
    private static YamlConfiguration tc;
    private static YamlConfiguration tm;
    private boolean loaded;

    public ConfigManager(boolean isLoading) {
        instance = this;
        if (!isLoading) {
            try {
                loadFile(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void backup(boolean apply) {
        if (!apply) {
            tc = config;
            tm = materials;
        } else {
            config = tc;
            materials = tm;
        }
    }

    private void loadFile(boolean update) {
        loaded = true;
        File folder = ObsidianDestroyer.getInstance().getDataFolder();
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        File configFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            ObsidianDestroyer.LOG.info("Creating config File...");
            createFile(configFile, "config.yml");
        } else {
            ObsidianDestroyer.LOG.info("Loading config File...");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.getString("Version", "").equals(ObsidianDestroyer.getInstance().getDescription().getVersion())) {
            if (update) {
                ObsidianDestroyer.LOG.log(Level.WARNING, "Loading failed on update check.  Aborting...");
                return;
            }
            ObsidianDestroyer.LOG.info("Config File outdated, backing up old...");
            File configFileOld = new File(ObsidianDestroyer.getInstance().getDataFolder(), "config.yml.old");
            try {
                config.save(configFileOld);
            } catch (IOException e) {
                loaded = false;
                e.printStackTrace();
            }
            configFile.delete();
            loadFile(true);
        }

        File structuresFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "materials.yml");
        if (!structuresFile.exists()) {
            ObsidianDestroyer.LOG.info("Creating materials File...");
            createFile(structuresFile, "materials.yml");
        } else {
            ObsidianDestroyer.LOG.info("Loading materials File...");
        }
        materials = YamlConfiguration.loadConfiguration(structuresFile);
    }

    private void createFile(File file, String resource) {
        file.getParentFile().mkdirs();

        InputStream inputStream = ObsidianDestroyer.getInstance().getResource(resource);

        if (inputStream == null) {
            ObsidianDestroyer.LOG.severe("Missing resource file: '" + resource + "'");
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

    public static ConfigManager getInstance() {
        return instance;
    }

    public List<String> getDisabledWorlds() {
        return (ArrayList<String>) config.getStringList("DisabledOnWorlds");
    }

    public Map<String, DurabilityMaterial> getDurabilityMaterials() {
        ConfigurationSection section = materials.getConfigurationSection("HandledMaterials");
        Map<String, DurabilityMaterial> durabilityMaterials = new HashMap<String, DurabilityMaterial>();
        for (String durabilityMaterial : section.getKeys(false)) {
            try {
                ConfigurationSection materialSection = section.getConfigurationSection(durabilityMaterial);
                Material material = Material.getMaterial(durabilityMaterial);
                if (material == null) {
                    ObsidianDestroyer.LOG.log(Level.SEVERE, "Invalid Material Type: Unable to load '" + durabilityMaterial + "'");
                    continue;
                }
                if (!Util.isSolid(material)) {
                    ObsidianDestroyer.LOG.log(Level.WARNING, "Non-Solid Material Type: Did not load '" + durabilityMaterial + "'");
                    continue;
                }
                DurabilityMaterial durablock = new DurabilityMaterial(material, materialSection);
                if (durablock.getEnabled()) {
                    ObsidianDestroyer.LOG.info("Loaded Durability monitor for '" + durabilityMaterial + "'");
                    durabilityMaterials.put(material.name(), durablock);
                }
            } catch (Exception e) {
                ObsidianDestroyer.LOG.log(Level.SEVERE, "Failed loading material '" + durabilityMaterial + "'");
            }
        }
        return durabilityMaterials;
    }

    public int getRadius() {
        return config.getInt("Radius", 3);
    }

    public boolean getMaterialsRegenerateOverTime() {
        return config.getBoolean("DurabilityRegeneratesOverTime", false);
    }

    public boolean getExplodeInLiquids() {
        return config.getBoolean("FluidsProtectIndustructables", true);
    }

    public boolean getWaterProtection() {
        return config.getBoolean("Explosions.BypassAllFluidProtection", false);
    }

    public boolean getProtectTNTCannons() {
        return config.getBoolean("Explosions.TNTCannonsProtected", true);
    }

    public boolean getIgnoreCancel() {
        return config.getBoolean("IgnoreCancel", false);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean getCheckUpdate() {
        return config.getBoolean("checkupdate");
    }

    public boolean getDownloadUpdate() {
        return config.getBoolean("downloadupdate");
    }

    public String getObsidianDurability() {
        return config.getString("HandledMaterials.OBSIDIAN.Durability.Amount", "N/A");
    }

    public boolean getEffectsEnabled() {
        return config.getBoolean("Effects.Enabled", true);
    }

    public double getEffectsChance() {
        double value = config.getDouble("Effects.Chance", 0.12);
        if (value > 0.6) {
            value = 0.6;
        }
        if (value <= 0) {
            value = 0.01;
        }
        return value;
    }
}
