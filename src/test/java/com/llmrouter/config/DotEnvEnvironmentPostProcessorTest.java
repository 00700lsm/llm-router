package com.llmrouter.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;

class DotEnvEnvironmentPostProcessorTest {

    @Test
    void parsesEnvFileWithoutOverridingExistingEnvironment(@TempDir Path tempDir) throws Exception {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, """
                # comment
                DOTENV_TEST_KEY="test-gemini-key"
                GEMINI_BASE_URL_TEST=https://generativelanguage.googleapis.com/v1beta
                export UNUSED_FLAG=true
                """);

        Map<String, Object> values = DotEnvEnvironmentPostProcessor.load(envFile);

        assertThat(values.get("DOTENV_TEST_KEY")).isEqualTo("test-gemini-key");
        assertThat(values.get("GEMINI_BASE_URL_TEST")).isEqualTo("https://generativelanguage.googleapis.com/v1beta");
        assertThat(values.get("UNUSED_FLAG")).isEqualTo("true");
    }

    @Test
    void ignoresCommentsAndInvalidLines() {
        assertThat(DotEnvEnvironmentPostProcessor.parseLine("# GEMINI_API_KEY=secret")).isNull();
        assertThat(DotEnvEnvironmentPostProcessor.parseLine("")).isNull();
        assertThat(DotEnvEnvironmentPostProcessor.parseLine("NOVALUE")).isNull();
        assertThat(DotEnvEnvironmentPostProcessor.parseLine("GEMINI_API_KEY=abc").value()).isEqualTo("abc");
    }

    @Test
    void loadsBeforeApplicationYmlPlaceholders() {
        assertThat(new DotEnvEnvironmentPostProcessor().getOrder())
                .isLessThan(ConfigDataEnvironmentPostProcessor.ORDER);
    }
}
