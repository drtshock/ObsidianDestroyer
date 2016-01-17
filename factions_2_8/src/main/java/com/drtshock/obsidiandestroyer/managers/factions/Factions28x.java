package com.drtshock.obsidiandestroyer.managers.factions;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Factions28x implements FactionsHook {

    @Override
    public boolean isFactionOffline(Location loc) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
        if ((faction.isNone()) ||
                ChatColor.stripColor(faction.getName()).equalsIgnoreCase("safezone") ||
                ChatColor.stripColor(faction.getName()).equalsIgnoreCase("warzone")) {
            //ObsidianDestroyer.debug("Factions25x.isFactionOffline: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions25x.isFactionOffline: " + faction.isFactionConsideredOffline());
        return faction.isFactionConsideredOffline() && faction.getFlag(MFlag.ID_OFFLINEEXPLOSIONS);
    }

    @Override
    public boolean isExplosionsEnabled(Location loc) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
        //ObsidianDestroyer.debug("Factions25x.isExplosionsEnabled: " + faction.getFlag(FFlag.EXPLOSIONS));
        return faction == null || faction.getFlag(MFlag.ID_EXPLOSIONS);
    }

    @Override
    public boolean isFactionAtPower(Location loc) {
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(loc));
        return faction.getPower() >= faction.getLandCount();
    }

    @Override
    public String getVersion() {
        return "2.8.X";
    }

}
