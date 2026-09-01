package com.dro.shared.automation;

import com.dro.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AutomationFailureException extends BusinessException {
    private final AutomationFailureCode failureCode;

    public AutomationFailureException(String message, HttpStatus status, AutomationFailureCode failureCode) {
        super(message, status);
        this.failureCode = failureCode;
    }

    public AutomationFailureCode getFailureCode() {
        return failureCode;
    }
}

