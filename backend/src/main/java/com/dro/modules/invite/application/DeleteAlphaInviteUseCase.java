package com.dro.modules.invite.application;

import com.dro.modules.invite.domain.AlphaInvite;
import com.dro.modules.invite.infra.AlphaInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteAlphaInviteUseCase {

    private final AlphaInviteRepository repository;

    @Transactional
    public void execute(UUID inviteId, UUID adminId) {

        AlphaInvite invite = repository.findById(inviteId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Convite Alpha não encontrado.")
                );

        invite.delete(
                adminId,
                LocalDateTime.now()
        );
    }
}