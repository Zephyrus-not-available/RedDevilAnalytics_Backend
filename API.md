# API Documentation

This document provides detailed information about the RedDevil Analytics Backend API endpoints.

## Base URL

```
http://localhost:8080/api
```

For production, replace with your actual domain.

## Authentication

Some endpoints require authentication via an API key:

```
Header: X-Admin-API-Key: your-admin-api-key
```

## Interactive Documentation

The application provides interactive API documentation via Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Endpoints Overview

### Matches API

#### Get Next Match
Retrieve the next scheduled Manchester United match.

```http
GET /api/matches/next
```

**Response:**
```json
{
  "id": 12345,
  "homeTeam": "Manchester United",
  "awayTeam": "Liverpool",
  "kickoffTime": "2024-03-15T15:00:00Z",
  "venue": "Old Trafford",
  "competition": "Premier League",
  "status": "SCHEDULED"
}
```

#### Get Live Matches
Get currently ongoing matches.

```http
GET /api/matches/live
```

**Response:**
```json
{
  "matches": [
    {
      "id": 12345,
      "homeTeam": "Manchester United",
      "homeScore": 2,
      "awayTeam": "Chelsea",
      "awayScore": 1,
      "minute": 67,
      "status": "IN_PLAY"
    }
  ]
}
```

#### Get Match Details
Retrieve detailed information about a specific match.

```http
GET /api/matches/{id}
```

**Path Parameters:**
- `id` (required): Match ID

**Response:**
```json
{
  "id": 12345,
  "homeTeam": {
    "id": 33,
    "name": "Manchester United",
    "logo": "url_to_logo"
  },
  "awayTeam": {
    "id": 34,
    "name": "Liverpool",
    "logo": "url_to_logo"
  },
  "score": {
    "home": 2,
    "away": 1,
    "halftime": {
      "home": 1,
      "away": 0
    }
  },
  "status": "FINISHED",
  "kickoffTime": "2024-03-15T15:00:00Z",
  "venue": "Old Trafford",
  "events": [
    {
      "minute": 23,
      "type": "GOAL",
      "player": "Bruno Fernandes",
      "team": "Manchester United"
    }
  ]
}
```

#### Get Match Prediction
Get AI-powered prediction for a specific match.

```http
GET /api/matches/predictions/{matchId}
```

**Path Parameters:**
- `matchId` (required): Match ID

**Response:**
```json
{
  "matchId": 12345,
  "homeTeam": "Manchester United",
  "awayTeam": "Liverpool",
  "prediction": {
    "homeWinProbability": 0.45,
    "drawProbability": 0.25,
    "awayWinProbability": 0.30,
    "predictedScore": "2-1"
  },
  "confidence": 0.78,
  "generatedAt": "2024-03-15T10:00:00Z"
}
```

### Standings API

#### Get Premier League Standings
Get current Premier League table standings.

```http
GET /api/standings/premier-league
```

**Response:**
```json
{
  "competition": "Premier League",
  "season": "2023/2024",
  "standings": [
    {
      "position": 1,
      "team": {
        "id": 33,
        "name": "Manchester United",
        "logo": "url_to_logo"
      },
      "played": 30,
      "won": 22,
      "drawn": 5,
      "lost": 3,
      "goalsFor": 68,
      "goalsAgainst": 25,
      "goalDifference": 43,
      "points": 71,
      "form": "WWDWW"
    }
  ],
  "lastUpdated": "2024-03-15T12:00:00Z"
}
```

#### Get Competition Standings
Get standings for a specific competition.

```http
GET /api/standings/{competitionId}
```

**Path Parameters:**
- `competitionId` (required): Competition ID

**Query Parameters:**
- `season` (optional): Season year (e.g., 2023)

**Response:** Same format as Premier League standings

### Assets API

#### Get Team Assets
Retrieve images and media for a specific team.

```http
GET /api/assets/team/{teamId}
```

**Path Parameters:**
- `teamId` (required): Team ID

**Response:**
```json
{
  "teamId": 33,
  "teamName": "Manchester United",
  "badge": "https://example.com/badges/manutd.png",
  "banner": "https://example.com/banners/manutd.png",
  "logo": "https://example.com/logos/manutd.png"
}
```

#### Get Player Assets
Retrieve images and media for a specific player.

```http
GET /api/assets/player/{playerId}
```

**Path Parameters:**
- `playerId` (required): Player ID

**Response:**
```json
{
  "playerId": 123,
  "playerName": "Bruno Fernandes",
  "photo": "https://example.com/players/bruno.png",
  "cutout": "https://example.com/cutouts/bruno.png",
  "thumbnail": "https://example.com/thumbs/bruno.png"
}
```

### Sync API (Admin Only)

All sync endpoints require admin authentication.

#### Sync Fixtures
Trigger synchronization of fixture data from external providers.

```http
POST /api/sync/fixtures
```

**Headers:**
```
X-Admin-API-Key: your-admin-api-key
```

**Response:**
```json
{
  "status": "success",
  "message": "Fixture sync initiated",
  "fixturesSynced": 45,
  "timestamp": "2024-03-15T12:00:00Z"
}
```

#### Sync Standings
Trigger synchronization of standings data.

```http
POST /api/sync/standings
```

**Headers:**
```
X-Admin-API-Key: your-admin-api-key
```

**Response:**
```json
{
  "status": "success",
  "message": "Standings sync completed",
  "standingsUpdated": 20,
  "timestamp": "2024-03-15T12:00:00Z"
}
```

#### Get Sync Status
Check the status of data synchronization.

```http
GET /api/sync/status
```

**Response:**
```json
{
  "lastFixtureSync": "2024-03-15T02:00:00Z",
  "lastStandingsSync": "2024-03-15T11:30:00Z",
  "nextScheduledSync": "2024-03-16T02:00:00Z",
  "syncHealth": "HEALTHY"
}
```

### Streaming API

#### Live Match Updates (SSE)
Server-Sent Events endpoint for real-time match updates.

```http
GET /api/streaming/live-match/{matchId}
```

**Path Parameters:**
- `matchId` (required): Match ID

**Response:** Server-Sent Events stream

Example event:
```
event: score-update
data: {"homeScore": 2, "awayScore": 1, "minute": 67}

event: goal
data: {"team": "Manchester United", "player": "Marcus Rashford", "minute": 67}
```

### Health & Monitoring

#### Application Health
Check overall application health.

```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "providers": {
      "status": "UP",
      "details": {
        "apiFootball": "UP",
        "footballData": "UP",
        "theSportsDB": "UP"
      }
    }
  }
}
```

#### Application Metrics
Get application metrics in Prometheus format.

```http
GET /actuator/metrics
```

#### Specific Metric
Get a specific metric value.

```http
GET /actuator/metrics/{metricName}
```

Example:
```http
GET /actuator/metrics/http.server.requests
```

## Error Responses

The API uses standard HTTP status codes and returns errors in the following format:

```json
{
  "timestamp": "2024-03-15T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Match with ID 99999 not found",
  "path": "/api/matches/99999"
}
```

### Common Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **204 No Content**: Request successful, no content to return
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Authentication required
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **429 Too Many Requests**: Rate limit exceeded
- **500 Internal Server Error**: Server error
- **503 Service Unavailable**: Service temporarily unavailable

## Rate Limiting

API endpoints are rate-limited to ensure fair usage:

- **Anonymous requests**: 60 requests per minute
- **Authenticated requests**: 300 requests per minute

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1710507600
```

## Caching

Responses are cached to improve performance:

- **Standings**: 30 minutes
- **Fixtures**: 1 hour
- **Live matches**: 30 seconds
- **Team assets**: 7 days
- **Player assets**: 7 days

Cache status is indicated in response headers:
```
X-Cache: HIT
X-Cache-TTL: 1800
```

## Examples

### cURL Examples

**Get next match:**
```bash
curl -X GET http://localhost:8080/api/matches/next
```

**Get standings:**
```bash
curl -X GET http://localhost:8080/api/standings/premier-league
```

**Sync fixtures (admin):**
```bash
curl -X POST http://localhost:8080/api/sync/fixtures \
  -H "X-Admin-API-Key: your-admin-api-key"
```

### JavaScript Examples

**Fetch next match:**
```javascript
fetch('http://localhost:8080/api/matches/next')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

**Listen to live match updates:**
```javascript
const eventSource = new EventSource('http://localhost:8080/api/streaming/live-match/12345');

eventSource.addEventListener('score-update', (event) => {
  const data = JSON.parse(event.data);
  console.log('Score update:', data);
});

eventSource.addEventListener('goal', (event) => {
  const data = JSON.parse(event.data);
  console.log('GOAL!', data);
});
```

## Support

For API support:
- Report issues: [GitHub Issues](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/issues)
- Documentation: [README.md](README.md)
- Interactive docs: http://localhost:8080/swagger-ui.html
