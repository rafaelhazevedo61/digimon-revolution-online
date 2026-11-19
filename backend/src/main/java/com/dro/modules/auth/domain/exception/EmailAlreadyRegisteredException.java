package com.dro.modules.auth.domain.exception;

import com.dro.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * Componente da camada de exceção de domínio do módulo de Autenticação.
 */
public class EmailAlreadyRegisteredException extends BusinessException {

    public EmailAlreadyRegisteredException() {
        super("Email already registered", HttpStatus.CONFLICT);
    }
}