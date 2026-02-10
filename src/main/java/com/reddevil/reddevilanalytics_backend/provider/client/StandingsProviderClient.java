package com.reddevil.reddevilanalytics_backend.provider.client;

import com.reddevil.reddevilanalytics_backend.provider.dto.StandingDTO;

import java.util.List;

public interface StandingsProviderClient {
    List<StandingDTO> getStandings(String competitionId, String seasonId);
}
