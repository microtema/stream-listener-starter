package de.microtema.stream.listener.processor;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;

public class ListenerScope implements Scope {

    private final Map<String, Object> listeners = new HashMap<>();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return this.listeners.get(name);
    }

    @Override
    public Object remove(String name) {
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }

    @Override
    public Object resolveContextualObject(String key) {
        return this.listeners.get(key);
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
