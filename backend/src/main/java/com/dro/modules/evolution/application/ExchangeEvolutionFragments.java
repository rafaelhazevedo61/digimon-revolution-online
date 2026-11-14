package com.dro.modules.evolution.application;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.evolution.domain.EvolutionLineStep;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.application.ConsumeItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExchangeEvolutionFragments {
    private static final int RATE = 3;
    private static final int MAX_INPUT = 2997;

    private final PlayerRepository players;
    private final DigimonRepository digimons;
    private final EvolutionLineRepository lines;
    private final InventoryRepository inventory;
    private final ItemDefinitionRepository definitions;
    private final ConsumeItemUseCase consume;
    private final AddItemUseCase grant;

    public ExchangeEvolutionFragments(PlayerRepository players, DigimonRepository digimons,
                                      EvolutionLineRepository lines, InventoryRepository inventory,
                                      ItemDefinitionRepository definitions, ConsumeItemUseCase consume,
                                      AddItemUseCase grant) {
        this.players = players;
        this.digimons = digimons;
        this.lines = lines;
        this.inventory = inventory;
        this.definitions = definitions;
        this.consume = consume;
        this.grant = grant;
    }

    @Transactional(readOnly = true)
    public Offer offer(UUID playerId, UUID digimonId, long lineId) {
        Player player = players.findById(playerId).orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        ItemDefinition target = target(player, digimonId, lineId);
        List<Fragment> inputs = inventory.findByPlayerId(playerId).stream()
                .filter(item -> item.getQuantity() > 0 && isInput(item.getItemDefinition(), target))
                .map(item -> new Fragment(item.getItemDefinition().getCode(),
                        item.getItemDefinition().getName(), item.getQuantity()))
                .sorted(Comparator.comparingInt(Fragment::quantity).reversed().thenComparing(Fragment::name))
                .toList();
        return new Offer(target.getName(), RATE, inputs);
    }

    @Transactional
    public Result exchange(UUID playerId, UUID digimonId, long lineId, Map<String, Integer> inputs) {
        Player player = players.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        ItemDefinition target = target(player, digimonId, lineId);
        long total = inputs.values().stream().mapToLong(Integer::longValue).sum();
        if (total < RATE || total > MAX_INPUT || total % RATE != 0) {
            throw new BadRequestException("Selecione fragmentos em múltiplos de 3, até " + MAX_INPUT + " por troca.");
        }
        for (String code : inputs.keySet().stream().sorted().toList()) {
            ItemDefinition item = definitions.findByCode(code).orElse(null);
            if (!isInput(item, target)) {
                throw new BadRequestException("Entregue fragmentos de outras formas Baby II.");
            }
            consume.consumePlayerMaterial(playerId, item.getId(), inputs.get(code));
        }
        int received = (int) (total / RATE);
        grant.addMaterialToPlayer(playerId, target, received);
        return new Result(target.getName(), received);
    }

    private ItemDefinition target(Player player, UUID digimonId, long lineId) {
        if (!digimonId.equals(player.getActiveDigimonId())) {
            throw new BadRequestException("Selecione o Digimon ativo para trocar fragmentos.");
        }
        var digimon = digimons.findById(digimonId)
                .filter(candidate -> player.getId().equals(candidate.getPlayerId()))
                .orElseThrow(() -> new NotFoundException("Digimon não encontrado."));
        if (digimon.getStage() != Stage.BABY || digimon.getDigimonInfoId() == null) {
            throw new BadRequestException("A troca está disponível para a evolução a Baby II.");
        }
        var line = lines.findByIdAndActiveTrue(lineId)
                .orElseThrow(() -> new NotFoundException("Linha evolutiva indisponível."));
        var steps = line.getSteps().stream().sorted(Comparator.comparingInt(EvolutionLineStep::getStepOrder)).toList();
        for (int index = 0; index < steps.size() - 1; index++) {
            EvolutionLineStep current = steps.get(index);
            EvolutionLineStep next = steps.get(index + 1);
            if (digimon.getDigimonInfoId().equals(current.getDigimonInfo().getId()) && next.getStage() == Stage.BABY_II) {
                return next.getMaterials().stream()
                        .map(material -> definitions.findByCode(material.getMaterialCode()).orElse(null))
                        .filter(ExchangeEvolutionFragments::isBabyFragment)
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("Essa evolução não tem fragmento Baby II para troca."));
            }
        }
        throw new BadRequestException("Essa linha não oferece uma evolução Baby II para o Digimon ativo.");
    }

    private static boolean isInput(ItemDefinition item, ItemDefinition target) {
        return isBabyFragment(item) && !item.getId().equals(target.getId());
    }

    private static boolean isBabyFragment(ItemDefinition item) {
        return item != null && "EVOLUTION_MATERIAL".equals(item.getCategory())
                && item.getCode().startsWith("FRAGMENT_") && "fragment_baby2".equals(item.getIcon());
    }

    public record Offer(String targetName, int rate, List<Fragment> inputs) {}
    public record Fragment(String code, String name, int quantity) {}
    public record Result(String targetName, int quantity) {}
}
