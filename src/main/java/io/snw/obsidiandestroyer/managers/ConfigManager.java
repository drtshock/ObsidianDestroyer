package io.snw.obsidiandestroyer.managers;

import io.snw.obsidiandestroyer.ObsidianDestroyer;
import io.snw.obsidiandestroyer.datatypes.DurabilityBlock;

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

    public ConfigManager() {
        loadFile();
        instance = this;
    }

    private void loadFile() {
        File folder = ObsidianDestroyer.getInstance().getDataFolder();
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        File configFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            ObsidianDestroyer.LOG.info("Creating config File...");
            createFile(configFile, "config.yml");
        }
        else {
            ObsidianDestroyer.LOG.info("Loading config File...");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        File structuresFile = new File(ObsidianDestroyer.getInstance().getDataFolder(), "materials.yml");
        if (!structuresFile.exists()) {
            ObsidianDestroyer.LOG.info("Creating materials File...");
            createFile(structuresFile, "materials.yml");
        }
        else {
            ObsidianDestroyer.LOG.info("Loading materials File...");
        }
        materials = YamlConfiguration.loadConfiguration(structuresFile);
    }

    protected void createFile(File file, String resource) {
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

    public int getRadius() {
        return config.getInt("Radius");
    }

    public Map<String, DurabilityBlock> getDurabilityBlocks() {
        ConfigurationSection section = materials.getConfigurationSection("HandledMaterials");
        Map<String, DurabilityBlock> durabilityBlocks = new HashMap<String, DurabilityBlock>();
        for (String durabilityMaterial : section.getKeys(false)) {
            ConfigurationSection s = section.getConfigurationSection(durabilityMaterial);
            ObsidianDestroyer.LOG.info("Apply Durability to " + durabilityMaterial);
            Material material = Material.getMaterial(durabilityMaterial);
            if (material == null) {
                ObsidianDestroyer.LOG.log(Level.SEVERE, "Unable to load material from config: " + durabilityMaterial);
                continue;
            }
            // derp constructor!
            DurabilityBlock durablock = new DurabilityBlock(material,
                    s.getInt("Durability.Amount"), 
                    s.getBoolean("Durability.Enabled"),
                    s.getDouble("ChanceToDrop"),
                    s.getBoolean("Durability.ResetEnabled", false),
                    s.getLong("Durability.ResetAfter", 10000L),
                    s.getBoolean("EnabledFor.TNT", true),
                    s.getBoolean("EnabledFor.Cannons", false),
                    s.getBoolean("EnabledFor.Creepers", false),
                    s.getBoolean("EnabledFor.Ghasts", false),
                    s.getBoolean("EnabledFor.Minecarts", false),
                    s.getBoolean("EnabledFor.Withers", false));

            durabilityBlocks.put(material.name(), durablock);
        }

        // Clear the blocks list then add all from config file
        return durabilityBlocks;
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

    public List<String> getDisabledWorlds() {
        return (ArrayList<String>) config.getStringList("DisabledOnWorlds");
    }
}
