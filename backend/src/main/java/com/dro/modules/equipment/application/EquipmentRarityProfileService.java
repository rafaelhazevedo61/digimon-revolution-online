package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.UpdateEquipmentRarityProfileRequest;
import com.dro.modules.equipment.api.dto.response.AdminEquipmentRarityProfileResponse;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentRarityProfileEntity;
import com.dro.modules.equipment.domain.EquipmentRarityRules;
import com.dro.modules.equipment.infra.EquipmentRarityProfileRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Resolve e administra os perfis percentuais de raridade de equipamentos.
 *
 * <p>O combate consulta este serviço para que a configuração exibida e
 * alterada pelo painel seja a mesma configuração utilizada no roll. O
 * fallback para {@link EquipmentRarityRules} preserva a compatibilidade
 * durante a inicialização ou em ambientes anteriores à V108.</p>
 */
@Service
@RequiredArgsConstructor
public class EquipmentRarityProfileService {

    private static final String BOSS_PREFIX = "BOSS_";

    private final EquipmentRarityProfileRepository repository;
    private final TransactionAuditPublisher transactionAuditPublisher;

    /** Lista somente os perfis de Boss que podem ser administrados nesta sprint. */
    @Transactional(readOnly = true)
    public List<AdminEquipmentRarityProfileResponse> listBossProfiles() {
        return repository.findAllByOrderByProfileKeyAsc().stream()
                .filter(profile -> profile.getProfileKey().startsWith(BOSS_PREFIX))
                .map(AdminEquipmentRarityProfileResponse::from)
                .toList();
    }

    /**
     * Atualiza os percentuais de um perfil de Boss.
     *
     * @param profileKey chave do perfil, por exemplo {@code BOSS_WEEKLY}
     * @param request percentuais informados pelo painel
     * @return perfil persistido
     */
    @Transactional
    public AdminEquipmentRarityProfileResponse updateBossProfile(
            String profileKey,
            UpdateEquipmentRarityProfileRequest request
    ) {
        String normalizedKey = profileKey == null ? "" : profileKey.trim().toUpperCase();
        if (!normalizedKey.startsWith(BOSS_PREFIX)) {
            throw new NotFoundException("Perfil de raridade de Boss não encontrado: " + profileKey);
        }

        int total = request.commonPercent()
                + request.rarePercent()
                + request.epicPercent()
                + request.legendaryPercent();
        if (total != 100) {
            throw new BadRequestException("Os percentuais de raridade devem somar 100%. Soma atual: " + total + "%." );
        }

        EquipmentRarityProfileEntity profile = repository.findByProfileKey(normalizedKey)
                .orElseThrow(() -> new NotFoundException(
                        "Perfil de raridade de Boss não encontrado: " + normalizedKey));

        profile.setCommonPercent(request.commonPercent());
        profile.setRarePercent(request.rarePercent());
        profile.setEpicPercent(request.epicPercent());
        profile.setLegendaryPercent(request.legendaryPercent());
        EquipmentRarityProfileEntity saved = repository.save(profile);

        transactionAuditPublisher.success(
                "equipment-rarity-profile:" + saved.getProfileKey(),
                "ADMIN_EQUIPMENT_RARITY_PROFILE_UPDATED",
                "EquipmentRarityProfile",
                saved.getProfileKey(),
                Map.of(
                        "profileKey", saved.getProfileKey(),
                        "commonPercent", saved.getCommonPercent(),
                        "rarePercent", saved.getRarePercent(),
                        "epicPercent", saved.getEpicPercent(),
                        "legendaryPercent", saved.getLegendaryPercent()
                )
        );

        return AdminEquipmentRarityProfileResponse.from(saved);
    }

    /** Resolve um perfil para o sorteio, preferindo a configuração persistida. */
    @Transactional(readOnly = true)
    public EquipmentRarityRules.RarityProfile resolve(String profileKey) {
        return repository.findByProfileKey(profileKey)
                .map(this::toRarityProfile)
                .orElseGet(() -> fallbackProfile(profileKey));
    }

    /** Executa o roll de raridade usando o perfil persistido do contexto. */
    @Transactional(readOnly = true)
    public EquipmentRarity roll(String profileKey, double qualityBonusPercent) {
        return EquipmentRarityRules.rollRarity(resolve(profileKey), qualityBonusPercent);
    }

    private EquipmentRarityRules.RarityProfile toRarityProfile(EquipmentRarityProfileEntity entity) {
        return new EquipmentRarityRules.RarityProfile(
                entity.getCommonPercent(),
                entity.getRarePercent(),
                entity.getEpicPercent(),
                entity.getLegendaryPercent()
        );
    }

    private EquipmentRarityRules.RarityProfile fallbackProfile(String profileKey) {
        EquipmentRarityRules.RarityProfile fallback = EquipmentRarityRules.getProfile(profileKey);
        if (fallback == null) {
            throw new NotFoundException("Perfil de raridade não encontrado: " + profileKey);
        }
        return fallback;
    }
}
