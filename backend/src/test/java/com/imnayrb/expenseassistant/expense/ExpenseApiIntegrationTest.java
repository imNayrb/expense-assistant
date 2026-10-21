package com.imnayrb.expenseassistant.expense;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Test completo: HTTP → controller → service → JPA → H2 (schema creato da Flyway). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ExpenseRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsAndReadsAnExpense() throws Exception {
        long id = create("Benzina", "30", "FUEL", "2026-10-20");

        mvc.perform(get("/api/expenses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Benzina"))
                .andExpect(jsonPath("$.amount").value(30.0))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.category").value("FUEL"))
                .andExpect(jsonPath("$.categoryLabel").value("Carburante"))
                .andExpect(jsonPath("$.date").value("2026-10-20"));
    }

    @Test
    void returnsLocationHeaderOnCreate() throws Exception {
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Caffè", "1.20", "RESTAURANTS", "2026-10-21")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void listsExpensesInRangeNewestFirstAndFiltersByCategory() throws Exception {
        create("Spesa", "45.10", "GROCERIES", "2026-10-02");
        create("Pizza", "18", "RESTAURANTS", "2026-10-15");
        create("Benzina", "60", "FUEL", "2026-09-30");

        mvc.perform(get("/api/expenses").param("from", "2026-10-01").param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("Pizza"))
                .andExpect(jsonPath("$[1].description").value("Spesa"));

        mvc.perform(get("/api/expenses")
                        .param("from", "2026-10-01").param("to", "2026-10-31").param("category", "GROCERIES"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Spesa"));
    }

    @Test
    void summarizesByCategory() throws Exception {
        create("Spesa", "40", "GROCERIES", "2026-10-02");
        create("Spesa", "2.50", "GROCERIES", "2026-10-03");
        create("Cinema", "9", "ENTERTAINMENT", "2026-10-04");

        mvc.perform(get("/api/expenses/summary").param("from", "2026-10-01").param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(51.5))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.categories", hasSize(2)))
                .andExpect(jsonPath("$.categories[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$.categories[0].total").value(42.5))
                .andExpect(jsonPath("$.categories[0].count").value(2));
    }

    @Test
    void updatesAndDeletesAnExpense() throws Exception {
        long id = create("Treno", "12", "TRANSPORT", "2026-10-10");

        mvc.perform(put("/api/expenses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Treno Roma-Napoli", "24.90", "TRAVEL", "2026-10-10")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Treno Roma-Napoli"))
                .andExpect(jsonPath("$.category").value("TRAVEL"));

        mvc.perform(delete("/api/expenses/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidExpenses() throws Exception {
        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "-5", "FUEL", "2026-10-10")))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Boh", "5", "NOT_A_CATEGORY", "2026-10-10")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsInvertedDateRange() throws Exception {
        mvc.perform(get("/api/expenses").param("from", "2026-10-31").param("to", "2026-10-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exposesCategoriesWithItalianLabels() throws Exception {
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Category.values().length)))
                .andExpect(jsonPath("$[0].code").value("GROCERIES"))
                .andExpect(jsonPath("$[0].label").value("Spesa alimentare"));
    }

    @Test
    void reportsAssistantAsDisabledWithoutApiKey() throws Exception {
        mvc.perform(get("/api/assistant/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mvc.perform(post("/api/assistant/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"ieri 30 euro di benzina\"}"))
                .andExpect(status().isServiceUnavailable());
    }

    private long create(String description, String amount, String category, String date) throws Exception {
        String response = mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(description, amount, category, date)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(response);
        return json.get("id").asLong();
    }

    private static String body(String description, String amount, String category, String date) {
        return """
                {"description":"%s","amount":%s,"category":"%s","date":"%s"}
                """.formatted(description, amount, category, date);
    }
}
