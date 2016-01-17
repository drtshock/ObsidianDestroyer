package com.drtshock.obsidiandestroyer.managers.factions;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Factions18x implements FactionsHook {

    @Override
    public boolean isFactionOffline(Location loc) {
        Faction faction = Board.getFactionAt(new FLocation(loc));
        if (faction.isNone()) {
            return false;
        } else if ((ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("wilderness") ||
                ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("safezone") ||
                ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("warzone"))) {
            //ObsidianDestroyer.debug("Factions18x.isFactionOffline: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions18x.isFactionOffline: " + faction.hasOfflineExplosionProtection());
        return faction.hasOfflineExplosionProtection();
    }

    @Override
    public boolean isExplosionsEnabled(Location loc) {
        Faction faction = Board.getFactionAt(new FLocation(loc));
        if (ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("safezone")
                || ChatColor.stripColor(faction.getTag()).equalsIgnoreCase("warzone")) {
            //ObsidianDestroyer.debug("Factions18x.isExplosionsEnabled: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions18x.isExplosionsEnabled: " + faction.getFlag(FFlag.EXPLOSIONS));
        return faction.getFlag(FFlag.EXPLOSIONS);
    }

    @Override
    public boolean isFactionAtPower(Location loc) {
        Faction faction = Board.getFactionAt(new FLocation(loc));
        return faction.getPower() >= faction.getLandRounded();
    }

    @Override
    public String getVersion() {
        return "1.8.X";
    }
}
