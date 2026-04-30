# Plano MVP - Sistema de Equipamentos

## Conceito
Equipamentos que pertencem ao **Digimon** (inventário por digimon). Cada equipamento ocupa um **slot** específico e possui bônus fixos baseados no template do item. O equipamento é concedido ao digimon e pode ser equipado/desequipado livremente.

## Estrutura do Módulo (seguindo padrão existente)

```
modules/equipment/
├── api/
│   ├── EquipmentController.java
│   └── dto/
│       ├── request/EquipRequest.java
│       ├── request/UnequipRequest.java
│       └── response/EquipmentResponse.java
│       └── response/DigimonEquipmentResponse.java
├── application/
│   ├── EquipUseCase.java
│   ├── UnequipUseCase.java
│   ├── GetDigimonInventoryUseCase.java
│   ├── GetDigimonEquipmentUseCase.java
│   └── GrantEquipmentUseCase.java
├── domain/
│   ├── Equipment.java           (entidade JPA)
│   ├── EquipmentSlot.java       (enum: WEAPON, ARMOR, ACCESSORY)
│   ├── EquipmentTemplate.java   (catálogo de equipamentos disponíveis)
│   └── EquipmentRules.java      (regras de negócio)
└── infra/
    └── EquipmentRepository.java
```

## Entidade `Equipment`

| Campo          | Tipo             | Descrição                        |
|----------------|------------------|----------------------------------|
| id             | UUID             | PK                               |
| digimonId      | UUID             | FK para digimon dono (inventário) |
| name           | String           | Nome do equipamento              |
| slot           | EquipmentSlot    | WEAPON, ARMOR, ACCESSORY         |
| rarity         | EquipmentRarity  | COMMON, RARE, EPIC, LEGENDARY    |
| bonusHp        | int              | Bônus de HP                      |
| bonusAttack    | int              | Bônus de Attack                  |
| bonusDefense   | int              | Bônus de Defense                 |
| equipped       | boolean          | Se está equipado (default false) |
| createdAt      | LocalDateTime    | Data de criação                  |

## Vínculo com Digimon

A tabela `digimons` possui colunas para cada slot de equipamento:
- `weapon_id` (UUID nullable) → referencia o equipamento do tipo WEAPON
- `armor_id` (UUID nullable) → referencia o equipamento do tipo ARMOR
- `accessory_id` (UUID nullable) → referencia o equipamento do tipo ACCESSORY

Isso garante no nível do banco que cada digimon só pode ter 1 item por slot.

## Slots por Digimon
- 1x WEAPON (bônus principal: attack)
- 1x ARMOR (bônus principal: defense)
- 1x ACCESSORY (bônus variado)

## Endpoints

| Método | Rota                                       | Descrição                                         |
|--------|--------------------------------------------|---------------------------------------------------|
| GET    | `/equipment/digimon/{digimonId}/inventory` | Lista todos os equipamentos do digimon (inventário) |
| GET    | `/equipment/digimon/{digimonId}`           | Equipamentos equipados num digimon + bônus totais  |
| POST   | `/equipment/equip`                | Equipar item num digimon                |
| POST   | `/equipment/unequip`              | Desequipar item de um digimon           |
| POST   | `/equipment/grant`                | Conceder equipamento (para testes/admin) |

## Regras de Negócio
1. Cada digimon pode ter no máximo **1 equipamento por slot**
2. Só pode equipar em digimon **do próprio jogador**
3. Ao equipar, se já houver item no slot, o item antigo é **automaticamente desequipado** (volta ao inventário) e o novo é equipado (auto-swap)
4. Ao desequipar, o item volta para o inventário do digimon (equipped = false, slot do digimon = null)
5. Equipamentos concedem bônus fixos de stats

## Catálogo Inicial (EquipmentTemplate)

| Nome                  | Slot       | Raridade   | HP  | ATK | DEF |
|-----------------------|------------|------------|-----|-----|-----|
| Iron Claw             | WEAPON     | COMMON     | 0   | 5   | 0   |
| Steel Blade           | WEAPON     | RARE       | 0   | 12  | 0   |
| Chrome Digizoid Sword | WEAPON     | EPIC       | 0   | 25  | 3   |
| Omega Blade           | WEAPON     | LEGENDARY  | 5   | 40  | 5   |
| Leather Armor         | ARMOR      | COMMON     | 5   | 0   | 5   |
| Digi-Armor            | ARMOR      | RARE       | 10  | 0   | 12  |
| Chrome Digizoid Armor | ARMOR      | EPIC       | 20  | 0   | 25  |
| Royal Knight Armor    | ARMOR      | LEGENDARY  | 35  | 5   | 40  |
| Holy Ring             | ACCESSORY  | COMMON     | 3   | 3   | 3   |
| Digivice              | ACCESSORY  | RARE       | 5   | 5   | 5   |
| Crest of Courage      | ACCESSORY  | EPIC       | 10  | 10  | 10  |
| Digi-Egg of Miracles  | ACCESSORY  | LEGENDARY  | 20  | 20  | 20  |

## Migrations
- **V16** – Criar tabela `equipments` com campos: id, digimon_id, name, slot, rarity, bonus_hp, bonus_attack, bonus_defense, equipped, created_at
- **V17** – Adicionar `weapon_id`, `armor_id`, `accessory_id` na tabela digimons
