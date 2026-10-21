package com.imnayrb.expenseassistant.assistant;

/** Il testo non descrive una spesa utilizzabile (per esempio manca l'importo). Diventa 422. */
public class UnparseableExpenseException extends RuntimeException {

    public UnparseableExpenseException(String message) {
        super(message);
    }
}
