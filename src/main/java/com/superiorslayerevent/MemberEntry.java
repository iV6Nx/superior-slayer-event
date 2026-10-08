package com.superiorslayerevent;

public class MemberEntry
{
    private final String player;
    private final String lastSeen;
    private final boolean online;
    private final int slayerLevel;
    private final int eventVersion;
    private final String buyInStatus;
    private final boolean eligible;

    public MemberEntry(
            String player,
            String lastSeen,
            boolean online,
            int slayerLevel,
            int eventVersion,
            String buyInStatus,
            boolean eligible)
    {
        this.player = player;
        this.lastSeen = lastSeen;
        this.online = online;
        this.slayerLevel = slayerLevel;
        this.eventVersion = eventVersion;
        this.buyInStatus = buyInStatus;
        this.eligible = eligible;
    }

    public String getPlayer()
    {
        return player;
    }

    public String getLastSeen()
    {
        return lastSeen;
    }

    public boolean isOnline()
    {
        return online;
    }

    public int getSlayerLevel()
    {
        return slayerLevel;
    }

    public int getEventVersion()
    {
        return eventVersion;
    }

    public String getBuyInStatus()
    {
        return buyInStatus;
    }

    public boolean isEligible()
    {
        return eligible;
    }
}