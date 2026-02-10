package com.reddevil.reddevilanalytics_backend.ai;

public record AIPredictionResponse(
    Double homeWinProbability,
    Double drawProbability,
    Double awayWinProbability,
    Double predictedHomeScore,
    Double predictedAwayScore,
    Double confidenceScore
) {}
