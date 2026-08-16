package com.llmrouter.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final Path ENV_FILE = Path.of(".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> values = load(ENV_FILE);
        if (values.isEmpty()) {
            return;
        }
        MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, values);
        if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
            environment.getPropertySources().addAfter(
                    StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    propertySource
            );
        } else {
            environment.getPropertySources().addFirst(propertySource);
        }
    }

    static Map<String, Object> load(Path path) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (!Files.exists(path)) {
            return values;
        }
        try {
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                ParsedLine parsed = parseLine(rawLine);
                if (parsed != null && !System.getenv().containsKey(parsed.key())) {
                    values.put(parsed.key(), parsed.value());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
        return values;
    }

    static ParsedLine parseLine(String rawLine) {
        if (rawLine == null) {
            return null;
        }
        String line = rawLine.trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return null;
        }
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }
        int separator = line.indexOf('=');
        if (separator <= 0) {
            return null;
        }
        String key = line.substring(0, separator).trim();
        String value = unquote(line.substring(separator + 1).trim());
        if (key.isEmpty()) {
            return null;
        }
        return new ParsedLine(key, value);
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    record ParsedLine(String key, String value) {
    }
}
