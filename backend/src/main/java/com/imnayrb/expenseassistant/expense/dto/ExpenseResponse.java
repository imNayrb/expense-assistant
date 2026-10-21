package com.imnayrb.expenseassistant.expense.dto;

import com.imnayrb.expenseassistant.expense.Category;
import com.imnayrb.expenseassistant.expense.Expense;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        String currency,
        Category category,
        String categoryLabel,
        LocalDate date,
        Instant createdAt) {

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCurrency(),
                expense.getCategory(),
                expense.getCategory().label(),
                expense.getDate(),
                expense.getCreatedAt());
    }
}
