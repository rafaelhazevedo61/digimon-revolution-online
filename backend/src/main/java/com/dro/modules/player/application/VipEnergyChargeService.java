package com.dro.modules.player.application;

import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class VipEnergyChargeService {
    private final PlayerRepository players;
    private final ItemDefinitionRepository items;
    private final AddItemUseCase addItem;
    private final VipAccess vipAccess;

    public VipEnergyChargeService(PlayerRepository players, ItemDefinitionRepository items,
                                  AddItemUseCase addItem, VipAccess vipAccess) {
        this.players = players;
        this.items = items;
        this.addItem = addItem;
        this.vipAccess = vipAccess;
    }

    @Transactional(readOnly = true)
    public Status getStatus(UUID playerId) {
        Player player = players.findById(playerId).orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        return status(player, Instant.now());
    }

    @Transactional
    public Status claim(UUID playerId) {
        Player player = vipAccess.lockPlayer(playerId);
        Instant now = Instant.now();
        Status status = status(player, now);
        if (!status.vipActive()) {
            throw new ForbiddenException("O resgate da Carga de Energia Total exige VIP ativo.");
        }
        if (status.claimedToday()) {
            throw new UnprocessableException("Você já resgatou sua Carga de Energia Total de hoje.");
        }

        ItemDefinition charge = items.findByCode(ItemType.VIP_ENERGY_CHARGE.name())
                .orElseThrow(() -> new NotFoundException("Carga de Energia Total não encontrada no catálogo."));
        addItem.addMaterialToPlayer(playerId, charge, 1);
        player.markVipEnergyChargeClaimed(LocalDate.ofInstant(now, ZoneOffset.UTC));
        return status(player, now);
    }

    private Status status(Player player, Instant now) {
        LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
        return new Status(player.getVipExpiresAt(), player.hasActiveVip(now),
                player.hasClaimedVipEnergyCharge(today));
    }

    public record Status(Instant vipExpiresAt, boolean vipActive, boolean claimedToday) {}
}
