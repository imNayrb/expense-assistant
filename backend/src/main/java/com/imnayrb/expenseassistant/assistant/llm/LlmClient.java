package com.imnayrb.expenseassistant.assistant.llm;

/**
 * Porta verso il modello linguistico. Tutto ciò che parla con Claude passa da questa interfaccia: il resto del
 * codice non dipende dai dettagli HTTP e nei test si può collegare un fake.
 */
@FunctionalInterface
public interface LlmClient {

    /**
     * Invia una richiesta alla Messages API.
     *
     * @throws AssistantUnavailableException se non è configurata una chiave API
     * @throws LlmException                  se la chiamata all'API fallisce
     */
    MessageResponse createMessage(MessageRequest request);
}
