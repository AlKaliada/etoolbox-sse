package com.exadel.etoolbox.sse.core.dto;

import ch.qos.logback.classic.Level;

import javax.servlet.AsyncContext;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncContextWrapper {

    private final AsyncContext asyncContext;
        private final Queue<String> messages = new ConcurrentLinkedQueue<>();
    private final String user;
    private List<Level> levels;

    public AsyncContextWrapper(AsyncContext asyncContext, String user, List<Level> levels) {
        this.asyncContext = asyncContext;
        this.user = user;
        this.levels = levels;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    public Queue<String> getMessages() {
        return messages;
    }

    public String getUser() {
        return user;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public void addMessage(String message) {
        messages.add(message);
    }
}
