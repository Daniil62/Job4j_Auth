package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.service.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/persons")
public class PersonController {

    private final PersonService persons;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PersonController.class.getSimpleName());

    public PersonController(final PersonService persons,
                            BCryptPasswordEncoder encoder,
                            ObjectMapper objectMapper) {
        this.persons = persons;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/all")
    public List<Person> findAll() {
        return persons.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        var person = persons.findById(id);
        return new ResponseEntity<>(
                person.orElse(new Person()),
                person.isPresent() ? HttpStatus.OK : HttpStatus.NOT_FOUND
        );
    }

    @PutMapping("/")
    public ResponseEntity<Optional<Person>> update(@RequestBody Person person) throws PSQLException {
        return ResponseEntity.ok().body(persons.save(person));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable int id) {
        boolean result = persons.existsById(id);
        if (result) {
            persons.deleteById(id);
        }
        return ResponseEntity
                .status(result ? HttpStatus.OK : HttpStatus.NOT_FOUND)
                .header("message", result ? "person successfully deleted"
                        : String.format("person with id %d not exists", id))
                .body(result);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Boolean> signUp(@RequestBody Person person) throws PSQLException {
        String password = person.getPassword();
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password should be more than six symbols.");
        }
        person.setPassword(encoder.encode(person.getPassword()));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(persons.save(person).isPresent());
    }

    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> showPhoto(@PathVariable int id) throws IOException {
        var person = persons.findById(id);
        var response = ResponseEntity
                .status(HttpStatus.NOT_FOUND).body(new byte[0]);
        if (person.isPresent()) {
            var content = Files.readAllBytes(Path.of(person.get().getPhoto()));
            response = ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentType(MediaType.IMAGE_PNG)
                    .contentType(MediaType.IMAGE_GIF)
                    .contentLength(content.length)
                    .body(content);
        }
        return response;
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    public void exceptionHandler(Exception e,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        handleError(e, request, response, e.getMessage());
    }

    @ExceptionHandler(value = { IOException.class })
    public void ioExceptionHandler(Exception e,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        handleError(e, request, response, "file not found");
    }

    private void handleError(Exception e, HttpServletRequest request,
                             HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("request", request.getRequestURI());
            put("message", message);
            put("type", e.getClass());
        }}));
        LOGGER.error(e.getLocalizedMessage());
    }
}