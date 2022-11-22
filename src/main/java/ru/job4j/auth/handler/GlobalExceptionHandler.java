package ru.job4j.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.
            getLogger(GlobalExceptionHandler.class.getSimpleName());
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = {NullPointerException.class})
    public void handleNullPointerException(Exception e, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        handle(e, request, response, null);
    }

    @ExceptionHandler(value = {PSQLException.class})
    public void handlePSQLException(Exception e, HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        handle(e, request, response, "Person with same login already exists.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handle(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                e.getFieldErrors().stream()
                        .map(f -> Map.of(
                                f.getField(),
                                String.format("%s. Actual value: %s",
                                        f.getDefaultMessage(),
                                        f.getRejectedValue())
                        ))
                        .collect(Collectors.toList())
        );
    }

    private void handle(Exception e, HttpServletRequest request,
                        HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("request type", request.getMethod());
            put("request URI", request.getRequestURI());
            put("message", message != null ? message : e.getMessage());
            put("error type", e.getClass());
        }}));
        LOGGER.error(e.getMessage());
    }
}