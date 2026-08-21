package com.dro.modules.loot.application;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.api.dto.request.LootTableAdminRequest;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootTableEntity;
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
import java.util.List;
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
class AdminLootTableUseCaseTest {

    @Mock
    private LootTableRepository lootTableRepository;

    @Mock
    private ItemDefinitionRepository itemDefinitionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    @InjectMocks
    private AdminLootTableUseCase adminLootTableUseCase;

    @Test
    void createResolvesCatalogItemAndAuditsOperation() {
        UUID adminId = UUID.randomUUID();
        Player admin = Player.builder()
                .id(adminId)
                .username("admin")
                .userType(UserType.ADMIN)
                .build();
        ItemDefinition trainingStone = ItemDefinition.builder()
                .id(10L)
                .code("TRAINING_STONE")
                .name("Pedra de Treino")
                .category("MATERIAL")
                .rarity("COMMON")
                .stackable(true)
                .maxStack(999)
                .tradable(true)
                .build();

        when(playerRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(lootTableRepository.findByCode("LOOT_TEST"))
                .thenReturn(Optional.empty());
        when(itemDefinitionRepository.findByCode("TRAINING_STONE"))
                .thenReturn(Optional.of(trainingStone));
        when(lootTableRepository.save(any(LootTableEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminLootTableAdminRequestFixture fixture = new AdminLootTableAdminRequestFixture();
        var response = adminLootTableUseCase.create(token(adminId), fixture.request());

        assertThat(response.code()).isEqualTo("LOOT_TEST");
        assertThat(response.name()).isEqualTo("Tabela de Teste");
        assertThat(response.rarityWeights()).hasSize(4);
        assertThat(response.entries()).hasSize(1);
        assertThat(response.entries().get(0).itemCode()).isEqualTo("TRAINING_STONE");
        assertThat(response.entries().get(0).itemName()).isEqualTo("Pedra de Treino");
        verify(transactionAuditPublisher).success(
                argThat(value -> value.startsWith("admin-loot-table:LOOT_TEST:created:")),
                eq("ADMIN_LOOT_TABLE_CREATED"),
                eq("LootTable"),
                eq("LOOT_TEST"),
                any(Map.class)
        );
    }

    @Test
    void rejectsNonAdminBeforeReadingLootTable() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .userType(UserType.PLAYER)
                .build();
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> adminLootTableUseCase.list(token(playerId), false))
                .isInstanceOf(ForbiddenException.class);
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

    private static final class AdminLootTableAdminRequestFixture {
        private LootTableAdminRequest request() {
            return new LootTableAdminRequest(
                    "LOOT_TEST",
                    "Tabela de Teste",
                    "Pool administrativa de teste",
                    1,
                    2,
                    List.of(
                            new LootTableAdminRequest.LootTableRarityWeightRequest(LootRarity.COMMON, 70),
                            new LootTableAdminRequest.LootTableRarityWeightRequest(LootRarity.RARE, 20),
                            new LootTableAdminRequest.LootTableRarityWeightRequest(LootRarity.EPIC, 8),
                            new LootTableAdminRequest.LootTableRarityWeightRequest(LootRarity.LEGENDARY, 2)
                    ),
                    List.of(new LootTableAdminRequest.LootTableEntryRequest(
                            LootRarity.COMMON,
                            ItemType.TRAINING_STONE,
                            null,
                            50,
                            1,
                            3,
                            true
                    )),
                    true
            );
        }
    }
}
