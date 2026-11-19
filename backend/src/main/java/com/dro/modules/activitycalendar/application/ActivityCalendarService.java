package com.dro.modules.activitycalendar.application;

import com.dro.modules.activitycalendar.api.dto.response.ActivityCalendarDayResponse;
import com.dro.modules.activitycalendar.api.dto.response.ActivityCalendarResponse;
import com.dro.modules.activitycalendar.domain.*;
import com.dro.modules.activitycalendar.infra.*;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ActivityCalendarService {
    private final ActivityCalendarDailyRepository dailyRepository;
    private final ActivityCalendarMonthlyRepository monthlyRepository;
    private final ActivityPointEventRepository eventRepository;
    private final PlayerRepository playerRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;
    private final AddItemUseCase addItemUseCase;
    private final GameplayConfig gameplayConfig;

    @Transactional
    public void recordActivity(UUID playerId, ActivitySource source, String referenceId) {
        var config = gameplayConfig.getActivityCalendar();
        if (!config.isEnabled()) return;
        int points = pointsFor(config, source);
        if (points <= 0 || eventRepository.existsByPlayerIdAndSourceAndSourceReferenceId(playerId, source.name(), referenceId)) return;
        LocalDate date = LocalDate.now();
        ActivityCalendarDaily daily = dailyRepository.findByPlayerIdAndActivityDate(playerId, date).orElseGet(() -> dailyRepository.save(ActivityCalendarDaily.create(playerId, date, Instant.now())));
        int limit = limitFor(config, source);
        if (limit > 0) {
            int already = eventRepository.findAll().stream().filter(e -> e.getPlayerId().equals(playerId) && e.getActivityDate().equals(date) && e.getSource().equals(source.name())).mapToInt(ActivityPointEvent::getPoints).sum();
            points = Math.min(points, Math.max(0, limit - already));
        }
        if (points <= 0) return;
        Instant now = Instant.now();
        eventRepository.save(ActivityPointEvent.create(playerId, date, source.name(), referenceId, points, null, now));
        daily.addPoints(points, config.getDailyGoal(), now);
        dailyRepository.save(daily);
    }

    @Transactional(readOnly = true)
    public ActivityCalendarResponse current(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return build(playerId, LocalDate.now());
    }

    @Transactional
    public ActivityCalendarResponse claimDay(String token, LocalDate date) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        LocalDate today = LocalDate.now();
        if (!YearMonth.from(date).equals(YearMonth.from(today))) throw new BadRequestException("Só é possível resgatar recompensas do mês atual.");
        ActivityCalendarDaily daily = dailyRepository.findByPlayerIdAndActivityDate(playerId, date).orElseThrow(() -> new BadRequestException("A meta diária ainda não foi atingida."));
        if (!daily.isGoalReached()) throw new BadRequestException("A meta diária ainda não foi atingida.");
        if (daily.isRewardClaimed()) throw new ConflictException("A recompensa deste dia já foi resgatada.");
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        ChestDefinitionEntity chest = resolveChest(gameplayConfig.getActivityCalendar().getRewardChestCode());
        addChest(player, chest);
        Instant now = Instant.now();
        daily.markRewardClaimed(now);
        dailyRepository.save(daily);
        ActivityCalendarMonthly monthly = getMonthly(playerId, YearMonth.from(today), now);
        monthly.incrementClaimedDays(now);
        monthlyRepository.save(monthly);
        return build(playerId, today);
    }

    @Transactional
    public ActivityCalendarResponse claimMonthly(String token, YearMonth month) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        YearMonth current = YearMonth.from(LocalDate.now());
        if (!current.equals(month)) throw new BadRequestException("Só é possível resgatar a conclusão do mês atual.");
        ActivityCalendarMonthly monthly = monthlyRepository.findByPlayerIdAndYearMonth(playerId, month.toString()).orElseThrow(() -> new BadRequestException("O calendário mensal ainda não foi concluído."));
        if (!monthly.isEligible()) throw new BadRequestException("Resgate todas as recompensas diárias para liberar o baú mensal.");
        if (monthly.isRewardClaimed()) throw new ConflictException("O baú de conclusão mensal já foi resgatado.");
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        addChest(player, resolveChest(gameplayConfig.getActivityCalendar().getMonthlyCompletionChestCode()));
        monthly.markRewardClaimed(Instant.now());
        monthlyRepository.save(monthly);
        return build(playerId, LocalDate.now());
    }

    private ActivityCalendarResponse build(UUID playerId, LocalDate today) {
        YearMonth month = YearMonth.from(today);
        int totalDays = month.lengthOfMonth();
        List<ActivityCalendarDaily> stored = dailyRepository.findByPlayerIdAndActivityDateBetweenOrderByActivityDateAsc(playerId, month.atDay(1), month.atEndOfMonth());
        Map<LocalDate, ActivityCalendarDaily> byDate = new HashMap<>();
        stored.forEach(d -> byDate.put(d.getActivityDate(), d));
        List<ActivityCalendarDayResponse> days = new ArrayList<>();
        for (int day = 1; day <= totalDays; day++) {
            LocalDate date = month.atDay(day); ActivityCalendarDaily d = byDate.get(date);
            days.add(new ActivityCalendarDayResponse(date, day, d == null ? 0 : d.getPoints(), d != null && d.isGoalReached(), d != null && d.isRewardClaimed(), d == null ? null : d.getGoalReachedAt(), d == null ? null : d.getRewardClaimedAt()));
        }
        ActivityCalendarMonthly m = monthlyRepository.findByPlayerIdAndYearMonth(playerId, month.toString()).orElse(null);
        ActivityCalendarDayResponse current = days.get(today.getDayOfMonth() - 1);
        return new ActivityCalendarResponse(month.toString(), gameplayConfig.getActivityCalendar().getDailyGoal(), gameplayConfig.getActivityCalendar().getRewardChestCode(), gameplayConfig.getActivityCalendar().getMonthlyCompletionChestCode(), totalDays, today, current.points(), current.goalReached(), m == null ? 0 : m.getClaimedDays(), m != null && m.isEligible(), m != null && m.isRewardClaimed(), m == null ? null : m.getMonthlyCompletionEligibleAt(), m == null ? null : m.getMonthlyRewardClaimedAt(), days);
    }

    private ActivityCalendarMonthly getMonthly(UUID playerId, YearMonth month, Instant now) {
        return monthlyRepository.findByPlayerIdAndYearMonth(playerId, month.toString()).orElseGet(() -> ActivityCalendarMonthly.create(playerId, month.toString(), month.lengthOfMonth(), now));
    }
    private void addChest(Player player, ChestDefinitionEntity chest) {
        if (player.getActiveDigimonId() == null) throw new BadRequestException("No active digimon selected");
        addItemUseCase.addMaterial(player.getActiveDigimonId(), chest.getItemDefinition(), 1);
    }
    private ChestDefinitionEntity resolveChest(String code) {
        ChestDefinitionEntity chest = chestDefinitionRepository.findWithCatalogByCode(code).orElseThrow(() -> new ConflictException("Baú do calendário não encontrado: " + code));
        if (!chest.isActive() || chest.getLootTable() == null || !chest.getLootTable().isActive()) throw new ConflictException("Baú do calendário ou sua Loot Table está inativa: " + code);
        return chest;
    }
    private int pointsFor(GameplayConfig.ActivityCalendar c, ActivitySource s) { return switch (s) { case MISSION_COMPLETED -> c.getMissionCompleted(); case ARENA_MATCH -> c.getArenaMatch(); case CLAN_RAID_ATTACK -> c.getClanRaidAttack(); case WORLD_BOSS_ATTACK -> c.getWorldBossAttack(); case BOSS_CHALLENGE -> c.getBossChallenge(); case DIGITAMA_HATCHED -> c.getDigitamaHatched(); }; }
    private int limitFor(GameplayConfig.ActivityCalendar c, ActivitySource s) { return switch (s) { case MISSION_COMPLETED -> c.getMissionLimit(); case ARENA_MATCH -> c.getArenaLimit(); case CLAN_RAID_ATTACK -> c.getClanRaidLimit(); case WORLD_BOSS_ATTACK -> c.getWorldBossLimit(); case BOSS_CHALLENGE -> c.getBossLimit(); case DIGITAMA_HATCHED -> c.getHatchLimit(); }; }
    public ActivityCalendarService(ActivityCalendarDailyRepository dailyRepository, ActivityCalendarMonthlyRepository monthlyRepository, ActivityPointEventRepository eventRepository, PlayerRepository playerRepository, ChestDefinitionRepository chestDefinitionRepository, AddItemUseCase addItemUseCase, GameplayConfig gameplayConfig) { this.dailyRepository = dailyRepository; this.monthlyRepository = monthlyRepository; this.eventRepository = eventRepository; this.playerRepository = playerRepository; this.chestDefinitionRepository = chestDefinitionRepository; this.addItemUseCase = addItemUseCase; this.gameplayConfig = gameplayConfig; }
}
