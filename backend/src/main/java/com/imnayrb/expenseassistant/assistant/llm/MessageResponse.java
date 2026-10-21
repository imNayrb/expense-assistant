package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;

/** The parts of a Messages API response this app needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageResponse(
        String id,
        List<ContentBlock> content,
        @JsonProperty("stop_reason") String stopReason) {

    public static final String STOP_TOOL_USE = "tool_use";

    public MessageResponse {
        content = content == null ? List.of() : List.copyOf(content);
    }

    /** All text blocks joined together. */
    public String text() {
        return content.stream()
                .filter(block -> ContentBlock.TEXT.equals(block.type()) && block.text() != null)
                .map(ContentBlock::text)
                .collect(Collectors.joining("\n"))
                .trim();
    }

    public List<ContentBlock> toolUses() {
        return content.stream().filter(ContentBlock::isToolUse).toList();
    }

    public boolean wantsToolUse() {
        return STOP_TOOL_USE.equals(stopReason);
    }
}
