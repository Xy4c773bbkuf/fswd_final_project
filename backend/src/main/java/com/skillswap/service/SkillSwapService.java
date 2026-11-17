package com.skillswap.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class SkillSwapService {
  private final JdbcTemplate db; private final PasswordEncoder encoder;
  public SkillSwapService(JdbcTemplate db, PasswordEncoder encoder) { this.db = db; this.encoder = encoder; }

  public Map<String,Object> register(String name, String email, String password) {
    if (name == null || name.isBlank() || email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$") || password == null || password.length() < 8)
      throw new IllegalArgumentException("Name, valid email and an 8 character password are required");
    var id = UUID.randomUUID();
    try { db.update("INSERT INTO app_user(id,email,password_hash,display_name,credits) VALUES (?,?,?,?,?)", id.toString(), email.toLowerCase(), encoder.encode(password), name.trim(), 5); }
    catch (DuplicateKeyException ex) { throw new IllegalArgumentException("An account with that email already exists"); }
    db.update("INSERT INTO credit_ledger(id,user_id,amount,entry_type,description) VALUES (?,?,?,?,?)", UUID.randomUUID().toString(), id.toString(), 5, "INTRODUCTORY_GRANT", "Welcome credits");
    return user(id);
  }
  public Map<String,Object> login(String email, String password) {
    var rows = db.queryForList("SELECT id,password_hash FROM app_user WHERE email=?", email.toLowerCase());
    if (rows.isEmpty() || !encoder.matches(password, (String) rows.getFirst().get("password_hash"))) throw new IllegalArgumentException("Invalid email or password");
    return user(UUID.fromString(String.valueOf(rows.getFirst().get("id"))));
  }
  public Map<String,Object> user(UUID id) {
    return db.queryForMap("SELECT id,email,display_name,bio,credits,created_at FROM app_user WHERE id=?", id.toString());
  }
  public List<Map<String,Object>> dashboard(UUID id) {
    var result = new LinkedHashMap<String,Object>();
    result.put("profile", user(id)); result.put("skills", skills(id));
    result.put("matches", matches(id)); result.put("upcomingSessions", sessions(id, "UPCOMING"));
    result.put("ledger", db.queryForList("SELECT amount,entry_type,description,created_at FROM credit_ledger WHERE user_id=? ORDER BY created_at DESC LIMIT 10", id.toString()));
    return List.of(result);
  }
  public List<Map<String,Object>> skills(UUID id) {
    return db.queryForList("SELECT s.id,s.name,s.category,us.direction,us.level FROM user_skill us JOIN skill s ON s.id=us.skill_id WHERE us.user_id=? ORDER BY s.name", id.toString());
  }
  public List<Map<String,Object>> search(String query, UUID current) {
    var value = query == null ? "" : "%" + query.toLowerCase() + "%";
    return db.queryForList("SELECT DISTINCT u.id,u.display_name,u.bio,u.credits,s.id AS skill_id,s.name AS skill_name FROM app_user u JOIN user_skill us ON us.user_id=u.id AND us.direction='OFFER' JOIN skill s ON s.id=us.skill_id WHERE u.id<>? AND (lower(u.display_name) LIKE ? OR lower(s.name) LIKE ?) ORDER BY u.display_name LIMIT 50", current.toString(), value, value);
  }
  public List<Map<String,Object>> matches(UUID current) {
    return db.queryForList("""
      SELECT DISTINCT u.id, u.display_name, u.bio, u.credits,
      80 AS compatibility_score, 'Complementary skills detected' AS matching_reason
      FROM app_user u JOIN user_skill offered ON offered.user_id=u.id AND offered.direction='OFFER'
      JOIN user_skill wanted ON wanted.user_id=? AND wanted.direction='WANT' AND wanted.skill_id=offered.skill_id
      WHERE u.id<>? ORDER BY compatibility_score DESC, u.display_name LIMIT 20
      """, current.toString(), current.toString());
  }
  public List<Map<String,Object>> sessions(UUID id, String filter) {
    return db.queryForList("SELECT e.id,e.teacher_id,e.learner_id,e.starts_at,e.duration_minutes,e.status,e.notes,s.name AS skill,teacher.display_name AS teacher,learner.display_name AS learner FROM exchange_session e JOIN skill s ON s.id=e.skill_id JOIN app_user teacher ON teacher.id=e.teacher_id JOIN app_user learner ON learner.id=e.learner_id WHERE (e.teacher_id=? OR e.learner_id=?) AND (?='ALL' OR (?='UPCOMING' AND e.starts_at>=now() AND e.status IN ('REQUESTED','ACCEPTED')) OR (?='PAST' AND (e.starts_at<now() OR e.status IN ('COMPLETED','CANCELLED','REJECTED')))) ORDER BY e.starts_at", id.toString(), id.toString(), filter, filter, filter);
  }
  public List<Map<String,Object>> offers(UUID teacher) {
    return db.queryForList("SELECT o.id,o.teacher_id,o.skill_id,o.starts_at,o.duration_minutes,o.status,o.notes,s.name AS skill FROM session_offer o JOIN skill s ON s.id=o.skill_id WHERE o.teacher_id=? ORDER BY o.starts_at", teacher.toString());
  }
  @Transactional public Map<String,Object> createOffer(UUID teacher, UUID skill, OffsetDateTime startsAt, int duration, String notes) {
    validateSessionTime(startsAt, duration);
    if (db.queryForObject("SELECT COUNT(*) FROM user_skill WHERE user_id=? AND skill_id=? AND direction='OFFER'", Integer.class, teacher.toString(), skill.toString()) == 0)
      throw new IllegalArgumentException("You can only teach skills listed in your offered skills");
    var id = UUID.randomUUID().toString();
    db.update("INSERT INTO session_offer(id,teacher_id,skill_id,starts_at,duration_minutes,status,notes) VALUES (?,?,?,?,?,?,?)", id, teacher.toString(), skill.toString(), startsAt.toLocalDateTime(), duration, "AVAILABLE", notes);
    return db.queryForMap("SELECT id,teacher_id,skill_id,starts_at,duration_minutes,status,notes FROM session_offer WHERE id=?", id);
  }
  private void validateSessionTime(OffsetDateTime startsAt, int duration) {
    if (startsAt == null || startsAt.isBefore(OffsetDateTime.now())) throw new IllegalArgumentException("Session must be in the future");
    if (!Set.of(30, 60, 90, 120).contains(duration)) throw new IllegalArgumentException("Duration must be 30, 60, 90, or 120 minutes");
  }
  @Transactional public Map<String,Object> createSession(UUID learner, UUID teacher, UUID skill, OffsetDateTime startsAt, int duration, String notes) {
    if (learner.equals(teacher)) throw new IllegalArgumentException("You cannot book a session with yourself");
    validateSessionTime(startsAt, duration);
    var cost = duration / 30; var credits = ((Number) db.queryForObject("SELECT credits FROM app_user WHERE id=?", Integer.class, learner.toString())).intValue();
    if (credits < cost) throw new IllegalArgumentException("Not enough available credits");
    var id = UUID.randomUUID();
    db.update("INSERT INTO exchange_session(id,teacher_id,learner_id,skill_id,starts_at,duration_minutes,status,notes) VALUES (?,?,?,?,?,?,?,?)", id.toString(), teacher.toString(), learner.toString(), skill.toString(), startsAt.toLocalDateTime(), duration, "REQUESTED", notes);
    return db.queryForMap("SELECT id,teacher_id,learner_id,skill_id,starts_at,duration_minutes,status,notes FROM exchange_session WHERE id=?", id.toString());
  }
  @Transactional public void completeSession(UUID actor, UUID sessionId) {
    var session = db.queryForMap("SELECT teacher_id,learner_id,duration_minutes,status FROM exchange_session WHERE id=?", sessionId.toString());
    if (!actor.toString().equals(String.valueOf(session.get("teacher_id"))) && !actor.toString().equals(String.valueOf(session.get("learner_id")))) throw new IllegalArgumentException("Only session participants can complete it");
    if (!"ACCEPTED".equals(session.get("status"))) throw new IllegalArgumentException("Only accepted sessions can be completed");
    var teacher = String.valueOf(session.get("teacher_id")); var learner = String.valueOf(session.get("learner_id")); var amount = ((Number) session.get("duration_minutes")).intValue() / 30;
    db.update("UPDATE app_user SET credits=credits+? WHERE id=?", amount, teacher);
    db.update("INSERT INTO credit_ledger(id,user_id,session_id,amount,entry_type,description) VALUES (?,?,?,?,?,?)", UUID.randomUUID().toString(), teacher, sessionId.toString(), amount, "SESSION_EARNED", "Completed teaching session");
    db.update("UPDATE app_user SET credits=credits-? WHERE id=?", amount, learner);
    db.update("INSERT INTO credit_ledger(id,user_id,session_id,amount,entry_type,description) VALUES (?,?,?,?,?,?)", UUID.randomUUID().toString(), learner, sessionId.toString(), -amount, "SESSION_SPENT", "Completed learning session");
    db.update("UPDATE exchange_session SET status='COMPLETED' WHERE id=?", sessionId.toString());
  }

  @Transactional public void acceptSession(UUID actor, UUID sessionId) {
    var session = db.queryForMap("SELECT teacher_id,status FROM exchange_session WHERE id=?", sessionId.toString());
    if (!actor.toString().equals(String.valueOf(session.get("teacher_id")))) throw new IllegalArgumentException("Only the teacher can accept this request");
    if (!"REQUESTED".equals(session.get("status"))) throw new IllegalArgumentException("Only requested sessions can be accepted");
    db.update("UPDATE exchange_session SET status='ACCEPTED' WHERE id=?", sessionId.toString());
  }
}
