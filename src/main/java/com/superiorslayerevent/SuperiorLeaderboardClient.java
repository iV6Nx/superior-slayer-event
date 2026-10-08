package com.superiorslayerevent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class SuperiorLeaderboardClient
{
    private static final MediaType JSON =
            MediaType.parse(
                    "application/json; charset=utf-8"
            );

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final SuperiorSlayerEventConfig config;

    /*
     * ==================================================
     * EVENT INFORMATION
     * ==================================================
     */

    private volatile String clanName = "";

    private volatile String eventName =
            "Superior Slayer Event";

    private volatile boolean eventActive = true;

    private volatile int resetVersion = 1;

    /*
     * Event end supplied by EventControl!B5.
     *
     * Example:
     *
     * 2026-10-31T23:59:00.000Z
     */
    private volatile String eventEnd = "";


    /*
     * ==================================================
     * CONSTRUCTOR
     * ==================================================
     */

    @Inject
    public SuperiorLeaderboardClient(
            OkHttpClient httpClient,
            Gson gson,
            SuperiorSlayerEventConfig config)
    {
        this.httpClient = httpClient;
        this.gson = gson;
        this.config = config;
    }


    /*
     * ==================================================
     * EVENT INFORMATION GETTERS
     * ==================================================
     */

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


    /*
     * ==================================================
     * SUBMIT SCORE
     * ==================================================
     *
     * Sends:
     *
     * - player
     * - kills
     * - points
     * - Slayer level
     *
     * The Apps Script also uses this request to:
     *
     * - register the player in BuyIns
     * - register/update the player in Members
     * - update Last Seen
     * ==================================================
     */

    public void submitScore(
            String player,
            int kills,
            int points,
            int slayerLevel)
    {
        String url =
                getEventUrl();

        String eventKey =
                getEventKey();

        if (!hasConnectionDetails(
                url,
                eventKey
        ))
        {
            log.warn(
                    "Leaderboard submission skipped because Clan Event URL or Event Key is missing."
            );

            return;
        }

        if (!eventActive)
        {
            log.info(
                    "Leaderboard submission skipped because the event has ended."
            );

            return;
        }

        if (
                player == null
                        || player.trim().isEmpty()
        )
        {
            log.warn(
                    "Leaderboard submission skipped because player name is missing."
            );

            return;
        }

        if (
                slayerLevel < 1
                        || slayerLevel > 99
        )
        {
            log.warn(
                    "Leaderboard submission skipped because Slayer level is invalid."
            );

            return;
        }

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "eventKey",
                eventKey
        );

        json.addProperty(
                "player",
                player.trim()
        );

        json.addProperty(
                "kills",
                kills
        );

        json.addProperty(
                "points",
                points
        );

        json.addProperty(
                "slayerLevel",
                slayerLevel
        );

        RequestBody body =
                RequestBody.create(
                        JSON,
                        gson.toJson(json)
                );

        Request request;

        try
        {
            request =
                    new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
        }
        catch (IllegalArgumentException exception)
        {
            log.warn(
                    "Invalid Clan Event URL.",
                    exception
            );

            return;
        }

        httpClient
                .newCall(request)
                .enqueue(
                        new Callback()
                        {
                            @Override
                            public void onFailure(
                                    Call call,
                                    IOException exception)
                            {
                                log.warn(
                                        "Failed to submit Superior Slayer leaderboard score.",
                                        exception
                                );
                            }

                            @Override
                            public void onResponse(
                                    Call call,
                                    Response response)
                            {
                                try (
                                        Response closeableResponse =
                                                response
                                )
                                {
                                    if (
                                            !closeableResponse
                                                    .isSuccessful()
                                    )
                                    {
                                        log.warn(
                                                "Leaderboard submission failed with HTTP {}.",
                                                closeableResponse.code()
                                        );

                                        return;
                                    }

                                    String responseBody =
                                            closeableResponse.body()
                                                    != null
                                                    ? closeableResponse
                                                    .body()
                                                    .string()
                                                    : "";

                                    if (responseBody.isEmpty())
                                    {
                                        log.warn(
                                                "Leaderboard submission returned an empty response."
                                        );

                                        return;
                                    }

                                    JsonObject result =
                                            gson.fromJson(
                                                    responseBody,
                                                    JsonObject.class
                                            );

                                    if (
                                            result == null
                                                    || !result.has("success")
                                                    || !result
                                                    .get("success")
                                                    .getAsBoolean()
                                    )
                                    {
                                        String error =
                                                result != null
                                                        && result.has("error")
                                                        ? result
                                                        .get("error")
                                                        .getAsString()
                                                        : "Unknown error";

                                        if (
                                                "Event has ended"
                                                        .equalsIgnoreCase(
                                                                error
                                                        )
                                        )
                                        {
                                            eventActive = false;
                                        }

                                        log.warn(
                                                "Leaderboard submission was rejected: {}",
                                                error
                                        );

                                        return;
                                    }

                                    /*
                                     * Update event information returned
                                     * by the backend.
                                     */
                                    updateEventInformation(
                                            result
                                    );

                                    log.info(
                                            "Superior Slayer leaderboard score submitted successfully."
                                    );
                                }
                                catch (Exception exception)
                                {
                                    log.warn(
                                            "Failed to read leaderboard submission response.",
                                            exception
                                    );
                                }
                            }
                        }
                );
    }


    /*
     * ==================================================
     * MEMBER HEARTBEAT
     * ==================================================
     *
     * Sent every couple of minutes while the player
     * is logged in and leaderboard sync is enabled.
     *
     * This does NOT change kills or points.
     *
     * It simply tells the backend:
     *
     * - this player is still using the plugin
     * - their current Slayer level
     *
     * The backend then updates:
     *
     * - Members.Last Seen
     * - Members.Online
     * - Members.Slayer Level
     * ==================================================
     */

    public void sendHeartbeat(
            String player,
            int slayerLevel)
    {
        String url =
                getEventUrl();

        String eventKey =
                getEventKey();

        if (!hasConnectionDetails(
                url,
                eventKey
        ))
        {
            return;
        }

        if (
                player == null
                        || player.trim().isEmpty()
        )
        {
            return;
        }

        if (
                slayerLevel < 1
                        || slayerLevel > 99
        )
        {
            return;
        }

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "eventKey",
                eventKey
        );

        json.addProperty(
                "action",
                "heartbeat"
        );

        json.addProperty(
                "player",
                player.trim()
        );

        json.addProperty(
                "slayerLevel",
                slayerLevel
        );

        RequestBody body =
                RequestBody.create(
                        JSON,
                        gson.toJson(json)
                );

        Request request;

        try
        {
            request =
                    new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
        }
        catch (IllegalArgumentException exception)
        {
            log.warn(
                    "Invalid Clan Event URL.",
                    exception
            );

            return;
        }

        httpClient
                .newCall(request)
                .enqueue(
                        new Callback()
                        {
                            @Override
                            public void onFailure(
                                    Call call,
                                    IOException exception)
                            {
                                log.warn(
                                        "Failed to send Superior Slayer member heartbeat.",
                                        exception
                                );
                            }

                            @Override
                            public void onResponse(
                                    Call call,
                                    Response response)
                            {
                                try (
                                        Response closeableResponse =
                                                response
                                )
                                {
                                    if (
                                            !closeableResponse
                                                    .isSuccessful()
                                    )
                                    {
                                        log.warn(
                                                "Member heartbeat failed with HTTP {}.",
                                                closeableResponse.code()
                                        );

                                        return;
                                    }

                                    String responseBody =
                                            closeableResponse.body()
                                                    != null
                                                    ? closeableResponse
                                                    .body()
                                                    .string()
                                                    : "";

                                    if (responseBody.isEmpty())
                                    {
                                        log.warn(
                                                "Member heartbeat returned an empty response."
                                        );

                                        return;
                                    }

                                    JsonObject result =
                                            gson.fromJson(
                                                    responseBody,
                                                    JsonObject.class
                                            );

                                    if (
                                            result == null
                                                    || !result.has("success")
                                                    || !result
                                                    .get("success")
                                                    .getAsBoolean()
                                    )
                                    {
                                        String error =
                                                result != null
                                                        && result.has("error")
                                                        ? result
                                                        .get("error")
                                                        .getAsString()
                                                        : "Unknown error";

                                        log.warn(
                                                "Member heartbeat was rejected: {}",
                                                error
                                        );

                                        return;
                                    }

                                    /*
                                     * Heartbeat response also includes
                                     * current event information.
                                     */
                                    updateEventInformation(
                                            result
                                    );

                                    log.debug(
                                            "Superior Slayer member heartbeat sent successfully."
                                    );
                                }
                                catch (Exception exception)
                                {
                                    log.warn(
                                            "Failed to read member heartbeat response.",
                                            exception
                                    );
                                }
                            }
                        }
                );
    }


    /*
     * ==================================================
     * FETCH EVENT SNAPSHOT
     * ==================================================
     *
     * Fetches:
     *
     * - clan name
     * - event name
     * - event active
     * - event version
     * - event end
     * - paid leaderboard
     * - all plugin members
     * ==================================================
     */

    public void fetchEventSnapshot(
            Consumer<EventSnapshot> callback)
    {
        String url =
                getEventUrl();

        String eventKey =
                getEventKey();

        if (!hasConnectionDetails(
                url,
                eventKey
        ))
        {
            log.warn(
                    "Event fetch skipped because Clan Event URL or Event Key is missing."
            );

            callback.accept(
                    null
            );

            return;
        }

        HttpUrl baseUrl =
                HttpUrl.parse(
                        url
                );

        if (baseUrl == null)
        {
            log.warn(
                    "Invalid Clan Event URL."
            );

            callback.accept(
                    null
            );

            return;
        }

        HttpUrl requestUrl =
                baseUrl
                        .newBuilder()
                        .addQueryParameter(
                                "eventKey",
                                eventKey
                        )
                        .build();

        Request request =
                new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .build();

        httpClient
                .newCall(request)
                .enqueue(
                        new Callback()
                        {
                            @Override
                            public void onFailure(
                                    Call call,
                                    IOException exception)
                            {
                                log.warn(
                                        "Failed to fetch Superior Slayer event data.",
                                        exception
                                );

                                callback.accept(
                                        null
                                );
                            }

                            @Override
                            public void onResponse(
                                    Call call,
                                    Response response)
                            {
                                try (
                                        Response closeableResponse =
                                                response
                                )
                                {
                                    if (
                                            !closeableResponse
                                                    .isSuccessful()
                                    )
                                    {
                                        log.warn(
                                                "Event fetch failed with HTTP {}.",
                                                closeableResponse.code()
                                        );

                                        callback.accept(
                                                null
                                        );

                                        return;
                                    }

                                    String responseBody =
                                            closeableResponse.body()
                                                    != null
                                                    ? closeableResponse
                                                    .body()
                                                    .string()
                                                    : "";

                                    if (responseBody.isEmpty())
                                    {
                                        log.warn(
                                                "Event fetch returned an empty response."
                                        );

                                        callback.accept(
                                                null
                                        );

                                        return;
                                    }

                                    JsonObject result =
                                            gson.fromJson(
                                                    responseBody,
                                                    JsonObject.class
                                            );

                                    if (
                                            result == null
                                                    || !result.has("success")
                                                    || !result
                                                    .get("success")
                                                    .getAsBoolean()
                                    )
                                    {
                                        String error =
                                                result != null
                                                        && result.has("error")
                                                        ? result
                                                        .get("error")
                                                        .getAsString()
                                                        : "Unknown error";

                                        log.warn(
                                                "Event fetch was rejected: {}",
                                                error
                                        );

                                        callback.accept(
                                                null
                                        );

                                        return;
                                    }

                                    /*
                                     * Read event information.
                                     */
                                    updateEventInformation(
                                            result
                                    );

                                    /*
                                     * Parse paid leaderboard.
                                     */
                                    List<LeaderboardEntry> entries =
                                            parseLeaderboard(
                                                    result
                                            );

                                    /*
                                     * Parse all plugin members.
                                     */
                                    List<MemberEntry> members =
                                            parseMembers(
                                                    result
                                            );

                                    /*
                                     * EventSnapshot now contains:
                                     *
                                     * - event information
                                     * - leaderboard
                                     * - members
                                     */
                                    EventSnapshot snapshot =
                                            new EventSnapshot(
                                                    clanName,
                                                    eventName,
                                                    eventActive,
                                                    resetVersion,
                                                    eventEnd,
                                                    entries,
                                                    members
                                            );

                                    callback.accept(
                                            snapshot
                                    );
                                }
                                catch (Exception exception)
                                {
                                    log.warn(
                                            "Failed to parse Superior Slayer event response.",
                                            exception
                                    );

                                    callback.accept(
                                            null
                                    );
                                }
                            }
                        }
                );
    }


    /*
     * ==================================================
     * FETCH LEADERBOARD
     * ==================================================
     *
     * Kept for compatibility with any existing
     * code that only wants the leaderboard.
     * ==================================================
     */

    public void fetchLeaderboard(
            Consumer<List<LeaderboardEntry>> callback)
    {
        fetchEventSnapshot(
                snapshot ->
                {
                    if (snapshot == null)
                    {
                        callback.accept(
                                new ArrayList<>()
                        );

                        return;
                    }

                    callback.accept(
                            snapshot.getLeaderboard()
                    );
                }
        );
    }


    /*
     * ==================================================
     * UPDATE EVENT INFORMATION
     * ==================================================
     */

    private void updateEventInformation(
            JsonObject result)
    {
        if (
                result.has("clanName")
                        && !result.get("clanName").isJsonNull()
        )
        {
            clanName =
                    result
                            .get("clanName")
                            .getAsString();
        }

        if (
                result.has("eventName")
                        && !result.get("eventName").isJsonNull()
        )
        {
            eventName =
                    result
                            .get("eventName")
                            .getAsString();
        }

        if (
                result.has("eventActive")
                        && !result.get("eventActive").isJsonNull()
        )
        {
            eventActive =
                    result
                            .get("eventActive")
                            .getAsBoolean();
        }

        if (
                result.has("resetVersion")
                        && !result.get("resetVersion").isJsonNull()
        )
        {
            resetVersion =
                    result
                            .get("resetVersion")
                            .getAsInt();
        }

        if (
                result.has("eventEnd")
                        && !result.get("eventEnd").isJsonNull()
        )
        {
            eventEnd =
                    result
                            .get("eventEnd")
                            .getAsString();
        }
        else
        {
            eventEnd = "";
        }
    }


    /*
     * ==================================================
     * PARSE LEADERBOARD
     * ==================================================
     */

    private List<LeaderboardEntry> parseLeaderboard(
            JsonObject result)
    {
        List<LeaderboardEntry> entries =
                new ArrayList<>();

        if (
                !result.has("leaderboard")
                        || result.get("leaderboard").isJsonNull()
        )
        {
            return entries;
        }

        JsonArray leaderboardArray =
                result.getAsJsonArray(
                        "leaderboard"
                );

        if (leaderboardArray == null)
        {
            return entries;
        }

        for (
                JsonElement element :
                leaderboardArray
        )
        {
            if (
                    element == null
                            || element.isJsonNull()
                            || !element.isJsonObject()
            )
            {
                continue;
            }

            JsonObject entry =
                    element.getAsJsonObject();

            String player =
                    entry.has("player")
                            && !entry.get("player").isJsonNull()
                            ? entry
                            .get("player")
                            .getAsString()
                            : "";

            int kills =
                    entry.has("kills")
                            && !entry.get("kills").isJsonNull()
                            ? entry
                            .get("kills")
                            .getAsInt()
                            : 0;

            int points =
                    entry.has("points")
                            && !entry.get("points").isJsonNull()
                            ? entry
                            .get("points")
                            .getAsInt()
                            : 0;

            int slayerLevel =
                    entry.has("slayerLevel")
                            && !entry.get("slayerLevel").isJsonNull()
                            ? entry
                            .get("slayerLevel")
                            .getAsInt()
                            : 0;

            if (!player.trim().isEmpty())
            {
                entries.add(
                        new LeaderboardEntry(
                                player.trim(),
                                kills,
                                points,
                                slayerLevel
                        )
                );
            }
        }

        return entries;
    }


    /*
     * ==================================================
     * PARSE MEMBERS
     * ==================================================
     *
     * Expected JSON example:
     *
     * {
     *   "player": "iV6N",
     *   "lastSeen": "...",
     *   "online": true,
     *   "slayerLevel": 84,
     *   "eventVersion": 2,
     *   "buyInStatus": "Paid",
     *   "eligible": true
     * }
     * ==================================================
     */

    private List<MemberEntry> parseMembers(
            JsonObject result)
    {
        List<MemberEntry> members =
                new ArrayList<>();

        if (
                !result.has("members")
                        || result.get("members").isJsonNull()
        )
        {
            return members;
        }

        JsonArray membersArray =
                result.getAsJsonArray(
                        "members"
                );

        if (membersArray == null)
        {
            return members;
        }

        for (
                JsonElement element :
                membersArray
        )
        {
            if (
                    element == null
                            || element.isJsonNull()
                            || !element.isJsonObject()
            )
            {
                continue;
            }

            JsonObject entry =
                    element.getAsJsonObject();

            String player =
                    entry.has("player")
                            && !entry.get("player").isJsonNull()
                            ? entry
                            .get("player")
                            .getAsString()
                            : "";

            String lastSeen =
                    entry.has("lastSeen")
                            && !entry.get("lastSeen").isJsonNull()
                            ? entry
                            .get("lastSeen")
                            .getAsString()
                            : "";

            boolean online =
                    entry.has("online")
                            && !entry.get("online").isJsonNull()
                            && entry
                            .get("online")
                            .getAsBoolean();

            int slayerLevel =
                    entry.has("slayerLevel")
                            && !entry.get("slayerLevel").isJsonNull()
                            ? entry
                            .get("slayerLevel")
                            .getAsInt()
                            : 0;

            int eventVersion =
                    entry.has("eventVersion")
                            && !entry.get("eventVersion").isJsonNull()
                            ? entry
                            .get("eventVersion")
                            .getAsInt()
                            : 0;

            String buyInStatus =
                    entry.has("buyInStatus")
                            && !entry.get("buyInStatus").isJsonNull()
                            ? entry
                            .get("buyInStatus")
                            .getAsString()
                            : "Not Paid";

            boolean eligible =
                    entry.has("eligible")
                            && !entry.get("eligible").isJsonNull()
                            && entry
                            .get("eligible")
                            .getAsBoolean();

            if (!player.trim().isEmpty())
            {
                members.add(
                        new MemberEntry(
                                player.trim(),
                                lastSeen,
                                online,
                                slayerLevel,
                                eventVersion,
                                buyInStatus,
                                eligible
                        )
                );
            }
        }

        return members;
    }


    /*
     * ==================================================
     * CONFIG HELPERS
     * ==================================================
     */

    private String getEventUrl()
    {
        String url =
                config.clanEventUrl();

        return url == null
                ? ""
                : url.trim();
    }

    private String getEventKey()
    {
        String eventKey =
                config.clanEventKey();

        return eventKey == null
                ? ""
                : eventKey.trim();
    }

    private boolean hasConnectionDetails(
            String url,
            String eventKey)
    {
        return !url.isEmpty()
                && !eventKey.isEmpty();
    }
}