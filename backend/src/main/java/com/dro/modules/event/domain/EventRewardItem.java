package com.dro.modules.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Item individual armazenado dentro de uma premiação de evento. */
@Entity
@Table(name = "event_reward_items")
public class EventRewardItem {
    @Id
    private UUID id;
    @Column(name = "event_reward_id", nullable = false)
    private UUID eventRewardId;
    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;
    @Column(name = "item_definition_code", length = 128)
    private String itemDefinitionCode;
    @Column(name = "item_quantity", nullable = false)
    private int itemQuantity;
    @Column(nullable = false)
    private int position;

    public static class EventRewardItemBuilder {
        private UUID id;
        private UUID eventRewardId;
        private String itemType;
        private String itemDefinitionCode;
        private int itemQuantity;
        private int position;

        EventRewardItemBuilder() {
        }

        public EventRewardItemBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public EventRewardItemBuilder eventRewardId(UUID eventRewardId) {
            this.eventRewardId = eventRewardId;
            return this;
        }

        public EventRewardItemBuilder itemType(String itemType) {
            this.itemType = itemType;
            return this;
        }

        public EventRewardItemBuilder itemDefinitionCode(String itemDefinitionCode) {
            this.itemDefinitionCode = itemDefinitionCode;
            return this;
        }

        public EventRewardItemBuilder itemQuantity(int itemQuantity) {
            this.itemQuantity = itemQuantity;
            return this;
        }

        public EventRewardItemBuilder position(int position) {
            this.position = position;
            return this;
        }

        public EventRewardItem build() {
            return new EventRewardItem(id, eventRewardId, itemType, itemDefinitionCode, itemQuantity, position);
        }
    }

    public static EventRewardItemBuilder builder() {
        return new EventRewardItemBuilder();
    }

    protected EventRewardItem() {
    }

    private EventRewardItem(UUID id, UUID eventRewardId, String itemType, String itemDefinitionCode, int itemQuantity, int position) {
        this.id = id;
        this.eventRewardId = eventRewardId;
        this.itemType = itemType;
        this.itemDefinitionCode = itemDefinitionCode;
        this.itemQuantity = itemQuantity;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventRewardId() {
        return eventRewardId;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemDefinitionCode() {
        return itemDefinitionCode;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public int getPosition() {
        return position;
    }
}
