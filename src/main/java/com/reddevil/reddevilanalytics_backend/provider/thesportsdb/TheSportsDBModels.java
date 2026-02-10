package com.reddevil.reddevilanalytics_backend.provider.thesportsdb;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TheSportsDBModels {

    public record TeamsResponse(
            @JsonProperty("teams")
            List<TeamAssetData> teams
    ) {}

    public record TeamAssetData(
            @JsonProperty("strTeam")
            String strTeam,
            
            @JsonProperty("strTeamBadge")
            String strTeamBadge,
            
            @JsonProperty("strTeamBanner")
            String strTeamBanner,
            
            @JsonProperty("strStadium")
            String strStadium,
            
            @JsonProperty("strTeamLogo")
            String strTeamLogo
    ) {}

    public record PlayersResponse(
            @JsonProperty("player")
            List<PlayerAssetData> player
    ) {}

    public record PlayerAssetData(
            @JsonProperty("strPlayer")
            String strPlayer,
            
            @JsonProperty("strCutout")
            String strCutout,
            
            @JsonProperty("strThumb")
            String strThumb
    ) {}
}
