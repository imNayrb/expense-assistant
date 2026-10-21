package com.imnayrb.expenseassistant.expense.dto;

import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;

/** Somma e numero di spese di una categoria. Costruito direttamente da una constructor expression JPQL. */
public record CategoryTotal(Category category, String categoryLabel, BigDecimal total, Long count) {

    public CategoryTotal(Category category, BigDecimal total, Long count) {
        this(category, category.label(), total, count);
    }
}
