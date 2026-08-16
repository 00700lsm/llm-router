package com.llmrouter.evaluation;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.llmrouter.config.LlmRouterProperties;
import com.llmrouter.error.ErrorCode;
import com.llmrouter.error.LlmRouterException;

@Component
@Profile("evaluate-models")
public class DirectModelEvaluationCommand implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DirectModelEvaluationCommand.class);
    private static final Path OUTPUT_PATH = Path.of("evaluation/results/001-model-baseline.json");

    private final DirectModelRunner directModelRunner;
    private final LlmRouterProperties properties;
    private final ConfigurableApplicationContext context;

    public DirectModelEvaluationCommand(
            DirectModelRunner directModelRunner,
            LlmRouterProperties properties,
            ConfigurableApplicationContext context
    ) {
        this.directModelRunner = directModelRunner;
        this.properties = properties;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.getProvider().getApiKey() == null || properties.getProvider().getApiKey().isBlank()) {
            throw new LlmRouterException(
                    ErrorCode.PROVIDER_ERROR,
                    "GEMINI_API_KEY is not configured. Direct model evaluation was not executed."
            );
        }
        DirectModelEvaluationReport report = directModelRunner.run();
        Path written = directModelRunner.write(report, OUTPUT_PATH);
        log.info("Direct model evaluation written to {}", written.toAbsolutePath());
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}
