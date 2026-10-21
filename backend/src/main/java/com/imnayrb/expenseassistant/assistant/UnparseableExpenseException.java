package com.imnayrb.expenseassistant.assistant;

/** The text doesn't describe a usable expense (for example, no amount). Mapped to 422. */
public class UnparseableExpenseException extends RuntimeException {

    public UnparseableExpenseException(String message) {
        super(message);
    }
}
