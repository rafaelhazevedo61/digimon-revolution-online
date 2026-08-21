package com.dro.modules.loot.application;

import com.dro.modules.loot.api.dto.request.AdminChestUpdateRequest;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.loot.infra.LootTableRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminChestUseCaseTest {

    @Mock
    private ChestDefinitionRepository chestDefinitionRepository;

    @Mock
    private LootTableRepository lootTableRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    @InjectMocks
    private AdminChestUseCase adminChestUseCase;

    @Test
    void updatesChestLootTableAndAuditsOperation() {
        UUID adminId = UUID.randomUUID();
        Player admin = admin(adminId, UserType.ADMIN);
        LootTableEntity table = LootTableEntity.builder()
                .code("LOOT_TABLE_NEW")
                .name("Nova Loot Table")
                .active(true)
                .build();
        ChestDefinitionEntity chest = ChestDefinitionEntity.builder()
                .id(7L)
                .code("CHEST_AREA_TEST")
                .name("Baú de Teste")
                .lootTable(table)
                .active(true)
                .tradable(true)
                .build();

        when(playerRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(chestDefinitionRepository.findWithCatalogByCode("CHEST_AREA_TEST"))
                .thenReturn(Optional.of(chest));
        when(lootTableRepository.findByCodeAndActiveTrue("LOOT_TABLE_NEW"))
                .thenReturn(Optional.of(table));
        when(chestDefinitionRepository.saveAndFlush(chest)).thenReturn(chest);

        AdminChestResponseAssertions.assertUpdated(
                adminChestUseCase.update(
                        token(adminId),
                        "CHEST_AREA_TEST",
                        new AdminChestUpdateRequest(
                                "Baú de Teste Atualizado",
                                "Descrição nova",
                                "chest-icon",
                                "LOOT_TABLE_NEW",
                                false,
                                true
                        )
                )
        );

        assertThat(chest.getName()).isEqualTo("Baú de Teste Atualizado");
        assertThat(chest.isTradable()).isFalse();
        verify(transactionAuditPublisher).success(
                argThat(value -> value.startsWith("admin-chest:CHEST_AREA_TEST:updated:")),
                eq("ADMIN_CHEST_UPDATED"),
                eq("ChestDefinition"),
                eq("CHEST_AREA_TEST"),
                any(Map.class)
        );
    }

    @Test
    void rejectsNonAdminBeforeReadingChest() {
        UUID playerId = UUID.randomUUID();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(admin(playerId, UserType.PLAYER)));

        assertThatThrownBy(() -> adminChestUseCase.list(token(playerId), false))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void togglesActiveStatusAndAuditsOperation() {
        UUID adminId = UUID.randomUUID();
        Player admin = admin(adminId, UserType.ADMIN);
        LootTableEntity table = LootTableEntity.builder()
                .code("LOOT_TABLE_TEST")
                .name("Loot Table Teste")
                .active(true)
                .build();
        ChestDefinitionEntity chest = ChestDefinitionEntity.builder()
                .id(9L)
                .code("CHEST_AREA_TOGGLE")
                .name("Baú Toggle")
                .lootTable(table)
                .active(true)
                .build();

        when(playerRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(chestDefinitionRepository.findWithCatalogByCode("CHEST_AREA_TOGGLE"))
                .thenReturn(Optional.of(chest));
        when(chestDefinitionRepository.saveAndFlush(chest)).thenReturn(chest);

        var response = adminChestUseCase.toggleActive(token(adminId), "CHEST_AREA_TOGGLE");

        assertThat(response.active()).isFalse();
        verify(transactionAuditPublisher).success(
                argThat(value -> value.startsWith("admin-chest:CHEST_AREA_TOGGLE:deactivated:")),
                eq("ADMIN_CHEST_DEACTIVATED"),
                eq("ChestDefinition"),
                eq("CHEST_AREA_TOGGLE"),
                any(Map.class)
        );
    }

    private Player admin(UUID id, UserType userType) {
        return Player.builder().id(id).username("user-test").userType(userType).build();
    }

    private String token(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
    }

    private static final class AdminChestResponseAssertions {
        private static void assertUpdated(com.dro.modules.loot.api.dto.response.AdminChestResponse response) {
            assertThat(response.code()).isEqualTo("CHEST_AREA_TEST");
            assertThat(response.name()).isEqualTo("Baú de Teste Atualizado");
            assertThat(response.lootTableCode()).isEqualTo("LOOT_TABLE_NEW");
        }
    }
}
