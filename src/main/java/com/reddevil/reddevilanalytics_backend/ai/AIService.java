package com.reddevil.reddevilanalytics_backend.ai;

import com.reddevil.reddevilanalytics_backend.domain.Match;
import com.reddevil.reddevilanalytics_backend.domain.MatchPrediction;
import com.reddevil.reddevilanalytics_backend.repository.MatchPredictionRepository;
import com.reddevil.reddevilanalytics_backend.repository.MatchRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient.Builder webClientBuilder;
    private final MatchPredictionRepository matchPredictionRepository;
    private final MatchRepository matchRepository;

    @Value("${providers.ai-service.base-url}")
    private String aiServiceBaseUrl;

    @Value("${providers.ai-service.enabled}")
    private boolean aiServiceEnabled;

    @Cacheable(value = "predictions", key = "#matchId")
    @CircuitBreaker(name = "aiService", fallbackMethod = "calculateFormBasedPrediction")
    public MatchPrediction getPrediction(Long matchId) {
        log.info("Getting AI prediction for match ID: {}", matchId);

        // Check if prediction already exists
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));
        
        return matchPredictionRepository.findByMatch(match)
                .orElseGet(() -> generateNewPrediction(match));
    }

    private MatchPrediction generateNewPrediction(Match match) {
        if (!aiServiceEnabled) {
            log.warn("AI Service is disabled, using form-based prediction");
            return calculateFormBasedPrediction(match.getId(), new RuntimeException("AI Service disabled"));
        }

        try {
            // Build request with team stats
            AIPredictionRequest request = buildPredictionRequest(match);
            
            // Call AI service
            WebClient webClient = webClientBuilder
                    .baseUrl(aiServiceBaseUrl)
                    .build();

            AIPredictionResponse response = webClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AIPredictionResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null) {
                throw new RuntimeException("AI Service returned null response");
            }

            // Save prediction
            MatchPrediction prediction = MatchPrediction.builder()
                    .match(match)
                    .homeWinProbability(BigDecimal.valueOf(response.homeWinProbability()))
                    .drawProbability(BigDecimal.valueOf(response.drawProbability()))
                    .awayWinProbability(BigDecimal.valueOf(response.awayWinProbability()))
                    .predictedHomeScore(BigDecimal.valueOf(response.predictedHomeScore()))
                    .predictedAwayScore(BigDecimal.valueOf(response.predictedAwayScore()))
                    .confidenceScore(BigDecimal.valueOf(response.confidenceScore()))
                    .build();

            return matchPredictionRepository.save(prediction);
        } catch (Exception e) {
            log.error("Error calling AI service for match {}: {}", match.getId(), e.getMessage());
            return calculateFormBasedPrediction(match.getId(), e);
        }
    }

    private AIPredictionRequest buildPredictionRequest(Match match) {
        // Build team stats from match data
        // For now, using mock data - should be enhanced to fetch real stats
        TeamStats homeStats = new TeamStats(10, 5, 3, 30, 15, "WWDLW");
        TeamStats awayStats = new TeamStats(8, 6, 4, 25, 20, "WLDWL");
        
        String venue = determineVenue(match);
        
        return new AIPredictionRequest(
            match.getId(),
            homeStats,
            awayStats,
            venue
        );
    }

    private String determineVenue(Match match) {
        // Simple venue determination - could be enhanced
        if (match.getVenue() != null && match.getVenue().contains(match.getHomeTeam().getName())) {
            return "HOME";
        }
        return "NEUTRAL";
    }

    public MatchPrediction calculateFormBasedPrediction(Long matchId, Throwable throwable) {
        log.warn("Using fallback prediction for match {}: {}", matchId, throwable.getMessage());
        
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with ID: " + matchId));

        // Check if fallback prediction already exists
        return matchPredictionRepository.findByMatch(match)
                .orElseGet(() -> {
                    // Simple heuristic based prediction
                    // Home team advantage: 45% home win, 30% draw, 25% away win
                    MatchPrediction fallbackPrediction = MatchPrediction.builder()
                            .match(match)
                            .homeWinProbability(BigDecimal.valueOf(45.0))
                            .drawProbability(BigDecimal.valueOf(30.0))
                            .awayWinProbability(BigDecimal.valueOf(25.0))
                            .predictedHomeScore(BigDecimal.valueOf(1.5))
                            .predictedAwayScore(BigDecimal.valueOf(1.0))
                            .confidenceScore(BigDecimal.valueOf(0.50))
                            .build();

                    log.info("Saving fallback prediction for match {}", matchId);
                    return matchPredictionRepository.save(fallbackPrediction);
                });
    }
}
