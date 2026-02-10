package com.reddevil.reddevilanalytics_backend.provider.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record FixturesResponse(
        List<FixtureItem> response
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record FixtureItem(
        FixtureDetails fixture,
        TeamsData teams,
        GoalsData goals,
        List<EventData> events
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record FixtureDetails(
        Long id,
        String date,
        Long timestamp,
        StatusData status,
        VenueData venue,
        String referee
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record StatusData(
        @JsonProperty("short") String shortStatus,
        @JsonProperty("long") String longStatus,
        Integer elapsed
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record VenueData(
        Long id,
        String name,
        String city
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record TeamsData(
        TeamInfo home,
        TeamInfo away
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record TeamInfo(
        Long id,
        String name,
        String logo
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GoalsData(
        Integer home,
        Integer away
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record EventData(
        TimeData time,
        TeamInfo team,
        PlayerInfo player,
        String type,
        String detail,
        String comments
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record TimeData(
        Integer elapsed,
        Integer extra
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record PlayerInfo(
        Long id,
        String name
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record LiveMatchesResponse(
        List<FixtureItem> response
) {}
