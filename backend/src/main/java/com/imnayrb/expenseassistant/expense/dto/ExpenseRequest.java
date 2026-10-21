package com.imnayrb.expenseassistant.expense.dto;

import com.imnayrb.expenseassistant.expense.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Payload to create or update an expense. {@code currency} is optional and defaults to EUR. */
public record ExpenseRequest(
        @NotBlank @Size(max = 255) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "[A-Za-z]{3}", message = "must be a 3-letter ISO currency code") String currency,
        @NotNull Category category,
        @NotNull LocalDate date) {
}
