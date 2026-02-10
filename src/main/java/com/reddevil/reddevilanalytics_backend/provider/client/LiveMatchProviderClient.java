package com.reddevil.reddevilanalytics_backend.provider.client;

import com.reddevil.reddevilanalytics_backend.provider.dto.LiveMatchDTO;

import java.util.List;

public interface LiveMatchProviderClient {
    List<LiveMatchDTO> getLiveMatches(String competitionId);
    LiveMatchDTO getLiveMatchById(String matchId);
}
