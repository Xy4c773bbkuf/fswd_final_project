CREATE TABLE app_user (
  id CHAR(36) PRIMARY KEY, email VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(80) NOT NULL, bio VARCHAR(500), avatar_url VARCHAR(500),
  credits INTEGER NOT NULL DEFAULT 5 CHECK (credits >= 0), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE skill (
  id CHAR(36) PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE, category VARCHAR(60) NOT NULL
);
CREATE TABLE user_skill (
  user_id CHAR(36) NOT NULL, skill_id CHAR(36) NOT NULL,
  direction VARCHAR(10) NOT NULL CHECK (direction IN ('OFFER','WANT')),
  level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER', PRIMARY KEY(user_id, skill_id, direction)
);
CREATE TABLE exchange_session (
  id CHAR(36) PRIMARY KEY, teacher_id CHAR(36) NOT NULL,
  learner_id CHAR(36) NOT NULL, skill_id CHAR(36) NOT NULL,
  starts_at TIMESTAMP NOT NULL, duration_minutes INTEGER NOT NULL CHECK (duration_minutes IN (30,60,90,120)),
  status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
  notes VARCHAR(1000), created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE credit_ledger (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, session_id CHAR(36),
  amount INTEGER NOT NULL CHECK (amount <> 0), entry_type VARCHAR(30) NOT NULL,
  description VARCHAR(255) NOT NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_skill_direction ON user_skill(direction, skill_id);
CREATE INDEX idx_session_participants ON exchange_session(teacher_id, learner_id, starts_at);
CREATE INDEX idx_ledger_user_created ON credit_ledger(user_id, created_at DESC);
INSERT INTO skill(id,name,category) VALUES
  ('00000000-0000-0000-0000-000000000001','Python','Programming'),
  ('00000000-0000-0000-0000-000000000002','UI/UX Design','Design'),
  ('00000000-0000-0000-0000-000000000003','Public Speaking','Communication'),
  ('00000000-0000-0000-0000-000000000004','Photography','Creative');
