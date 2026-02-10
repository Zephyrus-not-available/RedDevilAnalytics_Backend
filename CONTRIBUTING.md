# Contributing to RedDevil Analytics Backend

First off, thank you for considering contributing to RedDevil Analytics Backend! It's people like you that make this project such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

- **Use a clear and descriptive title**
- **Describe the exact steps to reproduce the problem**
- **Provide specific examples** (code snippets, API requests, etc.)
- **Describe the behavior you observed** and what you expected
- **Include logs and error messages**
- **Environment details** (OS, Java version, database version, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion:

- **Use a clear and descriptive title**
- **Provide a detailed description** of the suggested enhancement
- **Explain why this enhancement would be useful**
- **List examples** of how it would be used
- **Include mockups or diagrams** if applicable

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the coding standards** outlined below
3. **Add tests** for any new functionality
4. **Update documentation** as needed
5. **Ensure all tests pass** before submitting
6. **Write clear commit messages**
7. **Reference related issues** in your PR description

## Development Setup

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 14+
- Redis 6+
- Docker & Docker Compose (optional)

### Getting Started

1. **Clone your fork**
```bash
git clone https://github.com/YOUR_USERNAME/RedDevilAnalytics_Backend.git
cd RedDevilAnalytics_Backend
```

2. **Set up environment**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start dependencies** (using Docker)
```bash
docker-compose up -d postgres redis
```

4. **Build the project**
```bash
./mvnw clean install
```

5. **Run the application**
```bash
./mvnw spring-boot:run
```

## Coding Standards

### Java Style Guide

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods focused and concise (ideally under 50 lines)
- Add JavaDoc comments for public APIs
- Use Lombok annotations to reduce boilerplate

### Code Organization

- **Controllers**: Handle HTTP requests/responses only
- **Services**: Contain business logic
- **Repositories**: Handle database operations
- **DTOs**: For data transfer between layers
- **Entities**: JPA entities for database mapping

### Naming Conventions

- **Classes**: PascalCase (e.g., `MatchService`, `PlayerRepository`)
- **Methods**: camelCase (e.g., `getMatchById`, `calculateStatistics`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.reddevil.service`)

### Testing

- Write unit tests for all business logic
- Use integration tests for API endpoints
- Aim for at least 80% code coverage
- Use meaningful test names: `shouldReturnMatchWhenIdExists()`
- Mock external dependencies

```java
@Test
void shouldReturnMatchWhenIdExists() {
    // Given
    Long matchId = 1L;
    Match expectedMatch = new Match();
    when(matchRepository.findById(matchId)).thenReturn(Optional.of(expectedMatch));
    
    // When
    Match result = matchService.getMatchById(matchId);
    
    // Then
    assertNotNull(result);
    assertEquals(expectedMatch, result);
}
```

### Database Migrations

- Use Flyway for all schema changes
- Name migrations: `V{version}__{description}.sql`
  - Example: `V001__create_match_table.sql`
- Never modify existing migrations
- Test migrations on a clean database

### API Design

- Use RESTful conventions
- Return appropriate HTTP status codes
- Use consistent response formats
- Version APIs when breaking changes occur
- Document all endpoints with OpenAPI annotations

```java
@Operation(summary = "Get match by ID", description = "Returns detailed information about a specific match")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Successfully retrieved match"),
    @ApiResponse(responseCode = "404", description = "Match not found")
})
@GetMapping("/{id}")
public ResponseEntity<MatchResponse> getMatch(@PathVariable Long id) {
    // Implementation
}
```

### Error Handling

- Use custom exceptions for business logic errors
- Return meaningful error messages
- Never expose stack traces in production
- Log errors appropriately

### Logging

- Use appropriate log levels:
  - **ERROR**: System failures that need immediate attention
  - **WARN**: Recoverable issues or deprecations
  - **INFO**: Important business events
  - **DEBUG**: Detailed diagnostic information
- Include context in log messages
- Don't log sensitive information (passwords, API keys)

```java
@Slf4j
public class MatchService {
    public Match getMatch(Long id) {
        log.debug("Fetching match with id: {}", id);
        Match match = matchRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Match not found with id: {}", id);
                return new MatchNotFoundException(id);
            });
        log.info("Successfully retrieved match: {} vs {}", 
                 match.getHomeTeam(), match.getAwayTeam());
        return match;
    }
}
```

## Commit Message Guidelines

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples

```
feat(match): add AI prediction endpoint

Implement endpoint to fetch AI-powered match predictions.
Integrates with external ML service.

Closes #123
```

```
fix(standings): correct points calculation

Fixed bug where bonus points were not included in total.

Fixes #456
```

## Branch Naming

- Feature branches: `feature/short-description`
- Bug fixes: `fix/short-description`
- Hotfixes: `hotfix/short-description`
- Releases: `release/version`

Examples:
- `feature/add-player-comparison`
- `fix/standings-cache-issue`
- `hotfix/critical-api-error`

## Testing Guidelines

### Unit Tests

- Test one thing per test
- Use descriptive test names
- Follow AAA pattern: Arrange, Act, Assert
- Mock external dependencies
- Don't test framework code

### Integration Tests

- Use `@SpringBootTest` for full application context
- Use `@WebMvcTest` for controller tests
- Use test containers for database tests
- Clean up test data after each test

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=MatchServiceTest

# Run with coverage
./mvnw test jacoco:report
```

## Documentation

- Update README.md for user-facing changes
- Add JavaDoc for public APIs
- Document complex algorithms or business logic
- Update API documentation (OpenAPI annotations)
- Include examples where helpful

## Performance Considerations

- Use caching appropriately
- Optimize database queries (avoid N+1 problems)
- Use pagination for large datasets
- Consider rate limiting for expensive operations
- Profile code before optimizing

## Security

- Never commit secrets or API keys
- Validate all user input
- Use parameterized queries (JPA does this automatically)
- Sanitize output to prevent XSS
- Follow OWASP security guidelines
- Report security vulnerabilities privately

## Review Process

1. Automated checks must pass (build, tests, linting)
2. Code review by at least one maintainer
3. All feedback addressed
4. Documentation updated
5. No merge conflicts

## Questions?

Feel free to:
- Open an issue for discussion
- Join our community chat (if available)
- Contact the maintainers

Thank you for contributing! 🙏
