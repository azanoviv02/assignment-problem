package com.netcracker.utils.logging;

public class SystemOutLogger implements Logger {

    private final boolean enabled;

    public SystemOutLogger() {
        this(true);
    }

    public SystemOutLogger(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void info(String statement) {
        System.out.println(statement);
    }
}
