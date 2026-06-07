package com.projectmanagementsaas.health.service;

import com.projectmanagementsaas.health.dto.HealthResponse;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthService {
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public HealthService(DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public HealthResponse health() {
        Map<String, String> checks = new LinkedHashMap<>();
        checks.put("database", databaseCheck());
        checks.put("redis", redisCheck());
        boolean healthy = checks.values().stream().allMatch("UP"::equals);
        return new HealthResponse(healthy ? "UP" : "DOWN", Instant.now(), checks);
    }

    private String databaseCheck() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private String redisCheck() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            return connection.ping() == null ? "DOWN" : "UP";
        } catch (Exception exception) {
            return "DOWN";
        }
    }
}
