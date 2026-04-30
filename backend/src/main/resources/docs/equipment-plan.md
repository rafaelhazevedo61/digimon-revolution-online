# Plano MVP - Sistema de Equipamentos

## Conceito
Equipamentos que o jogador pode equipar no seu Digimon ativo para aumentar seus status (HP, Attack, Defense). Cada equipamento ocupa um **slot** específico e possui bônus fixos baseados no template do item.

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
│   ├── GetPlayerEquipmentUseCase.java
│   └── GetDigimonEquipmentUseCase.java
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
| playerId       | UUID             | FK para jogador dono             |
| digimonId      | UUID (nullable)  | FK para digimon equipado (null = no inventário) |
| name           | String           | Nome do equipamento              |
| slot           | EquipmentSlot    | WEAPON, ARMOR, ACCESSORY         |
| rarity         | EquipmentRarity  | COMMON, RARE, EPIC, LEGENDARY    |
| bonusHp        | int              | Bônus de HP                      |
| bonusAttack    | int              | Bônus de Attack                  |
| bonusDefense   | int              | Bônus de Defense                 |
| createdAt      | LocalDateTime    | Data de criação                  |

## Slots por Digimon
- 1x WEAPON (bônus principal: attack)
- 1x ARMOR (bônus principal: defense)
- 1x ACCESSORY (bônus variado)

## Endpoints

| Método | Rota                              | Descrição                               |
|--------|-----------------------------------|-----------------------------------------|
| GET    | `/equipment/player`               | Lista todos os equipamentos do jogador  |
| GET    | `/equipment/digimon/{digimonId}`  | Equipamentos equipados num digimon       |
| POST   | `/equipment/equip`                | Equipar item num digimon                |
| POST   | `/equipment/unequip`              | Desequipar item de um digimon           |
| POST   | `/equipment/grant`                | Conceder equipamento (para testes/admin) |

## Regras de Negócio
1. Cada digimon pode ter no máximo **1 equipamento por slot**
2. Só pode equipar em digimon **do próprio jogador**
3. Ao equipar, se já houver item no slot, retorna erro (precisa desequipar primeiro)
4. Ao desequipar, o item volta para o inventário do jogador (digimonId = null)
5. Equipamentos concedem bônus fixos de stats

## Catálogo Inicial (EquipmentTemplate)

| Nome               | Slot       | Raridade | HP  | ATK | DEF |
|--------------------|------------|----------|-----|-----|-----|
| Iron Claw          | WEAPON     | COMMON   | 0   | 5   | 0   |
| Steel Blade        | WEAPON     | RARE     | 0   | 12  | 0   |
| Chrome Digizoid Sword | WEAPON | EPIC     | 0   | 25  | 3   |
| Leather Armor      | ARMOR      | COMMON   | 5   | 0   | 5   |
| Digi-Armor         | ARMOR      | RARE     | 10  | 0   | 12  |
| Holy Ring           | ACCESSORY  | COMMON   | 3   | 3   | 3   |
| Digivice           | ACCESSORY  | RARE     | 5   | 5   | 5   |
| Crest of Courage   | ACCESSORY  | EPIC     | 10  | 10  | 10  |

## Migration (V16)
- Criar tabela `equipments` com todos os campos acima
