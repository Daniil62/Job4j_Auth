package ru.job4j.auth.component;

import java.lang.reflect.Method;
import java.util.Map;

public interface MethodsFetcher {

    Map<String, Method> fetch(Object object);
}
