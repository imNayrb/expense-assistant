package com.imnayrb.expenseassistant.expense;

/** Categorie di spesa. Nel database si salva il codice; l'etichetta viene mostrata nell'interfaccia. */
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

    /** Ricerca tollerante, usata quando il valore arriva dal modello: i valori sconosciuti diventano {@link #OTHER}. */
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
