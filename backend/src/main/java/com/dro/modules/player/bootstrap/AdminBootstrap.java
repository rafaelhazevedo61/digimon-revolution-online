package com.dro.modules.player.bootstrap;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        prefix = "dro.bootstrap-admin",
        name = "enabled",
        havingValue = "true"
)
public class AdminBootstrap implements CommandLineRunner {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${dro.bootstrap-admin.username}")
    private String username;

    @Value("${dro.bootstrap-admin.email}")
    private String email;

    @Value("${dro.bootstrap-admin.password}")
    private String password;

    public AdminBootstrap (PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (playerRepository.existsByUserType(UserType.ADMIN)) {
            return;
        }

        if (playerRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "Não foi possível criar o Admin inicial: "
                            + "o e-mail configurado já está em uso."
            );
        }

        if (playerRepository.existsByUsername(username)) {
            throw new IllegalStateException(
                    "Não foi possível criar o Admin inicial: "
                            + "o username configurado já está em uso."
            );
        }

        Player admin = Player.createPlayer(
                UUID.randomUUID(),
                username,
                email,
                passwordEncoder.encode(password),
                LocalDateTime.now()
        );

        admin.setUserType(UserType.ADMIN);

        playerRepository.save(admin);
    }
}