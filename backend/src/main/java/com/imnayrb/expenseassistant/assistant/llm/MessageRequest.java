package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Body of {@code POST /v1/messages}. Null fields are omitted from the JSON. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageRequest(
        String model,
        @JsonProperty("max_tokens") int maxTokens,
        String system,
        List<Message> messages,
        List<Tool> tools,
        @JsonProperty("tool_choice") ToolChoice toolChoice) {
}
