package com.dro.modules.clan.domain;

import com.dro.modules.player.domain.Player;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Regras de criação, progressão, capacidade e autorização dos Clãs.
 *
 * <p>O clã começa com cinco vagas e pode chegar ao limite do nível 10. A
 * experiência obtida em missões, Boss, Arena e Rebirth é definida aqui para
 * manter a progressão consistente entre os módulos.</p>
 */
public final class ClanRules {
    public static final int MAX_NAME_LENGTH = 30;
    public static final int MIN_TAG_LENGTH = 2;
    public static final int MAX_TAG_LENGTH = 5;
    public static final int MAX_DESCRIPTION_LENGTH = 280;
    public static final int INITIAL_MAX_MEMBERS = 5;
    public static final int CREATE_COST = 0; // bits; 0 no MVP, ajustável aqui
    public static final int MAX_LEVEL = 10;
    private static final List<LevelBonus> LEVEL_BONUSES = List.of(new LevelBonus(1, 0, 0), new LevelBonus(2, 1000, 1), new LevelBonus(3, 2500, 1), new LevelBonus(4, 5000, 1), new LevelBonus(5, 10000, 2), new LevelBonus(6, 18000, 1), new LevelBonus(7, 30000, 1), new LevelBonus(8, 48000, 1), new LevelBonus(9, 72000, 1), new LevelBonus(10, 105000, 1));
    private static final Map<Integer, LevelBonus> BY_LEVEL = LEVEL_BONUSES.stream().collect(java.util.stream.Collectors.toMap(LevelBonus::level, b -> b));
    private static final Map<Integer, Integer> CUMULATIVE_MEMBERS_BONUS = buildCumulativeBonus();

    private static Map<Integer, Integer> buildCumulativeBonus() {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        int cumulative = 0;
        for (LevelBonus bonus : LEVEL_BONUSES) {
            cumulative += bonus.maxMembersBonus();
            map.put(bonus.level(), cumulative);
        }
        return Map.copyOf(map);
    }

    /**
     * Verifica nome com até 30 caracteres e somente símbolos permitidos.
     */
    public static boolean isNameValid(String name) {
        return name != null && !name.isBlank() && name.length() <= MAX_NAME_LENGTH && name.matches("^[\\p{L}0-9 _-]+$");
    }

    /**
     * Verifica tag alfanumérica entre 2 e 5 caracteres.
     */
    public static boolean isTagValid(String tag) {
        return tag != null && tag.length() >= MIN_TAG_LENGTH && tag.length() <= MAX_TAG_LENGTH && tag.matches("^[A-Za-z0-9]+$");
    }

    /**
     * Verifica descrição opcional com até 280 caracteres.
     */
    public static boolean isDescriptionValid(String description) {
        return description == null || description.length() <= MAX_DESCRIPTION_LENGTH;
    }

    /**
     * Valida os dados de criação e lança erro quando qualquer limite é violado.
     */
    public static void validateCreateRequest(String name, String tag, String description) {
        if (!isNameValid(name)) {
            throw new BadRequestException("Clan name must be 1-30 characters and contain only letters, numbers, spaces, underscores or hyphens");
        }
        if (!isTagValid(tag)) {
            throw new BadRequestException("Clan tag must be " + MIN_TAG_LENGTH + "-" + MAX_TAG_LENGTH + " alphanumeric characters");
        }
        if (!isDescriptionValid(description)) {
            throw new BadRequestException("Clan description must be at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }

    /**
     * Retorna a capacidade acumulada de membros para o nível informado.
     */
    public static int getMaxMembersForLevel(int level) {
        int capped = Math.min(level, MAX_LEVEL);
        int bonus = CUMULATIVE_MEMBERS_BONUS.getOrDefault(capped, 0);
        return INITIAL_MAX_MEMBERS + bonus;
    }

    /**
     * Calcula a experiência restante até o próximo nível do clã.
     */
    public static int xpToNextLevel(int currentLevel, int currentXp) {
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        LevelBonus next = BY_LEVEL.get(currentLevel + 1);
        return Math.max(0, next.xpRequired() - currentXp);
    }

    /**
     * Adiciona experiência e aplica todos os níveis alcançados pelo clã.
     *
     * @return quantidade de níveis obtidos nesta operação
     */
    public static int addExperience(Clan clan, int xp) {
        if (xp <= 0) {
            return 0;
        }
        clan.setExperience(clan.getExperience() + xp);
        int levelsGained = 0;
        while (clan.getLevel() < MAX_LEVEL) {
            LevelBonus next = BY_LEVEL.get(clan.getLevel() + 1);
            if (clan.getExperience() < next.xpRequired()) {
                break;
            }
            clan.setLevel(clan.getLevel() + 1);
            clan.setMaxMembers(getMaxMembersForLevel(clan.getLevel()));
            levelsGained++;
        }
        return levelsGained;
    }

    /**
     * Informa se o cargo pode editar informações do clã.
     */
    public static boolean canManageClanInfo(ClanRole role) {
        return role == ClanRole.LEADER || role == ClanRole.OFFICER;
    }

    /**
     * Verifica se o cargo de origem pode expulsar o cargo de destino.
     */
    public static boolean canKick(ClanRole actorRole, ClanRole targetRole) {
        if (actorRole == ClanRole.LEADER) {
            return targetRole != ClanRole.LEADER;
        }
        return actorRole == ClanRole.OFFICER && targetRole == ClanRole.MEMBER;
    }

    /**
     * Verifica se o líder pode promover um membro a oficial.
     */
    public static boolean canPromote(ClanRole actorRole, ClanRole targetRole) {
        return actorRole == ClanRole.LEADER && targetRole == ClanRole.MEMBER;
    }

    /**
     * Verifica se o líder pode rebaixar um oficial a membro.
     */
    public static boolean canDemote(ClanRole actorRole, ClanRole targetRole) {
        return actorRole == ClanRole.LEADER && targetRole == ClanRole.OFFICER;
    }

    /**
     * Somente o líder atual pode transferir a liderança.
     */
    public static boolean canTransferLeadership(ClanRole actorRole) {
        return actorRole == ClanRole.LEADER;
    }

    /**
     * Somente o líder atual pode dissolver o clã.
     */
    public static boolean canDissolve(ClanRole actorRole) {
        return actorRole == ClanRole.LEADER;
    }

    public static void assertInClan(Player player, UUID clanId) {
        if (player.getClanId() == null || !player.getClanId().equals(clanId)) {
            throw new ForbiddenException("You are not a member of this clan");
        }
    }

    public static void assertLeader(Player player, UUID clanId) {
        assertInClan(player, clanId);
        if (player.getClanRole() != ClanRole.LEADER) {
            throw new ForbiddenException("Only the clan leader can do this");
        }
    }

    /**
     * Cria um clã normalizando nome, tag e descrição antes da persistência.
     */
    public static Clan create(String name, String tag, String description, UUID leaderId) {
        String normalizedName = name.trim();
        String normalizedTag = tag.toUpperCase().trim();
        String normalizedDescription = description != null ? description.trim() : null;
        validateCreateRequest(normalizedName, normalizedTag, normalizedDescription);
        LocalDateTime now = LocalDateTime.now();
        return Clan.builder().id(UUID.randomUUID()).name(normalizedName).tag(normalizedTag).description(normalizedDescription).leaderId(leaderId).emblem(null).maxMembers(INITIAL_MAX_MEMBERS).level(1).experience(0).createdAt(now).build();
    }

    /**
     * Experiência concedida por conclusão de missão.
     */
    public static int experienceForMission() {
        return 10;
    }

    /**
     * Experiência concedida por atividade de Boss.
     */
    public static int experienceForBoss() {
        return 25;
    }

    /**
     * Experiência concedida por Arena, diferenciando vitória e derrota.
     */
    public static int experienceForArena(boolean victory) {
        return victory ? 15 : 5;
    }

    /**
     * Experiência concedida por Rebirth.
     */
    public static int experienceForRebirth() {
        return 100;
    }


    private static record LevelBonus(int level, int xpRequired, int maxMembersBonus) {
    }

    private ClanRules() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
