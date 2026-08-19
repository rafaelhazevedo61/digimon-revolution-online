#!/usr/bin/env bash
# Collection oficial de exemplos curl do Digimon Revolution Online.
# Gerada a partir dos controllers Java; execute scripts/generate_api_curl_collection.py após alterar endpoints.
# Os payloads '{}' são placeholders nos endpoints que exigem JSON.
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
BOSS_CODE="${BOSS_CODE:-VALOR}"
BOSS_ID="${BOSS_ID:-00000000-0000-0000-0000-000000000000}"
DROP_ID="${DROP_ID:-00000000-0000-0000-0000-000000000000}"
MISSION_INSTANCE_ID="${MISSION_INSTANCE_ID:-00000000-0000-0000-0000-000000000000}"
NAME="${NAME:-VALOR}"

# Autenticação: substitua TOKEN/ADMIN_TOKEN antes de executar comandos protegidos.
# Endpoints públicos podem ser executados sem o header Authorization.

# ===== ADMIN =====

# AdminBossController.listAll (GET /admin/bosses)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.create (POST /admin/bosses)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminBossController.endpoint (DELETE /admin/bosses/drops/{dropId})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/drops/${DROP_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.addDrop (POST /admin/bosses/{bossId}/drops)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/bosses/${BOSS_ID}/drops" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminBossController.addDrop (DELETE /admin/bosses/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.getById (GET /admin/bosses/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminBossController.update (PUT /admin/bosses/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/bosses/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminDigimonController.addXp (POST /admin/digimon/add-xp)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/digimon/add-xp" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminDigimonController.getByPlayer (GET /admin/digimon/by-player/{playerId})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/digimon/by-player/${PLAYER_ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.list (GET /admin/equipment-templates)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.create (POST /admin/equipment-templates)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.grant (POST /admin/equipment-templates/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/equipment-templates/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.getByName (GET /admin/equipment-templates/{name})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminEquipmentTemplateController.update (PUT /admin/equipment-templates/{name})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/equipment-templates/${NAME}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminEquipmentTemplateController.toggleActive (PATCH /admin/equipment-templates/{name}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/equipment-templates/${NAME}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminInventoryController.grantItem (POST /admin/inventory/grant)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/inventory/grant" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminInventoryController.endpoint (GET /admin/inventory/item-definitions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/inventory/item-definitions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMailController.endpoint (POST /admin/mail/announcements)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/mail/announcements" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.list (GET /admin/missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.create (POST /admin/missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/missions" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.getById (GET /admin/missions/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminMissionController.update (PUT /admin/missions/{id})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/missions/${ID}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminMissionController.toggleActive (PATCH /admin/missions/{id}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/missions/${ID}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminPlayerController.getPlayers (GET /admin/players)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/players" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminPlayerController.wipe (POST /admin/players/wipe)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/wipe" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminPlayerController.resetPassword (POST /admin/players/{id}/reset-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/players/${ID}/reset-password" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.getDamageBuff (GET /admin/server/damage-buff)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminServerController.setDamageBuff (POST /admin/server/damage-buff)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminServerController.toggleDamageBuff (POST /admin/server/damage-buff/toggle)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/server/damage-buff/toggle" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.list (GET /admin/shop-products)
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.create (POST /admin/shop-products)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/shop-products" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.getByCode (GET /admin/shop-products/{code})
# curl --fail-with-body -i -X GET "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}"

# AdminShopProductController.update (PUT /admin/shop-products/{code})
# curl --fail-with-body -i -X PUT "${BASE_URL}/admin/shop-products/${CODE}" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminShopProductController.toggleActive (PATCH /admin/shop-products/{code}/toggle-active)
# curl --fail-with-body -i -X PATCH "${BASE_URL}/admin/shop-products/${CODE}/toggle-active" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminToolsController.endpoint (POST /admin/tools/complete-clan-missions)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/complete-clan-missions" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminToolsController.endpoint (POST /admin/tools/reset-clan-raid-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-clan-raid-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminToolsController.endpoint (POST /admin/tools/reset-daily-arena-attacks)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-daily-arena-attacks" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# AdminToolsController.endpoint (POST /admin/tools/reset-world-boss-daily)
# curl --fail-with-body -i -X POST "${BASE_URL}/admin/tools/reset-world-boss-daily" -H "Authorization: Bearer ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# AreaController.getAreas (GET /areas)
# curl --fail-with-body -i -X GET "${BASE_URL}/areas" -H "Authorization: Bearer ${TOKEN}"

# ===== ARENA =====

# ArenaController.challenge (POST /arena/challenge)
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/challenge" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ArenaController.getHistory (GET /arena/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/history" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getLobby (GET /arena/lobby)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/lobby" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getRanking (GET /arena/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/ranking" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.getShop (GET /arena/shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/arena/shop" -H "Authorization: Bearer ${TOKEN}"

# ArenaController.buyFromShop (POST /arena/shop/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/arena/shop/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== AUCTION =====

# AuctionController.history (GET /auction/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/history" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.list (GET /auction/listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/listings" -H "Authorization: Bearer ${TOKEN}"

# AuctionController.create (POST /auction/listings)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.buy (POST /auction/listings/{listingId}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.cancel (POST /auction/listings/{listingId}/cancel)
# curl --fail-with-body -i -X POST "${BASE_URL}/auction/listings/${LISTING_ID}/cancel" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# AuctionController.myListings (GET /auction/my-listings)
# curl --fail-with-body -i -X GET "${BASE_URL}/auction/my-listings" -H "Authorization: Bearer ${TOKEN}"

# ===== AUTH =====

# AuthController.login (POST /auth/login)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/login" -H "Content-Type: application/json" -d '{}'

# AuthController.register (POST /auth/register)
# curl --fail-with-body -i -X POST "${BASE_URL}/auth/register" -H "Content-Type: application/json" -d '{}'

# ===== BOSSES =====

# BossController.getAvailable (GET /bosses/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/available" -H "Authorization: Bearer ${TOKEN}"

# BossController.getCooldowns (GET /bosses/cooldowns)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/cooldowns" -H "Authorization: Bearer ${TOKEN}"

# BossController.getHistory (GET /bosses/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/bosses/history" -H "Authorization: Bearer ${TOKEN}"

# BossController.challenge (POST /bosses/{bossCode}/challenge)
# curl --fail-with-body -i -X POST "${BASE_URL}/bosses/${BOSS_CODE}/challenge" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# ClanMissionController.list (GET /clan-missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions" -H "Authorization: Bearer ${TOKEN}"

# ===== CLAN-MISSIONS =====

# ClanMissionController.getMyMission (GET /clan-missions/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions/me" -H "Authorization: Bearer ${TOKEN}"

# ClanMissionController.getRanking (GET /clan-missions/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-missions/ranking" -H "Authorization: Bearer ${TOKEN}"

# ClanMissionController.accept (POST /clan-missions/{id}/accept)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-missions/${ID}/accept" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanMissionController.claim (POST /clan-missions/{id}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-missions/${ID}/claim" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== CLAN-RAIDS =====

# ClanRaidController.attack (POST /clan-raids/attack)
# curl --fail-with-body -i -X POST "${BASE_URL}/clan-raids/attack" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanRaidController.getMyClanRaid (GET /clan-raids/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clan-raids/me" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# ClanController.list (GET /clans)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans" -H "Authorization: Bearer ${TOKEN}"

# ClanController.create (POST /clans)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== CLANS =====

# ClanController.getMyClan (GET /clans/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/me" -H "Authorization: Bearer ${TOKEN}"

# ClanController.getRanking (GET /clans/ranking)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/ranking" -H "Authorization: Bearer ${TOKEN}"

# ClanController.dissolve (DELETE /clans/{id})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}"

# ClanController.getById (GET /clans/{id})
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}"

# ClanController.update (PATCH /clans/{id})
# curl --fail-with-body -i -X PATCH "${BASE_URL}/clans/${ID}" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.invite (POST /clans/{id}/invite)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/invite" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.join (POST /clans/{id}/join)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/join" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.leave (POST /clans/{id}/leave)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/leave" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.kick (POST /clans/{id}/members/{username}/kick)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/kick" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.changeRole (POST /clans/{id}/members/{username}/role)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/role" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.transferLeadership (POST /clans/{id}/members/{username}/transfer)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/members/${USERNAME}/transfer" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ClanController.listUpgrades (GET /clans/{id}/upgrades)
# curl --fail-with-body -i -X GET "${BASE_URL}/clans/${ID}/upgrades" -H "Authorization: Bearer ${TOKEN}"

# ClanController.buyUpgrade (POST /clans/{id}/upgrades/{code}/buy)
# curl --fail-with-body -i -X POST "${BASE_URL}/clans/${ID}/upgrades/${CODE}/buy" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# DigimonInfoController.getDigimonInfos (GET /digimon-infos)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon-infos" -H "Authorization: Bearer ${TOKEN}"

# ===== DIGIMON =====

# DigimonController.evolutionOptions (POST /digimon/evolve)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/evolve" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.levelTable (GET /digimon/level-table)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/level-table" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.me (GET /digimon/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/me" -H "Authorization: Bearer ${TOKEN}"

# DigimonController.getById (POST /digimon/rebirth)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/rebirth" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.store (PUT /digimon/rename)
# curl --fail-with-body -i -X PUT "${BASE_URL}/digimon/rename" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.select (POST /digimon/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.simulateTraitHatch (GET /digimon/simulator/trait-hatch)
# curl --fail-with-body -i -X GET "${BASE_URL}/digimon/simulator/trait-hatch" -H "Authorization: Bearer ${TOKEN}"

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
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/retrieve" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# DigimonController.store (POST /digimon/{digimonId}/store)
# curl --fail-with-body -i -X POST "${BASE_URL}/digimon/${DIGIMON_ID}/store" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== DIGITAMA-POOLS =====

# DigitamaPoolController.getAvailablePools (GET /digitama-pools/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama-pools/available" -H "Authorization: Bearer ${TOKEN}"

# ===== DIGITAMA =====

# DigitamaController.hatch (GET /digitama/hatch)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama/hatch" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.history (GET /digitama/history)
# curl --fail-with-body -i -X GET "${BASE_URL}/digitama/history" -H "Authorization: Bearer ${TOKEN}"

# DigitamaController.select (POST /digitama/select)
# curl --fail-with-body -i -X POST "${BASE_URL}/digitama/select" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== EQUIPMENT =====

# EquipmentController.getDigimonEquipment (GET /equipment/digimon/{digimonId})
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.getDigimonInventory (GET /equipment/digimon/{digimonId}/inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/digimon/${DIGIMON_ID}/inventory" -H "Authorization: Bearer ${TOKEN}"

# EquipmentController.endpoint (POST /equipment/equip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/equip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.refine (POST /equipment/refine)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/refine" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.endpoint (POST /equipment/unequip)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.refine (POST /equipment/unequip-all)
# curl --fail-with-body -i -X POST "${BASE_URL}/equipment/unequip-all" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# EquipmentController.refinePreview (GET /equipment/{equipmentId}/refine-preview)
# curl --fail-with-body -i -X GET "${BASE_URL}/equipment/${EQUIPMENT_ID}/refine-preview" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# EvolutionLineController.getEvolutionLines (GET /evolution-lines)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines" -H "Authorization: Bearer ${TOKEN}"

# ===== EVOLUTION-LINES =====

# EvolutionLineController.getAvailableEvolutionLines (GET /evolution-lines/available)
# curl --fail-with-body -i -X GET "${BASE_URL}/evolution-lines/available" -H "Authorization: Bearer ${TOKEN}"

# ===== INCUBATION =====

# IncubationController.claim (POST /incubation/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/claim" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# IncubationController.me (GET /incubation/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/incubation/me" -H "Authorization: Bearer ${TOKEN}"

# IncubationController.start (POST /incubation/start)
# curl --fail-with-body -i -X POST "${BASE_URL}/incubation/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# InventoryController.getInventory (GET /inventory)
# curl --fail-with-body -i -X GET "${BASE_URL}/inventory" -H "Authorization: Bearer ${TOKEN}"

# ===== INVENTORY =====

# InventoryController.useItem (POST /inventory/use)
# curl --fail-with-body -i -X POST "${BASE_URL}/inventory/use" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# ItemDefinitionController.getItems (GET /items)
# curl --fail-with-body -i -X GET "${BASE_URL}/items" -H "Authorization: Bearer ${TOKEN}"

# MailController.send (POST /mail)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== MAIL =====

# MailController.inbox (GET /mail/inbox)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/inbox" -H "Authorization: Bearer ${TOKEN}"

# MailController.sent (GET /mail/sent)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/sent" -H "Authorization: Bearer ${TOKEN}"

# MailController.get (GET /mail/unread-count)
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/unread-count" -H "Authorization: Bearer ${TOKEN}"

# MailController.delete (DELETE /mail/{messageId})
# curl --fail-with-body -i -X DELETE "${BASE_URL}/mail/${MESSAGE_ID}" -H "Authorization: Bearer ${TOKEN}"

# MailController.get (GET /mail/{messageId})
# curl --fail-with-body -i -X GET "${BASE_URL}/mail/${MESSAGE_ID}" -H "Authorization: Bearer ${TOKEN}"

# MailController.action (POST /mail/{messageId}/action)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail/${MESSAGE_ID}/action" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# MailController.markRead (POST /mail/{messageId}/read)
# curl --fail-with-body -i -X POST "${BASE_URL}/mail/${MESSAGE_ID}/read" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== ROOT =====

# MissionController.list (GET /missions)
# curl --fail-with-body -i -X GET "${BASE_URL}/missions" -H "Authorization: Bearer ${TOKEN}"

# ===== MISSIONS =====

# MissionController.getActiveMissions (GET /missions/active)
# curl --fail-with-body -i -X GET "${BASE_URL}/missions/active" -H "Authorization: Bearer ${TOKEN}"

# MissionController.start (POST /missions/start)
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/start" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# MissionController.claimMission (POST /missions/{missionInstanceId}/claim)
# curl --fail-with-body -i -X POST "${BASE_URL}/missions/${MISSION_INSTANCE_ID}/claim" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# ===== PLAYERS =====

# PlayerController.me (GET /players/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.changePassword (POST /players/me/change-password)
# curl --fail-with-body -i -X POST "${BASE_URL}/players/me/change-password" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# PlayerController.dashboard (GET /players/me/dashboard)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/dashboard" -H "Authorization: Bearer ${TOKEN}"

# PlayerController.startup (GET /players/me/startup)
# curl --fail-with-body -i -X GET "${BASE_URL}/players/me/startup" -H "Authorization: Bearer ${TOKEN}"

# ===== RANKING =====

# RankingController.byGrade (GET /ranking/grade)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/grade" -H "Authorization: Bearer ${TOKEN}"

# RankingController.byLevel (GET /ranking/level)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/level" -H "Authorization: Bearer ${TOKEN}"

# RankingController.byRebirth (GET /ranking/rebirth)
# curl --fail-with-body -i -X GET "${BASE_URL}/ranking/rebirth" -H "Authorization: Bearer ${TOKEN}"

# ===== ROOT =====

# ShopController.getProducts (GET /shop)
# curl --fail-with-body -i -X GET "${BASE_URL}/shop" -H "Authorization: Bearer ${TOKEN}"

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
# curl --fail-with-body -i -X POST "${BASE_URL}/world-boss/attack" -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" -d '{}'

# WorldBossController.getMyWorldBoss (GET /world-boss/me)
# curl --fail-with-body -i -X GET "${BASE_URL}/world-boss/me" -H "Authorization: Bearer ${TOKEN}"
