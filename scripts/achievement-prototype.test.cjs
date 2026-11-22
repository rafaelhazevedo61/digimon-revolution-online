// npm install --prefix /tmp/dro-achievement-tests jsdom @electric-sql/pglite
// NODE_PATH=/tmp/dro-achievement-tests/node_modules node --test scripts/achievement-prototype.test.cjs
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { PGlite } = require('@electric-sql/pglite');
const { JSDOM } = require('jsdom');
const root = path.resolve(__dirname, '..');
const java = fs.readFileSync(path.join(root, 'backend/src/main/java/com/dro/modules/achievement/infra/AchievementRepository.java'), 'utf8');
const sql = fs.readFileSync(path.join(root, 'backend/src/main/resources/db/migration/V250__achievement_prototype_and_vip_pass.sql'), 'utf8');
const expansion = fs.readFileSync(path.join(root, 'backend/src/main/resources/db/migration/V251__expand_achievements_to_100.sql'), 'utf8');
const recorder = fs.readFileSync(path.join(root, 'backend/src/main/java/com/dro/modules/achievement/application/AchievementGameplayRecorder.java'), 'utf8');
const player = '00000000-0000-0000-0000-000000000001';
const other = '00000000-0000-0000-0000-000000000002';
const positional = sql => { let n=0; return sql.replace(/\?/g,()=>'$'+(++n)); };
const block = prefix => java.match(new RegExp('"""\\s*('+prefix+'[\\s\\S]*?)"""'))[1];
async function database(upgrade = true) {
  const db = new PGlite();
  await db.exec(`CREATE TABLE players(id uuid primary key,bits int default 0);
    CREATE TABLE item_definitions(code varchar(100) primary key,name text,description text,category text,stackable boolean,tradable boolean,sellable boolean,usable boolean,max_stack int,rarity text,icon text);
    CREATE TABLE digitama_history(player_id uuid,source text);
    CREATE TABLE incubations(player_id uuid,status text);
    CREATE TABLE mission_instances(player_id uuid,status text, mission_id text DEFAULT 'M_NATIVE_FOREST', team_id uuid,digimon_2_id uuid,digimon_3_id uuid);
    CREATE TABLE player_mission_progress(player_id uuid,mission_id text,completion_count int);
    CREATE TABLE mission_definitions(id text,area text,active boolean);
    CREATE TABLE available_contents(id bigint,active boolean);
    CREATE TABLE evolution_lines(id bigint, content_id bigint, active boolean);
    CREATE TABLE evolution_line_steps(evolution_line_id bigint,digimon_info_id bigint);
    CREATE TABLE boss_attempts(player_id uuid,boss_id bigint,status text);
    CREATE TABLE digimon_infos(id bigint,stage text);
    CREATE TABLE digimon_collection_entries(player_id uuid,digimon_info_id bigint,rarity text DEFAULT 'COMMON');
    CREATE TABLE digimons(player_id uuid,stage text,status text);
    CREATE TABLE arena_matches(attacker_player_id uuid,attacker_won boolean);
    CREATE TABLE world_boss_attacks(player_id uuid,world_boss_id bigint,damage int);
    CREATE TABLE world_boss_instances(id bigint,status text);
    CREATE TABLE clan_raid_attacks(player_id uuid,clan_raid_id bigint,damage int);
    CREATE TABLE clan_raid_instances(id bigint,status text);
    CREATE TABLE tutorial_progress(player_id uuid,step text);
    CREATE TABLE inventory_equipments(player_id uuid,refinement_level int,ascension_level int);`);
  // Itens existentes referenciados pelo catálogo novo.
  const itemCodes = new Set([...sql.matchAll(/, '(ENERGY_CELL|XP_DISC_\d+|INCUBATOR_\w+|RARITY_\w+|VIP_ENERGY_CHARGE|STORAGE_SLOT_\d+|ASCENSION_CORE|REFINEMENT_PROTECTION)', \d+, \d+\)/g)].map(m=>m[1]));
  assert.ok(itemCodes.size >= 10);
  for (const code of itemCodes) await db.query('INSERT INTO item_definitions(code,name) VALUES($1,$2)',[code,code]);
  await db.query('INSERT INTO players(id) VALUES ($1),($2)',[player,other]);
  await db.exec(sql);
  for (const area of ['NATIVE_FOREST','GEAR_SAVANNA','FACTORIAL_TOWN','FREEZELAND','SERVER_DESERT','INFINITY_MOUNTAIN']) {
    await db.query('INSERT INTO mission_definitions VALUES($1,$2,true)',['M_'+area,area]);
  }
  await db.exec(`INSERT INTO available_contents VALUES(1,true);
    INSERT INTO evolution_lines VALUES(10,1,true),(11,1,true);
    INSERT INTO evolution_line_steps VALUES(10,1),(10,2),(11,1),(11,3);`);
  if (upgrade) await db.exec(expansion);
  return db;
}

test('PostgreSQL: migration, catálogo e progressos permanentes por conta', async()=>{
 const db=await database();
 try {
  const definitions=await db.query('SELECT * FROM achievement_definitions ORDER BY sort_order');
  assert.equal(definitions.rows.length,100);
  assert.equal(definitions.rows.filter(r=>r.reward_item_code==='VIP_DAY_PASS').length,2);
  assert.equal(definitions.rows.filter(r=>r.reward_item_code==='STORAGE_SLOT_5').reduce((n,r)=>n+r.reward_quantity*5,0),20);
  const pass=(await db.query("SELECT * FROM item_definitions WHERE code='VIP_DAY_PASS'")).rows[0];
  assert.equal(pass.tradable,false); assert.equal(pass.sellable,false); assert.equal(pass.usable,true);
  const observe=positional(block('INSERT INTO player_achievements'));
  await db.query(observe,[player,'MISSION_10',10,10,10]);
  await db.query(observe,[player,'MISSION_10',2,2,10]);
  const row=(await db.query('SELECT * FROM player_achievements WHERE player_id=$1',[player])).rows[0];
  assert.equal(Number(row.progress),10); assert.ok(row.completed_at);
  assert.equal((await db.query('SELECT * FROM player_achievements WHERE player_id=$1',[other])).rows.length,0);
  const claim=positional(block('UPDATE player_achievements SET claimed_at'));
  assert.equal((await db.query(claim,[player,'MISSION_10'])).affectedRows,1);
  assert.equal((await db.query(claim,[player,'MISSION_10'])).affectedRows,0);
  await db.query(observe,[player,'MISSION_100',99,99,100]);
  assert.equal((await db.query(claim,[player,'MISSION_100'])).affectedRows,0);
  // Falha na entrega de item reverte também a marca de resgate e os Bits.
  await db.query(observe,[player,'MISSION_100',100,100,100]);
  await db.exec('BEGIN');
  await db.query(claim,[player,'MISSION_100']);
  await db.query('UPDATE players SET bits=bits+1500 WHERE id=$1',[player]);
  await db.exec('ROLLBACK');
  assert.equal((await db.query('SELECT claimed_at FROM player_achievements WHERE achievement_code=$1',['MISSION_100'])).rows[0].claimed_at,null);
  assert.equal((await db.query('SELECT bits FROM players WHERE id=$1',[player])).rows[0].bits,0);
 } finally {await db.close()}
});

test('PostgreSQL: consultas reais reconhecem históricos e separam contas',async()=>{
 const db=await database();
 try {
  await db.query("INSERT INTO digitama_history VALUES ($1,'DIRECT_HATCH'),($1,'REBIRTH'),($2,'DIRECT_HATCH')",[player,other]);
  await db.query("INSERT INTO incubations VALUES ($1,'CLAIMED'),($1,'IN_PROGRESS')",[player]);
  await db.query("INSERT INTO mission_instances(player_id,status) VALUES($1,'CLAIMED'),($1,'COMPLETED'),($2,'CLAIMED')",[player,other]);
  await db.query("INSERT INTO digimon_infos VALUES(1,'BABY_II'),(2,'ROOKIE')");
  await db.query("INSERT INTO digimon_collection_entries(player_id,digimon_info_id) VALUES($1,1),($1,1),($1,2)",[player]);
  await db.query("INSERT INTO digimons VALUES($1,'BABY','REBORN')",[player]);
  await db.query("INSERT INTO arena_matches VALUES($1,true),($1,false),($2,true)",[player,other]);
  const expected={HATCHES:2,MISSIONS:1,SPECIES:2,STAGE:2,REBIRTHS:1,ARENA_WINS:1,WORLD_ATTACKS:0,RAID_ATTACKS:0,EQUIPPED:0,REFINEMENT:0,ASCENSION:0,MASTERED_SPECIES:0,COMPLETE_LINES:1,BOSS_WINS:0,DISTINCT_BOSSES:0,WORLD_DEFEATS:0,RAID_DEFEATS:0};
  const pattern=/result\.put\("([A-Z_]+)", number\((?:"""([\s\S]*?)"""|"([^"\n]+)")/g;
  let count=0;
  for (const m of java.matchAll(pattern)) {
   const statement=positional(m[2]||m[3]);const params=Array((statement.match(/\$\d+/g)||[]).length).fill(player);
   const result=await db.query(statement,params);const value=Number(Object.values(result.rows[0])[0]);
   assert.equal(value,expected[m[1]],m[1]);count++;
  }
  assert.equal(count,17);
  // Atualizar CLAIMED novamente não conta duas vezes; apagar instâncias não reduz o total.
  await db.query("UPDATE mission_instances SET status='CLAIMED' WHERE player_id=$1 AND status='COMPLETED'",[player]);
  await db.query("UPDATE mission_instances SET status='CLAIMED' WHERE player_id=$1",[player]);
  await db.query('DELETE FROM mission_instances WHERE player_id=$1',[player]);
  assert.equal(Number((await db.query("SELECT value FROM player_achievement_counters WHERE player_id=$1 AND metric='MISSIONS'",[player])).rows[0].value),2);
 } finally {await db.close()}
});

test('Página: categorias, busca, filtros e resgate único com resumo atualizado',async()=>{
 const dom=await JSDOM.fromFile(path.join(root,'docs/prototypes/achievements.html'),{runScripts:'dangerously',resources:'usable',pretendToBeVisual:true,beforeParse(window){window.structuredClone=structuredClone;}});
 const wait=async predicate=>{for(let i=0;i<100;i++){if(predicate())return;await new Promise(r=>setTimeout(r,20));}throw Error('Timeout de renderização')};
 try {
  const d=dom.window.document;
  await wait(()=>d.querySelectorAll('.achievement-card').length===100);
  const ready=Number(d.querySelector('.achievements-stat-ready strong').textContent);
  d.querySelector('.achievement-ready [data-claim]').click();
  await wait(()=>Number(d.querySelector('.achievements-stat-ready strong').textContent)===ready-1);
  const status=d.querySelector('[data-status]');status.value='CLAIMED';status.dispatchEvent(new dom.window.Event('change'));
  assert.equal(d.querySelectorAll('.achievement-card:not(.achievement-claimed)').length,0);
  status.value='ALL';status.dispatchEvent(new dom.window.Event('change'));
  const search=d.querySelector('[data-search]');search.value='VIP';search.dispatchEvent(new dom.window.Event('input'));
  assert.equal(d.querySelectorAll('.achievement-card').length,2);
  search.value='';search.dispatchEvent(new dom.window.Event('input'));
  d.querySelector('[data-category="COLECAO"]').click();
  assert.equal(d.querySelectorAll('.achievement-card').length,18);
  const cards=d.querySelectorAll('.achievement-card');
  assert.ok([...cards].every(c=>c.querySelector('progress')));
 } finally {dom.window.close()}
});

async function metric(db, name, id=player) {
 const pattern=/result\.put\("([A-Z_]+)", number\((?:"""([\s\S]*?)"""|"([^"\n]+)")/g;
 const m=[...java.matchAll(pattern)].find(m=>m[1]===name);
 assert.ok(m,'Métrica registrada: '+name);
 const statement=positional(m[2]||m[3]);
 const result=await db.query(statement,Array((statement.match(/\$\d+/g)||[]).length).fill(id));
 return Number(Object.values(result.rows[0])[0]);
}

test('Nascimento: fotografia imutável, deduplicação, isolamento e rollback',async()=>{
 const db=await database();
 try {
  const insert=positional(recorder.match(/"""\s*(INSERT INTO achievement_birth_snapshots[\s\S]*?)"""/)[1]);
  const id='10000000-0000-0000-0000-000000000001';
  await db.query(insert,[id,player,'RARE',100,50,60,null]);
  // Reroll posterior ou repetição da captura não substitui a fotografia original.
  await db.query(insert,[id,player,'LEGENDARY',100,100,100,'FAST_LEARNER']);
  await db.query(insert,['10000000-0000-0000-0000-000000000002',player,'EPIC',100,100,100,'ENERGETIC']);
  await db.query(insert,['10000000-0000-0000-0000-000000000003',other,'LEGENDARY',100,100,100,'ENERGETIC']);
  const statement=positional(block('SELECT\\s+COUNT\\(\\*\\) FILTER'));
  const stats=(await db.query(statement,[player])).rows[0];
  assert.equal(Number(stats.rare),1);assert.equal(Number(stats.legendary),0);
  assert.equal(Number(stats.elite),1);assert.equal(Number(stats.perfect),2);
  assert.equal(Number(stats.all_perfect),1);assert.equal(Number(stats.traits),1);
  await db.exec('BEGIN');
  await db.query(insert,['10000000-0000-0000-0000-000000000004',player,'LEGENDARY',100,100,100,null]);
  await db.exec('ROLLBACK');
  assert.equal(Number((await db.query(statement,[player])).rows[0].legendary),0);
 }finally{await db.close()}
});

test('Missões: time completo, progresso por região e expurgo sem perder dados',async()=>{
 const db=await database();
 try {
  await db.query("INSERT INTO mission_instances(player_id,status,mission_id,team_id,digimon_2_id,digimon_3_id) VALUES($1,'RUNNING','M_GEAR_SAVANNA',$2,$2,$2)",[player,other]);
  await db.query("UPDATE mission_instances SET status='CLAIMED' WHERE player_id=$1",[player]);
  await db.query("UPDATE mission_instances SET status='CLAIMED' WHERE player_id=$1",[player]);
  // Dois parceiros não formam um time completo.
  await db.query("INSERT INTO mission_instances(player_id,status,mission_id,team_id,digimon_2_id) VALUES($1,'CLAIMED','M_NATIVE_FOREST',$2,$2)",[player,other]);
  assert.equal(Number((await db.query("SELECT value FROM player_achievement_counters WHERE player_id=$1 AND metric='TEAM_MISSIONS'",[player])).rows[0].value),1);
  await db.query('DELETE FROM mission_instances WHERE player_id=$1',[player]);
  assert.equal(await metric(db,'MISSIONS'),2);
  assert.equal((await db.query('SELECT * FROM achievement_completed_missions WHERE player_id=$1',[player])).rows.length,2);
  // Conteúdo futuro não altera os requisitos congelados da região.
  await db.query("INSERT INTO mission_definitions VALUES('FUTURE_MISSION','GEAR_SAVANNA',true)");
  const target=(await db.query("SELECT target FROM achievement_definitions WHERE code='EXPLORE_GEAR_SAVANNA'")).rows[0].target;
  assert.equal(Number(target),1);
  assert.equal((await db.query("SELECT * FROM achievement_area_missions WHERE area='GEAR_SAVANNA'")).rows.length,1);
 }finally{await db.close()}
});

test('Coleção: quatro raridades, espécies distintas e linhas completas congeladas',async()=>{
 const db=await database();
 try {
  await db.exec("INSERT INTO digimon_infos VALUES(1,'BABY'),(2,'ROOKIE'),(3,'CHAMPION')");
  for (const rarity of ['COMMON','RARE','EPIC','LEGENDARY'])await db.query('INSERT INTO digimon_collection_entries VALUES($1,1,$2)',[player,rarity]);
  await db.query("INSERT INTO digimon_collection_entries VALUES($1,2,'COMMON')",[player]);
  assert.equal(await metric(db,'SPECIES'),2);
  assert.equal(await metric(db,'MASTERED_SPECIES'),1);
  assert.equal(await metric(db,'COMPLETE_LINES'),1);
  assert.equal(await metric(db,'COMPLETE_LINES',other),0);
  await db.exec('INSERT INTO evolution_line_steps VALUES(10,3)');
  assert.equal(await metric(db,'COMPLETE_LINES'),1);
  await db.query("INSERT INTO digimon_collection_entries VALUES($1,3,'RARE')",[player]);
  assert.equal(await metric(db,'COMPLETE_LINES'),2);
 }finally{await db.close()}
});

test('Chefes: participação exige dano positivo no mesmo ciclo derrotado',async()=>{
 const db=await database();
 try{
  await db.exec("INSERT INTO world_boss_instances VALUES(1,'DEFEATED'),(2,'ACTIVE'),(3,'DEFEATED'); INSERT INTO clan_raid_instances VALUES(1,'DEFEATED'),(2,'ACTIVE')");
  await db.query('INSERT INTO world_boss_attacks VALUES($1,1,10),($1,1,15),($1,2,50),($1,3,0),($2,3,100)',[player,other]);
  await db.query('INSERT INTO clan_raid_attacks VALUES($1,1,10),($1,2,20),($2,1,100)',[player,other]);
  assert.equal(await metric(db,'WORLD_DEFEATS'),1);
  assert.equal(await metric(db,'RAID_DEFEATS'),1);
  await db.query("INSERT INTO boss_attempts VALUES($1,1,'VICTORY'),($1,1,'VICTORY'),($1,2,'DEFEAT'),($2,3,'VICTORY')",[player,other]);
  assert.equal(await metric(db,'BOSS_WINS'),2);assert.equal(await metric(db,'DISTINCT_BOSSES'),1);
 }finally{await db.close()}
});

test('Ações: set repetido, refinamento repetido e ascensão repetida não duplicam',async()=>{
 const db=await database();
 try{
  const insert=positional(recorder.match(/"""\s*(INSERT INTO achievement_gameplay_events[\s\S]*?)"""/)[1]);
  for(let i=0;i<2;i++)await db.query(insert,[player,'SETS_ACTIVATED','BERSERKER']);
  for(const set of ['GUARDIAN','VITALITY'])await db.query(insert,[player,'SETS_ACTIVATED',set]);
  for(let i=0;i<2;i++)await db.query(insert,[player,'REFINEMENT_SUCCESSES','attempt-1']);
  for(let i=0;i<2;i++)await db.query(insert,[player,'ASCENSIONS_DONE','equipment-1:1']);
  await db.query(insert,[other,'SETS_ACTIVATED','BALANCED']);
  const stats=(await db.query('SELECT metric,COUNT(*) AS n FROM achievement_gameplay_events WHERE player_id=$1 GROUP BY metric',[player])).rows;
  const values=Object.fromEntries(stats.map(r=>[r.metric,Number(r.n)]));
  assert.deepEqual(values,{ASCENSIONS_DONE:1,REFINEMENT_SUCCESSES:1,SETS_ACTIVATED:3});
 }finally{await db.close()}
});


test('Upgrade preserva as 33 definições antigas e recompensas já resgatadas', async()=>{
 const db=await database(false);
 try {
  const before=(await db.query('SELECT * FROM achievement_definitions ORDER BY code')).rows;
  await db.query("INSERT INTO player_achievements(player_id,achievement_code,progress,completed_at,claimed_at) VALUES($1,'FIRST_HATCH',1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",[player]);
  await db.exec(expansion);
  assert.equal((await db.query('SELECT * FROM achievement_definitions')).rows.length,100);
  const original=(await db.query('SELECT * FROM achievement_definitions WHERE code=ANY($1::varchar[]) ORDER BY code',[before.map(r=>r.code)])).rows;
  assert.deepEqual(original,before);
  const progress=(await db.query("SELECT * FROM player_achievements WHERE player_id=$1 AND achievement_code='FIRST_HATCH'",[player])).rows[0];
  assert.ok(progress.claimed_at);
  assert.equal((await db.query(positional(block('UPDATE player_achievements SET claimed_at')),[player,'FIRST_HATCH'])).affectedRows,0);
 } finally {await db.close()}
});
