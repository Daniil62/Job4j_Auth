package ru.job4j.auth.component;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class GetSetMethodsFetcher implements MethodsFetcher {

    @Override
    public Map<String, Method> fetch(Object object) {
        Method[] methods = object.getClass().getDeclaredMethods();
        Map<String, Method> result = new HashMap<>();
        for (Method method: methods) {
            String name = method.getName();
            if (name.startsWith("get") || name.startsWith("set")) {
                result.put(name, method);
            }
        }
        return result;
    }
}
