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

    private volatile String clanName = "";
    private volatile String eventName =
            "Superior Slayer Event";

    private volatile boolean eventActive = true;
    private volatile int resetVersion = 1;

    /*
     * NEW:
     * End date/time supplied by EventControl!B5
     *
     * Example:
     * 2026-10-31 23:59
     */
    private volatile String eventEnd = "";

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
     * EVENT INFORMATION
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

    /*
     * NEW
     */
    public String getEventEnd()
    {
        return eventEnd;
    }

    /*
     * ==================================================
     * SUBMIT SCORE
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

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "eventKey",
                eventKey
        );

        json.addProperty(
                "player",
                player
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
                                     * This now updates:
                                     *
                                     * clanName
                                     * eventName
                                     * eventActive
                                     * resetVersion
                                     * eventEnd
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
     * FETCH EVENT SNAPSHOT
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
                                     * Read all event information,
                                     * including the new eventEnd.
                                     */
                                    updateEventInformation(
                                            result
                                    );

                                    List<LeaderboardEntry> entries =
                                            parseLeaderboard(
                                                    result
                                            );

                                    /*
                                     * UPDATED:
                                     *
                                     * EventSnapshot now receives
                                     * eventEnd before the leaderboard.
                                     */
                                    EventSnapshot snapshot =
                                            new EventSnapshot(
                                                    clanName,
                                                    eventName,
                                                    eventActive,
                                                    resetVersion,
                                                    eventEnd,
                                                    entries
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
        if (result.has("clanName"))
        {
            clanName =
                    result
                            .get("clanName")
                            .getAsString();
        }

        if (result.has("eventName"))
        {
            eventName =
                    result
                            .get("eventName")
                            .getAsString();
        }

        if (result.has("eventActive"))
        {
            eventActive =
                    result
                            .get("eventActive")
                            .getAsBoolean();
        }

        if (result.has("resetVersion"))
        {
            resetVersion =
                    result
                            .get("resetVersion")
                            .getAsInt();
        }

        /*
         * NEW:
         * Read EventControl!B5 from the JSON response.
         */
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

        if (!result.has("leaderboard"))
        {
            return entries;
        }

        JsonArray leaderboardArray =
                result.getAsJsonArray(
                        "leaderboard"
                );

        for (
                JsonElement element :
                leaderboardArray
        )
        {
            JsonObject entry =
                    element
                            .getAsJsonObject();

            String player =
                    entry.has("player")
                            ? entry
                            .get("player")
                            .getAsString()
                            : "";

            int kills =
                    entry.has("kills")
                            ? entry
                            .get("kills")
                            .getAsInt()
                            : 0;

            int points =
                    entry.has("points")
                            ? entry
                            .get("points")
                            .getAsInt()
                            : 0;

            int slayerLevel =
                    entry.has("slayerLevel")
                            ? entry
                            .get("slayerLevel")
                            .getAsInt()
                            : 0;

            if (!player.isEmpty())
            {
                entries.add(
                        new LeaderboardEntry(
                                player,
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