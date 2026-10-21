package com.imnayrb.expenseassistant.expense;

import com.imnayrb.expenseassistant.expense.dto.CategoryTotal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByDateBetweenOrderByDateDescIdDesc(LocalDate from, LocalDate to);

    List<Expense> findByCategoryAndDateBetweenOrderByDateDescIdDesc(Category category, LocalDate from, LocalDate to);

    /** Totals per category in a date range, biggest first. Aggregation happens in the database. */
    @Query("""
            select new com.imnayrb.expenseassistant.expense.dto.CategoryTotal(
                e.category, sum(e.amount), count(e))
            from Expense e
            where e.date between :from and :to
            group by e.category
            order by sum(e.amount) desc
            """)
    List<CategoryTotal> totalsByCategory(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
