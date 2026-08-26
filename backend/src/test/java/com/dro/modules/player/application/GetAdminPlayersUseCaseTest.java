package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.response.AdminPlayerPageResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminPlayersUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GetAdminPlayersUseCase getAdminPlayersUseCase;

    @Test
    void preservesPaginationMetadataForAdminPlayerSearch() {
        PageRequest pageRequest = PageRequest.of(1, 10);
        Player player = Player.createPlayer(
                UUID.randomUUID(),
                "teste21",
                "teste21@example.com",
                "password",
                LocalDateTime.now()
        );
        when(playerRepository.findAll(any(Specification.class), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(player), pageRequest, 21));

        AdminPlayerPageResponse response = getAdminPlayersUseCase.execute(
                "teste",
                null,
                null,
                null,
                pageRequest
        );

        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalItems()).isEqualTo(21);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasPrevious()).isTrue();
        assertThat(response.hasNext()).isTrue();
        assertThat(response.items()).extracting("username").containsExactly("teste21");
        verify(playerRepository).findAll(any(Specification.class), eq(pageRequest));
    }
}
