package com.skillswap.controller;

import com.skillswap.security.JwtService;
import com.skillswap.service.SkillSwapService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final SkillSwapService service; private final JwtService jwt;
  public AuthController(SkillSwapService service, JwtService jwt) { this.service = service; this.jwt = jwt; }
  @PostMapping("/register") public Map<String,Object> register(@RequestBody Credentials request) { var user = service.register(request.name(), request.email(), request.password()); return Map.of("user", user, "token", jwt.issue(java.util.UUID.fromString(String.valueOf(user.get("id"))), (String) user.get("email"))); }
  @PostMapping("/login") public Map<String,Object> login(@RequestBody Credentials request) { var user = service.login(request.email(), request.password()); return Map.of("user", user, "token", jwt.issue(java.util.UUID.fromString(String.valueOf(user.get("id"))), (String) user.get("email"))); }
  public record Credentials(String name, String email, String password) {}
}
