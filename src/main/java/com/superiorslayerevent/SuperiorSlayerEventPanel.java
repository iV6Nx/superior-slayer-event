package com.superiorslayerevent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class SuperiorSlayerEventPanel extends PluginPanel
{
    private static final Color GOLD =
            new Color(255, 200, 70);

    private static final Color SILVER =
            new Color(200, 200, 200);

    private static final Color BRONZE =
            new Color(205, 135, 80);

    private static final Color GREEN =
            new Color(80, 220, 120);

    private static final Color RED =
            new Color(230, 80, 80);

    private static final int CONTENT_WIDTH = 236;
    private static final int BOARD_WIDTH = 210;

    private static final int STAT_ROW_WIDTH = 218;
    private static final int STAT_ROW_HEIGHT = 72;

    private static final DateTimeFormatter EVENT_END_FORMAT =
            DateTimeFormatter
                    .ofPattern(
                            "dd MMM yyyy 'at' HH:mm 'UTC'"
                    )
                    .withZone(
                            ZoneOffset.UTC
                    );

    private final SuperiorSlayerEventPlugin plugin;
    private final SuperiorSlayerEventConfig config;

    /*
     * ==================================================
     * HEADER
     * ==================================================
     */

    private final JLabel eventNameLabel =
            new JLabel(
                    "SUPERIOR SLAYER EVENT",
                    SwingConstants.CENTER
            );

    private final JLabel clanNameLabel =
            new JLabel(
                    "TNS Inc",
                    SwingConstants.CENTER
            );

    private final JLabel eventStatusLabel =
            new JLabel(
                    "● EVENT ACTIVE",
                    SwingConstants.CENTER
            );

    /*
     * ==================================================
     * EVENT COUNTDOWN
     * ==================================================
     */

    private final JLabel countdownTitleLabel =
            new JLabel(
                    "EVENT ENDS IN",
                    SwingConstants.CENTER
            );

    private final JLabel countdownValueLabel =
            new JLabel(
                    "--",
                    SwingConstants.CENTER
            );

    private final JLabel eventEndTimeLabel =
            new JLabel(
                    "Ends --",
                    SwingConstants.CENTER
            );

    private Timer countdownTimer;

    /*
     * ==================================================
     * PERSONAL STATUS
     * ==================================================
     */

    private final JLabel connectionStatusValue =
            new JLabel(
                    "● NOT CONNECTED",
                    SwingConstants.LEFT
            );

    private final JLabel personalRankValue =
            new JLabel(
                    "Rank: --",
                    SwingConstants.LEFT
            );

    private final JLabel personalBoardValue =
            new JLabel(
                    "Board: --",
                    SwingConstants.LEFT
            );

    private final JLabel nextRankValue =
            new JLabel(
                    "Next rank: --",
                    SwingConstants.LEFT
            );

    /*
     * ==================================================
     * FINAL EVENT RESULTS
     * ==================================================
     */

    private final JPanel finalResultsSection =
            new JPanel();

    private final JLabel finalYourResultValue =
            new JLabel(
                    "--",
                    SwingConstants.LEFT
            );

    private final JLabel finalYourBoardValue =
            new JLabel(
                    "--",
                    SwingConstants.LEFT
            );

    private final JLabel finalWinnerMidValue =
            new JLabel(
                    "70-89: --",
                    SwingConstants.LEFT
            );

    private final JLabel finalWinnerHighValue =
            new JLabel(
                    "90-99: --",
                    SwingConstants.LEFT
            );

    /*
     * ==================================================
     * STATS
     * ==================================================
     */

    private final JLabel totalKillsValue =
            new JLabel(
                    "0",
                    SwingConstants.CENTER
            );

    private final JLabel totalPointsValue =
            new JLabel(
                    "0",
                    SwingConstants.CENTER
            );

    private final JLabel sessionKillsValue =
            new JLabel(
                    "0",
                    SwingConstants.CENTER
            );

    private final JLabel sessionPointsValue =
            new JLabel(
                    "0",
                    SwingConstants.CENTER
            );

    /*
     * ==================================================
     * SYNC
     * ==================================================
     */

    private final JLabel membersSyncLabel =
            new JLabel(
                    "Last synced: Never",
                    SwingConstants.CENTER
            );

    private final JLabel boardsSyncLabel =
            new JLabel(
                    "Last synced: Never",
                    SwingConstants.CENTER
            );

    /*
     * ==================================================
     * PANELS
     * ==================================================
     */

    private final JPanel clanMembersPanel =
            new JPanel();

    private final JPanel breakdownPanel =
            new JPanel();

    private final JPanel highBoardPanel =
            new JPanel();

    private final JPanel midBoardPanel =
            new JPanel();

    private final JPanel sessionSection =
            new JPanel();

    /*
     * ==================================================
     * CONSTRUCTOR
     * ==================================================
     */

    public SuperiorSlayerEventPanel(
            SuperiorSlayerEventPlugin plugin,
            SuperiorSlayerEventConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setLayout(
                new BorderLayout()
        );

        setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        JTabbedPane tabs =
                new JTabbedPane();

        tabs.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        tabs.addTab(
                "Event",
                createEventTab()
        );

        tabs.addTab(
                "Monsters",
                createMonstersTab()
        );

        tabs.addTab(
                "Boards",
                createBoardsTab()
        );

        add(
                tabs,
                BorderLayout.CENTER
        );

        startCountdownTimer();

        refresh();
    }

    /*
     * ==================================================
     * EVENT TAB
     * ==================================================
     */

    private JPanel createEventTab()
    {
        JPanel content =
                createPage();

        eventNameLabel.setForeground(
                GOLD
        );

        eventNameLabel.setFont(
                eventNameLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                14f
                        )
        );

        eventNameLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        eventNameLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        22
                )
        );

        content.add(
                eventNameLabel
        );

        content.add(
                Box.createVerticalStrut(3)
        );

        clanNameLabel.setForeground(
                GOLD
        );

        clanNameLabel.setFont(
                clanNameLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                12f
                        )
        );

        clanNameLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        clanNameLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        content.add(
                clanNameLabel
        );

        content.add(
                Box.createVerticalStrut(3)
        );

        JLabel tracker =
                new JLabel(
                        "Clan Event Tracker",
                        SwingConstants.CENTER
                );

        tracker.setForeground(
                Color.LIGHT_GRAY
        );

        tracker.setAlignmentX(
                CENTER_ALIGNMENT
        );

        tracker.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        content.add(
                tracker
        );

        content.add(
                Box.createVerticalStrut(6)
        );

        eventStatusLabel.setFont(
                eventStatusLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                10f
                        )
        );

        eventStatusLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        eventStatusLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        18
                )
        );

        content.add(
                eventStatusLabel
        );

        /*
         * ==================================================
         * EVENT COUNTDOWN
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(8)
        );

        countdownTitleLabel.setForeground(
                GOLD
        );

        countdownTitleLabel.setFont(
                countdownTitleLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                10f
                        )
        );

        countdownTitleLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        countdownTitleLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        18
                )
        );

        content.add(
                countdownTitleLabel
        );

        content.add(
                Box.createVerticalStrut(2)
        );

        countdownValueLabel.setForeground(
                GOLD
        );

        countdownValueLabel.setFont(
                countdownValueLabel
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                12f
                        )
        );

        countdownValueLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        countdownValueLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        content.add(
                countdownValueLabel
        );

        content.add(
                Box.createVerticalStrut(2)
        );

        eventEndTimeLabel.setForeground(
                Color.LIGHT_GRAY
        );

        eventEndTimeLabel.setFont(
                eventEndTimeLabel
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                9f
                        )
        );

        eventEndTimeLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        eventEndTimeLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        18
                )
        );

        content.add(
                eventEndTimeLabel
        );

        /*
         * ==================================================
         * YOUR EVENT STATUS
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(10)
        );

        content.add(
                createSectionTitle(
                        "YOUR EVENT STATUS"
                )
        );

        content.add(
                Box.createVerticalStrut(7)
        );

        content.add(
                createPersonalStatusCard()
        );

        /*
         * ==================================================
         * FINAL EVENT RESULTS
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(12)
        );

        setupFinalResultsSection();

        content.add(
                finalResultsSection
        );

        /*
         * ==================================================
         * TOTAL STATS
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(15)
        );

        content.add(
                createStatsRow(
                        createStatBox(
                                "TOTAL KILLS",
                                totalKillsValue
                        ),
                        createStatBox(
                                "TOTAL POINTS",
                                totalPointsValue
                        )
                )
        );

        /*
         * ==================================================
         * CURRENT SESSION
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(16)
        );

        sessionSection.setLayout(
                new BoxLayout(
                        sessionSection,
                        BoxLayout.Y_AXIS
                )
        );

        sessionSection.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        sessionSection.setAlignmentX(
                CENTER_ALIGNMENT
        );

        sessionSection.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        105
                )
        );

        sessionSection.add(
                createSectionTitle(
                        "CURRENT SESSION"
                )
        );

        sessionSection.add(
                Box.createVerticalStrut(8)
        );

        sessionSection.add(
                createStatsRow(
                        createStatBox(
                                "KILLS",
                                sessionKillsValue
                        ),
                        createStatBox(
                                "POINTS",
                                sessionPointsValue
                        )
                )
        );

        content.add(
                sessionSection
        );

        /*
         * ==================================================
         * CLAN MEMBERS
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(18)
        );

        content.add(
                createSectionTitle(
                        "CLAN MEMBERS"
                )
        );

        content.add(
                Box.createVerticalStrut(8)
        );

        clanMembersPanel.setLayout(
                new BoxLayout(
                        clanMembersPanel,
                        BoxLayout.Y_AXIS
                )
        );

        clanMembersPanel.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        clanMembersPanel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        clanMembersPanel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        500
                )
        );

        content.add(
                clanMembersPanel
        );

        content.add(
                Box.createVerticalStrut(7)
        );

        membersSyncLabel.setForeground(
                Color.LIGHT_GRAY
        );

        membersSyncLabel.setFont(
                membersSyncLabel
                        .getFont()
                        .deriveFont(
                                9f
                        )
        );

        membersSyncLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        membersSyncLabel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        18
                )
        );

        content.add(
                membersSyncLabel
        );

        content.add(
                Box.createVerticalStrut(5)
        );

        JButton refreshMembers =
                new JButton(
                        "Refresh Members"
                );

        refreshMembers.setAlignmentX(
                CENTER_ALIGNMENT
        );

        refreshMembers.addActionListener(
                e ->
                {
                    membersSyncLabel.setText(
                            "Last synced: Syncing..."
                    );

                    plugin.refreshLeaderboard();
                }
        );

        content.add(
                refreshMembers
        );

        /*
         * ==================================================
         * SUPERIOR BREAKDOWN
         * ==================================================
         */

        content.add(
                Box.createVerticalStrut(18)
        );

        content.add(
                createSectionTitle(
                        "SUPERIOR BREAKDOWN"
                )
        );

        content.add(
                Box.createVerticalStrut(8)
        );

        breakdownPanel.setLayout(
                new BoxLayout(
                        breakdownPanel,
                        BoxLayout.Y_AXIS
                )
        );

        breakdownPanel.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        breakdownPanel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        breakdownPanel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        Integer.MAX_VALUE
                )
        );

        content.add(
                breakdownPanel
        );

        return wrapPage(
                content
        );
    }

    /*
     * ==================================================
     * FINAL RESULTS SETUP
     * ==================================================
     */

    private void setupFinalResultsSection()
    {
        finalResultsSection.removeAll();

        finalResultsSection.setLayout(
                new BorderLayout()
        );

        finalResultsSection.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        finalResultsSection.setAlignmentX(
                CENTER_ALIGNMENT
        );

        finalResultsSection.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        178
                )
        );

        finalResultsSection.setMinimumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        178
                )
        );

        finalResultsSection.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        178
                )
        );

        JPanel card =
                new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ColorScheme.MEDIUM_GRAY_COLOR,
                                1
                        ),
                        BorderFactory.createEmptyBorder(
                                10,
                                12,
                                10,
                                12
                        )
                )
        );

        card.setPreferredSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        178
                )
        );

        card.setMinimumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        178
                )
        );

        card.setMaximumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        178
                )
        );

        JLabel title =
                new JLabel(
                        "FINAL EVENT RESULTS"
                );

        title.setForeground(
                GOLD
        );

        title.setFont(
                title.getFont()
                        .deriveFont(
                                Font.BOLD,
                                11f
                        )
        );

        title.setAlignmentX(
                LEFT_ALIGNMENT
        );

        JLabel yourResultTitle =
                new JLabel(
                        "YOUR RESULT"
                );

        yourResultTitle.setForeground(
                Color.LIGHT_GRAY
        );

        yourResultTitle.setFont(
                yourResultTitle
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                9f
                        )
        );

        yourResultTitle.setAlignmentX(
                LEFT_ALIGNMENT
        );

        finalYourResultValue.setForeground(
                Color.WHITE
        );

        finalYourResultValue.setFont(
                finalYourResultValue
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                10f
                        )
        );

        finalYourResultValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        finalYourBoardValue.setForeground(
                GOLD
        );

        finalYourBoardValue.setFont(
                finalYourBoardValue
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                9f
                        )
        );

        finalYourBoardValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        JLabel winnersTitle =
                new JLabel(
                        "BOARD WINNERS"
                );

        winnersTitle.setForeground(
                Color.LIGHT_GRAY
        );

        winnersTitle.setFont(
                winnersTitle
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                9f
                        )
        );

        winnersTitle.setAlignmentX(
                LEFT_ALIGNMENT
        );

        finalWinnerMidValue.setForeground(
                GOLD
        );

        finalWinnerMidValue.setFont(
                finalWinnerMidValue
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                9f
                        )
        );

        finalWinnerMidValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        finalWinnerHighValue.setForeground(
                GOLD
        );

        finalWinnerHighValue.setFont(
                finalWinnerHighValue
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                9f
                        )
        );

        finalWinnerHighValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        card.add(
                title
        );

        card.add(
                Box.createVerticalStrut(10)
        );

        card.add(
                yourResultTitle
        );

        card.add(
                Box.createVerticalStrut(4)
        );

        card.add(
                finalYourResultValue
        );

        card.add(
                Box.createVerticalStrut(3)
        );

        card.add(
                finalYourBoardValue
        );

        card.add(
                Box.createVerticalStrut(12)
        );

        card.add(
                winnersTitle
        );

        card.add(
                Box.createVerticalStrut(5)
        );

        card.add(
                finalWinnerMidValue
        );

        card.add(
                Box.createVerticalStrut(4)
        );

        card.add(
                finalWinnerHighValue
        );

        finalResultsSection.add(
                card,
                BorderLayout.WEST
        );

        finalResultsSection.setVisible(
                false
        );
    }

    /*
     * ==================================================
     * MONSTERS TAB
     * ==================================================
     */

    private JPanel createMonstersTab()
    {
        JPanel content =
                createPage();

        content.add(
                createSectionTitle(
                        "SLAYER MONSTERS & POINTS"
                )
        );

        content.add(
                Box.createVerticalStrut(4)
        );

        JLabel subtitle =
                new JLabel(
                        "Sorted by Slayer level",
                        SwingConstants.CENTER
                );

        subtitle.setForeground(
                Color.LIGHT_GRAY
        );

        subtitle.setAlignmentX(
                CENTER_ALIGNMENT
        );

        subtitle.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        content.add(
                subtitle
        );

        content.add(
                Box.createVerticalStrut(10)
        );

        for (
                SuperiorMonster monster :
                SuperiorMonster.values()
        )
        {
            content.add(
                    createMonsterCard(
                            monster
                    )
            );

            content.add(
                    Box.createVerticalStrut(5)
            );
        }

        return wrapPage(
                content
        );
    }

    /*
     * ==================================================
     * BOARDS TAB
     * ==================================================
     */

    private JPanel createBoardsTab()
    {
        JPanel content =
                createPage();

        content.add(
                createSectionTitle(
                        "CLAN LEADERBOARDS"
                )
        );

        content.add(
                Box.createVerticalStrut(5)
        );

        JLabel subtitle =
                new JLabel(
                        "Ranked by event points",
                        SwingConstants.CENTER
                );

        subtitle.setForeground(
                Color.LIGHT_GRAY
        );

        subtitle.setAlignmentX(
                CENTER_ALIGNMENT
        );

        subtitle.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        content.add(
                subtitle
        );

        content.add(
                Box.createVerticalStrut(16)
        );

        content.add(
                createSectionTitle(
                        "SLAYER 90 - 99"
                )
        );

        content.add(
                Box.createVerticalStrut(8)
        );

        setupLeaderboardPanel(
                highBoardPanel
        );

        content.add(
                highBoardPanel
        );

        content.add(
                Box.createVerticalStrut(18)
        );

        content.add(
                createSectionTitle(
                        "SLAYER 70 - 89"
                )
        );

        content.add(
                Box.createVerticalStrut(8)
        );

        setupLeaderboardPanel(
                midBoardPanel
        );

        content.add(
                midBoardPanel
        );

        content.add(
                Box.createVerticalStrut(16)
        );

        boardsSyncLabel.setForeground(
                Color.LIGHT_GRAY
        );

        boardsSyncLabel.setFont(
                boardsSyncLabel
                        .getFont()
                        .deriveFont(
                                9f
                        )
        );

        boardsSyncLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        content.add(
                boardsSyncLabel
        );

        content.add(
                Box.createVerticalStrut(6)
        );

        JButton refreshBoards =
                new JButton(
                        "Refresh Leaderboards"
                );

        refreshBoards.setAlignmentX(
                CENTER_ALIGNMENT
        );

        refreshBoards.addActionListener(
                e ->
                {
                    boardsSyncLabel.setText(
                            "Last synced: Syncing..."
                    );

                    plugin.refreshLeaderboard();
                }
        );

        content.add(
                refreshBoards
        );

        return wrapPage(
                content
        );
    }

    /*
     * ==================================================
     * LEADERBOARD PANEL
     * ==================================================
     */

    private void setupLeaderboardPanel(
            JPanel panel)
    {
        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        panel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        panel.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        30
                )
        );

        panel.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        Integer.MAX_VALUE
                )
        );
    }

    /*
     * ==================================================
     * REFRESH
     * ==================================================
     */

    public void refresh()
    {
        refreshHeader();
        refreshCountdown();
        refreshPersonalStatus();
        refreshFinalResults();

        totalKillsValue.setText(
                String.valueOf(
                        plugin.getTotalSuperiorKills()
                )
        );

        totalPointsValue.setText(
                String.valueOf(
                        plugin.getTotalEventPoints()
                )
        );

        sessionKillsValue.setText(
                String.valueOf(
                        plugin.getSessionSuperiorKills()
                )
        );

        sessionPointsValue.setText(
                String.valueOf(
                        plugin.getSessionEventPoints()
                )
        );

        sessionSection.setVisible(
                config.showSessionStats()
        );

        String sync =
                plugin.getLastLeaderboardSync();

        membersSyncLabel.setText(
                "Last synced: " + sync
        );

        boardsSyncLabel.setText(
                "Last synced: " + sync
        );

        refreshClanMembers();
        refreshBreakdown();
        refreshLeaderboards();

        revalidate();
        repaint();
    }

    /*
     * ==================================================
     * HEADER
     * ==================================================
     */

    private void refreshHeader()
    {
        String eventName =
                plugin.getEventName();

        if (
                eventName != null
                        && !eventName.trim().isEmpty()
        )
        {
            eventNameLabel.setText(
                    eventName.toUpperCase()
            );
        }
        else
        {
            eventNameLabel.setText(
                    "SUPERIOR SLAYER EVENT"
            );
        }

        String clan =
                plugin.getClanName();

        if (
                clan != null
                        && !clan.trim().isEmpty()
        )
        {
            clanNameLabel.setText(
                    clan
            );
        }
        else
        {
            clanNameLabel.setText(
                    "Connect to Clan Event"
            );
        }

        if (!plugin.isLeaderboardSyncEnabled())
        {
            eventStatusLabel.setText(
                    "● NOT CONNECTED"
            );

            eventStatusLabel.setForeground(
                    Color.LIGHT_GRAY
            );
        }
        else if (plugin.isEventActive())
        {
            eventStatusLabel.setText(
                    "● EVENT ACTIVE"
            );

            eventStatusLabel.setForeground(
                    GREEN
            );
        }
        else
        {
            eventStatusLabel.setText(
                    "● EVENT ENDED"
            );

            eventStatusLabel.setForeground(
                    RED
            );
        }
    }

    /*
     * ==================================================
     * COUNTDOWN TIMER
     * ==================================================
     */

    private void startCountdownTimer()
    {
        if (countdownTimer != null)
        {
            countdownTimer.stop();
        }

        countdownTimer =
                new Timer(
                        30000,
                        e ->
                        {
                            refreshCountdown();

                            revalidate();
                            repaint();
                        }
                );

        countdownTimer.setRepeats(
                true
        );

        countdownTimer.start();
    }

    public void stopCountdownTimer()
    {
        if (countdownTimer != null)
        {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    /*
     * ==================================================
     * COUNTDOWN
     * ==================================================
     */

    private void refreshCountdown()
    {
        String eventEnd =
                plugin.getEventEnd();

        /*
         * ==================================================
         * EVENT ACTIVE = FALSE
         *
         * This is the master switch.
         * ==================================================
         */

        if (!plugin.isEventActive())
        {
            countdownTitleLabel.setText(
                    "EVENT"
            );

            countdownValueLabel.setText(
                    "ENDED"
            );

            countdownValueLabel.setForeground(
                    RED
            );

            if (
                    eventEnd != null
                            && !eventEnd.trim().isEmpty()
            )
            {
                try
                {
                    Instant endTime =
                            Instant.parse(
                                    eventEnd.trim()
                            );

                    String formattedEnd =
                            EVENT_END_FORMAT.format(
                                    endTime
                            );

                    eventEndTimeLabel.setText(
                            "Ended "
                                    + formattedEnd
                    );

                    eventEndTimeLabel.setForeground(
                            Color.LIGHT_GRAY
                    );
                }
                catch (DateTimeParseException exception)
                {
                    eventEndTimeLabel.setText(
                            "Event closed manually"
                    );

                    eventEndTimeLabel.setForeground(
                            Color.LIGHT_GRAY
                    );
                }
            }
            else
            {
                eventEndTimeLabel.setText(
                        "Event closed manually"
                );

                eventEndTimeLabel.setForeground(
                        Color.LIGHT_GRAY
                );
            }

            return;
        }

        /*
         * ==================================================
         * NO END DATE
         * ==================================================
         */

        if (
                eventEnd == null
                        || eventEnd.trim().isEmpty()
        )
        {
            countdownTitleLabel.setText(
                    "EVENT ENDS IN"
            );

            countdownValueLabel.setText(
                    "--"
            );

            countdownValueLabel.setForeground(
                    Color.LIGHT_GRAY
            );

            eventEndTimeLabel.setText(
                    "Ends --"
            );

            eventEndTimeLabel.setForeground(
                    Color.LIGHT_GRAY
            );

            return;
        }

        try
        {
            Instant endTime =
                    Instant.parse(
                            eventEnd.trim()
                    );

            String formattedEnd =
                    EVENT_END_FORMAT.format(
                            endTime
                    );

            Instant now =
                    Instant.now();

            /*
             * ==================================================
             * TIME HAS PASSED
             * ==================================================
             */

            if (!endTime.isAfter(now))
            {
                countdownTitleLabel.setText(
                        "EVENT"
                );

                countdownValueLabel.setText(
                        "ENDED"
                );

                countdownValueLabel.setForeground(
                        RED
                );

                eventEndTimeLabel.setText(
                        "Ended "
                                + formattedEnd
                );

                eventEndTimeLabel.setForeground(
                        Color.LIGHT_GRAY
                );

                return;
            }

            /*
             * ==================================================
             * EVENT RUNNING
             * ==================================================
             */

            countdownTitleLabel.setText(
                    "EVENT ENDS IN"
            );

            countdownValueLabel.setForeground(
                    GOLD
            );

            eventEndTimeLabel.setText(
                    "Ends "
                            + formattedEnd
            );

            eventEndTimeLabel.setForeground(
                    Color.LIGHT_GRAY
            );

            Duration remaining =
                    Duration.between(
                            now,
                            endTime
                    );

            long totalMinutes =
                    remaining.toMinutes();

            long days =
                    totalMinutes
                            / (24 * 60);

            long hours =
                    (
                            totalMinutes
                                    % (24 * 60)
                    ) / 60;

            long minutes =
                    totalMinutes % 60;

            if (days > 0)
            {
                countdownValueLabel.setText(
                        days
                                + (
                                days == 1
                                        ? " day "
                                        : " days "
                        )
                                + hours
                                + (
                                hours == 1
                                        ? " hour"
                                        : " hours"
                        )
                );

                return;
            }

            if (hours > 0)
            {
                countdownValueLabel.setText(
                        hours
                                + (
                                hours == 1
                                        ? " hour "
                                        : " hours "
                        )
                                + minutes
                                + (
                                minutes == 1
                                        ? " minute"
                                        : " minutes"
                        )
                );

                return;
            }

            countdownValueLabel.setText(
                    minutes
                            + (
                            minutes == 1
                                    ? " minute"
                                    : " minutes"
                    )
            );
        }
        catch (DateTimeParseException exception)
        {
            countdownTitleLabel.setText(
                    "EVENT END"
            );

            countdownValueLabel.setText(
                    "Invalid date"
            );

            countdownValueLabel.setForeground(
                    RED
            );

            eventEndTimeLabel.setText(
                    "Check EventControl"
            );

            eventEndTimeLabel.setForeground(
                    RED
            );
        }
    }

    /*
     * ==================================================
     * PERSONAL STATUS CARD
     * ==================================================
     */

    private JPanel createPersonalStatusCard()
    {
        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        wrapper.setAlignmentX(
                CENTER_ALIGNMENT
        );

        wrapper.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        116
                )
        );

        wrapper.setMinimumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        116
                )
        );

        wrapper.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        116
                )
        );

        JPanel card =
                new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ColorScheme.MEDIUM_GRAY_COLOR,
                                1
                        ),
                        BorderFactory.createEmptyBorder(
                                10,
                                12,
                                10,
                                12
                        )
                )
        );

        card.setPreferredSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        116
                )
        );

        card.setMinimumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        116
                )
        );

        card.setMaximumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        116
                )
        );

        connectionStatusValue.setFont(
                connectionStatusValue
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                10f
                        )
        );

        connectionStatusValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        personalRankValue.setForeground(
                Color.WHITE
        );

        personalRankValue.setFont(
                personalRankValue
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                11f
                        )
        );

        personalRankValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        personalBoardValue.setForeground(
                GOLD
        );

        personalBoardValue.setFont(
                personalBoardValue
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                10f
                        )
        );

        personalBoardValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        nextRankValue.setForeground(
                Color.LIGHT_GRAY
        );

        nextRankValue.setFont(
                nextRankValue
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                10f
                        )
        );

        nextRankValue.setAlignmentX(
                LEFT_ALIGNMENT
        );

        card.add(
                connectionStatusValue
        );

        card.add(
                Box.createVerticalStrut(8)
        );

        card.add(
                personalRankValue
        );

        card.add(
                Box.createVerticalStrut(6)
        );

        card.add(
                personalBoardValue
        );

        card.add(
                Box.createVerticalStrut(6)
        );

        card.add(
                nextRankValue
        );

        wrapper.add(
                card,
                BorderLayout.WEST
        );

        return wrapper;
    }

    /*
     * ==================================================
     * PERSONAL STATUS REFRESH
     * ==================================================
     */

    private void refreshPersonalStatus()
    {
        String sync =
                plugin.getLastLeaderboardSync();

        if (!plugin.isLeaderboardSyncEnabled())
        {
            connectionStatusValue.setText(
                    "● NOT CONNECTED"
            );

            connectionStatusValue.setForeground(
                    Color.LIGHT_GRAY
            );

            personalRankValue.setText(
                    "Rank: --"
            );

            personalBoardValue.setText(
                    "Board: --"
            );

            nextRankValue.setText(
                    "Enable clan leaderboard sync to join."
            );

            return;
        }

        if (
                sync == null
                        || "Never".equalsIgnoreCase(sync)
                        || "Syncing...".equalsIgnoreCase(sync)
        )
        {
            connectionStatusValue.setText(
                    "● CONNECTING..."
            );

            connectionStatusValue.setForeground(
                    GOLD
            );
        }
        else
        {
            connectionStatusValue.setText(
                    "● CONNECTED"
            );

            connectionStatusValue.setForeground(
                    GREEN
            );
        }

        String localName =
                plugin.getLocalPlayerName();

        int level =
                plugin.getSlayerLevel();

        if (
                localName != null
                        && !localName.trim().isEmpty()
        )
        {
            for (
                    LeaderboardEntry entry :
                    plugin.getLeaderboard()
            )
            {
                if (
                        entry.getPlayer() != null
                                && entry.getPlayer()
                                .equalsIgnoreCase(
                                        localName
                                )
                )
                {
                    int syncedLevel =
                            entry.getSlayerLevel();

                    if (
                            syncedLevel >= 1
                                    && syncedLevel <= 99
                    )
                    {
                        if (
                                level < 1
                                        || level > 99
                                        || syncedLevel > level
                        )
                        {
                            level =
                                    syncedLevel;
                        }
                    }

                    break;
                }
            }
        }

        if (
                level >= 90
                        && level <= 99
        )
        {
            personalBoardValue.setText(
                    "Board: Slayer 90 - 99"
            );
        }
        else if (
                level >= 70
                        && level <= 89
        )
        {
            personalBoardValue.setText(
                    "Board: Slayer 70 - 89"
            );
        }
        else
        {
            personalRankValue.setText(
                    "Rank: Not eligible"
            );

            personalBoardValue.setText(
                    level > 0
                            ? "Board: Slayer " + level
                            : "Board: Slayer level unavailable"
            );

            nextRankValue.setText(
                    "Event boards require Slayer 70 - 99."
            );

            return;
        }

        if (
                localName == null
                        || localName.trim().isEmpty()
        )
        {
            personalRankValue.setText(
                    "Rank: --"
            );

            nextRankValue.setText(
                    "Log in to view your event rank."
            );

            return;
        }

        List<LeaderboardEntry> board =
                new ArrayList<>();

        for (
                LeaderboardEntry entry :
                plugin.getLeaderboard()
        )
        {
            int entryLevel =
                    entry.getSlayerLevel();

            if (
                    level >= 90
                            && level <= 99
                            && entryLevel >= 90
                            && entryLevel <= 99
            )
            {
                board.add(
                        entry
                );
            }
            else if (
                    level >= 70
                            && level <= 89
                            && entryLevel >= 70
                            && entryLevel <= 89
            )
            {
                board.add(
                        entry
                );
            }
        }

        board.sort(
                createRankingComparator()
        );

        int playerIndex =
                -1;

        LeaderboardEntry playerEntry =
                null;

        for (
                int i = 0;
                i < board.size();
                i++
        )
        {
            LeaderboardEntry entry =
                    board.get(i);

            if (
                    entry.getPlayer()
                            .equalsIgnoreCase(
                                    localName
                            )
            )
            {
                playerIndex =
                        i;

                playerEntry =
                        entry;

                break;
            }
        }

        if (
                playerIndex < 0
                        || playerEntry == null
        )
        {
            personalRankValue.setText(
                    "Rank: Registering..."
            );

            nextRankValue.setText(
                    "Your clan event entry is syncing."
            );

            return;
        }

        int rank =
                playerIndex + 1;

        personalRankValue.setText(
                "Rank: #" + rank
        );

        if (!plugin.isEventActive())
        {
            if (rank == 1)
            {
                nextRankValue.setText(
                        "You finished first on this board."
                );
            }
            else
            {
                nextRankValue.setText(
                        "You finished #"
                                + rank
                                + " on this board."
                );
            }

            return;
        }

        if (rank == 1)
        {
            nextRankValue.setText(
                    "You are currently leading this board."
            );

            return;
        }

        LeaderboardEntry playerAbove =
                board.get(
                        playerIndex - 1
                );

        int pointsNeeded =
                playerAbove.getPoints()
                        - playerEntry.getPoints()
                        + 1;

        if (pointsNeeded < 1)
        {
            pointsNeeded =
                    1;
        }

        nextRankValue.setText(
                pointsNeeded
                        + (
                        pointsNeeded == 1
                                ? " point"
                                : " points"
                )
                        + " to overtake #"
                        + (rank - 1)
        );
    }

    /*
     * ==================================================
     * FINAL RESULTS
     * ==================================================
     */

    private void refreshFinalResults()
    {
        if (plugin.isEventActive())
        {
            finalResultsSection.setVisible(
                    false
            );

            return;
        }

        finalResultsSection.setVisible(
                true
        );

        List<LeaderboardEntry> midBoard =
                new ArrayList<>();

        List<LeaderboardEntry> highBoard =
                new ArrayList<>();

        for (
                LeaderboardEntry entry :
                plugin.getLeaderboard()
        )
        {
            int level =
                    entry.getSlayerLevel();

            if (
                    level >= 70
                            && level <= 89
            )
            {
                midBoard.add(
                        entry
                );
            }
            else if (
                    level >= 90
                            && level <= 99
            )
            {
                highBoard.add(
                        entry
                );
            }
        }

        Comparator<LeaderboardEntry> ranking =
                createRankingComparator();

        midBoard.sort(
                ranking
        );

        highBoard.sort(
                ranking
        );

        /*
         * BOARD WINNERS
         */

        if (midBoard.isEmpty())
        {
            finalWinnerMidValue.setText(
                    "70-89: No winner"
            );
        }
        else
        {
            LeaderboardEntry winner =
                    midBoard.get(0);

            finalWinnerMidValue.setText(
                    "70-89: "
                            + winner.getPlayer()
                            + " • "
                            + winner.getPoints()
                            + " pts"
            );
        }

        if (highBoard.isEmpty())
        {
            finalWinnerHighValue.setText(
                    "90-99: No winner"
            );
        }
        else
        {
            LeaderboardEntry winner =
                    highBoard.get(0);

            finalWinnerHighValue.setText(
                    "90-99: "
                            + winner.getPlayer()
                            + " • "
                            + winner.getPoints()
                            + " pts"
            );
        }

        /*
         * PLAYER RESULT
         */

        String localName =
                plugin.getLocalPlayerName();

        if (
                localName == null
                        || localName.trim().isEmpty()
        )
        {
            finalYourResultValue.setText(
                    "Log in to view your result."
            );

            finalYourBoardValue.setText(
                    "--"
            );

            return;
        }

        LeaderboardEntry localEntry =
                null;

        for (
                LeaderboardEntry entry :
                plugin.getLeaderboard()
        )
        {
            if (
                    entry.getPlayer() != null
                            && entry.getPlayer()
                            .equalsIgnoreCase(
                                    localName
                            )
            )
            {
                localEntry =
                        entry;

                break;
            }
        }

        if (localEntry == null)
        {
            finalYourResultValue.setText(
                    "No final leaderboard entry."
            );

            finalYourBoardValue.setText(
                    "--"
            );

            return;
        }

        int level =
                localEntry.getSlayerLevel();

        List<LeaderboardEntry> playerBoard;
        String boardName;

        if (
                level >= 90
                        && level <= 99
        )
        {
            playerBoard =
                    highBoard;

            boardName =
                    "Slayer 90 - 99";
        }
        else if (
                level >= 70
                        && level <= 89
        )
        {
            playerBoard =
                    midBoard;

            boardName =
                    "Slayer 70 - 89";
        }
        else
        {
            finalYourResultValue.setText(
                    "Not eligible for event boards."
            );

            finalYourBoardValue.setText(
                    "Slayer " + level
            );

            return;
        }

        int rank =
                -1;

        for (
                int i = 0;
                i < playerBoard.size();
                i++
        )
        {
            if (
                    playerBoard
                            .get(i)
                            .getPlayer()
                            .equalsIgnoreCase(
                                    localName
                            )
            )
            {
                rank =
                        i + 1;

                break;
            }
        }

        if (rank < 1)
        {
            finalYourResultValue.setText(
                    "Final rank unavailable."
            );
        }
        else
        {
            finalYourResultValue.setText(
                    "#"
                            + rank
                            + " • "
                            + localEntry.getPoints()
                            + " pts • "
                            + localEntry.getKills()
                            + " kills"
            );
        }

        finalYourBoardValue.setText(
                boardName
        );
    }

    /*
     * ==================================================
     * RANKING COMPARATOR
     * ==================================================
     */

    private Comparator<LeaderboardEntry>
    createRankingComparator()
    {
        return Comparator
                .comparingInt(
                        LeaderboardEntry::getPoints
                )
                .reversed()
                .thenComparing(
                        Comparator
                                .comparingInt(
                                        LeaderboardEntry::getKills
                                )
                                .reversed()
                )
                .thenComparing(
                        LeaderboardEntry::getPlayer,
                        String.CASE_INSENSITIVE_ORDER
                );
    }

    /*
     * ==================================================
     * CLAN MEMBERS
     * ==================================================
     */

    private void refreshClanMembers()
    {
        clanMembersPanel.removeAll();

        List<LeaderboardEntry> members =
                new ArrayList<>(
                        plugin.getLeaderboard()
                );

        members.sort(
                Comparator.comparing(
                        LeaderboardEntry::getPlayer,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

        int panelHeight;

        if (members.isEmpty())
        {
            clanMembersPanel.add(
                    createMessage(
                            "No clan members connected yet."
                    )
            );

            panelHeight =
                    28;
        }
        else
        {
            for (
                    LeaderboardEntry entry :
                    members
            )
            {
                clanMembersPanel.add(
                        createMemberRow(
                                entry
                        )
                );

                clanMembersPanel.add(
                        Box.createVerticalStrut(4)
                );
            }

            panelHeight =
                    members.size() * 42;
        }

        clanMembersPanel.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        panelHeight
                )
        );

        clanMembersPanel.setMinimumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        panelHeight
                )
        );

        clanMembersPanel.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        panelHeight
                )
        );

        clanMembersPanel.revalidate();
        clanMembersPanel.repaint();
    }

    /*
     * ==================================================
     * MEMBER ROW
     * ==================================================
     */

    private JPanel createMemberRow(
            LeaderboardEntry entry)
    {
        JPanel row =
                new JPanel(
                        new BorderLayout()
                );

        row.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        row.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        10,
                        8,
                        18
                )
        );

        row.setAlignmentX(
                CENTER_ALIGNMENT
        );

        row.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        38
                )
        );

        row.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        38
                )
        );

        JLabel player =
                new JLabel(
                        entry.getPlayer()
                );

        player.setForeground(
                Color.WHITE
        );

        JLabel slayer =
                new JLabel(
                        entry.getSlayerLevel() > 0
                                ? "Slayer "
                                + entry.getSlayerLevel()
                                : "Slayer ?"
                );

        slayer.setForeground(
                GOLD
        );

        JPanel slayerPanel =
                new JPanel(
                        new BorderLayout()
                );

        slayerPanel.setOpaque(
                false
        );

        slayerPanel.setPreferredSize(
                new Dimension(
                        78,
                        22
                )
        );

        slayerPanel.add(
                slayer,
                BorderLayout.WEST
        );

        row.add(
                player,
                BorderLayout.CENTER
        );

        row.add(
                slayerPanel,
                BorderLayout.EAST
        );

        return row;
    }

    /*
     * ==================================================
     * BREAKDOWN
     * ==================================================
     */

    private void refreshBreakdown()
    {
        breakdownPanel.removeAll();

        boolean found =
                false;

        if (config.showKillBreakdown())
        {
            for (
                    SuperiorMonster monster :
                    SuperiorMonster.values()
            )
            {
                int kills =
                        plugin.getKillsForMonster(
                                monster
                        );

                if (
                        config.onlyShowKilledMonsters()
                                && kills == 0
                )
                {
                    continue;
                }

                breakdownPanel.add(
                        createBreakdownRow(
                                monster,
                                kills
                        )
                );

                breakdownPanel.add(
                        Box.createVerticalStrut(4)
                );

                found =
                        true;
            }
        }

        if (!found)
        {
            breakdownPanel.add(
                    createMessage(
                            "No Superior kills recorded yet."
                    )
            );
        }

        breakdownPanel.revalidate();
        breakdownPanel.repaint();
    }

    /*
     * ==================================================
     * LEADERBOARDS
     * ==================================================
     */

    private void refreshLeaderboards()
    {
        highBoardPanel.removeAll();
        midBoardPanel.removeAll();

        List<LeaderboardEntry> high =
                new ArrayList<>();

        List<LeaderboardEntry> mid =
                new ArrayList<>();

        for (
                LeaderboardEntry entry :
                plugin.getLeaderboard()
        )
        {
            int level =
                    entry.getSlayerLevel();

            if (
                    level >= 90
                            && level <= 99
            )
            {
                high.add(
                        entry
                );
            }
            else if (
                    level >= 70
                            && level <= 89
            )
            {
                mid.add(
                        entry
                );
            }
        }

        Comparator<LeaderboardEntry> ranking =
                createRankingComparator();

        high.sort(
                ranking
        );

        mid.sort(
                ranking
        );

        buildCompactBoard(
                highBoardPanel,
                high
        );

        buildCompactBoard(
                midBoardPanel,
                mid
        );

        highBoardPanel.revalidate();
        highBoardPanel.repaint();

        midBoardPanel.revalidate();
        midBoardPanel.repaint();
    }

    /*
     * ==================================================
     * BUILD BOARD
     * ==================================================
     */

    private void buildCompactBoard(
            JPanel panel,
            List<LeaderboardEntry> entries)
    {
        panel.removeAll();

        if (entries.isEmpty())
        {
            panel.add(
                    createBoardMessage(
                            "No eligible players yet."
                    )
            );

            panel.setPreferredSize(
                    new Dimension(
                            BOARD_WIDTH,
                            30
                    )
            );

            panel.setMaximumSize(
                    new Dimension(
                            BOARD_WIDTH,
                            30
                    )
            );

            return;
        }

        int topCount =
                Math.min(
                        3,
                        entries.size()
                );

        for (
                int i = 0;
                i < topCount;
                i++
        )
        {
            panel.add(
                    createCompactPodiumRow(
                            i + 1,
                            entries.get(i)
                    )
            );

            panel.add(
                    Box.createVerticalStrut(5)
            );
        }

        panel.add(
                Box.createVerticalStrut(4)
        );

        JLabel rankingTitle =
                new JLabel(
                        "FULL RANKING",
                        SwingConstants.CENTER
                );

        rankingTitle.setForeground(
                Color.LIGHT_GRAY
        );

        rankingTitle.setFont(
                rankingTitle
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                9f
                        )
        );

        rankingTitle.setAlignmentX(
                CENTER_ALIGNMENT
        );

        rankingTitle.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        18
                )
        );

        rankingTitle.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        18
                )
        );

        panel.add(
                rankingTitle
        );

        panel.add(
                Box.createVerticalStrut(5)
        );

        for (
                int i = 0;
                i < entries.size();
                i++
        )
        {
            panel.add(
                    createCompactRankingRow(
                            i + 1,
                            entries.get(i)
                    )
            );

            panel.add(
                    Box.createVerticalStrut(3)
            );
        }

        int height =
                (topCount * 61)
                        + (entries.size() * 38)
                        + 45;

        panel.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        height
                )
        );

        panel.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        height
                )
        );
    }

    /*
     * ==================================================
     * PODIUM ROW
     * ==================================================
     */

    private JPanel createCompactPodiumRow(
            int rank,
            LeaderboardEntry entry)
    {
        JPanel row =
                new JPanel();

        row.setLayout(
                new BoxLayout(
                        row,
                        BoxLayout.Y_AXIS
                )
        );

        row.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        row.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        10,
                        8,
                        10
                )
        );

        row.setAlignmentX(
                CENTER_ALIGNMENT
        );

        row.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        56
                )
        );

        row.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        56
                )
        );

        JPanel topLine =
                new JPanel(
                        new BorderLayout(
                                6,
                                0
                        )
                );

        topLine.setOpaque(
                false
        );

        topLine.setAlignmentX(
                LEFT_ALIGNMENT
        );

        topLine.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH - 20,
                        22
                )
        );

        topLine.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH - 20,
                        22
                )
        );

        JLabel player =
                new JLabel(
                        podiumMedal(rank)
                                + " #"
                                + rank
                                + " "
                                + entry.getPlayer()
                );

        player.setForeground(
                podiumColor(rank)
        );

        player.setFont(
                player
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                12f
                        )
        );

        JPanel pointsPanel =
                new JPanel(
                        new BorderLayout()
                );

        pointsPanel.setOpaque(
                false
        );

        pointsPanel.setPreferredSize(
                new Dimension(
                        46,
                        22
                )
        );

        JLabel points =
                new JLabel(
                        entry.getPoints()
                                + " pts",
                        SwingConstants.RIGHT
                );

        points.setForeground(
                GOLD
        );

        points.setFont(
                points
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                11f
                        )
        );

        pointsPanel.add(
                points,
                BorderLayout.CENTER
        );

        topLine.add(
                player,
                BorderLayout.CENTER
        );

        topLine.add(
                pointsPanel,
                BorderLayout.EAST
        );

        JLabel details =
                new JLabel(
                        "Slayer "
                                + entry.getSlayerLevel()
                                + "  •  "
                                + entry.getKills()
                                + " kills"
                );

        details.setForeground(
                Color.LIGHT_GRAY
        );

        details.setFont(
                details
                        .getFont()
                        .deriveFont(
                                Font.PLAIN,
                                11f
                        )
        );

        details.setAlignmentX(
                LEFT_ALIGNMENT
        );

        row.add(
                topLine
        );

        row.add(
                Box.createVerticalStrut(5)
        );

        row.add(
                details
        );

        return row;
    }

    /*
     * ==================================================
     * RANKING ROW
     * ==================================================
     */

    private JPanel createCompactRankingRow(
            int rank,
            LeaderboardEntry entry)
    {
        JPanel row =
                new JPanel(
                        new BorderLayout(
                                6,
                                0
                        )
                );

        String localName =
                plugin.getLocalPlayerName();

        boolean me =
                localName != null
                        && !localName.isEmpty()
                        && localName.equalsIgnoreCase(
                        entry.getPlayer()
                );

        row.setBackground(
                me
                        ? new Color(
                        45,
                        70,
                        50
                )
                        : ColorScheme.DARKER_GRAY_COLOR
        );

        row.setBorder(
                BorderFactory.createEmptyBorder(
                        6,
                        10,
                        6,
                        10
                )
        );

        row.setAlignmentX(
                CENTER_ALIGNMENT
        );

        row.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        34
                )
        );

        row.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        34
                )
        );

        JLabel player =
                new JLabel(
                        "#"
                                + rank
                                + " "
                                + entry.getPlayer()
                );

        player.setForeground(
                me
                        ? GREEN
                        : Color.WHITE
        );

        JPanel pointsPanel =
                new JPanel(
                        new BorderLayout()
                );

        pointsPanel.setOpaque(
                false
        );

        pointsPanel.setPreferredSize(
                new Dimension(
                        46,
                        22
                )
        );

        JLabel points =
                new JLabel(
                        entry.getPoints()
                                + " pts",
                        SwingConstants.RIGHT
                );

        points.setForeground(
                GOLD
        );

        pointsPanel.add(
                points,
                BorderLayout.CENTER
        );

        row.add(
                player,
                BorderLayout.CENTER
        );

        row.add(
                pointsPanel,
                BorderLayout.EAST
        );

        return row;
    }

    /*
     * ==================================================
     * PODIUM HELPERS
     * ==================================================
     */

    private String podiumMedal(
            int rank)
    {
        if (rank == 1)
        {
            return "🥇";
        }

        if (rank == 2)
        {
            return "🥈";
        }

        return "🥉";
    }

    private Color podiumColor(
            int rank)
    {
        if (rank == 1)
        {
            return GOLD;
        }

        if (rank == 2)
        {
            return SILVER;
        }

        return BRONZE;
    }

    /*
     * ==================================================
     * STAT ROW
     * ==================================================
     */

    private JPanel createStatsRow(
            JPanel leftBox,
            JPanel rightBox)
    {
        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        wrapper.setAlignmentX(
                CENTER_ALIGNMENT
        );

        wrapper.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        wrapper.setMinimumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        wrapper.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        JPanel boxes =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                8,
                                0
                        )
                );

        boxes.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        boxes.setPreferredSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        boxes.setMinimumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        boxes.setMaximumSize(
                new Dimension(
                        STAT_ROW_WIDTH,
                        STAT_ROW_HEIGHT
                )
        );

        boxes.add(
                leftBox
        );

        boxes.add(
                rightBox
        );

        wrapper.add(
                boxes,
                BorderLayout.WEST
        );

        return wrapper;
    }

    /*
     * ==================================================
     * STAT BOX
     * ==================================================
     */

    private JPanel createStatBox(
            String title,
            JLabel value)
    {
        JPanel box =
                new JPanel();

        box.setLayout(
                new BoxLayout(
                        box,
                        BoxLayout.Y_AXIS
                )
        );

        box.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        box.setBorder(
                BorderFactory.createLineBorder(
                        ColorScheme.MEDIUM_GRAY_COLOR,
                        1
                )
        );

        box.setPreferredSize(
                new Dimension(
                        105,
                        STAT_ROW_HEIGHT
                )
        );

        box.setMinimumSize(
                new Dimension(
                        105,
                        STAT_ROW_HEIGHT
                )
        );

        box.setMaximumSize(
                new Dimension(
                        105,
                        STAT_ROW_HEIGHT
                )
        );

        JLabel titleLabel =
                new JLabel(
                        title,
                        SwingConstants.CENTER
                );

        titleLabel.setForeground(
                Color.LIGHT_GRAY
        );

        titleLabel.setAlignmentX(
                CENTER_ALIGNMENT
        );

        value.setForeground(
                Color.WHITE
        );

        value.setFont(
                value
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                16f
                        )
        );

        value.setAlignmentX(
                CENTER_ALIGNMENT
        );

        box.add(
                Box.createVerticalStrut(10)
        );

        box.add(
                titleLabel
        );

        box.add(
                Box.createVerticalStrut(6)
        );

        box.add(
                value
        );

        return box;
    }

    /*
     * ==================================================
     * MONSTER CARD
     * ==================================================
     */

    private JPanel createMonsterCard(
            SuperiorMonster monster)
    {
        JPanel card =
                new JPanel();

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                ColorScheme.MEDIUM_GRAY_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                8,
                                8,
                                8,
                                8
                        )
                )
        );

        card.setAlignmentX(
                CENTER_ALIGNMENT
        );

        card.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        80
                )
        );

        JLabel level =
                new JLabel(
                        "Slayer Lv. "
                                + monster.getSlayerLevel()
                                + " • "
                                + monster.getPoints()
                                + (
                                monster.getPoints() == 1
                                        ? " pt"
                                        : " pts"
                        )
                );

        level.setForeground(
                GOLD
        );

        JLabel normal =
                new JLabel(
                        monster.getNormalMonster()
                );

        normal.setForeground(
                Color.WHITE
        );

        JLabel superior =
                new JLabel(
                        "→ "
                                + monster.getSuperiorMonster()
                );

        superior.setForeground(
                GREEN
        );

        card.add(
                level
        );

        card.add(
                Box.createVerticalStrut(4)
        );

        card.add(
                normal
        );

        card.add(
                Box.createVerticalStrut(3)
        );

        card.add(
                superior
        );

        return card;
    }

    /*
     * ==================================================
     * BREAKDOWN ROW
     * ==================================================
     */

    private JPanel createBreakdownRow(
            SuperiorMonster monster,
            int kills)
    {
        JPanel row =
                new JPanel();

        row.setLayout(
                new BoxLayout(
                        row,
                        BoxLayout.Y_AXIS
                )
        );

        row.setBackground(
                ColorScheme.DARKER_GRAY_COLOR
        );

        row.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        row.setAlignmentX(
                CENTER_ALIGNMENT
        );

        row.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        55
                )
        );

        JLabel monsterName =
                new JLabel(
                        monster.getSuperiorMonster()
                );

        monsterName.setForeground(
                Color.WHITE
        );

        JLabel details =
                new JLabel(
                        "Kills: "
                                + kills
                                + " | Points: "
                                + (
                                kills
                                        * monster.getPoints()
                        )
                );

        details.setForeground(
                Color.LIGHT_GRAY
        );

        row.add(
                monsterName
        );

        row.add(
                Box.createVerticalStrut(3)
        );

        row.add(
                details
        );

        return row;
    }

    /*
     * ==================================================
     * SECTION TITLE
     * ==================================================
     */

    private JLabel createSectionTitle(
            String text)
    {
        JLabel label =
                new JLabel(
                        text,
                        SwingConstants.CENTER
                );

        label.setForeground(
                GOLD
        );

        label.setFont(
                label
                        .getFont()
                        .deriveFont(
                                Font.BOLD,
                                12f
                        )
        );

        label.setAlignmentX(
                CENTER_ALIGNMENT
        );

        label.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        20
                )
        );

        return label;
    }

    /*
     * ==================================================
     * MESSAGE
     * ==================================================
     */

    private JLabel createMessage(
            String text)
    {
        JLabel label =
                new JLabel(
                        text,
                        SwingConstants.CENTER
                );

        label.setForeground(
                Color.LIGHT_GRAY
        );

        label.setAlignmentX(
                CENTER_ALIGNMENT
        );

        label.setPreferredSize(
                new Dimension(
                        CONTENT_WIDTH,
                        24
                )
        );

        label.setMaximumSize(
                new Dimension(
                        CONTENT_WIDTH,
                        24
                )
        );

        return label;
    }

    /*
     * ==================================================
     * BOARD MESSAGE
     * ==================================================
     */

    private JLabel createBoardMessage(
            String text)
    {
        JLabel label =
                new JLabel(
                        text,
                        SwingConstants.CENTER
                );

        label.setForeground(
                Color.LIGHT_GRAY
        );

        label.setAlignmentX(
                CENTER_ALIGNMENT
        );

        label.setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        24
                )
        );

        label.setMaximumSize(
                new Dimension(
                        BOARD_WIDTH,
                        24
                )
        );

        return label;
    }

    /*
     * ==================================================
     * PAGE
     * ==================================================
     */

    private JPanel createPage()
    {
        JPanel page =
                new JPanel();

        page.setLayout(
                new BoxLayout(
                        page,
                        BoxLayout.Y_AXIS
                )
        );

        page.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        page.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        3,
                        10,
                        3
                )
        );

        return page;
    }

    /*
     * ==================================================
     * SCROLL WRAPPER
     * ==================================================
     */

    private JPanel wrapPage(
            JPanel page)
    {
        JPanel wrapper =
                new JPanel(
                        new BorderLayout()
                );

        wrapper.setBackground(
                ColorScheme.DARK_GRAY_COLOR
        );

        JScrollPane scroll =
                new JScrollPane(
                        page
                );

        scroll.setBorder(
                null
        );

        scroll
                .getViewport()
                .setBackground(
                        ColorScheme.DARK_GRAY_COLOR
                );

        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        wrapper.add(
                scroll,
                BorderLayout.CENTER
        );

        return wrapper;
    }
}