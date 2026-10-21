package com.imnayrb.expenseassistant.expense;

public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(long id) {
        super("Spesa " + id + " non trovata");
    }
}
