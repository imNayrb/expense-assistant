package com.imnayrb.expenseassistant.assistant.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.imnayrb.expenseassistant.config.AnthropicProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Checks the HTTP contract with the Messages API without calling the real service. */
class AnthropicLlmClientTest {

    private static final AnthropicProperties PROPS =
            new AnthropicProperties("sk-test", "https://api.anthropic.com", "claude-haiku-5-5", 512, "2023-06-01");

    private static final MessageRequest REQUEST = new MessageRequest(
            "claude-haiku-5-5", 512, "system prompt", List.of(Message.user("ciao")), null, null);

    @Test
    void sendsHeadersAndSnakeCaseBodyAndParsesTheResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AnthropicLlmClient client = new AnthropicLlmClient(builder, PROPS);

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "sk-test"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(jsonPath("$.max_tokens").value(512))
                .andExpect(jsonPath("$.messages[0].content[0].type").value("text"))
                .andExpect(jsonPath("$.tools").doesNotExist())
                .andRespond(withSuccess("""
                        {"id":"msg_1","type":"message","role":"assistant",
                         "content":[{"type":"text","text":"Ciao!"}],
                         "stop_reason":"end_turn","usage":{"input_tokens":5,"output_tokens":2}}
                        """, MediaType.APPLICATION_JSON));

        MessageResponse response = client.createMessage(REQUEST);

        assertThat(response.text()).isEqualTo("Ciao!");
        assertThat(response.wantsToolUse()).isFalse();
        server.verify();
    }

    @Test
    void wrapsApiErrors() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AnthropicLlmClient client = new AnthropicLlmClient(builder, PROPS);
        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.createMessage(REQUEST))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("429");
    }

    @Test
    void refusesToCallTheApiWithoutAKey() {
        AnthropicProperties noKey = new AnthropicProperties("", "https://api.anthropic.com", "m", 512, "2023-06-01");
        AnthropicLlmClient client = new AnthropicLlmClient(RestClient.builder(), noKey);

        assertThatThrownBy(() -> client.createMessage(REQUEST)).isInstanceOf(AssistantUnavailableException.class);
    }
}
