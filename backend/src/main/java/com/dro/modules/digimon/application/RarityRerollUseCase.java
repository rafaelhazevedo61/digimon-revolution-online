package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.RarityRerollResponse;
import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.*;
import com.dro.modules.inventory.domain.*;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RarityRerollUseCase {
    private final PlayerRepository players;
    private final DigimonRepository digimons;
    private final InventoryRepository inventory;
    private final RarityRerollRepository rerolls;
    private final int keepCostBits;

    public RarityRerollUseCase(PlayerRepository players, DigimonRepository digimons, InventoryRepository inventory, RarityRerollRepository rerolls, @Value("${dro.gameplay.rarity-reroll.keep-cost-bits:5000}") int keepCostBits) {
        this.players = players; this.digimons = digimons; this.inventory = inventory; this.rerolls = rerolls; this.keepCostBits = keepCostBits;
    }

    @Transactional
    public RarityRerollResponse start(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = players.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon d = digimons.findByPlayerIdAndStatusForUpdate(playerId, DigimonStatus.ACTIVE).stream().findFirst().orElseThrow(() -> new BadRequestException("Nenhum Digimon ativo selecionado."));
        InventoryItem item = inventory.findByDigimonIdAndItemTypeForUpdate(d.getId(), ItemType.RARITY_REROLL).orElseThrow(() -> new NotFoundException("Item não encontrado."));
        if (item.getQuantity() <= 0) throw new BadRequestException("Você não possui este item.");
        item.setQuantity(item.getQuantity() - 1); if (item.getQuantity() == 0) inventory.delete(item); else inventory.save(item);
        var next = RarityRoller.rollForRarityDie(d.getRarity());
        if (next.isEmpty()) {
            return new RarityRerollResponse(null, d.getRarity(), null, keepCostBits, d.getBits(), "O Dado de Raridade não alterou a raridade do Digimon.");
        }
        RarityReroll rr = rerolls.save(new RarityReroll(UUID.randomUUID(), playerId, d.getId(), d.getRarity(), next.get()));
        return response(rr, d.getBits(), "Escolha se deseja aceitar a nova raridade ou manter a anterior usando o Dado de Raridade.");
    }

    @Transactional
    public RarityRerollResponse accept(String token, UUID id) {
        UUID pid = TokenExtractor.extractPlayerId(token); RarityReroll rr = rerolls.findPendingForUpdate(id, pid, RerollStatus.PENDING).orElseThrow(() -> new NotFoundException("Proposta do Dado de Raridade não encontrada."));
        Digimon d = digimons.findByIdForUpdate(rr.getDigimonId()).orElseThrow(() -> new NotFoundException("Digimon não encontrado.")); d.markRarityChangedByDie(rr.getCurrentRarity(), LocalDateTime.now()); d.setRarity(rr.getNewRarity()); rr.accept(); digimons.save(d); rerolls.save(rr); return response(rr, d.getBits(), "Nova raridade aceita com sucesso.");
    }

    @Transactional
    public RarityRerollResponse keep(String token, UUID id) {
        UUID pid = TokenExtractor.extractPlayerId(token); Player p = players.findByIdForUpdate(pid).orElseThrow(() -> new NotFoundException("Player not found")); RarityReroll rr = rerolls.findPendingForUpdate(id, pid, RerollStatus.PENDING).orElseThrow(() -> new NotFoundException("Proposta do Dado de Raridade não encontrada.")); Digimon d = digimons.findByIdForUpdate(rr.getDigimonId()).orElseThrow(() -> new NotFoundException("Digimon não encontrado.")); if (d.getBits() < keepCostBits) throw new BadRequestException("Você não possui Bits suficientes para manter a raridade anterior."); d.setBits(d.getBits() - keepCostBits); rr.keep(); digimons.save(d); rerolls.save(rr); return response(rr, d.getBits(), "Raridade anterior mantida com o Dado de Raridade.");
    }

    private RarityRerollResponse response(RarityReroll rr, int bits, String message) { return new RarityRerollResponse(rr.getId(), rr.getCurrentRarity(), rr.getNewRarity(), keepCostBits, bits, message); }
}
