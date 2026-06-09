package com.denizlektemur.steamachievementtracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SteamGameDto(
        @JsonProperty("appid") Integer appId,
        @JsonProperty("name") String name,
        @JsonProperty("playtime_forever") Integer playtimeForever,
        @JsonProperty("img_icon_url") String imgIconUrl,
        @JsonProperty("rtime_last_played") Long lastPlayedTimestamp
) {}