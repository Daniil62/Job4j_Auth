package ru.job4j.auth.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.job4j.auth.domain.Person;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Integer> {

    @Override
    List<Person> findAll();

    @Query("SELECT p FROM Person p WHERE p.login = :login")
    Person findByLogin(String login);
}