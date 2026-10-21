package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Un blocco di contenuto della Messages API. Un unico record "piatto" copre i tre tipi usati dall'app:
 * <ul>
 *   <li>{@code text}: {@code text}</li>
 *   <li>{@code tool_use} (inviato da Claude): {@code id}, {@code name}, {@code input}</li>
 *   <li>{@code tool_result} (inviato da noi): {@code tool_use_id}, {@code content}, {@code is_error}</li>
 * </ul>
 * I campi che non servono restano null e non vengono serializzati; i campi sconosciuti nelle risposte
 * vengono ignorati.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContentBlock(
        String type,
        String text,
        String id,
        String name,
        JsonNode input,
        @JsonProperty("tool_use_id") String toolUseId,
        String content,
        @JsonProperty("is_error") Boolean isError) {

    public static final String TEXT = "text";
    public static final String TOOL_USE = "tool_use";
    public static final String TOOL_RESULT = "tool_result";

    public static ContentBlock text(String text) {
        return new ContentBlock(TEXT, text, null, null, null, null, null, null);
    }

    public static ContentBlock toolUse(String id, String name, JsonNode input) {
        return new ContentBlock(TOOL_USE, null, id, name, input, null, null, null);
    }

    public static ContentBlock toolResult(String toolUseId, String content, boolean isError) {
        return new ContentBlock(TOOL_RESULT, null, null, null, null, toolUseId, content, isError ? Boolean.TRUE : null);
    }

    public boolean isToolUse() {
        return TOOL_USE.equals(type);
    }
}
