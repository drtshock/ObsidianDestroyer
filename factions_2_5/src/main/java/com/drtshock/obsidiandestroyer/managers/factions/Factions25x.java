package com.drtshock.obsidiandestroyer.managers.factions;

import com.massivecraft.factions.FFlag;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Factions25x implements FactionsHook {

    @Override
    public boolean isFactionOffline(Location loc) {
        Faction faction = BoardColls.get().getFactionAt(PS.valueOf(loc));
        if (faction.isNone() ||
                ChatColor.stripColor(faction.getName()).equalsIgnoreCase("safezone") ||
                ChatColor.stripColor(faction.getName()).equalsIgnoreCase("warzone")) {
            //ObsidianDestroyer.debug("Factions25x.isFactionOffline: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions25x.isFactionOffline: " + faction.isFactionConsideredOffline());
        return faction.isFactionConsideredOffline() && faction.getFlag(FFlag.OFFLINE_EXPLOSIONS);
    }

    @Override
    public boolean isExplosionsEnabled(Location loc) {
        Faction faction = BoardColls.get().getFactionAt(PS.valueOf(loc));
        //ObsidianDestroyer.debug("Factions25x.isExplosionsEnabled: " + faction.getFlag(FFlag.EXPLOSIONS));
        return faction.getFlag(FFlag.EXPLOSIONS);
    }

    @Override
    public boolean isFactionAtPower(Location loc) {
        Faction faction = BoardColls.get().getFactionAt(PS.valueOf(loc));
        return faction.getPower() >= faction.getLandCount();
    }

    @Override
    public String getVersion() {
        return "2.5.X";
    }

}
