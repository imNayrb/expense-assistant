package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Corpo di {@code POST /v1/messages}. I campi null non vengono inclusi nel JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Message> messages,
        List<Tool> tools,
        @JsonProperty("tool_choice") ToolChoice toolChoice) {
}
