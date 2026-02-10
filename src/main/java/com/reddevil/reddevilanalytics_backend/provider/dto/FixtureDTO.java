package com.reddevil.reddevilanalytics_backend.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureDTO {
    private Long id;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private LocalDateTime matchDate;
    private String competition;
    private String season;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private String venue;
    private String referee;
}
