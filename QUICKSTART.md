# Quick Start Guide

Get RedDevil Analytics Backend up and running in 5 minutes!

## Prerequisites

- Java 17+ installed
- Docker and Docker Compose installed (recommended)
- OR PostgreSQL 14+ and Redis 6+ installed locally

## Option 1: Docker Compose (Recommended)

The fastest way to get started with all dependencies included.

### 1. Clone the repository
```bash
git clone https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend.git
cd RedDevilAnalytics_Backend
```

### 2. Configure environment variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env and add your API keys
nano .env
```

**Minimum required variables:**
```bash
API_FOOTBALL_KEY=your_api_key_here
FOOTBALL_DATA_KEY=your_api_key_here
```

Get your free API keys:
- API-Football: https://rapidapi.com/api-sports/api/api-football
- Football-Data.org: https://www.football-data.org/client/register

### 3. Start all services
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis cache
- Spring Boot application
- Adminer (database UI on port 8081)
- Redis Commander (Redis UI on port 8082)

### 4. Verify it's running
```bash
# Check application health
curl http://localhost:8080/actuator/health

# View logs
docker-compose logs -f app
```

### 5. Access the application
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Database UI**: http://localhost:8081 (Adminer)
- **Redis UI**: http://localhost:8082 (Redis Commander)

### 6. Try some API calls
```bash
# Get Premier League standings
curl http://localhost:8080/api/standings/premier-league

# Get next Manchester United match
curl http://localhost:8080/api/matches/next

# Get live matches
curl http://localhost:8080/api/matches/live
```

### 7. Stop the services
```bash
docker-compose down
```

## Option 2: Local Development (Without Docker)

If you prefer to run services locally without Docker.

### 1. Install dependencies

**PostgreSQL:**
```bash
# Ubuntu/Debian
sudo apt-get install postgresql-14

# macOS
brew install postgresql@14

# Create database
createdb reddevil_analytics
```

**Redis:**
```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# macOS
brew install redis

# Start Redis
redis-server
```

### 2. Clone and configure
```bash
git clone https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend.git
cd RedDevilAnalytics_Backend

# Copy environment template
cp .env.example .env
```

### 3. Update database configuration
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/reddevil_analytics
    username: your_postgres_user
    password: your_postgres_password
```

### 4. Build and run
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

### 5. Verify
```bash
curl http://localhost:8080/actuator/health
```

## Option 3: IDE Setup (IntelliJ IDEA / Eclipse)

### IntelliJ IDEA

1. **Open Project**: File → Open → Select the project folder
2. **Wait for indexing** to complete
3. **Configure SDK**: File → Project Structure → Project SDK (Java 17)
4. **Set up database**: 
   - Open Database tool window
   - Add PostgreSQL datasource
   - Create `reddevil_analytics` database
5. **Configure Run Configuration**:
   - Click "Add Configuration"
   - Add Spring Boot configuration
   - Set main class: `RedDevilAnalyticsBackendApplication`
   - Set environment variables from `.env.example`
6. **Run the application**: Click the green play button

### Eclipse

1. **Import Project**: File → Import → Maven → Existing Maven Projects
2. **Select project directory**
3. **Configure JRE**: Right-click project → Build Path → Configure Build Path → Java 17
4. **Set up Run Configuration**:
   - Run → Run Configurations → Spring Boot App
   - Main class: `RedDevilAnalyticsBackendApplication`
   - Add environment variables
5. **Run the application**

### VS Code

1. **Install Extensions**:
   - Java Extension Pack
   - Spring Boot Extension Pack
2. **Open Folder**: File → Open Folder → Select project
3. **Configure launch.json**:
```json
{
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.reddevil.reddevilanalytics_backend.RedDevilAnalyticsBackendApplication",
      "projectName": "RedDevilAnalytics_Backend",
      "env": {
        "SPRING_DATASOURCE_URL": "jdbc:postgresql://localhost:5432/reddevil_analytics",
        "API_FOOTBALL_KEY": "your_key"
      }
    }
  ]
}
```
4. **Run**: Press F5

## Next Steps

### 1. Sync Data
Trigger initial data synchronization (requires admin API key):
```bash
# Sync fixtures
curl -X POST http://localhost:8080/api/sync/fixtures \
  -H "X-Admin-API-Key: admin-secret-key"

# Sync standings
curl -X POST http://localhost:8080/api/sync/standings \
  -H "X-Admin-API-Key: admin-secret-key"
```

### 2. Explore the API
Open Swagger UI to explore all available endpoints:
```
http://localhost:8080/swagger-ui.html
```

### 3. Monitor the Application
Check application health and metrics:
```bash
# Overall health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Redis health
curl http://localhost:8080/actuator/health/redis

# Application metrics
curl http://localhost:8080/actuator/metrics
```

### 4. View Logs
```bash
# Docker
docker-compose logs -f app

# Local
tail -f logs/application.log
```

## Troubleshooting

### Application won't start

**Error: "Cannot connect to database"**
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Check credentials in application.yml or .env
```

**Error: "Cannot connect to Redis"**
```bash
# Check Redis is running
redis-cli ping
# Should return PONG

# Start Redis if not running
redis-server
```

### Port already in use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process (replace PID with actual process ID)
kill -9 <PID>

# Or change the application port
export SERVER_PORT=8081
```

### API keys not working

1. Verify keys are correct in `.env` or `application.yml`
2. Check provider websites for quota limits
3. Review application logs for specific errors:
```bash
docker-compose logs app | grep -i "api"
```

### Database migration fails

```bash
# Reset database (WARNING: deletes all data)
dropdb reddevil_analytics
createdb reddevil_analytics

# Restart application to run migrations
docker-compose restart app
```

## Common Tasks

### Reset the database
```bash
# Stop application
docker-compose down

# Remove volumes (deletes data)
docker-compose down -v

# Start fresh
docker-compose up -d
```

### View database
```bash
# Using psql
psql -h localhost -U postgres -d reddevil_analytics

# Or use Adminer
open http://localhost:8081
```

### View Redis cache
```bash
# Using redis-cli
redis-cli
> KEYS *
> GET standings:2021:2023

# Or use Redis Commander
open http://localhost:8082
```

### Update dependencies
```bash
./mvnw versions:display-dependency-updates
./mvnw versions:use-latest-versions
```

## Development Tips

### Enable hot reload
```bash
# Add spring-boot-devtools dependency
# Application will auto-restart on code changes
./mvnw spring-boot:run
```

### Run tests
```bash
# All tests
./mvnw test

# Specific test
./mvnw test -Dtest=MatchServiceTest

# With coverage
./mvnw test jacoco:report
```

### Format code
```bash
# If formatter is configured
./mvnw spring-javaformat:apply
```

### Generate API documentation
```bash
# Start application and visit
open http://localhost:8080/swagger-ui.html

# Download OpenAPI JSON
curl http://localhost:8080/v3/api-docs > openapi.json
```

## Resources

- **Full Documentation**: [README.md](README.md)
- **API Reference**: [API.md](API.md)
- **Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md)
- **Contributing**: [CONTRIBUTING.md](CONTRIBUTING.md)
- **Deployment**: [DEPLOYMENT.md](DEPLOYMENT.md)

## Getting Help

- **Issues**: [GitHub Issues](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/discussions)

## What's Next?

1. ✅ Application is running
2. 📚 Read the [API Documentation](API.md)
3. 🔧 Check [CONTRIBUTING.md](CONTRIBUTING.md) to contribute
4. 🏗️ Learn about the [Architecture](ARCHITECTURE.md)
5. 🚀 Deploy to production using [DEPLOYMENT.md](DEPLOYMENT.md)

Happy coding! ⚽
