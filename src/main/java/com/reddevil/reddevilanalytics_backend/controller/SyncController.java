package com.reddevil.reddevilanalytics_backend.controller;

import com.reddevil.reddevilanalytics_backend.service.IngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Data synchronization endpoints (Admin only)")
public class SyncController {

    private final IngestionService ingestionService;

    @Value("${security.admin.api-key}")
    private String adminApiKey;

    @PostMapping("/fixtures")
    @Operation(summary = "Sync fixtures", 
               description = "Manually trigger fixture synchronization for a competition (Admin only)")
    public ResponseEntity<Map<String, String>> syncFixtures(
            @Parameter(description = "Competition ID") @RequestParam Long competitionId,
            @RequestHeader("X-Admin-Key") String apiKey) {
        
        if (!validateApiKey(apiKey)) {
            log.warn("Invalid API key provided for fixture sync");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid API key"));
        }
        
        log.info("Manual fixture sync triggered for competition ID: {}", competitionId);
        
        try {
            ingestionService.syncCompetitionFixtures(competitionId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Fixtures synced successfully for competition ID: " + competitionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing fixtures: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to sync fixtures: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/standings")
    @Operation(summary = "Sync standings", 
               description = "Manually trigger standings synchronization for a competition (Admin only)")
    public ResponseEntity<Map<String, String>> syncStandings(
            @Parameter(description = "Competition ID") @RequestParam Long competitionId,
            @RequestHeader("X-Admin-Key") String apiKey) {
        
        if (!validateApiKey(apiKey)) {
            log.warn("Invalid API key provided for standings sync");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid API key"));
        }
        
        log.info("Manual standings sync triggered for competition ID: {}", competitionId);
        
        try {
            ingestionService.syncCompetitionStandings(competitionId);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Standings synced successfully for competition ID: " + competitionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing standings: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to sync standings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/all")
    @Operation(summary = "Sync all data", 
               description = "Manually trigger synchronization of all data (Admin only)")
    public ResponseEntity<Map<String, String>> syncAll(
            @RequestHeader("X-Admin-Key") String apiKey) {
        
        if (!validateApiKey(apiKey)) {
            log.warn("Invalid API key provided for full sync");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid API key"));
        }
        
        log.info("Manual full sync triggered");
        
        try {
            ingestionService.syncAll();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "All data synced successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing all data: {}", e.getMessage(), e);
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to sync all data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean validateApiKey(String apiKey) {
        return apiKey != null && apiKey.equals(adminApiKey);
    }
}
