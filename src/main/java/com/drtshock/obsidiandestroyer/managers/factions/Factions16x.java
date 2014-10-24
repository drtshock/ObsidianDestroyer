package com.drtshock.obsidiandestroyer.managers.factions;

import com.drtshock.obsidiandestroyer.managers.ConfigManager;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Factions16x implements FactionsHook {

    @Override
    public boolean isFactionOffline(Location loc) {
        if (ConfigManager.getInstance().getUsingFactions()) {
            Faction faction = Board.getFactionAt(new FLocation(loc));
            if (ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("wilderness") ||
                    ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("safezone") ||
                    ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("warzone")) {
                //ObsidianDestroyer.debug("Factions16x.isFactionOffline: false");
                return false;
            }
            //ObsidianDestroyer.debug("Factions16x.isFactionOffline: " + (faction.getOnlinePlayers().size() == 0));
            return faction.getOnlinePlayers().size() == 0;
        } else {
            return false;
        }
    }

    @Override
    public boolean isExplosionsEnabled(Location loc) {
        if (!ConfigManager.getInstance().getUsingFactions()) {
            return true;
        }
        Faction faction = Board.getFactionAt(new FLocation(loc));
        if (ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("wilderness")) {
            //ObsidianDestroyer.debug("Factions16x.isExplosionsEnabled: true");
            return true;
        }
        if (/*Conf.safeZoneBlockTNT && */ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("safezone")) {
            //ObsidianDestroyer.debug("Factions16x.isExplosionsEnabled: false");
            return false;
        }
        if (/*Conf.warZoneBlockTNT && */ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("warzone")) {
            //ObsidianDestroyer.debug("Factions16x.isExplosionsEnabled: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions16x.isExplosionsEnabled: " + !faction.noExplosionsInTerritory());
        return !faction.noExplosionsInTerritory();
    }

    @Override
    public String getVersion() {
        return "1.6.9.X";
    }
}
