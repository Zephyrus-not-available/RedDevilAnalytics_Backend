-- Create core tables with proper constraints and indexes

-- Seasons table
CREATE TABLE seasons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_season_name UNIQUE (name)
);

-- Competitions table
CREATE TABLE competitions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50),
    type VARCHAR(50),
    emblem_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Teams table (enhanced from existing)
CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    short_name VARCHAR(100),
    tla VARCHAR(10),
    logo_url VARCHAR(500),
    stadium VARCHAR(255),
    venue VARCHAR(255),
    address VARCHAR(500),
    website VARCHAR(255),
    founded INTEGER,
    club_colors VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_team_name UNIQUE (name)
);

-- Players table
CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    date_of_birth DATE,
    nationality VARCHAR(100),
    position VARCHAR(50),
    shirt_number INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Squad members (temporal modeling with start/end dates)
CREATE TABLE squad_members (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    season_id BIGINT NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    shirt_number INTEGER,
    position VARCHAR(50),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_squad_member UNIQUE (player_id, team_id, season_id, start_date)
);

-- External references (multi-provider ID mapping)
CREATE TABLE external_refs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_external_ref UNIQUE (entity_type, provider, external_id)
);

-- Team assets
CREATE TABLE team_assets (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    logo_url VARCHAR(500),
    banner_url VARCHAR(500),
    provider VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_team_asset UNIQUE (team_id, provider)
);

-- Player assets
CREATE TABLE player_assets (
    id BIGSERIAL PRIMARY KEY,
    player_id BIGINT NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    photo_url VARCHAR(500),
    cutout_url VARCHAR(500),
    provider VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_player_asset UNIQUE (player_id, provider)
);

-- Matches table (enhanced from existing)
CREATE TABLE IF NOT EXISTS matches (
    id BIGSERIAL PRIMARY KEY,
    home_team_id BIGINT NOT NULL REFERENCES teams(id),
    away_team_id BIGINT NOT NULL REFERENCES teams(id),
    competition_id BIGINT REFERENCES competitions(id),
    season_id BIGINT REFERENCES seasons(id),
    match_date TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    home_score INTEGER,
    away_score INTEGER,
    venue VARCHAR(255),
    referee VARCHAR(255),
    attendance INTEGER,
    round VARCHAR(50),
    matchday INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Standings table
CREATE TABLE standings (
    id BIGSERIAL PRIMARY KEY,
    competition_id BIGINT NOT NULL REFERENCES competitions(id),
    season_id BIGINT NOT NULL REFERENCES seasons(id),
    team_id BIGINT NOT NULL REFERENCES teams(id),
    position INTEGER NOT NULL,
    played_games INTEGER DEFAULT 0,
    won INTEGER DEFAULT 0,
    draw INTEGER DEFAULT 0,
    lost INTEGER DEFAULT 0,
    points INTEGER DEFAULT 0,
    goals_for INTEGER DEFAULT 0,
    goals_against INTEGER DEFAULT 0,
    goal_difference INTEGER DEFAULT 0,
    form VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_standing UNIQUE (competition_id, season_id, team_id)
);

-- Match predictions (for AI service)
CREATE TABLE match_predictions (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    home_win_probability DECIMAL(5,2),
    draw_probability DECIMAL(5,2),
    away_win_probability DECIMAL(5,2),
    predicted_home_score DECIMAL(4,2),
    predicted_away_score DECIMAL(4,2),
    confidence_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_prediction UNIQUE (match_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_external_refs_entity ON external_refs(entity_type, entity_id);
CREATE INDEX idx_external_refs_provider ON external_refs(provider, external_id);
CREATE INDEX idx_squad_members_player ON squad_members(player_id);
CREATE INDEX idx_squad_members_team ON squad_members(team_id);
CREATE INDEX idx_squad_members_season ON squad_members(season_id);
CREATE INDEX idx_squad_members_active ON squad_members(is_active);
CREATE INDEX idx_matches_date ON matches(match_date);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_competition ON matches(competition_id);
CREATE INDEX idx_matches_season ON matches(season_id);
CREATE INDEX idx_matches_teams ON matches(home_team_id, away_team_id);
CREATE INDEX idx_standings_competition_season ON standings(competition_id, season_id);
CREATE INDEX idx_standings_position ON standings(position);
