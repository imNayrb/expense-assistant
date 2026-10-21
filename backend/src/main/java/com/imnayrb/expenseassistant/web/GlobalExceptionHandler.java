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
 * Converte le eccezioni di dominio in problem details (RFC 9457). Gli errori di validazione e le richieste
 * malformate sono già gestiti da {@link ResponseEntityExceptionHandler} (400).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpenseNotFoundException.class)
    ProblemDetail notFound(ExpenseNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Spesa non trovata", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "Richiesta non valida", e.getMessage());
    }

    @ExceptionHandler(UnparseableExpenseException.class)
    ProblemDetail unparseable(UnparseableExpenseException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Spesa non riconosciuta", e.getMessage());
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    ProblemDetail unavailable(AssistantUnavailableException e) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Assistente disattivato", e.getMessage());
    }

    @ExceptionHandler(LlmException.class)
    ProblemDetail llmFailure(LlmException e) {
        log.warn("Richiesta all'assistente fallita", e);
        return problem(HttpStatus.BAD_GATEWAY, "Errore dell'assistente", e.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
