package ru.job4j.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.job4j.auth.component.GetSetMethodsFetcher;
import ru.job4j.auth.component.UpdateMaster;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.repository.PersonRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository repository;
    private final UpdateMaster<Person> updateMaster;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder encoder;

    public PersonService(PersonRepository repository,
                         GetSetMethodsFetcher methodsFetcher,
                         ObjectMapper objectMapper, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.updateMaster = new UpdateMaster<>(methodsFetcher);
        this.objectMapper = objectMapper;
        this.encoder = encoder;
    }

    public List<Person> findAll() {
        return repository.findAll();
    }

    public Optional<Person> findById(int id) {
        Optional<Person> result = repository.findById(id);
        validate(result, id);
        return result;
    }

    public Optional<Person> save(Person person) throws PSQLException {
        return Optional.of(repository.save(person));
    }

    public boolean existsById(int id) {
        return repository.existsById(id);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public Person patch(Person person) throws
            InvocationTargetException, IllegalAccessException, PSQLException {
        int id = person.getId();
        String password = person.getPassword();
        if (password != null) {
            person.setPassword(encoder.encode(password));
        }
        Optional<Person> currentPerson = repository.findById(id);
        validate(currentPerson, id);
        Person newPerson = updateMaster.update(person, currentPerson.get());
        repository.save(newPerson);
        return newPerson;
    }

    private void validate(Optional<Person> person, int id) {
        if (person.isEmpty()) {
            throw new NullPointerException(String.format("Person with id %d not exists", id));
        }
    }

    public void handleError(Exception e, HttpServletRequest request,
                            HttpServletResponse response,
                            String message, Logger logger) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() { {
            put("request", request.getRequestURI());
            put("message", message);
            put("type", e.getClass());
        }}));
        logger.error(e.getLocalizedMessage());
    }
}
