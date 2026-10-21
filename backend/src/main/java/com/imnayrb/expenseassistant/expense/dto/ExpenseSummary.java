package com.imnayrb.expenseassistant.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseSummary(
        LocalDate from,
        LocalDate to,
        BigDecimal total,
        long count,
        List<CategoryTotal> categories) {
}
