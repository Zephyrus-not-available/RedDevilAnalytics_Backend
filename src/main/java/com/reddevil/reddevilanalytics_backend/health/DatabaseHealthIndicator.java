package com.reddevil.reddevilanalytics_backend.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> details = new HashMap<>();
            
            details.put("database", connection.getMetaData().getDatabaseProductName());
            details.put("version", connection.getMetaData().getDatabaseProductVersion());
            details.put("url", connection.getMetaData().getURL());
            
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1");
                details.put("connectivity", "OK");
            }

            Map<String, Integer> tableCounts = getTableCounts(connection);
            details.put("tableCounts", tableCounts);
            details.put("totalRecords", tableCounts.values().stream().mapToInt(Integer::intValue).sum());

            return Health.up().withDetails(details).build();

        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }

    private Map<String, Integer> getTableCounts(Connection connection) {
        Map<String, Integer> counts = new HashMap<>();
        
        String[] tables = {
            "teams", "players", "matches", "competitions", 
            "seasons", "standings", "squad_members",
            "team_assets", "player_assets", "match_predictions",
            "external_refs"
        };

        for (String table : tables) {
            try (Statement statement = connection.createStatement()) {
                String query = String.format("SELECT COUNT(*) FROM %s", table);
                ResultSet rs = statement.executeQuery(query);
                if (rs.next()) {
                    counts.put(table, rs.getInt(1));
                }
            } catch (Exception e) {
                log.warn("Failed to get count for table: {}", table, e);
                counts.put(table, -1);
            }
        }

        return counts;
    }
}
