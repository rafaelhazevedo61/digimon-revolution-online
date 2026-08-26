#!/usr/bin/env bash
# Collection oficial de exemplos curl do Digimon Revolution Online.
# Gerada a partir dos controllers Java; execute scripts/generate_api_curl_collection.py após alterar endpoints.
# Os payloads são derivados dos DTOs quando possível: revise-os antes de executar.
# Por segurança, as chamadas estão comentadas: descomente apenas o curl que deseja testar.
# Não execute este arquivo inteiro; ele contém operações de criação, compra, exclusão e administração.

BASE_URL="${BASE_URL:-http://localhost:8080}"
TOKEN="${TOKEN:-COLE_SEU_TOKEN_DE_JOGADOR_AQUI}"
ADMIN_TOKEN="${ADMIN_TOKEN:-COLE_SEU_TOKEN_DE_ADMIN_AQUI}"
ID="${ID:-00000000-0000-0000-0000-000000000000}"
USERNAME="${USERNAME:-nome-do-jogador}"
CODE="${CODE:-CODIGO}"
CLAN_ID="${CLAN_ID:-00000000-0000-0000-0000-000000000000}"
MESSAGE_ID="${MESSAGE_ID:-00000000-0000-0000-0000-000000000000}"
MISSION_ID="${MISSION_ID:-00000000-0000-0000-0000-000000000000}"
INSTANCE_ID="${INSTANCE_ID:-00000000-0000-0000-0000-000000000000}"
LISTING_ID="${LISTING_ID:-00000000-0000-0000-0000-000000000000}"
DIGIMON_ID="${DIGIMON_ID:-00000000-0000-0000-0000-000000000000}"
EQUIPMENT_ID="${EQUIPMENT_ID:-00000000-0000-0000-0000-000000000000}"
PLAYER_ID="${PLAYER_ID:-00000000-0000-0000-0000-000000000000}"
ACTIVE="${ACTIVE:-VALOR}"
ACTIVE_ONLY="${ACTIVE_ONLY:-VALOR}"
AMOUNT="${AMOUNT:-VALOR}"
AREA="${AREA:-VALOR}"
ATTEMPTS="${ATTEMPTS:-VALOR}"
ATTRIBUTE="${ATTRIBUTE:-VALOR}"
BOSS_CODE="${BOSS_CODE:-VALOR}"
BOSS_ID="${BOSS_ID:-00000000-0000-0000-0000-000000000000}"
CATEGORY="${CATEGORY:-VALOR}"
CHEST_CODE="${CHEST_CODE:-VALOR}"
DROP_ID="${DROP_ID:-00000000-0000-0000-0000-000000000000}"
ELEMENT="${ELEMENT:-VALOR}"
EMAIL="${EMAIL:-VALOR}"
LOOT_ITEM_TYPE="${LOOT_ITEM_TYPE:-VALOR}"
MISSION_INSTANCE_ID="${MISSION_INSTANCE_ID:-00000000-0000-0000-0000-000000000000}"
NAME="${NAME:-VALOR}"
PAGE="${PAGE:-VALOR}"
PROFILE_KEY="${PROFILE_KEY:-VALOR}"
QUERY="${QUERY:-VALOR}"
RARITY="${RARITY:-VALOR}"
SEARCH="${SEARCH:-VALOR}"
SELECTED_DIGITAMA="${SELECTED_DIGITAMA:-VALOR}"
SELLABLE="${SELLABLE:-VALOR}"
SIZE="${SIZE:-VALOR}"
SPECIE="${SPECIE:-VALOR}"
STAGE="${STAGE:-VALOR}"
STARTER_SELECTED="${STARTER_SELECTED:-VALOR}"
STEP="${STEP:-VALOR}"
TRADABLE="${TRADABLE:-VALOR}"
USABLE="${USABLE:-VALOR}"

# Autenticação: substitua TOKEN/ADMIN_TOKEN antes de executar comandos protegidos.
# Endpoints públicos não precisam do header Authorization.

# ===== ADMIN =====

# AdminBossController.listAll (GET /admin/bosses)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.create (POST /admin/bosses)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"code": "example-code", "name": "example-name", "bossType": "example-bossType", "requiredStage": "example-requiredStage", "requiredLevel": 1, "requiredRebirths": 1, "hp": 1, "atk": 1, "def": 1, "energyCost": 1, "cooldownMinutes": 1, "cooldownEnabled": true, "baseXpReward": 1, "baseBitsReward": 1, "defeatXpPercent": 1, "imageUrl": "example-imageUrl", "chestCode": "example-chestCode", "worldAttemptChestCode": "example-worldAttemptChestCode", "worldTopDamageChestCode": "example-worldTopDamageChestCode", "worldFinalBlowChestCode": "example-worldFinalBlowChestCode"}'

# AdminBossController.chestOptions (GET /admin/bosses/chest-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/chest-options" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.deleteDrop (DELETE /admin/bosses/drops/{dropId})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/drops/${DROP_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossRarityProfileController.list (GET /admin/bosses/rarity-profiles)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/rarity-profiles" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossRarityProfileController.update (PUT /admin/bosses/rarity-profiles/{profileKey})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/bosses/rarity-profiles/${PROFILE_KEY}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"commonPercent": 1, "rarePercent": 1, "epicPercent": 1, "legendaryPercent": 1}'

# AdminBossController.addDrop (POST /admin/bosses/{bossId}/drops)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses/${BOSS_ID}/drops" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"dropType": "example-dropType", "itemCode": "example-itemCode", "templateName": "example-templateName", "equipmentRarity": "example-equipmentRarity", "chance": 1, "minQuantity": 1, "maxQuantity": 1}'

# AdminBossController.delete (DELETE /admin/bosses/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.getById (GET /admin/bosses/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.update (PUT /admin/bosses/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "bossType": "example-bossType", "requiredStage": "example-requiredStage", "requiredLevel": 1, "requiredRebirths": 1, "hp": 1, "atk": 1, "def": 1, "energyCost": 1, "cooldownMinutes": 1, "cooldownEnabled": true, "baseXpReward": 1, "baseBitsReward": 1, "defeatXpPercent": 1, "imageUrl": "example-imageUrl", "active": true, "chestCode": "example-chestCode", "equipmentChance": 1, "worldAttemptChestCode": "example-worldAttemptChestCode", "worldTopDamageChestCode": "example-worldTopDamageChestCode", "worldFinalBlowChestCode": "example-worldFinalBlowChestCode"}'

# AdminChestController.list (GET /admin/chests)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/chests?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminChestController.get (GET /admin/chests/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/chests/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminChestController.update (PUT /admin/chests/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/chests/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "description": "example-description", "icon": "example-icon", "lootTableCode": "example-lootTableCode", "tradable": true, "active": true}'

# AdminChestController.toggleActive (PATCH /admin/chests/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/chests/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminDigimonController.addXp (POST /admin/digimon/add-xp)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/digimon/add-xp?digimonId=${DIGIMON_ID}&amount=${AMOUNT}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminDigimonController.getByPlayer (GET /admin/digimon/by-player/{playerId})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/digimon/by-player/${PLAYER_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminDigimonController.simulateTraitHatch (GET /admin/digimon/simulator/trait-hatch)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/digimon/simulator/trait-hatch?attempts=${ATTEMPTS}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.list (GET /admin/equipment-templates)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.create (POST /admin/equipment-templates)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "setCode": "example-setCode", "tier": 1, "slot": "WEAPON", "rarity": "COMMON", "bonusHp": 1, "bonusAttack": 1, "bonusDefense": 1}'

# AdminEquipmentTemplateController.grant (POST /admin/equipment-templates/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000", "templateName": "example-templateName", "rarity": "COMMON"}'

# AdminEquipmentTemplateController.getByName (GET /admin/equipment-templates/{name})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.update (PUT /admin/equipment-templates/{name})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"slot": "WEAPON", "setCode": "example-setCode", "tier": 1, "rarity": "COMMON", "bonusHp": 1, "bonusAttack": 1, "bonusDefense": 1}'

# AdminEquipmentTemplateController.toggleActive (PATCH /admin/equipment-templates/{name}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/equipment-templates/${NAME}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminInventoryController.grantItem (POST /admin/inventory/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/inventory/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000", "itemCode": "example-itemCode", "quantity": 1}'

# AdminInventoryController.listItemDefinitions (GET /admin/inventory/item-definitions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/inventory/item-definitions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminItemDefinitionController.update (PUT /admin/items/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/items/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "description": "example-description", "category": "example-category", "stackable": true, "buyPrice": 1, "sellPrice": 1, "tradable": true, "sellable": true, "usable": true, "maxStack": 1, "rarity": "example-rarity", "icon": "example-icon"}'

# AdminLootTableController.list (GET /admin/loot-tables)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.create (POST /admin/loot-tables)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/loot-tables" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"code": "example-code", "name": "example-name", "description": "example-description", "minItems": 1, "maxItems": 1, "rarityWeights": [{"rarity": "COMMON", "weight": 1}], "entries": [{"rarity": "COMMON", "itemType": "POTION_SMALL", "materialCode": "example-materialCode", "weight": 1, "minQuantity": 1, "maxQuantity": 1, "active": true}], "active": true}'

# AdminLootTableController.catalog (GET /admin/loot-tables/catalog/items)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables/catalog/items?category=${CATEGORY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.get (GET /admin/loot-tables/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.update (PUT /admin/loot-tables/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/loot-tables/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"code": "example-code", "name": "example-name", "description": "example-description", "minItems": 1, "maxItems": 1, "rarityWeights": [{"rarity": "COMMON", "weight": 1}], "entries": [{"rarity": "COMMON", "itemType": "POTION_SMALL", "materialCode": "example-materialCode", "weight": 1, "minQuantity": 1, "maxQuantity": 1, "active": true}], "active": true}'

# AdminLootTableController.toggleActive (PATCH /admin/loot-tables/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/loot-tables/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMailController.createAnnouncement (POST /admin/mail/announcements)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/mail/announcements" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"subject": "Manutenção programada", "body": "Comunicado de teste. Altere este texto antes de enviar."}'

# AdminEventRewardController.create (POST /admin/mail/event-rewards)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/mail/event-rewards" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"recipientType": "PLAYER", "playerUsername": "jogador-alvo", "clanId": null, "playerUsernames": [], "sourceType": "EVENT", "sourceId": "evento-teste-001", "subject": "Premiação de teste", "body": "Você recebeu esta recompensa pelo evento.", "bitsAmount": 5000, "itemType": null, "itemDefinitionCode": "FRAGMENT_AGUMON", "itemQuantity": 2, "validityDays": 7}'
# Compatibilidade legada: `itemType` pode ser informado quando a premiação não usa uma definição específica do catálogo.

# AdminEventRewardRecipientController.listClans (GET /admin/mail/recipients/clans)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/clans" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEventRewardRecipientController.listClanMembers (GET /admin/mail/recipients/clans/{clanId}/members)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/clans/${CLAN_ID}/members" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEventRewardRecipientController.searchPlayers (GET /admin/mail/recipients/players)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/players?query=${QUERY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEventRewardRecipientController.countEligiblePlayers (GET /admin/mail/recipients/players/count)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/players/count" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.list (GET /admin/missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions?activeOnly=${ACTIVE_ONLY}&area=${AREA}&stage=${STAGE}&chestCode=${CHEST_CODE}&lootItemType=${LOOT_ITEM_TYPE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.create (POST /admin/missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/missions" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"id": "example-id", "name": "example-name", "description": "example-description", "area": "NATIVE_FOREST", "requiredStage": "BABY", "requiredLevel": 1, "baseXp": 1, "baseBits": 1, "energyCost": 1, "durationSeconds": 1, "chestCode": "example-chestCode", "rewards": [{"itemType": "POTION_SMALL", "baseQuantity": 1}], "lootChances": [{"rarity": "COMMON", "chance": 1}], "lootItems": [{"rarity": "COMMON", "itemType": "POTION_SMALL", "quantity": 1}]}'

# AdminMissionController.chestOptions (GET /admin/missions/chest-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions/chest-options" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.getById (GET /admin/missions/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.update (PUT /admin/missions/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "description": "example-description", "area": "NATIVE_FOREST", "requiredStage": "BABY", "requiredLevel": 1, "baseXp": 1, "baseBits": 1, "energyCost": 1, "durationSeconds": 1, "chestCode": "example-chestCode", "rewards": [{"itemType": "POTION_SMALL", "baseQuantity": 1}], "lootChances": [{"rarity": "COMMON", "chance": 1}], "lootItems": [{"rarity": "COMMON", "itemType": "POTION_SMALL", "quantity": 1}]}'

# AdminMissionController.toggleActive (PATCH /admin/missions/{id}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/missions/${ID}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminPlayerController.getPlayers (GET /admin/players)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/players?username=${USERNAME}&email=${EMAIL}&selectedDigitama=${SELECTED_DIGITAMA}&starterSelected=${STARTER_SELECTED}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminPlayerController.wipe (POST /admin/players/wipe)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/wipe" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"confirmation": "WIPE"}'

# AdminPlayerController.resetPassword (POST /admin/players/{id}/reset-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/${ID}/reset-password" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"newPassword": "example-newPassword", "generateRandom": true}'

# AdminServerController.getDamageBuff (GET /admin/server/damage-buff)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminServerController.setDamageBuff (POST /admin/server/damage-buff)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.toggleDamageBuff (POST /admin/server/damage-buff/toggle)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff/toggle" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.list (GET /admin/shop-products)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.create (POST /admin/shop-products)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/shop-products" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"code": "example-code", "name": "example-name", "description": "example-description", "productType": "ITEM", "category": "POTION", "itemType": "POTION_SMALL", "itemDefinitionCode": "example-itemDefinitionCode", "equipmentTemplateName": "example-equipmentTemplateName", "price": 1, "sellPrice": 1}'

# AdminShopProductController.getByCode (GET /admin/shop-products/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.update (PUT /admin/shop-products/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name": "example-name", "description": "example-description", "productType": "ITEM", "category": "POTION", "itemType": "POTION_SMALL", "itemDefinitionCode": "example-itemDefinitionCode", "equipmentTemplateName": "example-equipmentTemplateName", "price": 1, "sellPrice": 1}'

# AdminShopProductController.toggleActive (PATCH /admin/shop-products/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/shop-products/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.completeClanMissions (POST /admin/tools/complete-clan-missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/complete-clan-missions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.forceNewWorldBossCycle (POST /admin/tools/force-new-world-boss-cycle)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/force-new-world-boss-cycle" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetClanRaidDaily (POST /admin/tools/reset-clan-raid-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-clan-raid-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetDailyArenaAttacks (POST /admin/tools/reset-daily-arena-attacks)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-daily-arena-attacks" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetWorldBossDaily (POST /admin/tools/reset-world-boss-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-world-boss-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# ===== ROOT =====

# AreaController.getAreas (GET /areas)
# curl --fail-with-body -i -X GET "${BASE_URL}/areas" -H "Authorization: Bearer ${TOKEN}"

# ===== ARENA =====

# ArenaController.challenge (POST /arena/challenge)
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/challenge" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"opponentDigimonId": "00000000-0000-0000-0000-000000000000"}'

# ArenaController.getHistory (GET /arena/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getLobby (GET /arena/lobby)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/lobby" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getRanking (GET /arena/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/ranking?page=${PAGE}&size=${SIZE}"

# ArenaController.getShop (GET /arena/shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/shop" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.buyFromShop (POST /arena/shop/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/shop/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"productCode": "example-productCode", "quantity": 1}'

# ===== AUCTION =====

# AuctionController.history (GET /auction/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.list (GET /auction/listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/listings?search=${SEARCH}&category=${CATEGORY}&rarity=${RARITY}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.create (POST /auction/listings)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"itemDefinitionId": 1, "quantity": 1, "unitPrice": 1, "durationHours": 1}'

# AuctionController.buy (POST /auction/listings/{listingId}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"quantity": 1}'

# AuctionController.cancel (POST /auction/listings/{listingId}/cancel)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/cancel" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.myListings (GET /auction/my-listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/my-listings?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ===== AUTH =====

# AuthController.login (POST /auth/login)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d '{"email": "jogador@example.com", "password": "troque-esta-senha"}'

# AuthController.register (POST /auth/register)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/register" -H "Content-Type: application/json" -d '{"username": "jogador.teste", "email": "jogador@example.com", "password": "troque-esta-senha"}'

# ===== BOSSES =====

# BossController.getAvailable (GET /bosses/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/available" -H "Authorization: Bearer ${TOKEN}"

# BossController.getCooldowns (GET /bosses/cooldowns)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/cooldowns" -H "Authorization: Bearer ${TOKEN}"

# BossController.getHistory (GET /bosses/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# BossController.challenge (POST /bosses/{bossCode}/challenge)
# curl --fail-with-body -i -X POST "${BASE_URL}/bosses/${BOSS_CODE}/challenge" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000"}'

# ===== ROOT =====

# ClanMissionController.list (GET /clan-missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions" -H "Authorization: Bearer ${TOKEN}"

# ===== CLAN-MISSIONS =====

# ClanMissionController.getMyMission (GET /clan-missions/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions/me" -H "Authorization: Bearer ${TOKEN}"

# ClanMissionController.getRanking (GET /clan-missions/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions/ranking" -H "Authorization: Bearer ${TOKEN}"

# ClanMissionController.accept (POST /clan-missions/{id}/accept)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-missions/${ID}/accept" -H "Authorization: Bearer ${TOKEN}"

# ClanMissionController.claim (POST /clan-missions/{id}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-missions/${ID}/claim" -H "Authorization: Bearer ${TOKEN}"

# ===== CLAN-RAIDS =====

# ClanRaidController.attack (POST /clan-raids/attack)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-raids/attack" -H "Authorization: Bearer ${TOKEN}"

# ClanRaidController.getMyClanRaid (GET /clan-raids/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-raids/me" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# ClanController.list (GET /clans)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans?query=${QUERY}&page=${PAGE}&size=${SIZE}"

# ClanController.create (POST /clans)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"name": "Clã de Teste", "tag": "TEST", "description": "Clã criado para testes."}'

# ===== CLANS =====

# ClanController.getMyClan (GET /clans/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/me" -H "Authorization: Bearer ${TOKEN}"

# ClanController.getRanking (GET /clans/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/ranking?page=${PAGE}&size=${SIZE}"

# ClanController.dissolve (DELETE /clans/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}"

# ClanController.getById (GET /clans/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}"

# ClanController.update (PATCH /clans/{id})
# curl --fail-with-body -i -X PATCH "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"description": "example-description", "emblem": "example-emblem"}'

# ClanController.invite (POST /clans/{id}/invite)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/invite" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"username": "jogador-alvo"}'

# ClanController.join (POST /clans/{id}/join)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/join" -H "Authorization: Bearer ${TOKEN}"

# ClanController.leave (POST /clans/{id}/leave)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/leave" -H "Authorization: Bearer ${TOKEN}"

# ClanController.kick (POST /clans/{id}/members/{username}/kick)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/kick" -H "Authorization: Bearer ${TOKEN}"

# ClanController.changeRole (POST /clans/{id}/members/{username}/role)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/role" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"role": "LEADER"}'

# ClanController.transferLeadership (POST /clans/{id}/members/{username}/transfer)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/transfer" -H "Authorization: Bearer ${TOKEN}"

# ClanController.listUpgrades (GET /clans/{id}/upgrades)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}/upgrades" -H "Authorization: Bearer ${TOKEN}"

# ClanController.buyUpgrade (POST /clans/{id}/upgrades/{code}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/upgrades/${CODE}/buy" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# DigimonInfoController.getDigimonInfos (GET /digimon-infos)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon-infos?name=${NAME}&stage=${STAGE}&attribute=${ATTRIBUTE}&element=${ELEMENT}&specie=${SPECIE}&page=${PAGE}&size=${SIZE}"

# ===== DIGIMON =====

# DigimonController.evolve (POST /digimon/evolve)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/evolve" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"evolutionLineId": 1}'

# DigimonController.levelTable (GET /digimon/level-table)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/level-table"

# DigimonController.me (GET /digimon/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/me" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.rebirth (POST /digimon/rebirth)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/rebirth" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000"}'

# DigimonController.rename (PUT /digimon/rename)
# curl --fail-with-body -i -X PUT "${BASE_URL}/digimon/rename" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000", "newName": "example-newName"}'

# DigimonController.select (POST /digimon/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"digimonId": "00000000-0000-0000-0000-000000000000"}'

# DigimonController.storage (GET /digimon/storage)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/storage" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.getById (GET /digimon/{digimonId})
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.evolutionOptions (GET /digimon/{digimonId}/evolution-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/evolution-options" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.lineage (GET /digimon/{digimonId}/lineage)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/lineage" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.rebirthPreview (GET /digimon/{digimonId}/rebirth-preview)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/rebirth-preview" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.retrieve (POST /digimon/{digimonId}/retrieve)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/retrieve" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.store (POST /digimon/{digimonId}/store)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/store" -H "Authorization: Bearer ${TOKEN}"

# ===== DIGITAMA-POOLS =====

# DigitamaPoolController.getAvailablePools (GET /digitama-pools/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama-pools/available"

# ===== DIGITAMA =====

# DigitamaController.hatch (POST /digitama/hatch)
# curl --fail-with-body -i -X POST "${BASE_URL}/digitama/hatch" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.history (GET /digitama/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama/history" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.select (POST /digitama/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digitama/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"type": "STARTER"}'

# ===== EQUIPMENT =====

# EquipmentController.getDigimonEquipment (GET /equipment/digimon/{digimonId})
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.getDigimonInventory (GET /equipment/digimon/{digimonId}/inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}/inventory" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.equip (POST /equipment/equip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/equip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"equipmentId": "00000000-0000-0000-0000-000000000000"}'

# EquipmentController.refine (POST /equipment/refine)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/refine" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"equipmentId": "00000000-0000-0000-0000-000000000000"}'

# EquipmentController.unequip (POST /equipment/unequip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"equipmentId": "00000000-0000-0000-0000-000000000000"}'

# EquipmentController.unequipAll (POST /equipment/unequip-all)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip-all" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.refinePreview (GET /equipment/{equipmentId}/refine-preview)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/${EQUIPMENT_ID}/refine-preview" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# EvolutionLineController.getEvolutionLines (GET /evolution-lines)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines?code=${CODE}&name=${NAME}&active=${ACTIVE}&page=${PAGE}&size=${SIZE}"

# ===== EVOLUTION-LINES =====

# EvolutionLineController.getAvailableEvolutionLines (GET /evolution-lines/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines/available"

# ===== INCUBATION =====

# IncubationController.claim (POST /incubation/{incubationId}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/${ID}/claim" -H "Authorization: Bearer ${TOKEN}"

# IncubationController.me (GET /incubation/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/incubation/me" -H "Authorization: Bearer ${TOKEN}"

# IncubationController.start (POST /incubation/start)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"slotNumber": 1, "digitamaType": "DIGITAMA_FIRE", "incubatorType": "INCUBATOR_COMMON"}'

# ===== ROOT =====

# InventoryController.getInventory (GET /inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/inventory" -H "Authorization: Bearer ${TOKEN}"

# ===== INVENTORY =====

# InventoryController.openChest (POST /inventory/chests/open)
# curl --fail-with-body -i -X POST "${BASE_URL}/inventory/chests/open" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"chestCode": "example-chestCode", "requestId": "example-requestId"}'

# InventoryController.useItem (POST /inventory/use)
# curl --fail-with-body -i -X POST "${BASE_URL}/inventory/use" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"itemType": "POTION_SMALL"}'

# ===== ROOT =====

# ItemDefinitionController.getItems (GET /items)
# curl --fail-with-body -i -X GET "${BASE_URL}/items?search=${SEARCH}&category=${CATEGORY}&rarity=${RARITY}&usable=${USABLE}&sellable=${SELLABLE}&tradable=${TRADABLE}&page=${PAGE}&size=${SIZE}"

# MailController.send (POST /mail)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"recipientUsername": "jogador-alvo", "subject": "Mensagem de teste", "body": "Conteúdo da mensagem de teste."}'

# ===== MAIL =====

# MailController.inbox (GET /mail/inbox)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/inbox?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# MailController.sent (GET /mail/sent)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/sent?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# MailController.unreadCount (GET /mail/unread-count)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/unread-count" -H "Authorization: Bearer ${TOKEN}"

# MailController.delete (DELETE /mail/{messageId})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/mail/${MESSAGE_ID}" -H "Authorization: Bearer ${TOKEN}"

# MailController.get (GET /mail/{messageId})
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/${MESSAGE_ID}" -H "Authorization: Bearer ${TOKEN}"

# MailController.action (POST /mail/{messageId}/action)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail/${MESSAGE_ID}/action" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"action": "ACCEPT"}'

# MailController.markRead (POST /mail/{messageId}/read)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail/${MESSAGE_ID}/read" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# MissionController.list (GET /missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/missions" -H "Authorization: Bearer ${TOKEN}"

# ===== MISSIONS =====

# MissionController.getActiveMissions (GET /missions/active)
# curl --fail-with-body -i -X GET "${BASE_URL}/missions/active" -H "Authorization: Bearer ${TOKEN}"

# MissionController.start (POST /missions/start)
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"missionId": "example-missionId"}'

# MissionController.claimMission (POST /missions/{missionInstanceId}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/${MISSION_INSTANCE_ID}/claim" -H "Authorization: Bearer ${TOKEN}"

# ===== PLAYERS =====

# PlayerController.me (GET /players/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.changePassword (POST /players/me/change-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/players/me/change-password" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"currentPassword": "example-currentPassword", "newPassword": "example-newPassword"}'

# PlayerController.dashboard (GET /players/me/dashboard)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/dashboard" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.logoutAll (POST /players/me/logout-all)
# curl --fail-with-body -i -X POST "${BASE_URL}/players/me/logout-all" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.startup (GET /players/me/startup)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/startup" -H "Authorization: Bearer ${TOKEN}"

# ===== RANKING =====

# RankingController.byGrade (GET /ranking/grade)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/grade?page=${PAGE}&size=${SIZE}"

# RankingController.byLevel (GET /ranking/level)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/level?page=${PAGE}&size=${SIZE}"

# RankingController.byRebirth (GET /ranking/rebirth)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/rebirth?page=${PAGE}&size=${SIZE}"

# ===== ROOT =====

# ShopController.getProducts (GET /shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/shop"

# ===== SHOP =====

# ShopController.buy (POST /shop/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/shop/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"productCode": "example-productCode", "quantity": 1}'

# ShopController.sell (POST /shop/sell)
# curl --fail-with-body -i -X POST "${BASE_URL}/shop/sell" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"productCode": "example-productCode", "equipmentId": "00000000-0000-0000-0000-000000000000", "itemType": "example-itemType", "quantity": 1}'

# ===== ROOT =====

# TutorialController.getProgress (GET /tutorial)
# curl --fail-with-body -i -X GET "${BASE_URL}/tutorial" -H "Authorization: Bearer ${TOKEN}"

# ===== TUTORIAL =====

# TutorialController.finishTutorial (POST /tutorial/finish)
# curl --fail-with-body -i -X POST "${BASE_URL}/tutorial/finish" -H "Authorization: Bearer ${TOKEN}"

# TutorialController.claimReward (POST /tutorial/steps/{step}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/tutorial/steps/${STEP}/claim" -H "Authorization: Bearer ${TOKEN}"

# ===== WORLD-BOSS =====

# WorldBossController.attack (POST /world-boss/attack)
# curl --fail-with-body -i -X POST "${BASE_URL}/world-boss/attack" -H "Authorization: Bearer ${TOKEN}"

# WorldBossController.getMyWorldBoss (GET /world-boss/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/world-boss/me" -H "Authorization: Bearer ${TOKEN}"
