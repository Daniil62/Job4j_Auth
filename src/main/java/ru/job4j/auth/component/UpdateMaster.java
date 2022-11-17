package ru.job4j.auth.component;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class UpdateMaster<T> {

    private final MethodsFetcher methodsFetcher;

    public UpdateMaster(MethodsFetcher methodsFetcher) {
        this.methodsFetcher = methodsFetcher;
    }

    public T update(T from, T current) throws InvocationTargetException, IllegalAccessException {
        T result = current;
        Map<String, Method> namePerMethod = methodsFetcher.fetch(result);
        for (String name : namePerMethod.keySet()) {
            if (name.startsWith("get")) {
                Method getMethod = namePerMethod.get(name);
                Method setMethod = namePerMethod.get(name.replace("get", "set"));
                if (setMethod == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Impossible invoke set method from object : " + current
                                    + ", Check set and get pairs.");
                }
                var newValue = getMethod.invoke(from);
                System.out.println(newValue);
                if (newValue != null) {
                    setMethod.invoke(result, newValue);
                }
            }
        }
        return result;
    }
}
