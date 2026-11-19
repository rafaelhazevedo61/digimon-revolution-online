package com.dro.modules.auction.application;

import com.dro.modules.auction.domain.AuctionListing;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/** Runs inside the listing transaction so Bits, custody and history commit or roll back together. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class AuctionEquipmentService {
    private final EquipmentRepository equipmentRepository;

    public AuctionEquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public Equipment reserve(UUID equipmentId, UUID sellerId) {
        Equipment equipment = equipmentRepository.findByIdForUpdate(equipmentId)
                .orElseThrow(() -> new ConflictException("Equipamento indisponível"));
        equipment.reserveForAuction(sellerId);
        equipmentRepository.save(equipment);
        return equipment;
    }

    public void deliver(AuctionListing listing, UUID recipientId) {
        Equipment equipment = equipmentRepository.findByIdForUpdate(listing.getEquipmentId())
                .orElseThrow(() -> new ConflictException("Equipamento reservado não encontrado"));
        equipment.releaseFromAuction(listing.getSellerPlayerId(), recipientId);
        equipmentRepository.save(equipment);
    }
}
