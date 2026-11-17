package com.skillswap.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Configuration
public class SeedData {
  @Bean @Transactional CommandLineRunner seedDemo(JdbcTemplate db, PasswordEncoder encoder) {
    return args -> {
      var demo = ensureUser(db, encoder, "demo@skillswap.local", "Jordan Davis", "Computer science student and curious learner.", 12);
      var ananya = ensureUser(db, encoder, "ananya@skillswap.local", "Ananya Sharma", "Product designer who loves making complex ideas feel simple.", 18);
      var rohan = ensureUser(db, encoder, "rohan@skillswap.local", "Rohan Mehta", "Developer, public speaking coach, and weekend cyclist.", 9);
      var maya = ensureUser(db, encoder, "maya@skillswap.local", "Maya Patel", "Photographer and visual storyteller.", 15);
      addSkill(db, ananya, "00000000-0000-0000-0000-000000000002", "OFFER", "INTERMEDIATE");
      addSkill(db, ananya, "00000000-0000-0000-0000-000000000001", "WANT", "BEGINNER");
      addSkill(db, rohan, "00000000-0000-0000-0000-000000000001", "OFFER", "ADVANCED");
      addSkill(db, rohan, "00000000-0000-0000-0000-000000000003", "WANT", "INTERMEDIATE");
      addSkill(db, maya, "00000000-0000-0000-0000-000000000004", "OFFER", "ADVANCED");
      addSkill(db, maya, "00000000-0000-0000-0000-000000000002", "WANT", "BEGINNER");
      addSkill(db, demo, "00000000-0000-0000-0000-000000000001", "OFFER", "INTERMEDIATE");
      addSkill(db, demo, "00000000-0000-0000-0000-000000000002", "WANT", "BEGINNER");
      if (db.queryForObject("SELECT count(*) FROM exchange_session WHERE learner_id=? AND teacher_id=?", Integer.class, demo, ananya) == 0) {
        db.update("INSERT INTO exchange_session(id,teacher_id,learner_id,skill_id,starts_at,duration_minutes,status,notes) VALUES (?,?,?,?,DATE_ADD(NOW(), INTERVAL 2 DAY),?,?,?)",
          UUID.randomUUID().toString(), ananya, demo, "00000000-0000-0000-0000-000000000002", 60, "ACCEPTED", "Bring one app screen you would like to improve.");
      }
    };
  }

  private String ensureUser(JdbcTemplate db, PasswordEncoder encoder, String email, String name, String bio, int credits) {
    var existing = db.queryForList("SELECT id FROM app_user WHERE email=?", email);
    if (!existing.isEmpty()) return String.valueOf(existing.getFirst().get("id"));
    var id = UUID.randomUUID().toString();
    db.update("INSERT INTO app_user(id,email,password_hash,display_name,bio,credits) VALUES (?,?,?,?,?,?)", id, email, encoder.encode("Password123!"), name, bio, credits);
    db.update("INSERT INTO credit_ledger(id,user_id,amount,entry_type,description) VALUES (?,?,?,?,?)", UUID.randomUUID().toString(), id, credits, "INTRODUCTORY_GRANT", "Welcome credits");
    return id;
  }

  private void addSkill(JdbcTemplate db, String user, String skill, String direction, String level) {
    db.update("INSERT IGNORE INTO user_skill(user_id,skill_id,direction,level) VALUES (?,?,?,?)", user, skill, direction, level);
  }
}
