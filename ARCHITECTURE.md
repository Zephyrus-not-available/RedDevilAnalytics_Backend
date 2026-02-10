# Architecture Documentation

This document describes the architecture and design decisions for the RedDevil Analytics Backend application.

## Table of Contents

- [System Overview](#system-overview)
- [Architecture Layers](#architecture-layers)
- [Components](#components)
- [Data Flow](#data-flow)
- [External Integrations](#external-integrations)
- [Database Schema](#database-schema)
- [Caching Strategy](#caching-strategy)
- [Resilience Patterns](#resilience-patterns)
- [Security](#security)
- [Scalability](#scalability)

## System Overview

RedDevil Analytics Backend is a Spring Boot application that aggregates football data from multiple providers, processes it, and exposes it through a RESTful API. The system focuses on Manchester United football analytics, including matches, standings, player statistics, and AI-powered predictions.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend Clients                         │
│            (Web, Mobile, Third-party Applications)               │
└─────────────────────────┬───────────────────────────────────────┘
                          │ HTTPS/REST
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Load Balancer / CDN                        │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    API Layer                              │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │  │
│  │  │ Matches  │ │Standings │ │  Assets  │ │   Sync   │    │  │
│  │  │Controller│ │Controller│ │Controller│ │Controller│    │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    │  │
│  └───────────────────────┬───────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────▼───────────────────────────────────┐  │
│  │                  Service Layer                            │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │  │
│  │  │  Match   │ │Standings │ │  Asset   │ │Ingestion │    │  │
│  │  │ Service  │ │ Service  │ │ Service  │ │ Service  │    │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    │  │
│  └───────────────────────┬───────────────────────────────────┘  │
│                          │                                       │
│  ┌───────────────────────▼───────────────────────────────────┐  │
│  │              External Provider Clients                    │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    │  │
│  │  │   API    │ │ Football │ │  Sports  │ │    AI    │    │  │
│  │  │ Football │ │   Data   │ │    DB    │ │ Service  │    │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    │  │
│  │       │             │             │             │         │  │
│  └───────┼─────────────┼─────────────┼─────────────┼─────────┘  │
│          │             │             │             │             │
│  ┌───────▼─────────────▼─────────────▼─────────────▼─────────┐  │
│  │              Repository Layer (JPA)                       │  │
│  └───────────────────────┬───────────────────────────────────┘  │
└──────────────────────────┼──────────────────────────────────────┘
                           │
         ┌─────────────────┴─────────────────┐
         │                                   │
         ▼                                   ▼
┌──────────────────┐              ┌──────────────────┐
│   PostgreSQL     │              │      Redis       │
│    Database      │              │      Cache       │
└──────────────────┘              └──────────────────┘
         │                                   │
         ▼                                   ▼
┌──────────────────┐              ┌──────────────────┐
│  RDS Backup /    │              │  ElastiCache     │
│  Flyway Mgmt     │              │   (Optional)     │
└──────────────────┘              └──────────────────┘
```

## Architecture Layers

### 1. Controller Layer
**Purpose**: Handle HTTP requests and responses

**Responsibilities**:
- Request validation
- Response formatting
- Exception handling
- API documentation (OpenAPI/Swagger)

**Key Components**:
- `MatchController` - Match-related endpoints
- `StandingsController` - League standings endpoints
- `AssetController` - Media assets endpoints
- `SyncController` - Admin synchronization endpoints
- `StreamingController` - Server-Sent Events for live updates

### 2. Service Layer
**Purpose**: Business logic and orchestration

**Responsibilities**:
- Business rule implementation
- Transaction management
- Data transformation
- Provider orchestration
- Caching logic

**Key Components**:
- `MatchService` - Match data processing
- `StandingsService` - Standings calculations
- `AssetService` - Media asset management
- `IngestionService` - Data synchronization orchestration
- `AIService` - AI prediction integration

### 3. Provider Client Layer
**Purpose**: External API integration

**Responsibilities**:
- API communication
- Rate limiting
- Circuit breaking
- Retry logic
- Data mapping

**Key Components**:
- `ApiFootballClient` - API-Football integration
- `FootballDataClient` - Football-Data.org integration
- `TheSportsDBClient` - TheSportsDB integration
- `RateLimitManager` - Rate limit enforcement

### 4. Repository Layer
**Purpose**: Data persistence

**Responsibilities**:
- Database operations (CRUD)
- Query optimization
- Transaction support

**Key Components**:
- JPA Repositories for all domain entities
- Custom queries for complex operations

### 5. Domain Layer
**Purpose**: Core business entities

**Key Components**:
- `Match` - Match information
- `Team` - Team details
- `Player` - Player information
- `Standing` - League standings
- `Competition` - Competition/League info
- `MatchPrediction` - AI predictions
- `ExternalRef` - External provider mappings

## Components

### Core Components

#### WebClient Configuration
```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024))
            .filter(logRequest())
            .filter(logResponse());
    }
}
```

#### Cache Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Multi-layer caching: Redis + Caffeine
        // Different TTLs for different data types
    }
}
```

#### Circuit Breaker Configuration
```java
@Configuration
public class CircuitBreakerConfig {
    // Resilience4j configuration for each provider
    // Failure rate threshold: 50%
    // Wait duration in open state: 60s
    // Sliding window size: 10 calls
}
```

### Health Indicators

Custom health indicators for monitoring:
- `DatabaseHealthIndicator` - PostgreSQL connectivity
- `RedisHealthIndicator` - Redis connectivity
- `ProviderHealthIndicator` - External API availability

## Data Flow

### 1. Data Ingestion Flow

```
┌─────────────┐
│   Scheduler │
│  (Cron Job) │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ IngestionService│
└──────┬──────────┘
       │
       ├─────────────────────────────────────┐
       │                                     │
       ▼                                     ▼
┌─────────────────┐                 ┌─────────────────┐
│  Fixture Sync   │                 │ Standings Sync  │
└──────┬──────────┘                 └──────┬──────────┘
       │                                    │
       ▼                                    ▼
┌─────────────────────────────────────────────────────┐
│            Provider Clients                         │
│  (with Circuit Breaker & Rate Limiting)             │
└──────┬──────────────────────────────────────────────┘
       │
       ▼
┌─────────────────┐
│  Data Mapping   │
│  & Validation   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│   Repository    │
│   (Database)    │
└─────────────────┘
```

### 2. API Request Flow

```
┌─────────────┐
│   Client    │
│   Request   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   Controller    │
│  (Validation)   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐     Cache Hit?
│    Service      │◄──────────────┐
└──────┬──────────┘                │
       │ Cache Miss                │
       ▼                            │
┌─────────────────┐                │
│   Repository    │                │
│   (Database)    │                │
└──────┬──────────┘                │
       │                            │
       ▼                            │
┌─────────────────┐                │
│   Update Cache  ├────────────────┘
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│    Response     │
└─────────────────┘
```

## External Integrations

### Provider Comparison

| Provider | Coverage | Rate Limit | Cost | Use Case |
|----------|----------|------------|------|----------|
| API-Football | Comprehensive | 100/day | $$ | Primary data source |
| Football-Data.org | Good | 10/min | Free tier | Standings & fixtures |
| TheSportsDB | Media assets | Unlimited | Free | Team/player images |
| AI Service | Predictions | Custom | Internal | Match predictions |

### Provider Failover Strategy

1. **Primary Provider**: API-Football
2. **Fallback 1**: Football-Data.org
3. **Fallback 2**: TheSportsDB (limited data)
4. **Cache**: Serve stale data if all providers fail

```java
public MatchDTO getMatchData(Long matchId) {
    try {
        return apiFootballClient.getMatch(matchId);
    } catch (Exception e1) {
        try {
            return footballDataClient.getMatch(matchId);
        } catch (Exception e2) {
            return cacheManager.getFromCache(matchId);
        }
    }
}
```

## Database Schema

### Core Tables

**matches**
- id (PK)
- home_team_id (FK)
- away_team_id (FK)
- competition_id (FK)
- season_id (FK)
- kickoff_time
- status
- home_score
- away_score
- venue

**teams**
- id (PK)
- name
- short_name
- logo_url
- founded_year

**players**
- id (PK)
- name
- position
- nationality
- birth_date
- photo_url

**standings**
- id (PK)
- team_id (FK)
- competition_id (FK)
- season_id (FK)
- position
- played
- won
- drawn
- lost
- points

**external_refs**
- id (PK)
- entity_type (ENUM)
- internal_id
- provider (ENUM)
- external_id

### Relationships

```
teams 1──────* matches (home_team)
teams 1──────* matches (away_team)
teams 1──────* standings
teams 1──────* players (squad_members)
competitions 1──────* matches
competitions 1──────* standings
```

## Caching Strategy

### Cache Layers

1. **L1 Cache**: Caffeine (In-Memory)
   - Fastest access
   - Limited by heap size
   - Per-instance

2. **L2 Cache**: Redis (Distributed)
   - Shared across instances
   - Persistence option
   - Scalable

### Cache Keys

```
standings:{competitionId}:{season}
match:{matchId}
team-assets:{teamId}
player-assets:{playerId}
live-match:{matchId}
next-match:{teamId}
```

### TTL Strategy

| Data Type | TTL | Reason |
|-----------|-----|--------|
| Standings | 30 min | Updates periodically |
| Fixtures | 1 hour | Rarely changes |
| Live matches | 30 sec | Real-time updates |
| Team assets | 7 days | Rarely changes |
| Player assets | 7 days | Rarely changes |
| Next match | 5 min | Moderate frequency |

## Resilience Patterns

### Circuit Breaker

Prevents cascading failures when external services are down.

**States**:
- **Closed**: Normal operation
- **Open**: All requests fail fast (60s)
- **Half-Open**: Test with limited requests

**Configuration**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      apiFootball:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        slidingWindowSize: 10
```

### Retry Mechanism

Automatic retries with exponential backoff.

```yaml
resilience4j:
  retry:
    instances:
      apiFootball:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

### Rate Limiting

Token bucket algorithm per provider.

```java
public class RateLimitManager {
    private final Map<Provider, Bucket> buckets;
    
    public boolean tryConsume(Provider provider) {
        return buckets.get(provider).tryConsume(1);
    }
}
```

## Security

### API Key Authentication

Admin endpoints require API key in header:
```
X-Admin-API-Key: your-secret-key
```

### CORS Configuration

Configured allowed origins for cross-origin requests:
```yaml
cors:
  allowed-origins: https://yourdomain.com
  allowed-methods: GET,POST,PUT,DELETE
  allow-credentials: true
```

### Database Security

- SSL/TLS connections
- Prepared statements (SQL injection prevention)
- Connection pooling with HikariCP
- Least privilege access

### Secrets Management

- Environment variables for configuration
- No secrets in code or version control
- Use cloud provider secrets managers in production

## Scalability

### Horizontal Scaling

Application is stateless and can scale horizontally:
- Multiple instances behind load balancer
- Shared Redis cache across instances
- Database connection pooling

### Vertical Scaling

Resource allocation per instance:
- **Memory**: 512MB - 2GB
- **CPU**: 0.5 - 2 cores
- **Connections**: 10 database connections per instance

### Database Scaling

- Read replicas for read-heavy operations
- Connection pooling (HikariCP)
- Query optimization and indexing
- Partitioning (by season/date)

### Caching Scaling

- Redis cluster for distributed caching
- Cache warming on startup
- TTL-based cache invalidation

## Monitoring and Observability

### Metrics

Exposed via Spring Boot Actuator:
- JVM metrics (memory, threads, GC)
- HTTP metrics (request count, latency)
- Database metrics (connection pool)
- Cache metrics (hit rate, evictions)
- Custom business metrics

### Logging

Structured logging with context:
- Request ID tracking
- User context
- Performance markers
- Error stack traces

### Health Checks

Multiple health indicators:
- Application readiness
- Database connectivity
- Redis connectivity
- External provider status
- Circuit breaker states

## Technology Decisions

### Why Spring Boot?

- Mature ecosystem
- Production-ready features (Actuator)
- Excellent database integration (JPA)
- Strong community support

### Why PostgreSQL?

- ACID compliance
- JSON support
- Performance
- Open source

### Why Redis?

- Fast in-memory cache
- Distributed caching support
- Rich data structures
- Persistence options

### Why Resilience4j?

- Lightweight
- Spring Boot integration
- Modern reactive support
- Comprehensive patterns

## Future Enhancements

- [ ] GraphQL API support
- [ ] WebSocket for real-time updates
- [ ] Machine learning for predictions
- [ ] Multi-tenancy support
- [ ] Event sourcing for analytics
- [ ] API versioning
- [ ] Rate limiting per client
- [ ] Advanced search capabilities

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Resilience4j Guide](https://resilience4j.readme.io/)
- [Redis Best Practices](https://redis.io/docs/management/optimization/)
- [PostgreSQL Performance](https://www.postgresql.org/docs/current/performance-tips.html)
