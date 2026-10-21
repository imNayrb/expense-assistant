package com.imnayrb.expenseassistant.assistant;

import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una spesa estratta dal linguaggio naturale ma non ancora salvata: l'utente la conferma (o la corregge)
 * nell'interfaccia, poi viene inviata a {@code POST /api/expenses}. Tenere una persona nel ciclo evita di
 * salvare in silenzio un'interpretazione sbagliata.
 */
public record ExpenseDraft(
        String description,
        BigDecimal amount,
        String currency,
        Category category,
        String categoryLabel,
        LocalDate date) {
}
