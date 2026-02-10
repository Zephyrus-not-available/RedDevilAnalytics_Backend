package com.reddevil.reddevilanalytics_backend.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveMatchDTO {
    private Long id;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private Integer homeScore;
    private Integer awayScore;
    private String status;
    private Integer minute;
    private List<MatchEventDTO> events;
}
