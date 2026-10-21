package com.imnayrb.expenseassistant.expense.dto;

import com.imnayrb.expenseassistant.expense.Category;

public record CategoryInfo(Category code, String label) {

    public static CategoryInfo from(Category category) {
        return new CategoryInfo(category, category.label());
    }
}
