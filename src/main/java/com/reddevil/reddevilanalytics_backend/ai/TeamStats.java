package com.reddevil.reddevilanalytics_backend.ai;

public record TeamStats(
    Integer wins,
    Integer draws,
    Integer losses,
    Integer goalsFor,
    Integer goalsAgainst,
    String form
) {}
