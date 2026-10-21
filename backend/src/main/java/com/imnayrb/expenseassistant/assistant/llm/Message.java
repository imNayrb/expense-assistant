package com.imnayrb.expenseassistant.assistant.llm;

import java.util.List;

/** One turn of the conversation: {@code role} is "user" or "assistant". */
public record Message(String role, List<ContentBlock> content) {

    public static Message user(String text) {
        return new Message("user", List.of(ContentBlock.text(text)));
    }

    public static Message user(List<ContentBlock> blocks) {
        return new Message("user", blocks);
    }

    public static Message assistant(List<ContentBlock> blocks) {
        return new Message("assistant", blocks);
    }
}
