package com.llmrouter.api;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.llmrouter.TestModels;
import com.llmrouter.llm.ModelResponse;
import com.llmrouter.llm.ProviderClient;
import com.llmrouter.metrics.Usage;

@SpringBootTest
@AutoConfigureMockMvc
class ChatApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderClient providerClient;

    @Test
    void rejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void rejectsMissingMessage() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void routesToDefaultModelAndReturnsAnswer() throws Exception {
        when(providerClient.complete(org.mockito.ArgumentMatchers.any(), anyString()))
                .thenReturn(ModelResponse.success(
                        "synchronized는 모니터 기반이고 ReentrantLock은 명시적 Lock API다.",
                        TestModels.small().id(),
                        TestModels.small().provider(),
                        new Usage(20, 40, 60),
                        100
                ));

        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Java에서 synchronized와 ReentrantLock 차이를 설명해줘.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.model").value("model-small"))
                .andExpect(jsonPath("$.provider").value("OPENAI"))
                .andExpect(jsonPath("$.routingReason").doesNotExist())
                .andExpect(jsonPath("$.cost").doesNotExist())
                .andExpect(jsonPath("$.latency").doesNotExist());
    }
}
