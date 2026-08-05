package com.example.reactivecrud.error;

import com.example.reactivecrud.product.exception.ProductNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ServerWebInputException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void notFoundShouldMapTo404ProblemDetail() {
        ProblemDetail problem = handler.handleNotFound(new ProductNotFoundException(7L));

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        Assertions.assertEquals("Resource not found", problem.getTitle());
        Assertions.assertTrue(problem.getDetail().contains("7"));
    }

    @Test
    void constraintViolationShouldMapTo400ValidationProblem() {
        ConstraintViolationException ex = new ConstraintViolationException("invalid", Set.of());

        ProblemDetail problem = handler.handleConstraintViolation(ex);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        Assertions.assertEquals("Validation failed", problem.getTitle());
        Assertions.assertNotNull(problem.getProperties().get("errors"));
    }

    @Test
    void malformedInputShouldMapTo400() {
        ProblemDetail problem = handler.handleBadInput(new ServerWebInputException("bad body"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        Assertions.assertEquals("Malformed request", problem.getTitle());
    }

    @Test
    void unexpectedErrorShouldMapTo500() {
        ProblemDetail problem = handler.handleUnexpected(new RuntimeException("boom"));

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        Assertions.assertEquals("Internal server error", problem.getTitle());
    }
}
