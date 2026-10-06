package com.superiorslayerevent;

import java.util.List;

public class EventSnapshot
{
    private final String clanName;
    private final String eventName;
    private final boolean eventActive;
    private final int resetVersion;
    private final String eventEnd;
    private final List<LeaderboardEntry> leaderboard;

    public EventSnapshot(
            String clanName,
            String eventName,
            boolean eventActive,
            int resetVersion,
            String eventEnd,
            List<LeaderboardEntry> leaderboard)
    {
        this.clanName = clanName;
        this.eventName = eventName;
        this.eventActive = eventActive;
        this.resetVersion = resetVersion;
        this.eventEnd = eventEnd;
        this.leaderboard = leaderboard;
    }

    public String getClanName()
    {
        return clanName;
    }

    public String getEventName()
    {
        return eventName;
    }

    public boolean isEventActive()
    {
        return eventActive;
    }

    public int getResetVersion()
    {
        return resetVersion;
    }

    public String getEventEnd()
    {
        return eventEnd;
    }

    public List<LeaderboardEntry> getLeaderboard()
    {
        return leaderboard;
    }
}