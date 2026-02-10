package com.reddevil.reddevilanalytics_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveMatchItem {
    private Long matchId;
    private TeamInfo homeTeam;
    private TeamInfo awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private Integer minute;
    private String status;
}
