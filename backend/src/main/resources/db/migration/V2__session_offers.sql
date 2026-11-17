CREATE TABLE session_offer (
  id CHAR(36) PRIMARY KEY,
  teacher_id CHAR(36) NOT NULL,
  skill_id CHAR(36) NOT NULL,
  starts_at TIMESTAMP NOT NULL,
  duration_minutes INTEGER NOT NULL CHECK (duration_minutes IN (30,60,90,120)),
  notes VARCHAR(1000),
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_offer_teacher_time (teacher_id, starts_at),
  INDEX idx_offer_skill_status (skill_id, status)
);
