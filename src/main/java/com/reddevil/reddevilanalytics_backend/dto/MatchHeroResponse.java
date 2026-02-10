package com.reddevil.reddevilanalytics_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHeroResponse {
    private Long matchId;
    private LocalDateTime matchDate;
    private String venue;
    private TeamInfo homeTeam;
    private TeamInfo awayTeam;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private String competition;
    private PredictionInfo prediction;
    private Integer currentMinute;
}
