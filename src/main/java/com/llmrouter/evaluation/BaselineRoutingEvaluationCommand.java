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
@Profile("evaluate-routing")
public class BaselineRoutingEvaluationCommand implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaselineRoutingEvaluationCommand.class);
    private static final Path OUTPUT_PATH = Path.of("evaluation/results/002-baseline-routing.json");

    private final EvaluationRunner evaluationRunner;
    private final LlmRouterProperties properties;
    private final ConfigurableApplicationContext context;

    public BaselineRoutingEvaluationCommand(
            EvaluationRunner evaluationRunner,
            LlmRouterProperties properties,
            ConfigurableApplicationContext context
    ) {
        this.evaluationRunner = evaluationRunner;
        this.properties = properties;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.getProvider().getApiKey() == null || properties.getProvider().getApiKey().isBlank()) {
            throw new LlmRouterException(
                    ErrorCode.PROVIDER_ERROR,
                    "GEMINI_API_KEY is not configured. Baseline routing evaluation was not executed."
            );
        }
        BaselineRoutingEvaluationReport report = evaluationRunner.runAll();
        Path written = evaluationRunner.write(report, OUTPUT_PATH);
        log.info("Baseline routing evaluation written to {}", written.toAbsolutePath());
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }
}
