package com.reddevil.reddevilanalytics_backend.provider.client;

import com.reddevil.reddevilanalytics_backend.provider.dto.AssetDTO;

public interface AssetProviderClient {
    AssetDTO getTeamAssets(String teamName);
    AssetDTO getPlayerAssets(String playerName);
}
