package com.dro.modules.admin.api;

import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/mail/recipients")
@RequiredArgsConstructor
public class AdminEventRewardRecipientController {

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;

    @GetMapping("/clans")
    public List<Map<String, Object>> listClans(@RequestHeader("Authorization") String authorization) {
        requireAdmin(authorization);
        List<Map<String, Object>> result = new ArrayList<>();
        clanRepository.findAll().forEach(clan -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", clan.getId());
            item.put("name", clan.getName());
            item.put("tag", clan.getTag());
            item.put("memberCount", playerRepository.countByClanId(clan.getId()));
            item.put("maxMembers", clan.getMaxMembers());
            result.add(item);
        });
        return result;
    }

    @GetMapping("/clans/{clanId}/members")
    public List<Map<String, Object>> listClanMembers(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID clanId
    ) {
        requireAdmin(authorization);
        if (!clanRepository.existsById(clanId)) {
            throw new NotFoundException("Clã não encontrado.");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        playerRepository.findByClanId(clanId).forEach(player -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", player.getId());
            item.put("username", player.getUsername());
            result.add(item);
        });
        return result;
    }

    @GetMapping("/players")
    public List<Map<String, Object>> searchPlayers(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "") String query
    ) {
        requireAdmin(authorization);
        List<Map<String, Object>> result = new ArrayList<>();
        playerRepository.findTop100ByUsernameContainingIgnoreCaseOrderByUsernameAsc(query.trim())
                .forEach(player -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", player.getId());
                    item.put("username", player.getUsername());
                    item.put("clanId", player.getClanId() == null ? "" : player.getClanId().toString());
                    result.add(item);
                });
        return result;
    }

    private void requireAdmin(String authorization) {
        UUID adminId = TokenExtractor.extractPlayerId(authorization);
        var player = playerRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Administrador não encontrado."));
        if (player.getUserType() != UserType.ADMIN) {
            throw new ForbiddenException("Somente administradores podem consultar destinatários.");
        }
    }
}
