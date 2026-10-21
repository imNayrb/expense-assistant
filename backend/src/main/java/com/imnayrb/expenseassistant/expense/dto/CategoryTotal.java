package com.imnayrb.expenseassistant.expense.dto;

import com.imnayrb.expenseassistant.expense.Category;
import java.math.BigDecimal;

/** Sum and number of expenses for one category. Built directly by a JPQL constructor expression. */
public record CategoryTotal(Category category, String categoryLabel, BigDecimal total, Long count) {

    public CategoryTotal(Category category, BigDecimal total, Long count) {
        this(category, category.label(), total, count);
    }
}
