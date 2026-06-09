package com.denizlektemur.steamachievementtracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SteamAchievementDto(
        // Player achievements endpoint fields
        @JsonProperty("apiname") String apiName,
        @JsonProperty("achieved") Integer achieved,
        @JsonProperty("unlocktime") Long unlockTime,

        // Schema endpoint fields (different field names)
        @JsonProperty("name") String name,
        @JsonProperty("displayName") String displayName,
        @JsonProperty("description") String description,
        @JsonProperty("icon") String icon,
        @JsonProperty("icongray") String iconGray
) {
    // Helper to get the right name regardless of which endpoint returned this
    public String resolvedName() {
        if (displayName != null && !displayName.isBlank()) return displayName;
        if (name != null && !name.isBlank()) return name;
        return apiName;
    }

    public String resolvedApiName() {
        // Schema endpoint uses "name" as the api name, player endpoint uses "apiname"
        if (apiName != null && !apiName.isBlank()) return apiName;
        return name;
    }
}