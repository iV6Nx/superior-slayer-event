package com.superiorslayerevent;

import com.google.inject.Provides;

import java.awt.image.BufferedImage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Skill;

import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
		name = "Superior Slayer Event",
		description = "Tracks Superior Slayer monsters for a clan event",
		tags = {"slayer", "superior", "clan", "event"}
)
public class SuperiorSlayerEventPlugin extends Plugin
{
	private static final String CONFIG_GROUP =
			"superiorslayerevent";

	private static final String TOTAL_KILLS_KEY =
			"totalSuperiorKills";

	private static final String TOTAL_POINTS_KEY =
			"totalEventPoints";

	private static final String MONSTER_KILL_PREFIX =
			"kills_";

	private static final String EVENT_VERSION_KEY =
			"eventResetVersion";

	private static final int LEADERBOARD_REFRESH_MS =
			120000;

	private static final DateTimeFormatter TIME_FORMAT =
			DateTimeFormatter.ofPattern("HH:mm:ss");

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SuperiorSlayerEventConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SuperiorLeaderboardClient leaderboardClient;

	private final Map<SuperiorMonster, Integer> monsterKills =
			new EnumMap<>(SuperiorMonster.class);

	private final List<LeaderboardEntry> leaderboard =
			new ArrayList<>();

	private int totalSuperiorKills = 0;
	private int totalEventPoints = 0;

	private int sessionSuperiorKills = 0;
	private int sessionEventPoints = 0;

	private int slayerLevel = 0;

	private String eventName =
			"Superior Slayer Event";

	private boolean eventActive =
			true;

	private int currentEventVersion =
			1;

	/*
	 * ==================================================
	 * EVENT END
	 *
	 * Supplied by EventControl!B5.
	 *
	 * Example:
	 * 2026-10-31 23:59
	 * ==================================================
	 */

	private String eventEnd =
			"";

	private String lastLeaderboardSync =
			"Never";

	/*
	 * Keeps the most recent valid logged-in
	 * RuneScape name.
	 *
	 * RuneLite can occasionally return no local
	 * player briefly while loading/changing state.
	 */
	private String lastKnownPlayerName =
			"";

	/*
	 * These values remember the last player state
	 * that was sent to the clan event.
	 *
	 * This stops every leaderboard refresh from
	 * uploading the exact same information again.
	 */
	private String lastSubmittedPlayer =
			"";

	private int lastSubmittedKills =
			-1;

	private int lastSubmittedPoints =
			-1;

	private int lastSubmittedSlayerLevel =
			-1;

	private int lastSubmittedEventVersion =
			-1;

	private Timer leaderboardRefreshTimer;

	private SuperiorSlayerEventPanel panel;
	private NavigationButton navButton;

	private BufferedImage sidebarIcon;

	/*
	 * ==================================================
	 * STARTUP
	 * ==================================================
	 */

	@Override
	protected void startUp()
	{
		loadData();

		sessionSuperiorKills = 0;
		sessionEventPoints = 0;

		updateSlayerLevel();

		resetSubmissionFingerprint();

		sidebarIcon =
				ImageUtil.loadImageResource(
						SuperiorSlayerEventPlugin.class,
						"sse_icon.png"
				);

		panel =
				new SuperiorSlayerEventPanel(
						this,
						config
				);

		navButton =
				NavigationButton.builder()
						.tooltip(
								"Superior Slayer Event"
						)
						.icon(
								sidebarIcon
						)
						.priority(5)
						.panel(panel)
						.build();

		if (config.showSidebar())
		{
			clientToolbar.addNavigation(
					navButton
			);
		}

		startLeaderboardTimer();

		/*
		 * Fetch server state first.
		 *
		 * Once the snapshot arrives,
		 * handleEventSnapshot() automatically
		 * registers the player.
		 */
		if (isLeaderboardSyncEnabled())
		{
			refreshLeaderboard();
		}

		refreshPanel();

		log.info(
				"Superior Slayer Event started. Kills: {} Points: {} Slayer Level: {}",
				totalSuperiorKills,
				totalEventPoints,
				slayerLevel
		);
	}

	/*
	 * ==================================================
	 * SHUTDOWN
	 * ==================================================
	 */

	@Override
	protected void shutDown()
	{
		saveAllData();

		if (leaderboardRefreshTimer != null)
		{
			leaderboardRefreshTimer.stop();
			leaderboardRefreshTimer = null;
		}

		if (navButton != null)
		{
			clientToolbar.removeNavigation(
					navButton
			);
		}

		panel = null;
		navButton = null;
		sidebarIcon = null;

		resetSubmissionFingerprint();

		log.info(
				"Superior Slayer Event stopped."
		);
	}

	/*
	 * ==================================================
	 * AUTO REFRESH
	 * ==================================================
	 */

	private void startLeaderboardTimer()
	{
		if (leaderboardRefreshTimer != null)
		{
			leaderboardRefreshTimer.stop();
		}

		leaderboardRefreshTimer =
				new Timer(
						LEADERBOARD_REFRESH_MS,
						event ->
						{
							if (!isLeaderboardSyncEnabled())
							{
								return;
							}

							updateSlayerLevel();

							refreshLeaderboard();
						}
				);

		leaderboardRefreshTimer.setInitialDelay(
				LEADERBOARD_REFRESH_MS
		);

		leaderboardRefreshTimer.setRepeats(
				true
		);

		leaderboardRefreshTimer.start();
	}

	/*
	 * ==================================================
	 * LOGIN
	 * ==================================================
	 */

	@Subscribe
	public void onGameStateChanged(
			GameStateChanged event)
	{
		if (
				event.getGameState()
						!= GameState.LOGGED_IN
		)
		{
			return;
		}

		updateSlayerLevel();

		/*
		 * Remember the player's current name.
		 */
		Player localPlayer =
				client.getLocalPlayer();

		if (
				localPlayer != null
						&& localPlayer.getName() != null
						&& !localPlayer.getName().trim().isEmpty()
		)
		{
			lastKnownPlayerName =
					localPlayer.getName().trim();
		}

		if (isLeaderboardSyncEnabled())
		{
			resetSubmissionFingerprint();

			refreshLeaderboard();
		}

		refreshPanel();
	}

	/*
	 * ==================================================
	 * SLAYER LEVEL CHANGE
	 * ==================================================
	 */

	@Subscribe
	public void onStatChanged(
			StatChanged event)
	{
		if (
				event.getSkill()
						!= Skill.SLAYER
		)
		{
			return;
		}

		int previousLevel =
				slayerLevel;

		updateSlayerLevel();

		if (
				slayerLevel
						== previousLevel
		)
		{
			return;
		}

		log.info(
				"Slayer level changed from {} to {}.",
				previousLevel,
				slayerLevel
		);

		refreshPanel();

		if (
				isLeaderboardSyncEnabled()
						&& eventActive
		)
		{
			submitScore();
		}
	}

	/*
	 * ==================================================
	 * READ REAL SLAYER LEVEL
	 * ==================================================
	 */

	private void updateSlayerLevel()
	{
		if (
				client.getGameState()
						!= GameState.LOGGED_IN
		)
		{
			return;
		}

		int level =
				client.getRealSkillLevel(
						Skill.SLAYER
				);

		if (
				level >= 1
						&& level <= 99
		)
		{
			slayerLevel =
					level;
		}
	}

	/*
	 * ==================================================
	 * SUPERIOR SPAWN
	 * ==================================================
	 */

	@Subscribe
	public void onNpcSpawned(
			NpcSpawned event)
	{
		NPC npc =
				event.getNpc();

		if (
				npc == null
						|| npc.getName() == null
		)
		{
			return;
		}

		SuperiorMonster superior =
				SuperiorMonster.fromNpcName(
						npc.getName()
				);

		if (superior == null)
		{
			return;
		}

		if (
				isLeaderboardSyncEnabled()
						&& !eventActive
		)
		{
			return;
		}

		if (config.showSpawnMessages())
		{
			String message =
					"<col=ff981f>Superior detected:</col> "
							+ superior.getSuperiorMonster()
							+ " | Slayer level: "
							+ superior.getSlayerLevel()
							+ " | Worth: "
							+ superior.getPoints()
							+ " point"
							+ (
							superior.getPoints() == 1
									? ""
									: "s"
					);

			client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					message,
					null
			);
		}

		log.info(
				"Superior detected: {}",
				superior.getSuperiorMonster()
		);
	}

	/*
	 * ==================================================
	 * SUPERIOR DEATH
	 * ==================================================
	 */

	@Subscribe
	public void onActorDeath(
			ActorDeath event)
	{
		Actor actor =
				event.getActor();

		if (!(actor instanceof NPC))
		{
			return;
		}

		NPC npc =
				(NPC) actor;

		if (npc.getName() == null)
		{
			return;
		}

		SuperiorMonster superior =
				SuperiorMonster.fromNpcName(
						npc.getName()
				);

		if (superior == null)
		{
			return;
		}

		if (
				isLeaderboardSyncEnabled()
						&& !eventActive
		)
		{
			log.info(
					"Superior kill ignored because the event has ended."
			);

			return;
		}

		processSuperiorKill(
				superior
		);
	}

	/*
	 * ==================================================
	 * PROCESS SUPERIOR KILL
	 * ==================================================
	 */

	private void processSuperiorKill(
			SuperiorMonster superior)
	{
		int newMonsterCount =
				monsterKills.getOrDefault(
						superior,
						0
				) + 1;

		monsterKills.put(
				superior,
				newMonsterCount
		);

		int pointsEarned =
				superior.getPoints();

		totalSuperiorKills++;
		totalEventPoints += pointsEarned;

		sessionSuperiorKills++;
		sessionEventPoints += pointsEarned;

		saveMonsterKillCount(
				superior,
				newMonsterCount
		);

		saveTotals();

		refreshPanel();

		if (
				isLeaderboardSyncEnabled()
						&& eventActive
		)
		{
			updateSlayerLevel();
			submitScore();
		}

		if (config.showKillMessages())
		{
			StringBuilder message =
					new StringBuilder();

			message.append(
					"<col=00ff00>Superior slain:</col> "
			);

			message.append(
					superior.getSuperiorMonster()
			);

			if (config.showPointsInChat())
			{
				message.append(
						" | <col=ffff00>+"
				);

				message.append(
						pointsEarned
				);

				message.append(
						pointsEarned == 1
								? " point"
								: " points"
				);

				message.append(
						"</col>"
				);
			}

			message.append(
					" | "
			);

			message.append(
					superior.getSuperiorMonster()
			);

			message.append(
					" kills: "
			);

			message.append(
					newMonsterCount
			);

			message.append(
					" | Total kills: "
			);

			message.append(
					totalSuperiorKills
			);

			message.append(
					" | Total points: "
			);

			message.append(
					totalEventPoints
			);

			client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					message.toString(),
					null
			);
		}

		log.info(
				"Superior killed: {} | Monster kills: {} | Total kills: {} | Points earned: {} | Total points: {} | Slayer: {}",
				superior.getSuperiorMonster(),
				newMonsterCount,
				totalSuperiorKills,
				pointsEarned,
				totalEventPoints,
				slayerLevel
		);
	}

	/*
	 * ==================================================
	 * AUTOMATIC PARTICIPANT / SCORE SUBMISSION
	 * ==================================================
	 */

	private void submitScore()
	{
		if (!isLeaderboardSyncEnabled())
		{
			return;
		}

		if (!eventActive)
		{
			return;
		}

		if (!isPlayerLoggedIn())
		{
			return;
		}

		Player localPlayer =
				client.getLocalPlayer();

		if (
				localPlayer == null
						|| localPlayer.getName() == null
						|| localPlayer.getName()
						.trim()
						.isEmpty()
		)
		{
			return;
		}

		updateSlayerLevel();

		if (
				slayerLevel < 1
						|| slayerLevel > 99
		)
		{
			log.warn(
					"Participant submission skipped because Slayer level could not be read."
			);

			return;
		}

		String playerName =
				localPlayer.getName()
						.trim();

		/*
		 * Remember the valid player name.
		 */
		lastKnownPlayerName =
				playerName;

		if (
				isSameAsLastSubmission(
						playerName,
						totalSuperiorKills,
						totalEventPoints,
						slayerLevel,
						currentEventVersion
				)
		)
		{
			return;
		}

		log.info(
				"Submitting clan participant: {} | Slayer: {} | Kills: {} | Points: {} | Event version: {}",
				playerName,
				slayerLevel,
				totalSuperiorKills,
				totalEventPoints,
				currentEventVersion
		);

		leaderboardClient.submitScore(
				playerName,
				totalSuperiorKills,
				totalEventPoints,
				slayerLevel
		);

		rememberSubmission(
				playerName,
				totalSuperiorKills,
				totalEventPoints,
				slayerLevel,
				currentEventVersion
		);

		Timer delayedRefresh =
				new Timer(
						2000,
						event ->
						{
							if (isLeaderboardSyncEnabled())
							{
								refreshLeaderboard();
							}
						}
				);

		delayedRefresh.setRepeats(
				false
		);

		delayedRefresh.start();
	}

	/*
	 * ==================================================
	 * SUBMISSION FINGERPRINT
	 * ==================================================
	 */

	private boolean isSameAsLastSubmission(
			String playerName,
			int kills,
			int points,
			int playerSlayerLevel,
			int eventVersion)
	{
		return playerName.equalsIgnoreCase(
				lastSubmittedPlayer
		)
				&& kills
				== lastSubmittedKills
				&& points
				== lastSubmittedPoints
				&& playerSlayerLevel
				== lastSubmittedSlayerLevel
				&& eventVersion
				== lastSubmittedEventVersion;
	}

	private void rememberSubmission(
			String playerName,
			int kills,
			int points,
			int playerSlayerLevel,
			int eventVersion)
	{
		lastSubmittedPlayer =
				playerName;

		lastSubmittedKills =
				kills;

		lastSubmittedPoints =
				points;

		lastSubmittedSlayerLevel =
				playerSlayerLevel;

		lastSubmittedEventVersion =
				eventVersion;
	}

	private void resetSubmissionFingerprint()
	{
		lastSubmittedPlayer =
				"";

		lastSubmittedKills =
				-1;

		lastSubmittedPoints =
				-1;

		lastSubmittedSlayerLevel =
				-1;

		lastSubmittedEventVersion =
				-1;
	}

	/*
	 * ==================================================
	 * FETCH EVENT + LEADERBOARD
	 * ==================================================
	 */

	public void refreshLeaderboard()
	{
		if (!isLeaderboardSyncEnabled())
		{
			leaderboard.clear();

			lastLeaderboardSync =
					"Disabled";

			refreshPanel();

			return;
		}

		leaderboardClient.fetchEventSnapshot(
				snapshot ->
						SwingUtilities.invokeLater(
								() ->
								{
									if (snapshot == null)
									{
										log.warn(
												"Event refresh returned no data."
										);

										return;
									}

									handleEventSnapshot(
											snapshot
									);
								}
						)
		);
	}

	/*
	 * ==================================================
	 * PROCESS EVENT SNAPSHOT
	 * ==================================================
	 */

	private void handleEventSnapshot(
			EventSnapshot snapshot)
	{
		eventName =
				snapshot.getEventName();

		eventActive =
				snapshot.isEventActive();

		currentEventVersion =
				snapshot.getResetVersion();

		/*
		 * NEW:
		 * Read the event end date/time.
		 */
		eventEnd =
				snapshot.getEventEnd() == null
						? ""
						: snapshot.getEventEnd().trim();

		checkForNewEvent(
				currentEventVersion
		);

		leaderboard.clear();

		leaderboard.addAll(
				snapshot.getLeaderboard()
		);

		lastLeaderboardSync =
				LocalTime.now()
						.format(
								TIME_FORMAT
						);

		refreshPanel();

		log.info(
				"Event refreshed. Name: {} Active: {} Version: {} End: {}",
				eventName,
				eventActive,
				currentEventVersion,
				eventEnd
		);

		/*
		 * ==================================================
		 * AUTOMATIC EVENT PARTICIPATION
		 * ==================================================
		 */

		if (
				eventActive
						&& isPlayerLoggedIn()
		)
		{
			updateSlayerLevel();

			submitScore();
		}
	}

	/*
	 * ==================================================
	 * AUTO RESET FOR NEW EVENT
	 * ==================================================
	 */

	private void checkForNewEvent(
			int serverVersion)
	{
		Integer savedVersion =
				configManager.getConfiguration(
						CONFIG_GROUP,
						EVENT_VERSION_KEY,
						Integer.class
				);

		if (savedVersion == null)
		{
			configManager.setConfiguration(
					CONFIG_GROUP,
					EVENT_VERSION_KEY,
					serverVersion
			);

			log.info(
					"Initial event version stored: {}",
					serverVersion
			);

			return;
		}

		if (
				savedVersion
						== serverVersion
		)
		{
			return;
		}

		log.info(
				"New event detected. Old version: {} New version: {}",
				savedVersion,
				serverVersion
		);

		resetLocalEventDataForNewEvent();

		configManager.setConfiguration(
				CONFIG_GROUP,
				EVENT_VERSION_KEY,
				serverVersion
		);

		resetSubmissionFingerprint();

		client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"<col=00ff00>Superior Slayer Event:</col> "
						+ "A new clan event has started. "
						+ "Your event kills and points have been reset.",
				null
		);
	}

	private void resetLocalEventDataForNewEvent()
	{
		totalSuperiorKills = 0;
		totalEventPoints = 0;

		sessionSuperiorKills = 0;
		sessionEventPoints = 0;

		monsterKills.clear();

		for (
				SuperiorMonster monster :
				SuperiorMonster.values()
		)
		{
			monsterKills.put(
					monster,
					0
			);

			configManager.unsetConfiguration(
					CONFIG_GROUP,
					getMonsterConfigKey(
							monster
					)
			);
		}

		configManager.setConfiguration(
				CONFIG_GROUP,
				TOTAL_KILLS_KEY,
				0
		);

		configManager.setConfiguration(
				CONFIG_GROUP,
				TOTAL_POINTS_KEY,
				0
		);

		leaderboard.clear();

		resetSubmissionFingerprint();

		refreshPanel();

		log.info(
				"Local Superior Slayer Event progress automatically reset for new event."
		);
	}

	/*
	 * ==================================================
	 * HELPERS
	 * ==================================================
	 */

	private boolean isPlayerLoggedIn()
	{
		return client.getGameState()
				== GameState.LOGGED_IN
				&& client.getLocalPlayer()
				!= null;
	}

	public boolean isLeaderboardSyncEnabled()
	{
		Boolean enabled =
				configManager.getConfiguration(
						CONFIG_GROUP,
						"enableLeaderboardSync",
						Boolean.class
				);

		return Boolean.TRUE.equals(
				enabled
		);
	}

	/*
	 * ==================================================
	 * EVENT GETTERS
	 * ==================================================
	 */

	public String getLastLeaderboardSync()
	{
		return lastLeaderboardSync;
	}

	public String getClanName()
	{
		return leaderboardClient.getClanName();
	}

	public String getEventName()
	{
		return eventName;
	}

	public boolean isEventActive()
	{
		return eventActive;
	}

	public int getCurrentEventVersion()
	{
		return currentEventVersion;
	}

	/*
	 * NEW:
	 * Used by the panel countdown.
	 */
	public String getEventEnd()
	{
		return eventEnd;
	}

	public int getSlayerLevel()
	{
		return slayerLevel;
	}

	/*
	 * ==================================================
	 * PLAYER
	 * ==================================================
	 */

	public String getLocalPlayerName()
	{
		Player player =
				client.getLocalPlayer();

		if (
				player != null
						&& player.getName() != null
						&& !player.getName().trim().isEmpty()
		)
		{
			lastKnownPlayerName =
					player.getName().trim();

			return lastKnownPlayerName;
		}

		return lastKnownPlayerName;
	}

	/*
	 * ==================================================
	 * LEADERBOARD
	 * ==================================================
	 */

	public List<LeaderboardEntry> getLeaderboard()
	{
		return new ArrayList<>(
				leaderboard
		);
	}

	public int getPlayerRank()
	{
		String playerName =
				getLocalPlayerName();

		if (playerName.isEmpty())
		{
			return -1;
		}

		for (
				int i = 0;
				i < leaderboard.size();
				i++
		)
		{
			LeaderboardEntry entry =
					leaderboard.get(i);

			if (
					entry.getPlayer()
							.equalsIgnoreCase(
									playerName
							)
			)
			{
				return i + 1;
			}
		}

		return -1;
	}

	/*
	 * ==================================================
	 * SETTINGS
	 * ==================================================
	 */

	@Subscribe
	public void onConfigChanged(
			ConfigChanged event)
	{
		if (
				!CONFIG_GROUP.equals(
						event.getGroup()
				)
		)
		{
			return;
		}

		/*
		 * SIDEBAR
		 */
		if (
				"showSidebar".equals(
						event.getKey()
				)
		)
		{
			if (config.showSidebar())
			{
				if (navButton != null)
				{
					clientToolbar.addNavigation(
							navButton
					);
				}
			}
			else
			{
				if (navButton != null)
				{
					clientToolbar.removeNavigation(
							navButton
					);
				}
			}
		}

		/*
		 * LEADERBOARD SYNC ENABLED / DISABLED
		 */
		if (
				"enableLeaderboardSync".equals(
						event.getKey()
				)
		)
		{
			if (isLeaderboardSyncEnabled())
			{
				lastLeaderboardSync =
						"Syncing...";

				updateSlayerLevel();

				resetSubmissionFingerprint();

				refreshLeaderboard();
			}
			else
			{
				leaderboard.clear();

				lastLeaderboardSync =
						"Disabled";

				resetSubmissionFingerprint();

				refreshPanel();
			}
		}

		/*
		 * EVENT URL OR KEY CHANGED
		 */
		if (
				"clanEventUrl".equals(
						event.getKey()
				)
						|| "clanEventKey".equals(
						event.getKey()
				)
		)
		{
			if (isLeaderboardSyncEnabled())
			{
				lastLeaderboardSync =
						"Syncing...";

				updateSlayerLevel();

				resetSubmissionFingerprint();

				refreshLeaderboard();
			}
		}

		SwingUtilities.invokeLater(
				() ->
				{
					if (panel != null)
					{
						panel.refresh();
					}
				}
		);
	}

	/*
	 * ==================================================
	 * LOAD DATA
	 * ==================================================
	 */

	private void loadData()
	{
		monsterKills.clear();

		Integer savedKills =
				configManager.getConfiguration(
						CONFIG_GROUP,
						TOTAL_KILLS_KEY,
						Integer.class
				);

		Integer savedPoints =
				configManager.getConfiguration(
						CONFIG_GROUP,
						TOTAL_POINTS_KEY,
						Integer.class
				);

		totalSuperiorKills =
				savedKills != null
						? savedKills
						: 0;

		totalEventPoints =
				savedPoints != null
						? savedPoints
						: 0;

		for (
				SuperiorMonster monster :
				SuperiorMonster.values()
		)
		{
			Integer kills =
					configManager.getConfiguration(
							CONFIG_GROUP,
							getMonsterConfigKey(
									monster
							),
							Integer.class
					);

			monsterKills.put(
					monster,
					kills != null
							? kills
							: 0
			);
		}
	}

	/*
	 * ==================================================
	 * SAVE DATA
	 * ==================================================
	 */

	private void saveAllData()
	{
		saveTotals();

		for (
				Map.Entry<
						SuperiorMonster,
						Integer
						> entry :
				monsterKills.entrySet()
		)
		{
			saveMonsterKillCount(
					entry.getKey(),
					entry.getValue()
			);
		}
	}

	private void saveTotals()
	{
		configManager.setConfiguration(
				CONFIG_GROUP,
				TOTAL_KILLS_KEY,
				totalSuperiorKills
		);

		configManager.setConfiguration(
				CONFIG_GROUP,
				TOTAL_POINTS_KEY,
				totalEventPoints
		);
	}

	private void saveMonsterKillCount(
			SuperiorMonster monster,
			int kills)
	{
		configManager.setConfiguration(
				CONFIG_GROUP,
				getMonsterConfigKey(
						monster
				),
				kills
		);
	}

	private String getMonsterConfigKey(
			SuperiorMonster monster)
	{
		return MONSTER_KILL_PREFIX
				+ monster.name()
				.toLowerCase();
	}

	/*
	 * ==================================================
	 * PANEL
	 * ==================================================
	 */

	private void refreshPanel()
	{
		if (panel == null)
		{
			return;
		}

		SwingUtilities.invokeLater(
				panel::refresh
		);
	}

	/*
	 * ==================================================
	 * STAT GETTERS
	 * ==================================================
	 */

	public int getTotalSuperiorKills()
	{
		return totalSuperiorKills;
	}

	public int getTotalEventPoints()
	{
		return totalEventPoints;
	}

	public int getSessionSuperiorKills()
	{
		return sessionSuperiorKills;
	}

	public int getSessionEventPoints()
	{
		return sessionEventPoints;
	}

	public int getKillsForMonster(
			SuperiorMonster monster)
	{
		return monsterKills.getOrDefault(
				monster,
				0
		);
	}

	public Map<SuperiorMonster, Integer> getMonsterKills()
	{
		return new EnumMap<>(
				monsterKills
		);
	}

	/*
	 * ==================================================
	 * MANUAL RESET
	 * ==================================================
	 */

	@Override
	public void resetConfiguration()
	{
		totalSuperiorKills = 0;
		totalEventPoints = 0;

		sessionSuperiorKills = 0;
		sessionEventPoints = 0;

		monsterKills.clear();
		leaderboard.clear();

		for (
				SuperiorMonster monster :
				SuperiorMonster.values()
		)
		{
			monsterKills.put(
					monster,
					0
			);

			configManager.unsetConfiguration(
					CONFIG_GROUP,
					getMonsterConfigKey(
							monster
					)
			);
		}

		configManager.unsetConfiguration(
				CONFIG_GROUP,
				TOTAL_KILLS_KEY
		);

		configManager.unsetConfiguration(
				CONFIG_GROUP,
				TOTAL_POINTS_KEY
		);

		/*
		 * Do not remove EVENT_VERSION_KEY.
		 *
		 * This prevents a normal manual reset
		 * interfering with the server-side
		 * new-event detection.
		 */

		resetSubmissionFingerprint();

		refreshPanel();

		log.info(
				"Superior Slayer Event data has been manually reset."
		);
	}

	/*
	 * ==================================================
	 * CONFIG
	 * ==================================================
	 */

	@Provides
	SuperiorSlayerEventConfig provideConfig(
			ConfigManager configManager)
	{
		return configManager.getConfig(
				SuperiorSlayerEventConfig.class
		);
	}
}