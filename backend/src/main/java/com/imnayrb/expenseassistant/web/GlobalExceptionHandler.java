package com.imnayrb.expenseassistant.web;

import com.imnayrb.expenseassistant.assistant.UnparseableExpenseException;
import com.imnayrb.expenseassistant.assistant.llm.AssistantUnavailableException;
import com.imnayrb.expenseassistant.assistant.llm.LlmException;
import com.imnayrb.expenseassistant.expense.ExpenseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps domain exceptions to RFC 9457 problem details. Validation and malformed requests are already handled by
 * {@link ResponseEntityExceptionHandler} (400).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpenseNotFoundException.class)
    ProblemDetail notFound(ExpenseNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Expense not found", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", e.getMessage());
    }

    @ExceptionHandler(UnparseableExpenseException.class)
    ProblemDetail unparseable(UnparseableExpenseException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Expense not recognized", e.getMessage());
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    ProblemDetail unavailable(AssistantUnavailableException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Assistant disabled", e.getMessage());
    }

    @ExceptionHandler(LlmException.class)
    ProblemDetail llmFailure(LlmException e) {
        log.warn("Assistant request failed", e);
        return problem(HttpStatus.BAD_GATEWAY, "Assistant error", e.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
