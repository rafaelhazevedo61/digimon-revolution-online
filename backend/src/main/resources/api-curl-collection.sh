#!/usr/bin/env bash
# Collection oficial de exemplos curl do Digimon Revolution Online.
# Gerada a partir dos controllers Java; execute scripts/generate_api_curl_collection.py após alterar endpoints.
# Os payloads '{}' são exemplos: revise-os antes de executar.
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
OPPONENT_DIGIMON_ID="${OPPONENT_DIGIMON_ID:-00000000-0000-0000-0000-000000000000}"
EQUIPMENT_ID="${EQUIPMENT_ID:-00000000-0000-0000-0000-000000000000}"
PLAYER_ID="${PLAYER_ID:-00000000-0000-0000-0000-000000000000}"
ACTIVE="${ACTIVE:-VALOR}"
ACTIVE_ONLY="${ACTIVE_ONLY:-VALOR}"
AMOUNT="${AMOUNT:-VALOR}"
AREA="${AREA:-VALOR}"
ATTEMPTS="${ATTEMPTS:-VALOR}"
ATTRIBUTE="${ATTRIBUTE:-VALOR}"
BOSS_CODE="${BOSS_CODE:-VALOR}"
BOSS_ID="${BOSS_ID:-1}"
PROFILE_KEY="${PROFILE_KEY:-BOSS_NORMAL}"
CATEGORY="${CATEGORY:-VALOR}"
CHEST_CODE="${CHEST_CODE:-VALOR}"
DROP_ID="${DROP_ID:-00000000-0000-0000-0000-000000000000}"
ELEMENT="${ELEMENT:-VALOR}"
EMAIL="${EMAIL:-VALOR}"
LOOT_ITEM_TYPE="${LOOT_ITEM_TYPE:-VALOR}"
MISSION_INSTANCE_ID="${MISSION_INSTANCE_ID:-00000000-0000-0000-0000-000000000000}"
NAME="${NAME:-VALOR}"
PAGE="${PAGE:-VALOR}"
QUERY="${QUERY:-VALOR}"
RARITY="${RARITY:-VALOR}"
SEARCH="${SEARCH:-VALOR}"
SELECTED_DIGITAMA="${SELECTED_DIGITAMA:-VALOR}"
SELLABLE="${SELLABLE:-VALOR}"
SIZE="${SIZE:-VALOR}"
SPECIE="${SPECIE:-VALOR}"
STAGE="${STAGE:-VALOR}"
STARTER_SELECTED="${STARTER_SELECTED:-VALOR}"
TRADABLE="${TRADABLE:-VALOR}"
USABLE="${USABLE:-VALOR}"

# Autenticação: substitua TOKEN/ADMIN_TOKEN antes de executar comandos protegidos.
# Endpoints públicos não precisam do header Authorization.

# ===== ADMIN =====

# AdminBossController.chestOptions (GET /admin/bosses/chest-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/chest-options" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossRarityProfileController.list (GET /admin/bosses/rarity-profiles)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/rarity-profiles" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossRarityProfileController.update (PUT /admin/bosses/rarity-profiles/{profileKey})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/bosses/rarity-profiles/${PROFILE_KEY}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"commonPercent":65,"rarePercent":22,"epicPercent":10,"legendaryPercent":3}'

# AdminBossController.listAll (GET /admin/bosses)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.create (POST /admin/bosses)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"code":"BOSS_NEW","name":"Novo Boss","bossType":"DAILY","requiredStage":"ROOKIE","requiredLevel":10,"requiredRebirths":0,"hp":1000,"atk":100,"def":100,"energyCost":5,"cooldownMinutes":1440,"baseXpReward":500,"baseBitsReward":200,"defeatXpPercent":10,"imageUrl":null,"chestCode":"CHEST_BOSS_DAILY_NEW"}'

# AdminBossController.deleteDrop (DELETE /admin/bosses/drops/{dropId})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/drops/${DROP_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.addDrop (POST /admin/bosses/{bossId}/drops)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses/${BOSS_ID}/drops" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminBossController.delete (DELETE /admin/bosses/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminBossController.getById (GET /admin/bosses/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminBossController.update (PUT /admin/bosses/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"name":"Boss Atualizado","chestCode":"CHEST_BOSS_DAILY_NEW","equipmentChance":42}'

# AdminChestController.list (GET /admin/chests)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/chests?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminChestController.get (GET /admin/chests/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/chests/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminChestController.update (PUT /admin/chests/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/chests/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminChestController.toggleActive (PATCH /admin/chests/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/chests/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminDigimonController.addXp (POST /admin/digimon/add-xp)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/digimon/add-xp?digimonId=${DIGIMON_ID}&amount=${AMOUNT}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminDigimonController.getByPlayer (GET /admin/digimon/by-player/{playerId})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/digimon/by-player/${PLAYER_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.list (GET /admin/equipment-templates)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.create (POST /admin/equipment-templates)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.grant (POST /admin/equipment-templates/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.getByName (GET /admin/equipment-templates/{name})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.update (PUT /admin/equipment-templates/{name})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.toggleActive (PATCH /admin/equipment-templates/{name}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/equipment-templates/${NAME}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminInventoryController.grantItem (POST /admin/inventory/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/inventory/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminInventoryController.listItemDefinitions (GET /admin/inventory/item-definitions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/inventory/item-definitions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.list (GET /admin/loot-tables)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables?activeOnly=${ACTIVE_ONLY}&category=${CATEGORY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.create (POST /admin/loot-tables)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/loot-tables?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminLootTableController.catalog (GET /admin/loot-tables/catalog/items)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables/catalog/items?category=${CATEGORY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminLootTableController.get (GET /admin/loot-tables/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/loot-tables/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminLootTableController.update (PUT /admin/loot-tables/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/loot-tables/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminLootTableController.toggleActive (PATCH /admin/loot-tables/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/loot-tables/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMailController.createAnnouncement (POST /admin/mail/announcements)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/mail/announcements" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"subject": "Manutenção programada", "body": "Comunicado de teste. Altere este texto antes de enviar."}'

# AdminEventRewardController.create (POST /admin/mail/event-rewards)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/mail/event-rewards" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"playerUsername": "jogador-alvo", "sourceType": "EVENT", "sourceId": "evento-teste-001", "subject": "Premiação de teste", "body": "Você recebeu esta recompensa pelo evento.", "bitsAmount": 5000, "itemType": "TRAINING_STONE", "itemQuantity": 2, "validityDays": 7}'

# AdminEventRewardRecipientController.listClans (GET /admin/mail/recipients/clans)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/clans" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEventRewardRecipientController.listClanMembers (GET /admin/mail/recipients/clans/{clanId}/members)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/clans/${CLAN_ID}/members" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEventRewardRecipientController.searchPlayers (GET /admin/mail/recipients/players)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/mail/recipients/players?query=${QUERY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.list (GET /admin/missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions?activeOnly=${ACTIVE_ONLY}&area=${AREA}&stage=${STAGE}&chestCode=${CHEST_CODE}&lootItemType=${LOOT_ITEM_TYPE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.create (POST /admin/missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/missions?activeOnly=${ACTIVE_ONLY}&area=${AREA}&stage=${STAGE}&chestCode=${CHEST_CODE}&lootItemType=${LOOT_ITEM_TYPE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.chestOptions (GET /admin/missions/chest-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions/chest-options" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.getById (GET /admin/missions/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.update (PUT /admin/missions/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.toggleActive (PATCH /admin/missions/{id}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/missions/${ID}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminPlayerController.getPlayers (GET /admin/players)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/players?username=${USERNAME}&email=${EMAIL}&selectedDigitama=${SELECTED_DIGITAMA}&starterSelected=${STARTER_SELECTED}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminPlayerController.wipe (POST /admin/players/wipe)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/wipe" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminPlayerController.resetPassword (POST /admin/players/{id}/reset-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/${ID}/reset-password" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.getDamageBuff (GET /admin/server/damage-buff)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.setDamageBuff (POST /admin/server/damage-buff)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.toggleDamageBuff (POST /admin/server/damage-buff/toggle)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff/toggle" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.list (GET /admin/shop-products)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.create (POST /admin/shop-products)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/shop-products?activeOnly=${ACTIVE_ONLY}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.getByCode (GET /admin/shop-products/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.update (PUT /admin/shop-products/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.toggleActive (PATCH /admin/shop-products/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/shop-products/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.completeClanMissions (POST /admin/tools/complete-clan-missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/complete-clan-missions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetClanRaidDaily (POST /admin/tools/reset-clan-raid-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-clan-raid-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetDailyArenaAttacks (POST /admin/tools/reset-daily-arena-attacks)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-daily-arena-attacks" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.resetWorldBossDaily (POST /admin/tools/reset-world-boss-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-world-boss-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminToolsController.forceNewWorldBossCycle (POST /admin/tools/force-new-world-boss-cycle)
# Disponível somente após a derrota do ciclo atual; preserva o histórico anterior.
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/force-new-world-boss-cycle" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# ===== ROOT =====

# AreaController.getAreas (GET /areas)
# curl --fail-with-body -i -X GET "${BASE_URL}/areas" -H "Authorization: Bearer ${TOKEN}"

# ===== ARENA =====

# ArenaController.challenge (POST /arena/challenge)
# Em caso de vitória, o retorno inclui rewardChestCode e rewardChestName; a pool não é exposta.
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/challenge" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"opponentDigimonId":"'"${OPPONENT_DIGIMON_ID}"'"}'

# ArenaController.getHistory (GET /arena/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getLobby (GET /arena/lobby)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/lobby" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ArenaController.getRanking (GET /arena/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/ranking?page=${PAGE}&size=${SIZE}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getShop (GET /arena/shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/shop" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ArenaController.buyFromShop (POST /arena/shop/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/shop/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== AUCTION =====

# AuctionController.history (GET /auction/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.list (GET /auction/listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/listings?search=${SEARCH}&category=${CATEGORY}&rarity=${RARITY}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.create (POST /auction/listings)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.buy (POST /auction/listings/{listingId}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.cancel (POST /auction/listings/{listingId}/cancel)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/cancel" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.myListings (GET /auction/my-listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/my-listings?page=${PAGE}&size=${SIZE}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ===== AUTH =====

# AuthController.login (POST /auth/login)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d '{"email": "jogador@example.com", "password": "troque-esta-senha"}'

# AuthController.register (POST /auth/register)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/register" -H "Content-Type: application/json" -d '{"username": "jogador.teste", "email": "jogador@example.com", "password": "troque-esta-senha"}'

# ===== BOSSES =====

# BossController.getAvailable (GET /bosses/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/available" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# BossController.getCooldowns (GET /bosses/cooldowns)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/cooldowns" -H "Authorization: Bearer ${TOKEN}"

# BossController.getHistory (GET /bosses/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/history?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# BossController.challenge (POST /bosses/{bossCode}/challenge)
# curl --fail-with-body -i -X POST "${BASE_URL}/bosses/${BOSS_CODE}/challenge?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

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
# curl --fail-with-body -i -X GET "${BASE_URL}/clans?query=${QUERY}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ClanController.create (POST /clans)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans?query=${QUERY}&page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"name": "Clã de Teste", "tag": "TEST", "description": "Clã criado para testes."}'

# ===== CLANS =====

# ClanController.getMyClan (GET /clans/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/me" -H "Authorization: Bearer ${TOKEN}"

# ClanController.getRanking (GET /clans/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/ranking?page=${PAGE}&size=${SIZE}"

# ClanController.dissolve (DELETE /clans/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.getById (GET /clans/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.update (PATCH /clans/{id})
# curl --fail-with-body -i -X PATCH "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.invite (POST /clans/{id}/invite)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/invite" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{"username": "jogador-alvo"}'

# ClanController.join (POST /clans/{id}/join)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/join" -H "Authorization: Bearer ${TOKEN}"

# ClanController.leave (POST /clans/{id}/leave)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/leave" -H "Authorization: Bearer ${TOKEN}"

# ClanController.kick (POST /clans/{id}/members/{username}/kick)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/kick" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.changeRole (POST /clans/{id}/members/{username}/role)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/role" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.transferLeadership (POST /clans/{id}/members/{username}/transfer)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/transfer" -H "Authorization: Bearer ${TOKEN}"

# ClanController.listUpgrades (GET /clans/{id}/upgrades)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}/upgrades" -H "Authorization: Bearer ${TOKEN}"

# ClanController.buyUpgrade (POST /clans/{id}/upgrades/{code}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/upgrades/${CODE}/buy?page=${PAGE}&size=${SIZE}" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# DigimonInfoController.getDigimonInfos (GET /digimon-infos)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon-infos?name=${NAME}&stage=${STAGE}&attribute=${ATTRIBUTE}&element=${ELEMENT}&specie=${SPECIE}&page=${PAGE}&size=${SIZE}"

# ===== DIGIMON =====

# DigimonController.evolve (POST /digimon/evolve)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/evolve" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.levelTable (GET /digimon/level-table)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/level-table" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.me (GET /digimon/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/me" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.rebirth (POST /digimon/rebirth)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/rebirth" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.rename (PUT /digimon/rename)
# curl --fail-with-body -i -X PUT "${BASE_URL}/digimon/rename" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.select (POST /digimon/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.simulateTraitHatch (GET /digimon/simulator/trait-hatch)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/simulator/trait-hatch?attempts=${ATTEMPTS}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.storage (GET /digimon/storage)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/storage" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.getById (GET /digimon/{digimonId})
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.evolutionOptions (GET /digimon/{digimonId}/evolution-options)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/evolution-options" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.lineage (GET /digimon/{digimonId}/lineage)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/lineage" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.rebirthPreview (GET /digimon/{digimonId}/rebirth-preview)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/${DIGIMON_ID}/rebirth-preview?attempts=${ATTEMPTS}" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.retrieve (POST /digimon/{digimonId}/retrieve)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/retrieve" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.store (POST /digimon/{digimonId}/store)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/store" -H "Authorization: Bearer ${TOKEN}"

# ===== DIGITAMA-POOLS =====

# DigitamaPoolController.getAvailablePools (GET /digitama-pools/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama-pools/available"

# ===== DIGITAMA =====

# DigitamaController.hatch (GET /digitama/hatch)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama/hatch" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.history (GET /digitama/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama/history" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.select (POST /digitama/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digitama/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== EQUIPMENT =====

# EquipmentController.getDigimonEquipment (GET /equipment/digimon/{digimonId})
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.getDigimonInventory (GET /equipment/digimon/{digimonId}/inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}/inventory" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.equip (POST /equipment/equip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/equip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.refine (POST /equipment/refine)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/refine" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.unequip (POST /equipment/unequip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.unequipAll (POST /equipment/unequip-all)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip-all" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.refinePreview (GET /equipment/{equipmentId}/refine-preview)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/${EQUIPMENT_ID}/refine-preview" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# EvolutionLineController.getEvolutionLines (GET /evolution-lines)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines?code=${CODE}&name=${NAME}&active=${ACTIVE}&page=${PAGE}&size=${SIZE}"

# ===== EVOLUTION-LINES =====

# EvolutionLineController.getAvailableEvolutionLines (GET /evolution-lines/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines/available?code=${CODE}&name=${NAME}&active=${ACTIVE}&page=${PAGE}&size=${SIZE}"

# ===== INCUBATION =====

# IncubationController.claim (POST /incubation/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/claim" -H "Authorization: Bearer ${TOKEN}"

# IncubationController.me (GET /incubation/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/incubation/me" -H "Authorization: Bearer ${TOKEN}"

# IncubationController.start (POST /incubation/start)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# InventoryController.getInventory (GET /inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/inventory" -H "Authorization: Bearer ${TOKEN}"

# ===== INVENTORY =====

# InventoryController.openChest (POST /inventory/chests/open)
# curl --fail-with-body -i -X POST "${BASE_URL}/inventory/chests/open" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# InventoryController.useItem (POST /inventory/use)
# curl --fail-with-body -i -X POST "${BASE_URL}/inventory/use" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# ItemDefinitionController.getItems (GET /items)
# curl --fail-with-body -i -X GET "${BASE_URL}/items?category=${CATEGORY}&rarity=${RARITY}&usable=${USABLE}&sellable=${SELLABLE}&tradable=${TRADABLE}&page=${PAGE}&size=${SIZE}"

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
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# MissionController.claimMission (POST /missions/{missionInstanceId}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/${MISSION_INSTANCE_ID}/claim" -H "Authorization: Bearer ${TOKEN}"

# ===== PLAYERS =====

# PlayerController.me (GET /players/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.changePassword (POST /players/me/change-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/players/me/change-password" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# PlayerController.dashboard (GET /players/me/dashboard)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/dashboard" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.startup (GET /players/me/startup)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/startup" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== RANKING =====

# RankingController.byGrade (GET /ranking/grade)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/grade?page=${PAGE}&size=${SIZE}&page=${PAGE}&size=${SIZE}"

# RankingController.byLevel (GET /ranking/level)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/level?page=${PAGE}&size=${SIZE}&page=${PAGE}&size=${SIZE}"

# RankingController.byRebirth (GET /ranking/rebirth)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/rebirth?page=${PAGE}&size=${SIZE}"

# ===== ROOT =====

# ShopController.getProducts (GET /shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/shop" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== SHOP =====

# ShopController.buy (POST /shop/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/shop/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ShopController.sell (POST /shop/sell)
# curl --fail-with-body -i -X POST "${BASE_URL}/shop/sell" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# TutorialController.getProgress (GET /tutorial)
# curl --fail-with-body -i -X GET "${BASE_URL}/tutorial" -H "Authorization: Bearer ${TOKEN}"

# ===== WORLD-BOSS =====

# WorldBossController.attack (POST /world-boss/attack)
# curl --fail-with-body -i -X POST "${BASE_URL}/world-boss/attack" -H "Authorization: Bearer ${TOKEN}"

# WorldBossController.getMyWorldBoss (GET /world-boss/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/world-boss/me" -H "Authorization: Bearer ${TOKEN}"
