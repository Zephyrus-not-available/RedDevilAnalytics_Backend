# RedDevil Analytics Backend

A comprehensive Spring Boot backend application for Manchester United football analytics, providing real-time match data, player statistics, team standings, and AI-powered match predictions.

## 🚀 Features

- **Multi-Provider Data Integration**: Aggregates data from multiple football data providers (API-Football, Football-Data.org, TheSportsDB)
- **Real-time Match Updates**: Live score tracking and match event streaming
- **Team & Player Analytics**: Comprehensive statistics and performance metrics
- **AI-Powered Predictions**: Integration with ML service for match outcome predictions
- **Caching Strategy**: Multi-layer caching with Redis and Caffeine for optimal performance
- **Circuit Breakers**: Resilience4j implementation for fault-tolerant external API calls
- **Rate Limiting**: Built-in rate limiting to respect API quotas
- **Health Monitoring**: Spring Actuator endpoints for application health and metrics
- **Database Migrations**: Flyway-managed PostgreSQL schema versioning
- **API Documentation**: Interactive OpenAPI/Swagger UI

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **PostgreSQL 14+**
- **Redis 6+** (optional, for caching)
- **Docker & Docker Compose** (optional, for containerized setup)

## 🛠️ Technology Stack

- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Caching**: Redis + Caffeine
- **Migration**: Flyway
- **Resilience**: Resilience4j (Circuit Breaker, Retry)
- **API Clients**: Spring WebFlux (WebClient)
- **Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven
- **Monitoring**: Spring Boot Actuator + Prometheus

## 📦 Installation

### Option 1: Local Development Setup

1. **Clone the repository**
```bash
git clone https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend.git
cd RedDevilAnalytics_Backend
```

2. **Set up PostgreSQL database**
```bash
# Create database
createdb reddevil_analytics

# Or using psql
psql -U postgres -c "CREATE DATABASE reddevil_analytics;"
```

3. **Configure environment variables**
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your configuration
nano .env
```

4. **Build the project**
```bash
./mvnw clean install
```

5. **Run the application**
```bash
./mvnw spring-boot:run
```

### Option 2: Docker Compose Setup

```bash
# Start all services (PostgreSQL, Redis, Application)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

## ⚙️ Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/reddevil_analytics` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `your_password` |
| `API_FOOTBALL_KEY` | API-Football API key | `your_api_key` |
| `FOOTBALL_DATA_KEY` | Football-Data.org API key | `your_api_key` |
| `THESPORTSDB_KEY` | TheSportsDB API key | `3` (free tier) |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | `8080` |
| `AI_SERVICE_URL` | AI prediction service URL | `http://localhost:8001` |
| `AI_SERVICE_ENABLED` | Enable AI predictions | `false` |
| `CORS_ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000,http://localhost:5173` |
| `ADMIN_API_KEY` | Admin API authentication key | `admin-secret-key` |

See `.env.example` for a complete list of configuration options.

## 📚 API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Main Endpoints

#### Matches
- `GET /api/matches/next` - Get next Manchester United match
- `GET /api/matches/live` - Get live match updates
- `GET /api/matches/{id}` - Get match details
- `GET /api/matches/predictions/{matchId}` - Get AI match prediction

#### Standings
- `GET /api/standings/premier-league` - Get Premier League standings
- `GET /api/standings/{competitionId}` - Get standings for specific competition

#### Assets (Images)
- `GET /api/assets/team/{teamId}` - Get team badge/images
- `GET /api/assets/player/{playerId}` - Get player images

#### Sync & Admin
- `POST /api/sync/fixtures` - Trigger fixture data sync (requires admin key)
- `POST /api/sync/standings` - Trigger standings sync (requires admin key)
- `GET /api/sync/status` - Get sync status

#### Health & Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/info` - Application information

## 🔧 Development

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=MatchServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Database Migrations

Migrations are managed by Flyway and located in `src/main/resources/db/migration/`.

```bash
# Flyway migrations run automatically on startup
# To manually trigger migration:
./mvnw flyway:migrate

# Check migration status:
./mvnw flyway:info

# Rollback (if needed):
./mvnw flyway:clean
```

### Code Style

```bash
# Format code (if formatter plugin is configured)
./mvnw spring-javaformat:apply

# Check code style
./mvnw verify
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/reddevil/reddevilanalytics_backend/
│   │   ├── ai/                    # AI prediction service integration
│   │   ├── config/                # Spring configuration classes
│   │   ├── controller/            # REST API controllers
│   │   ├── domain/                # JPA entity models
│   │   ├── dto/                   # Data Transfer Objects
│   │   ├── health/                # Custom health indicators
│   │   ├── provider/              # External API provider clients
│   │   │   ├── apifootball/       # API-Football integration
│   │   │   ├── footballdata/      # Football-Data.org integration
│   │   │   ├── thesportsdb/       # TheSportsDB integration
│   │   │   ├── client/            # Provider client interfaces
│   │   │   ├── dto/               # Provider DTOs
│   │   │   └── ratelimit/         # Rate limiting logic
│   │   ├── repository/            # JPA repositories
│   │   └── service/               # Business logic services
│   └── resources/
│       ├── application.yml        # Main configuration
│       ├── application-dev.yml    # Development profile
│       ├── application-test.yml   # Test profile
│       └── db/migration/          # Flyway migration scripts
└── test/                          # Test classes
```

## 🚀 Deployment

### Production Considerations

1. **Environment Variables**: Use secure secrets management (e.g., AWS Secrets Manager, HashiCorp Vault)
2. **Database**: Use managed PostgreSQL (e.g., AWS RDS, Azure Database)
3. **Caching**: Use managed Redis (e.g., AWS ElastiCache, Azure Cache)
4. **Monitoring**: Enable Prometheus metrics and connect to Grafana
5. **Logging**: Configure centralized logging (e.g., ELK stack, CloudWatch)
6. **SSL/TLS**: Enable HTTPS with proper certificates
7. **Rate Limiting**: Configure rate limits per API provider quotas

### Docker Deployment

```bash
# Build Docker image
docker build -t reddevil-analytics-backend:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  --env-file .env \
  --name reddevil-backend \
  reddevil-analytics-backend:latest
```

### Kubernetes Deployment

See `k8s/` directory (if available) for Kubernetes manifests.

## 📊 Monitoring & Health Checks

The application exposes several health check endpoints:

- `/actuator/health` - Overall health status
- `/actuator/health/db` - Database connectivity
- `/actuator/health/redis` - Redis connectivity
- `/actuator/health/providers` - External API providers status
- `/actuator/metrics` - Prometheus metrics

### Circuit Breaker Status

Monitor circuit breaker states through the health endpoint:
```bash
curl http://localhost:8080/actuator/health/circuitbreakers
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Zephyrus** - [Zephyrus-not-available](https://github.com/Zephyrus-not-available)

## 🙏 Acknowledgments

- Manchester United Football Club
- API-Football for comprehensive football data
- Football-Data.org for standings and fixtures
- TheSportsDB for media assets
- Spring Boot community

## 📞 Support

For issues, questions, or contributions:
- **Issues**: [GitHub Issues](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/discussions)

## 🗺️ Roadmap

- [ ] Add WebSocket support for real-time match updates
- [ ] Implement user authentication and authorization
- [ ] Add player comparison analytics
- [ ] Integrate additional data providers
- [ ] Add GraphQL API support
- [ ] Implement data export features (CSV, JSON)
- [ ] Add machine learning model for injury prediction
- [ ] Create admin dashboard
