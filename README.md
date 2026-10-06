# Superior Slayer Event

A RuneLite plugin for tracking Superior Slayer monster kills during clan events.

Created for the **TNS Inc** clan.

## Features

- Tracks Superior Slayer monster kills
- Tracks total event points
- Tracks kills per Superior monster
- Tracks current RuneLite session kills and points
- Saves event progress between RuneLite sessions
- Detects new clan event versions and resets event progress automatically
- Shared clan leaderboard support
- Automatic clan participant registration
- Slayer level based leaderboard brackets
- Personal event rank
- Event countdown
- Exact event finish time
- Final event results
- Board winners
- Superior spawn notifications
- Superior kill notifications

## Leaderboard Brackets

The event contains two leaderboard brackets:

- **Slayer 70–89**
- **Slayer 90–99**

Players are automatically placed into the correct board using their current Slayer level.

## Event Points

| Slayer Level | Points |
|---|---:|
| 5–25 | 1 |
| 30–45 | 2 |
| 50–58 | 3 |
| 60–65 | 4 |
| 70–78 | 6 |
| 80–85 | 8 |
| 90–95 | 10 |

## Clan Leaderboard

The shared clan leaderboard is optional and disabled by default.

When enabled, the plugin communicates with the clan event leaderboard service.

The following information is submitted:

- RuneScape character name
- Slayer level
- Superior Slayer kill total
- Event point total

Connecting to a third-party leaderboard service also exposes the user's IP address to that service.

The leaderboard service is not controlled or verified by the RuneLite developers.

## Event Configuration

Clan event organisers provide:

- Clan Event URL
- Event Key

These are entered through the RuneLite plugin configuration.

The Event Key is stored using RuneLite's secret configuration option.

## Event Management

The clan event backend controls:

- Clan name
- Event name
- Event active status
- Event version
- Event end date/time

Changing the event version starts a new event and automatically resets local event progress.

Setting the event to inactive prevents further event submissions and displays the final event results.

## Development

This plugin requires Java 11.

To run the plugin in development mode:

1. Open the project in IntelliJ IDEA.
2. Reload the Gradle project.
3. Run the Gradle `clean` task.
4. Run the Gradle `run` task.
5. RuneLite will launch in developer mode.

## Privacy

Clan leaderboard syncing is optional.

No leaderboard information is submitted unless the user explicitly enables the Clan Leaderboard setting.

## License

This project is licensed under the BSD 2-Clause License.

See [LICENSE](LICENSE) for details.