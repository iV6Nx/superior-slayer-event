package com.superiorslayerevent;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("superiorslayerevent")
public interface SuperiorSlayerEventConfig extends Config
{
    @ConfigSection(
            name = "Event Display",
            description = "Controls what is shown for the Superior Slayer event.",
            position = 0
    )
    String displaySection = "displaySection";

    @ConfigSection(
            name = "Clan Leaderboard",
            description = "Connect to your clan's shared Superior Slayer leaderboard.",
            position = 1
    )
    String leaderboardSection = "leaderboardSection";

    /*
     * ==================================================
     * EVENT DISPLAY
     * ==================================================
     */

    @ConfigItem(
            keyName = "showSidebar",
            name = "Show Event Sidebar",
            description = "Show the Superior Slayer Event sidebar.",
            position = 0,
            section = displaySection
    )
    default boolean showSidebar()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showKillBreakdown",
            name = "Show Kill Breakdown",
            description = "Show the Superior monster kill breakdown in the sidebar.",
            position = 1,
            section = displaySection
    )
    default boolean showKillBreakdown()
    {
        return true;
    }

    @ConfigItem(
            keyName = "onlyShowKilledMonsters",
            name = "Only Show Monsters With Kills",
            description = "Only show Superior monsters that you have killed.",
            position = 2,
            section = displaySection
    )
    default boolean onlyShowKilledMonsters()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showSessionStats",
            name = "Show Session Stats",
            description = "Show kills and points earned during the current RuneLite session.",
            position = 3,
            section = displaySection
    )
    default boolean showSessionStats()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showSpawnMessages",
            name = "Show Spawn Messages",
            description = "Show a chat message when a Superior Slayer monster spawns.",
            position = 4,
            section = displaySection
    )
    default boolean showSpawnMessages()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showKillMessages",
            name = "Show Kill Messages",
            description = "Show a chat message when a Superior Slayer monster is killed.",
            position = 5,
            section = displaySection
    )
    default boolean showKillMessages()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showPointsInChat",
            name = "Show Points in Chat",
            description = "Include event points in Superior kill messages.",
            position = 6,
            section = displaySection
    )
    default boolean showPointsInChat()
    {
        return true;
    }

    /*
     * ==================================================
     * CLAN LEADERBOARD
     * ==================================================
     */

    @ConfigItem(
            keyName = "enableLeaderboardSync",
            name = "Enable Clan Leaderboard",
            description =
                    "Share your RuneScape name, Slayer level, Superior kill total and event point total "
                            + "with the configured clan event leaderboard.",
            warning =
                    "This feature submits your IP address to a 3rd-party server not controlled or "
                            + "verified by RuneLite developers. It also sends your RuneScape name, Slayer "
                            + "level, Superior kill total and event point total to the configured clan "
                            + "leaderboard service.",
            position = 0,
            section = leaderboardSection
    )
    default boolean enableLeaderboardSync()
    {
        /*
         * Must remain opt-in because this feature
         * communicates with a third-party server.
         */
        return false;
    }

    @ConfigItem(
            keyName = "showClanLeaderboard",
            name = "Show Clan Leaderboard",
            description = "Show the shared clan leaderboard in the sidebar.",
            position = 1,
            section = leaderboardSection
    )
    default boolean showClanLeaderboard()
    {
        return true;
    }

    @ConfigItem(
            keyName = "clanEventUrl",
            name = "Clan Event URL",
            description = "Paste the Google Apps Script /exec URL supplied by your clan event organiser.",
            position = 2,
            section = leaderboardSection
    )
    default String clanEventUrl()
    {
        return "";
    }

    @ConfigItem(
            keyName = "clanEventKey",
            name = "Clan Event Key",
            description = "Paste the event key supplied by your clan event organiser.",
            secret = true,
            position = 3,
            section = leaderboardSection
    )
    default String clanEventKey()
    {
        return "";
    }
}