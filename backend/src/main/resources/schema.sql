CREATE TABLE IF NOT EXISTS registration_invitations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    token VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_initial VARCHAR(1) NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_code VARCHAR(9) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    start_year INT NOT NULL,
    end_year INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS active_weeks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT fk_active_weeks_section
        FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT uq_active_weeks_section_week
        UNIQUE (section_id, week_start_date)
);

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    project_name VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_teams_section
        FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT uq_teams_section_name
        UNIQUE (section_id, name)
);

CREATE TABLE IF NOT EXISTS team_memberships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    membership_role VARCHAR(20) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_memberships_team
        FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_memberships_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_team_memberships_team_user
        UNIQUE (team_id, user_id)
);
