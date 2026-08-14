package com.dro.modules.clan.api;

import com.dro.modules.clan.api.dto.request.ChangeRoleRequest;
import com.dro.modules.clan.api.dto.request.ClanCreateRequest;
import com.dro.modules.clan.api.dto.request.ClanUpdateRequest;
import com.dro.modules.clan.api.dto.response.BuySlotResponse;
import com.dro.modules.clan.api.dto.response.ClanRankingEntryResponse;
import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.api.dto.response.ClanSummaryResponse;
import com.dro.modules.clan.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clans")
@RequiredArgsConstructor
public class ClanController {

    private final CreateClanUseCase createClanUseCase;
    private final ListClansUseCase listClansUseCase;
    private final GetClanUseCase getClanUseCase;
    private final GetMyClanUseCase getMyClanUseCase;
    private final JoinClanUseCase joinClanUseCase;
    private final LeaveClanUseCase leaveClanUseCase;
    private final UpdateClanUseCase updateClanUseCase;
    private final KickMemberUseCase kickMemberUseCase;
    private final ChangeRoleUseCase changeRoleUseCase;
    private final TransferLeadershipUseCase transferLeadershipUseCase;
    private final DissolveClanUseCase dissolveClanUseCase;
    private final BuySlotUseCase buySlotUseCase;
    private final GetClanRankingUseCase getClanRankingUseCase;

    @PostMapping
    public ResponseEntity<ClanResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid ClanCreateRequest request
    ) {
        return ResponseEntity.ok(createClanUseCase.execute(
                authorization, request.name(), request.tag(), request.description()));
    }

    @GetMapping
    public ResponseEntity<Page<ClanSummaryResponse>> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(listClansUseCase.execute(query, page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<ClanResponse> getMyClan(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getMyClanUseCase.execute(authorization));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClanResponse> getById(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(getClanUseCase.execute(authorization, id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClanResponse> update(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id,
            @RequestBody @Valid ClanUpdateRequest request
    ) {
        return ResponseEntity.ok(updateClanUseCase.execute(
                authorization, id, request.description(), request.emblem()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> dissolve(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        dissolveClanUseCase.execute(authorization, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ClanResponse> join(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(joinClanUseCase.execute(authorization, id));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<ClanResponse> leave(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(leaveClanUseCase.execute(authorization, id));
    }

    @PostMapping("/{id}/members/{username}/kick")
    public ResponseEntity<ClanResponse> kick(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id,
            @PathVariable String username
    ) {
        return ResponseEntity.ok(kickMemberUseCase.execute(authorization, id, username));
    }

    @PostMapping("/{id}/members/{username}/role")
    public ResponseEntity<ClanResponse> changeRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id,
            @PathVariable String username,
            @RequestBody @Valid ChangeRoleRequest request
    ) {
        return ResponseEntity.ok(changeRoleUseCase.execute(authorization, id, username, request));
    }

    @PostMapping("/{id}/members/{username}/transfer")
    public ResponseEntity<ClanResponse> transferLeadership(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id,
            @PathVariable String username
    ) {
        return ResponseEntity.ok(transferLeadershipUseCase.execute(authorization, id, username));
    }

    @PostMapping("/{id}/upgrade/buy-slot")
    public ResponseEntity<BuySlotResponse> buySlot(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(buySlotUseCase.execute(authorization, id));
    }

    @GetMapping("/ranking")
    public ResponseEntity<Page<ClanRankingEntryResponse>> getRanking(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getClanRankingUseCase.execute(page, size));
    }
}
