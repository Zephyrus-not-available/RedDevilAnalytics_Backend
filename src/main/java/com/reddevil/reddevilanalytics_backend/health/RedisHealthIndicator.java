package com.reddevil.reddevilanalytics_backend.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(
            RedisConnectionFactory redisConnectionFactory,
            RedisTemplate<String, Object> redisTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            RedisConnection connection = redisConnectionFactory.getConnection();
            
            try {
                connection.ping();
                details.put("connectivity", "OK");

                Properties info = connection.info();
                if (info != null && !info.isEmpty()) {
                    details.put("version", info.getProperty("redis_version", "unknown"));
                    details.put("mode", info.getProperty("redis_mode", "unknown"));
                    details.put("uptime", info.getProperty("uptime_in_seconds", "unknown") + "s");
                    details.put("connectedClients", info.getProperty("connected_clients", "unknown"));
                    
                    String usedMemory = info.getProperty("used_memory_human", "unknown");
                    String maxMemory = info.getProperty("maxmemory_human", "0");
                    details.put("usedMemory", usedMemory);
                    details.put("maxMemory", maxMemory.equals("0") ? "unlimited" : maxMemory);
                }

                Properties keyspaceInfo = connection.info("keyspace");
                if (keyspaceInfo != null) {
                    int totalKeys = 0;
                    for (String key : keyspaceInfo.stringPropertyNames()) {
                        if (key.startsWith("db")) {
                            String value = keyspaceInfo.getProperty(key);
                            String keysStr = value.split(",")[0].split("=")[1];
                            totalKeys += Integer.parseInt(keysStr);
                        }
                    }
                    details.put("totalKeys", totalKeys);
                }

                Properties stats = connection.info("stats");
                if (stats != null) {
                    details.put("totalConnectionsReceived", stats.getProperty("total_connections_received", "unknown"));
                    details.put("totalCommandsProcessed", stats.getProperty("total_commands_processed", "unknown"));
                    details.put("keyspaceHits", stats.getProperty("keyspace_hits", "unknown"));
                    details.put("keyspaceMisses", stats.getProperty("keyspace_misses", "unknown"));
                    
                    try {
                        long hits = Long.parseLong(stats.getProperty("keyspace_hits", "0"));
                        long misses = Long.parseLong(stats.getProperty("keyspace_misses", "0"));
                        long total = hits + misses;
                        if (total > 0) {
                            double hitRate = (hits * 100.0) / total;
                            details.put("cacheHitRate", String.format("%.2f%%", hitRate));
                        }
                    } catch (NumberFormatException e) {
                        log.debug("Could not calculate hit rate", e);
                    }
                }

                return Health.up().withDetails(details).build();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("errorType", e.getClass().getSimpleName())
                    .build();
        }
    }
}
