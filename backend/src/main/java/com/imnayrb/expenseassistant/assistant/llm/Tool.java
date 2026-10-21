package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** A tool Claude may call, described by a JSON Schema for its input. */
public record Tool(String name, String description, @JsonProperty("input_schema") JsonNode inputSchema) {
}
