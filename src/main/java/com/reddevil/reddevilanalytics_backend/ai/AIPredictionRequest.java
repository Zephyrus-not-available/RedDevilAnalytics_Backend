package com.reddevil.reddevilanalytics_backend.ai;

public record AIPredictionRequest(
    Long matchId,
    TeamStats homeTeamStats,
    TeamStats awayTeamStats,
    String venue
) {}
