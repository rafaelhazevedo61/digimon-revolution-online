package com.dro.modules.auth.domain.exception;

import com.dro.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends BusinessException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered", HttpStatus.CONFLICT);
    }
}