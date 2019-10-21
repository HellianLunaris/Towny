package com.palmergames.bukkit.towny.war.siegewar;

import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.towny.utils.SiegeWarUtil;
import com.palmergames.bukkit.util.Colors;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.palmergames.bukkit.towny.utils.SiegeWarUtil.ONE_HOUR_IN_MILLIS;

/**
 * Created by Goosius on 07/05/2019.
 */
public class Siege {
    private Town defendingTown;
    private SiegeStatus status;
    private boolean townPlundered;
    private boolean townInvaded;
    private Nation attackerWinner;
    private long actualStartTime;
    private long scheduledEndTime;
    private long actualEndTime;
    private long nextUpkeepTime;
    private SiegeStats siegeStatsDefenders;
    private Map<Nation, SiegeStats> siegeStatsAttackers;

    public Siege(Town defendingTown) {
        this.defendingTown = defendingTown;
        status = SiegeStatus.IN_PROGRESS;
        this.attackerWinner = null;
    }

    public Town getDefendingTown() {
        return defendingTown;
    }

    public Map<Nation, SiegeStats> getSiegeStatsAttackers() {
        return siegeStatsAttackers;
    }

    public SiegeStats getSiegeStatsDefenders() {
        return siegeStatsDefenders;
    }


    public long getScheduledEndTime() {
        return scheduledEndTime;
    }

    public long getActualEndTime() {
        return actualEndTime;
    }


    public long getNextUpkeepTime() {
        return nextUpkeepTime;
    }

    public void setActualStartTime(long actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public void setScheduledEndTime(long scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public void setActualEndTime(long actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public void setNextUpkeepTime(long nextUpkeepTime) {
        this.nextUpkeepTime = nextUpkeepTime;
    }

    public long getActualStartTime() {
        return actualStartTime;
    }

    public void setSiegeStatsDefenders(SiegeStats defendersSiegeStats) {
        this.siegeStatsDefenders = defendersSiegeStats;
    }

    public void setSiegeStatsAttackers(Map<Nation, SiegeStats> siegeStatsAttackers) {
        this.siegeStatsAttackers = siegeStatsAttackers;
    }

    public List<Nation> getActiveAttackers() {
        List<Nation> result = new ArrayList<>();
        for (Nation nation : new ArrayList<Nation>(siegeStatsAttackers.keySet())) {
            if (siegeStatsAttackers.get(nation) != null
                    && siegeStatsAttackers.get(nation).isActive()) {
                result.add(nation);
            }
        }
        return result;
    }

    public void setStatus(SiegeStatus status) {
        this.status = status;
    }

    public void setTownPlundered(boolean townPlundered) {
        this.townPlundered = townPlundered;
    }

    public void setTownInvaded(boolean townInvaded) {
        this.townInvaded = townInvaded;
    }
    public void setAttackerWinner(Nation attackerWinner) {
        this.attackerWinner = attackerWinner;
    }

    public SiegeStatus getStatus() {
        return status;
    }

    public boolean isTownPlundered() {
        return townPlundered;
    }

    public boolean isTownInvaded() {
        return townInvaded;
    }
    public Nation getAttackerWinner() {
        return attackerWinner;
    }

    public boolean hasAttackerWinner() {
        return attackerWinner != null;
    }

    public double getTimeUntilCompletionMillis() {
        return scheduledEndTime - System.currentTimeMillis();
    }

    public String getFormattedHoursUntilCompletion() {
        if(status == SiegeStatus.IN_PROGRESS) {
            double timeUntilCompletionMillis = getTimeUntilCompletionMillis();
            return SiegeWarUtil.getFormattedTimeValue(timeUntilCompletionMillis);
        } else {
            return "n/a";
        }
    }

    public String getWinnerName() {
        switch(status) {
            case ATTACKER_WIN:
            case DEFENDER_SURRENDER:
                return TownyFormatter.getFormattedNationName(attackerWinner);
            case DEFENDER_WIN:
            case ATTACKER_ABANDON:
                if(defendingTown.hasNation()) {
                    try {
                        return TownyFormatter.getFormattedNationName(defendingTown.getNation());
                    } catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }
                } else {
                    return TownyFormatter.getFormattedTownName(defendingTown);
                }
            case IN_PROGRESS:
                return "n/a";
            default:
                return "Unknown siege status";
        }
    }

}