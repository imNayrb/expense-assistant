package com.imnayrb.expenseassistant.assistant.llm;

import com.fasterxml.jackson.annotation.JsonInclude;

/** {@code {"type":"auto"}} lets Claude decide; {@code {"type":"tool","name":...}} forces one tool. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolChoice(String type, String name) {

    public static ToolChoice auto() {
        return new ToolChoice("auto", null);
    }

    public static ToolChoice tool(String name) {
        return new ToolChoice("tool", name);
    }
}
