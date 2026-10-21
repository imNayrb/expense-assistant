package com.imnayrb.expenseassistant.assistant;

import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * An expense extracted from natural language but not saved yet: the user confirms (or fixes) it in the UI,
 * then it is sent to {@code POST /api/expenses}. Keeping a human in the loop avoids silently storing a wrong
 * interpretation.
 */
public record ExpenseDraft(
        String description,
        BigDecimal amount,
        String currency,
        Category category,
        String categoryLabel,
        LocalDate date) {
}
