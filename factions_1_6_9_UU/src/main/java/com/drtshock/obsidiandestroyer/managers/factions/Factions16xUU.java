package com.drtshock.obsidiandestroyer.managers.factions;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;

public class Factions16xUU implements FactionsHook {

    @Override
    public boolean isFactionOffline(Location loc) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
        if (!faction.isNormal()) { // Checks if faction isn't safezone, warzone, or wilderness.
            //ObsidianDestroyer.debug("Factions16x.isFactionOffline: false");
            return false;
        }
        //ObsidianDestroyer.debug("Factions16x.isFactionOffline: " + (faction.getOnlinePlayers().size() == 0));
        return faction.getOnlinePlayers().size() == 0;
    }

    @Override
    public boolean isExplosionsEnabled(Location loc) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
        if (!faction.isNormal()) {
            return faction.getId().equalsIgnoreCase("0"); // Checks if faction is wilderness.
        }
        //ObsidianDestroyer.debug("Factions16x.isExplosionsEnabled: " + !faction.noExplosionsInTerritory());
        return !faction.noExplosionsInTerritory();
    }

    @Override
    public boolean isFactionAtPower(Location loc) {
        Faction faction = Board.getInstance().getFactionAt(new FLocation(loc));
        return faction.getPower() >= faction.getLandRounded();
    }

    @Override
    public String getVersion() {
        return "1.6.9-UU";
    }
}
