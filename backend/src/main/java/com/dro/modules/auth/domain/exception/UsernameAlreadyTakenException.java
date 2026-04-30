package com.dro.modules.auth.domain.exception;

import com.dro.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyTakenException extends BusinessException {

    public UsernameAlreadyTakenException() {
        super("Username already taken", HttpStatus.CONFLICT);
    }
}