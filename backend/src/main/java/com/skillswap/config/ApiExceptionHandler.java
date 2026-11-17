package com.skillswap.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String,String> badRequest(IllegalArgumentException ex) { return Map.of("error", ex.getMessage()); }
  @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Map<String,String> unexpected(Exception ex) { return Map.of("error", "An unexpected server error occurred"); }
}
