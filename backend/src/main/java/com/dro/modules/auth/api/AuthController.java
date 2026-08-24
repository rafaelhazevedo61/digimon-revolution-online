package com.dro.modules.auth.api;

import com.dro.modules.auth.api.dto.request.LoginRequest;
import com.dro.modules.auth.api.dto.response.LoginResponse;
import com.dro.modules.auth.api.dto.request.RegisterRequest;
import com.dro.modules.auth.application.LoginUseCase;
import com.dro.modules.auth.application.RegisterUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Componente da camada de controller da API do módulo de Autenticação.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest request) {
        registerUseCase.execute(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    public AuthController(final RegisterUseCase registerUseCase, final LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }
}
