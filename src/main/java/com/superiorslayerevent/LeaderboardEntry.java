package com.superiorslayerevent;

public class LeaderboardEntry
{
    private final String player;
    private final int kills;
    private final int points;
    private final int slayerLevel;

    public LeaderboardEntry(
            String player,
            int kills,
            int points,
            int slayerLevel)
    {
        this.player = player;
        this.kills = kills;
        this.points = points;
        this.slayerLevel = slayerLevel;
    }

    public String getPlayer()
    {
        return player;
    }

    public int getKills()
    {
        return kills;
    }

    public int getPoints()
    {
        return points;
    }

    public int getSlayerLevel()
    {
        return slayerLevel;
    }
}