package com.reddevil.reddevilanalytics_backend.controller;

import com.reddevil.reddevilanalytics_backend.ai.AIService;
import com.reddevil.reddevilanalytics_backend.domain.Match;
import com.reddevil.reddevilanalytics_backend.domain.MatchPrediction;
import com.reddevil.reddevilanalytics_backend.domain.MatchStatus;
import com.reddevil.reddevilanalytics_backend.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final MatchRepository matchRepository;
    private final AIService aiService;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AtomicLong emitterIdCounter = new AtomicLong(0);

    private static final long SSE_TIMEOUT = 600000L; // 10 minutes

    @GetMapping(value = "/analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalysis(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
        String emitterId = "emitter-" + emitterIdCounter.incrementAndGet();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        log.info("New SSE connection established: {}", emitterId);
        if (lastEventId != null) {
            log.info("Reconnection detected with Last-Event-ID: {}", lastEventId);
        }

        emitters.put(emitterId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE connection completed: {}", emitterId);
            emitters.remove(emitterId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE connection timeout: {}", emitterId);
            emitters.remove(emitterId);
        });

        emitter.onError((ex) -> {
            log.error("SSE connection error for {}: {}", emitterId, ex.getMessage());
            emitters.remove(emitterId);
        });

        // Send initial connection event
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    .id(String.valueOf(System.currentTimeMillis()))
                    .name("connected")
                    .data(Map.of(
                        "message", "Connected to analysis stream",
                        "timestamp", LocalDateTime.now().toString()
                    ));
            emitter.send(event);
        } catch (IOException e) {
            log.error("Error sending initial connection event: {}", e.getMessage());
            emitters.remove(emitterId);
        }

        return emitter;
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void broadcastMatchUpdates() {
        if (emitters.isEmpty()) {
            return;
        }

        log.debug("Broadcasting match updates to {} clients", emitters.size());

        try {
            // Get live or upcoming matches
            List<Match> liveMatches = matchRepository.findByStatusOrderByMatchDateAsc(MatchStatus.LIVE);
            List<Match> upcomingMatches = matchRepository.findByStatusOrderByMatchDateAsc(MatchStatus.SCHEDULED);

            // Broadcast live match updates
            for (Match match : liveMatches) {
                broadcastMatchUpdate(match, "live-update");
            }

            // Broadcast scanning insights for upcoming matches
            for (Match match : upcomingMatches) {
                if (match.getMatchDate().isAfter(LocalDateTime.now()) && 
                    match.getMatchDate().isBefore(LocalDateTime.now().plusDays(7))) {
                    broadcastScanningInsight(match);
                }
            }

        } catch (Exception e) {
            log.error("Error broadcasting match updates: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void broadcastPredictionUpdates() {
        if (emitters.isEmpty()) {
            return;
        }

        log.debug("Broadcasting prediction updates to {} clients", emitters.size());

        try {
            // Get upcoming matches for prediction updates
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextWeek = now.plusDays(7);
            List<Match> upcomingMatches = matchRepository.findByMatchDateBetween(now, nextWeek);

            for (Match match : upcomingMatches) {
                try {
                    MatchPrediction prediction = aiService.getPrediction(match.getId());
                    broadcastPredictionUpdate(match, prediction);
                } catch (Exception e) {
                    log.warn("Error getting prediction for match {}: {}", match.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error broadcasting prediction updates: {}", e.getMessage());
        }
    }

    private void broadcastMatchUpdate(Match match, String eventType) {
        Map<String, Object> data = Map.of(
            "matchId", match.getId(),
            "homeTeam", match.getHomeTeam().getName(),
            "awayTeam", match.getAwayTeam().getName(),
            "homeScore", match.getHomeScore() != null ? match.getHomeScore() : 0,
            "awayScore", match.getAwayScore() != null ? match.getAwayScore() : 0,
            "status", match.getStatus().toString(),
            "timestamp", LocalDateTime.now().toString()
        );

        broadcastToAll(eventType, data);
    }

    private void broadcastScanningInsight(Match match) {
        Map<String, Object> data = Map.of(
            "matchId", match.getId(),
            "message", String.format("Analyzing match %d: %s vs %s", 
                match.getId(), 
                match.getHomeTeam().getName(), 
                match.getAwayTeam().getName()),
            "matchDate", match.getMatchDate().toString(),
            "timestamp", LocalDateTime.now().toString()
        );

        broadcastToAll("scanning-insight", data);
    }

    private void broadcastPredictionUpdate(Match match, MatchPrediction prediction) {
        Map<String, Object> data = Map.of(
            "matchId", match.getId(),
            "homeTeam", match.getHomeTeam().getName(),
            "awayTeam", match.getAwayTeam().getName(),
            "homeWinProbability", prediction.getHomeWinProbability(),
            "drawProbability", prediction.getDrawProbability(),
            "awayWinProbability", prediction.getAwayWinProbability(),
            "predictedHomeScore", prediction.getPredictedHomeScore(),
            "predictedAwayScore", prediction.getPredictedAwayScore(),
            "confidenceScore", prediction.getConfidenceScore(),
            "timestamp", LocalDateTime.now().toString()
        );

        broadcastToAll("prediction-update", data);
    }

    private void broadcastToAll(String eventName, Object data) {
        List<String> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach((id, emitter) -> {
            try {
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .name(eventName)
                        .data(data);
                emitter.send(event);
            } catch (IOException e) {
                log.warn("Failed to send event to emitter {}: {}", id, e.getMessage());
                deadEmitters.add(id);
            }
        });

        // Remove dead emitters
        deadEmitters.forEach(emitters::remove);
        if (!deadEmitters.isEmpty()) {
            log.info("Removed {} dead emitters", deadEmitters.size());
        }
    }

    @GetMapping("/health")
    public Map<String, Object> getStreamHealth() {
        return Map.of(
            "activeConnections", emitters.size(),
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
