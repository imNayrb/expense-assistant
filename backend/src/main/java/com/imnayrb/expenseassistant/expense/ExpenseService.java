package com.imnayrb.expenseassistant.expense;

import com.imnayrb.expenseassistant.expense.dto.CategoryTotal;
import com.imnayrb.expenseassistant.expense.dto.ExpenseRequest;
import com.imnayrb.expenseassistant.expense.dto.ExpenseResponse;
import com.imnayrb.expenseassistant.expense.dto.ExpenseSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExpenseService {

    static final String DEFAULT_CURRENCY = "EUR";

    private final ExpenseRepository repository;
    private final Clock clock;

    public ExpenseService(ExpenseRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<ExpenseResponse> list(LocalDate from, LocalDate to, Category category) {
        DateRange range = resolveRange(from, to);
        List<Expense> expenses = category == null
                ? repository.findByDateBetweenOrderByDateDescIdDesc(range.from(), range.to())
                : repository.findByCategoryAndDateBetweenOrderByDateDescIdDesc(category, range.from(), range.to());
        return expenses.stream().map(ExpenseResponse::from).toList();
    }

    public ExpenseResponse get(long id) {
        return ExpenseResponse.from(find(id));
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense(
                request.description().trim(),
                normalizeAmount(request.amount()),
                normalizeCurrency(request.currency()),
                request.category(),
                request.date());
        return ExpenseResponse.from(repository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseRequest request) {
        Expense expense = find(id);
        expense.update(
                request.description().trim(),
                normalizeAmount(request.amount()),
                normalizeCurrency(request.currency()),
                request.category(),
                request.date());
        return ExpenseResponse.from(expense);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(find(id));
    }

    public ExpenseSummary summary(LocalDate from, LocalDate to) {
        DateRange range = resolveRange(from, to);
        List<CategoryTotal> categories = repository.totalsByCategory(range.from(), range.to());
        BigDecimal total = categories.stream()
                .map(CategoryTotal::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        long count = categories.stream().mapToLong(CategoryTotal::count).sum();
        return new ExpenseSummary(range.from(), range.to(), total, count, categories);
    }

    /**
     * Completa gli estremi mancanti: senza date l'intervallo è il mese corrente; con un solo estremo,
     * l'altro viene preso dal mese di quello indicato.
     */
    DateRange resolveRange(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from
                : (to != null ? to : LocalDate.now(clock)).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = to != null ? to : start.with(TemporalAdjusters.lastDayOfMonth());
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("'from' (" + start + ") non può essere dopo 'to' (" + end + ")");
        }
        return new DateRange(start, end);
    }

    private Expense find(long id) {
        return repository.findById(id).orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank() ? DEFAULT_CURRENCY : currency.toUpperCase(Locale.ROOT);
    }

    record DateRange(LocalDate from, LocalDate to) {
    }
}
