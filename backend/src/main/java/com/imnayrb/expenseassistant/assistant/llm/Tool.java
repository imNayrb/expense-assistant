package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** Un tool che Claude può chiamare, descritto da uno JSON Schema per il suo input. */
public record Tool(String name, String description, @JsonProperty("input_schema") JsonNode inputSchema) {
}
