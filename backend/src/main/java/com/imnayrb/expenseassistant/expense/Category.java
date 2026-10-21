package com.imnayrb.expenseassistant.expense;

/** Expense categories. The code is stored in the database; the label is shown in the UI. */
public enum Category {
    GROCERIES("Spesa alimentare"),
    RESTAURANTS("Ristoranti e bar"),
    TRANSPORT("Trasporti"),
    FUEL("Carburante"),
    HOUSING("Casa e affitto"),
    UTILITIES("Bollette"),
    HEALTH("Salute"),
    ENTERTAINMENT("Svago"),
    SHOPPING("Shopping"),
    TRAVEL("Viaggi"),
    EDUCATION("Formazione"),
    SUBSCRIPTIONS("Abbonamenti"),
    OTHER("Altro");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** Lenient lookup used when the value comes from the model: unknown values become {@link #OTHER}. */
    public static Category fromCode(String code) {
        if (code == null) {
            return OTHER;
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
