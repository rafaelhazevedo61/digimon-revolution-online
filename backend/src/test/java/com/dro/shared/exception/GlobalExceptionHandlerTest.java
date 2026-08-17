package com.dro.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    @Test
    void handleOptimisticLockingFailure_returnsConflictWithRefreshMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = new MockHttpServletRequest("POST", "/world-boss/attack");

        var response = handler.handleOptimisticLockingFailure(
                new OptimisticLockingFailureException("version changed"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ApiErrorCode.CONFLICT, response.getBody().code());
        assertTrue(response.getBody().message().contains("Refresh"));
    }
}
