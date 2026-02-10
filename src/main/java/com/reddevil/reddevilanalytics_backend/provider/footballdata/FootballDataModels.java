package com.reddevil.reddevilanalytics_backend.provider.footballdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class FootballDataModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FixturesResponse(
            @JsonProperty("matches") List<MatchResponse> matches
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("homeTeam") TeamResponse homeTeam,
            @JsonProperty("awayTeam") TeamResponse awayTeam,
            @JsonProperty("utcDate") LocalDateTime utcDate,
            @JsonProperty("status") String status,
            @JsonProperty("score") ScoreResponse score,
            @JsonProperty("competition") CompetitionResponse competition,
            @JsonProperty("season") SeasonResponse season,
            @JsonProperty("venue") String venue,
            @JsonProperty("matchday") Integer matchday
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingsResponse(
            @JsonProperty("standings") List<StandingTableResponse> standings,
            @JsonProperty("competition") CompetitionResponse competition,
            @JsonProperty("season") SeasonResponse season
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StandingTableResponse(
            @JsonProperty("type") String type,
            @JsonProperty("table") List<TableEntryResponse> table
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TableEntryResponse(
            @JsonProperty("position") Integer position,
            @JsonProperty("team") TeamResponse team,
            @JsonProperty("playedGames") Integer playedGames,
            @JsonProperty("won") Integer won,
            @JsonProperty("draw") Integer draw,
            @JsonProperty("lost") Integer lost,
            @JsonProperty("points") Integer points,
            @JsonProperty("goalsFor") Integer goalsFor,
            @JsonProperty("goalsAgainst") Integer goalsAgainst,
            @JsonProperty("goalDifference") Integer goalDifference,
            @JsonProperty("form") String form
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TeamResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("shortName") String shortName,
            @JsonProperty("tla") String tla,
            @JsonProperty("crest") String crest
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScoreResponse(
            @JsonProperty("home") Integer home,
            @JsonProperty("away") Integer away,
            @JsonProperty("fullTime") FullTimeScore fullTime,
            @JsonProperty("halfTime") HalfTimeScore halfTime
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FullTimeScore(
            @JsonProperty("home") Integer home,
            @JsonProperty("away") Integer away
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HalfTimeScore(
            @JsonProperty("home") Integer home,
            @JsonProperty("away") Integer away
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CompetitionResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("code") String code,
            @JsonProperty("type") String type,
            @JsonProperty("emblem") String emblem
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SeasonResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate,
            @JsonProperty("currentMatchday") Integer currentMatchday
    ) {}
}
