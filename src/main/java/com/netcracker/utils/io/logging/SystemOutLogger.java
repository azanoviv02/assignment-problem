package com.netcracker.utils.io.logging;

public class SystemOutLogger implements Logger {

    private final boolean enabled;

    public SystemOutLogger() {
        this(true);
    }

    public SystemOutLogger(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void info(String format, Object... args) {
        if (enabled) {
            String formattedString = String.format(format, args);
            System.out.println(formattedString);
        }
    }
}
