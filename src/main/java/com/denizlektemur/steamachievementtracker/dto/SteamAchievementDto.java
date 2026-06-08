package com.denizlektemur.steamachievementtracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamAchievementDto(
        @JsonProperty("apiname") String apiName,
        @JsonProperty("achieved") int achieved,
        @JsonProperty("unlocktime") long unlockTime,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description
) {}