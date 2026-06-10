package com.denizlektemur.steamachievementtracker.client;

import com.denizlektemur.steamachievementtracker.dto.SteamAchievementDto;
import com.denizlektemur.steamachievementtracker.dto.SteamGameDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SteamApiClient {

    private final RestClient steamRestClient;

    @Value("${steam.api.key}")
    private String apiKey;

    // ── Owned games ───────────────────────────────────────────────────────────

    public List<SteamGameDto> getOwnedGames(String steamId) {
        OwnedGamesResponse response = steamRestClient.get()
                .uri("/IPlayerService/GetOwnedGames/v1/?key={key}&steamid={steamId}&include_appinfo=true",
                        apiKey, steamId)
                .retrieve()
                .body(OwnedGamesResponse.class);

        if (response == null || response.response() == null
                || response.response().games() == null) {
            return Collections.emptyList();
        }
        return response.response().games();
    }

    // ── Player achievements ───────────────────────────────────────────────────

    public List<SteamAchievementDto> getPlayerAchievements(String steamId, Integer appId) {
        try {
            PlayerAchievementsResponse response = steamRestClient.get()
                    .uri("/ISteamUserStats/GetPlayerAchievements/v1/?key={key}&steamid={steamId}&appid={appId}",
                            apiKey, steamId, appId)
                    .retrieve()
                    .body(PlayerAchievementsResponse.class);

            if (response == null || response.playerStats() == null
                    || response.playerStats().achievements() == null) {
                return Collections.emptyList();
            }
            return response.playerStats().achievements();
        } catch (Exception e) {
            // Steam returns 400 for games with no achievement support
            return Collections.emptyList();
        }
    }

    public List<SteamAchievementDto> getGameSchema(Integer appId) {
        try {
            GameSchemaResponse response = steamRestClient.get()
                    .uri("/ISteamUserStats/GetSchemaForGame/v2/?key={key}&appid={appId}",
                            apiKey, appId)
                    .retrieve()
                    .body(GameSchemaResponse.class);

            if (response == null || response.game() == null
                    || response.game().availableGameStats() == null
                    || response.game().availableGameStats().achievements() == null) {
                return Collections.emptyList();
            }
            return response.game().availableGameStats().achievements();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Map<String, Double> getGlobalAchievementPercentages(Integer appId) {
        try {
            GlobalAchievementResponse response = steamRestClient.get()
                    .uri("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v2/?gameid={appId}",
                            appId)
                    .retrieve()
                    .body(GlobalAchievementResponse.class);

            if (response == null || response.achievementpercentages() == null
                    || response.achievementpercentages().achievements() == null) {
                return Collections.emptyMap();
            }

            return response.achievementpercentages().achievements().stream()
                    .collect(Collectors.toMap(
                            GlobalAchievementDto::name,
                            GlobalAchievementDto::percent,
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private record GlobalAchievementResponse(
            @JsonProperty("achievementpercentages") GlobalAchievementPercentages achievementpercentages) {}

    private record GlobalAchievementPercentages(
            @JsonProperty("achievements") List<GlobalAchievementDto> achievements) {}

    private record GlobalAchievementDto(
            @JsonProperty("name") String name,
            @JsonProperty("percent") Double percent) {}

    private record GameSchemaResponse(
            @JsonProperty("game") GameSchemaInner game) {}

    private record GameSchemaInner(
            @JsonProperty("availableGameStats") GameStatsInner availableGameStats) {}

    private record GameStatsInner(
            @JsonProperty("achievements") List<SteamAchievementDto> achievements) {}

    // ── Internal response wrappers ────────────────────────────────────────────

    private record OwnedGamesResponse(
            @JsonProperty("response") OwnedGamesInner response) {}

    private record OwnedGamesInner(
            @JsonProperty("games") List<SteamGameDto> games) {}

    private record PlayerAchievementsResponse(
            @JsonProperty("playerstats") PlayerStatsInner playerStats) {}

    private record PlayerStatsInner(
            @JsonProperty("achievements") List<SteamAchievementDto> achievements) {}
}