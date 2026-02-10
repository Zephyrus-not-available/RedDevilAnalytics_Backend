package com.reddevil.reddevilanalytics_backend.provider.client;

import com.reddevil.reddevilanalytics_backend.provider.dto.FixtureDTO;

import java.util.List;

public interface FixtureProviderClient {
    List<FixtureDTO> getFixtures(String competitionId, String seasonId);
    FixtureDTO getFixtureById(String fixtureId);
    FixtureDTO getNextFixture(String teamId, String seasonId);
}
