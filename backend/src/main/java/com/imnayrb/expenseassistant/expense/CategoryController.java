package com.imnayrb.expenseassistant.expense;

import com.imnayrb.expenseassistant.expense.dto.CategoryInfo;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {

    @GetMapping("/api/categories")
    public List<CategoryInfo> categories() {
        return Arrays.stream(Category.values()).map(CategoryInfo::from).toList();
    }
}
