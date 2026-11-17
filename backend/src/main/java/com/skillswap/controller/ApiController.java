package com.skillswap.controller;

import com.skillswap.service.SkillSwapService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final SkillSwapService service;
  public ApiController(SkillSwapService service) { this.service = service; }
  private UUID current(org.springframework.security.core.Authentication auth) { return (UUID) auth.getPrincipal(); }
  @GetMapping("/me") public Map<String,Object> me(org.springframework.security.core.Authentication a) { return service.user(current(a)); }
  @GetMapping("/dashboard") public Map<String,Object> dashboard(org.springframework.security.core.Authentication a) { return service.dashboard(current(a)).getFirst(); }
  @GetMapping("/skills") public List<Map<String,Object>> skills(org.springframework.security.core.Authentication a) { return service.skills(current(a)); }
  @GetMapping("/matches") public List<Map<String,Object>> matches(org.springframework.security.core.Authentication a) { return service.matches(current(a)); }
  @GetMapping("/students") public List<Map<String,Object>> students(@RequestParam(required=false) String q, org.springframework.security.core.Authentication a) { return service.search(q, current(a)); }
  @GetMapping("/sessions") public List<Map<String,Object>> sessions(@RequestParam(defaultValue="ALL") String filter, org.springframework.security.core.Authentication a) { return service.sessions(current(a), filter); }
  @PostMapping("/sessions") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> create(@RequestBody CreateSession request, org.springframework.security.core.Authentication a) { return service.createSession(current(a), request.teacherId(), request.skillId(), request.startsAt(), request.durationMinutes(), request.notes()); }
  @PostMapping("/sessions/{id}/complete") @ResponseStatus(HttpStatus.NO_CONTENT) public void complete(@PathVariable UUID id, org.springframework.security.core.Authentication a) { service.completeSession(current(a), id); }
  @PostMapping("/sessions/{id}/accept") @ResponseStatus(HttpStatus.NO_CONTENT) public void accept(@PathVariable UUID id, org.springframework.security.core.Authentication a) { service.acceptSession(current(a), id); }
  public record CreateSession(UUID teacherId, UUID skillId, OffsetDateTime startsAt, int durationMinutes, String notes) {}
}
