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