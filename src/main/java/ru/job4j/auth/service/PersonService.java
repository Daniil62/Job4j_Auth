package ru.job4j.auth.service;

import org.postgresql.util.PSQLException;
import org.springframework.stereotype.Service;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.repository.PersonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository repository;

    public PersonService(PersonRepository repository) {
        this.repository = repository;
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

    private void validate(Optional<Person> person, int id) {
        if (person.isEmpty()) {
            throw new NullPointerException(String.format("Person with id %d not exists", id));
        }
    }
}
