package com.dro.modules.clan.storage.application;

import com.dro.modules.clan.application.ClanAuthorizationService;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.ClanUpgradeTypeRepository;
import com.dro.modules.clan.storage.api.dto.request.ClanStorageItemRequest;
import com.dro.modules.clan.storage.domain.ClanStorageItem;
import com.dro.modules.clan.storage.infra.ClanStorageHistoryRepository;
import com.dro.modules.clan.storage.infra.ClanStorageItemRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanStorageServiceTest {
    @Mock private ClanAuthorizationService authorization;
    @Mock private ClanRepository clanRepository;
    @Mock private ClanUpgradeTypeRepository upgradeTypeRepository;
    @Mock private ClanBonusService clanBonusService;
    @Mock private PlayerRepository playerRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private ItemDefinitionRepository itemDefinitionRepository;
    @Mock private ClanStorageItemRepository storageItemRepository;
    @Mock private ClanStorageHistoryRepository historyRepository;

    @InjectMocks
    private ClanStorageService service;

    private String token(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private Player player(UUID id, UUID clanId, ClanRole role) {
        return Player.builder()
                .id(id)
                .username("member")
                .clanId(clanId)
                .clanRole(role)
                .build();
    }

    private Clan clan(UUID id) {
        return Clan.builder()
                .id(id)
                .name("Clan")
                .tag("TAG")
                .leaderId(UUID.randomUUID())
                .level(1)
                .honorMarks(500)
                .build();
    }

    private ItemDefinition definition(long id, boolean tradable) {
        return ItemDefinition.builder()
                .id(id)
                .code("DATA_CORE")
                .name("Data Core")
                .category("MATERIAL")
                .stackable(true)
                .maxStack(999)
                .tradable(tradable)
                .sellable(true)
                .usable(false)
                .rarity("COMMON")
                .build();
    }

    private void stubResponse(UUID clanId) {
        when(storageItemRepository.findByClanIdOrderByCreatedAtAsc(clanId)).thenReturn(List.of());
        when(historyRepository.findRecentByClanId(clanId, 100)).thenReturn(List.of());
        when(clanBonusService.getUpgradeLevel(clanId, "CLAN_STORAGE_CAPACITY")).thenReturn(0);
        when(upgradeTypeRepository.findById("CLAN_STORAGE_CAPACITY")).thenReturn(Optional.empty());
    }

    @Test
    void depositMovesQuantityFromPlayerToClanStorage() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        Player actor = player(playerId, clanId, ClanRole.MEMBER);
        Clan clan = clan(clanId);
        ItemDefinition definition = definition(10L, true);
        InventoryItem personal = InventoryItem.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .itemType(ItemType.DATA_CORE)
                .itemDefinition(definition)
                .quantity(12)
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(actor));
        when(clanRepository.findByIdForUpdate(clanId)).thenReturn(Optional.of(clan));
        when(itemDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));
        when(inventoryRepository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, 10L)).thenReturn(Optional.of(personal));
        when(storageItemRepository.findByClanIdAndItemDefinitionIdForUpdate(clanId, 10L)).thenReturn(List.of());
        when(storageItemRepository.countByClanId(clanId)).thenReturn(0L);
        stubResponse(clanId);

        service.deposit(token(playerId), clanId, new ClanStorageItemRequest(10L, 5));

        assertEquals(7, personal.getQuantity());
        verify(inventoryRepository).save(personal);
        verify(storageItemRepository).save(argThat(item -> item.getClanId().equals(clanId)
                && item.getItemDefinition().getId().equals(10L)
                && item.getQuantity() == 5));
        verify(historyRepository).save(any());
    }

    @Test
    void memberCannotWithdrawFromClanStorage() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        Player actor = player(playerId, clanId, ClanRole.MEMBER);
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(actor));
        when(clanRepository.findByIdForUpdate(clanId)).thenReturn(Optional.of(clan(clanId)));
        doThrow(new ForbiddenException("Only clan officers and leaders can withdraw items"))
                .when(authorization).assertCanWithdrawStorage(any(Player.class), any(Clan.class));

        assertThrows(ForbiddenException.class, () -> service.withdraw(
                token(playerId), clanId, new ClanStorageItemRequest(10L, 1)
        ));
        verifyNoInteractions(itemDefinitionRepository, storageItemRepository, inventoryRepository);
    }

    @Test
    void nonTradableItemCannotBeDeposited() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        Player actor = player(playerId, clanId, ClanRole.MEMBER);
        ItemDefinition definition = definition(10L, false);
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(actor));
        when(clanRepository.findByIdForUpdate(clanId)).thenReturn(Optional.of(clan(clanId)));
        when(itemDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        assertThrows(BadRequestException.class, () -> service.deposit(
                token(playerId), clanId, new ClanStorageItemRequest(10L, 1)
        ));
        verifyNoInteractions(inventoryRepository, storageItemRepository, historyRepository);
    }

    @Test
    void officerCanWithdrawPartialStack() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        Player actor = player(playerId, clanId, ClanRole.OFFICER);
        Clan clan = clan(clanId);
        ItemDefinition definition = definition(10L, true);
        ClanStorageItem stored = ClanStorageItem.create(clanId, definition, 8);

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(actor));
        when(clanRepository.findByIdForUpdate(clanId)).thenReturn(Optional.of(clan));
        when(itemDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));
        when(storageItemRepository.findByClanIdAndItemDefinitionIdForUpdate(clanId, 10L)).thenReturn(List.of(stored));
        when(inventoryRepository.findByPlayerIdAndItemDefinitionIdForUpdate(playerId, 10L)).thenReturn(Optional.empty());
        stubResponse(clanId);

        service.withdraw(token(playerId), clanId, new ClanStorageItemRequest(10L, 3));

        assertEquals(5, stored.getQuantity());
        verify(storageItemRepository).save(stored);
        verify(inventoryRepository).save(argThat(item -> item.getPlayerId().equals(playerId)
                && item.getQuantity() == 3));
        verify(historyRepository).save(any());
    }
}
