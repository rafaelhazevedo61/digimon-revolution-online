// npm install --prefix /tmp/clan-tests jsdom @electric-sql/pglite
// NODE_PATH=/tmp/clan-tests/node_modules node --test scripts/clan-contribution-chest.test.cjs
const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const {JSDOM}=require('jsdom'),{PGlite}=require('@electric-sql/pglite');
const root=path.resolve(__dirname,'..'),read=p=>fs.readFileSync(path.join(root,p),'utf8');
const migrations='backend/src/main/resources/db/migration/';
test('migration links personal chest to one-roll pool with exact final chances and quantities',async()=>{
 const db=new PGlite();try{
 await db.exec(`CREATE TABLE item_definitions(id bigserial primary key,code varchar(80) unique not null,name varchar(120) not null,description text,category varchar(40) not null,stackable boolean not null default true,buy_price integer,sell_price integer,tradable boolean not null,sellable boolean not null,usable boolean not null,max_stack integer,rarity varchar(20) not null,icon varchar(120));`);
 const original=read(migrations+'V101__create_loot_tables_and_chests.sql');
 await db.exec(original.slice(0,original.indexOf('-- A single reusable baseline')));
 await db.exec(read(migrations+'V102__add_chest_item_count_range.sql'));
 await db.exec(read(migrations+'V260__clan_contribution_chest.sql'));
 const chest=(await db.query(`SELECT c.*,i.category,i.max_stack,i.sellable,i.usable,t.min_items,t.max_items FROM chest_definitions c JOIN item_definitions i ON i.id=c.item_definition_id JOIN loot_tables t ON t.id=c.loot_table_id WHERE c.code='CHEST_CLAN_CONTRIBUTION'`)).rows[0];
 assert.equal(chest.name,'Baú de Contribuição');assert.equal(chest.tradable,false);assert.equal(chest.sellable,false);assert.equal(chest.usable,true);assert.equal(chest.category,'CHEST');assert.equal(chest.max_stack,999);assert.equal(chest.min_items,1);assert.equal(chest.max_items,1);
 const entries=(await db.query(`SELECT e.*,r.weight AS rarity_weight FROM loot_table_entries e JOIN loot_table_rarity_weights r ON r.loot_table_id=e.loot_table_id AND r.rarity=e.rarity WHERE e.loot_table_id=$1`,[chest.loot_table_id])).rows;
 assert.equal(entries.length,7);
 const expected={ENERGY_CELL:[35,2,4],XP_DISC_3:[30,1,2],RARITY_PRESERVATION:[15,1,1],RARITY_REROLL:[10,1,1],ASCENSION_CORE:[6,1,1],VIP_ENERGY_CHARGE:[3,1,1],VIP_DAY_PASS:[1,1,1]};
 const enumSource=read('backend/src/main/java/com/dro/modules/inventory/domain/ItemType.java');
 for(const e of entries){const group=entries.filter(x=>x.rarity===e.rarity).reduce((n,x)=>n+x.weight,0);const chance=e.rarity_weight*e.weight/group;assert.deepEqual([chance,e.min_quantity,e.max_quantity],expected[e.item_type]);assert.equal(e.active,true);assert.ok(enumSource.includes(e.item_type));}
 assert.equal(entries.reduce((n,e)=>n+expected[e.item_type][0],0),100);
 }finally{await db.close();}
});
function setup(){const d=new JSDOM('<div id="clan-tab-content"></div>',{runScripts:'outside-only'}),w=d.window;
 w.escapeHtml=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;');w.safeContent=id=>w.document.getElementById(id);w.eval(read('game-frontend/assets/js/clan.js')+'\ncurrentClan={id:"clan",level:1};');return d;}
const reward={personalRewardItemCode:'CHEST_CLAN_CONTRIBUTION',personalRewardItemName:'Baú de Contribuição',personalRewardQuantity:1};
const mission={id:'mission',title:'Contribuição',description:'Complete missões',objectiveType:'MISSIONS_COMPLETED',targetValue:5,progress:5,status:'COMPLETED',minHonorMarksReward:10,maxHonorMarksReward:20,honorMarksReward:15,clanXpReward:50,minClanLevel:1,...reward};
test('catalog and active mission distinguish clan and personal rewards',async()=>{
 const d=setup(),w=d.window;try{w.apiGet=async p=>p==='/clan-missions'?[mission]:p==='/clan-missions/me'?mission:[];await w.clanLoadMissions();const text=w.document.body.textContent;assert.ok(text.includes('Para o clã'));assert.ok(text.includes('Para você'));assert.ok(text.includes('Baú de Contribuição'));assert.ok(text.includes('50 XP'));assert.ok(text.includes('Resgatar recompensa'));assert.ok(text.includes('Seu progresso nesta missão'));}finally{w.close();}
});
test('personal reward text escapes names and tolerates old server contract',()=>{
 const d=setup(),w=d.window;try{w.document.body.innerHTML=w.renderClanPersonalReward({...reward,personalRewardItemName:'<img src=x onerror=bad()>'});assert.equal(w.document.querySelector('img'),null);assert.ok(w.document.body.textContent.includes('<img'));assert.equal(w.renderClanPersonalReward({}),'');}finally{w.close();}
});
test('successful claim confirms inventory delivery, failed claim does not show success',async()=>{
 const d=setup(),w=d.window,events=[];try{
 w.showToast=(...a)=>events.push(a);w.apiPost=async()=>reward;w.apiGet=async()=>({id:'clan'});w.renderClanDetail=()=>{};w.clanShowTab=()=>{};
 await w.clanClaimMission('mission');assert.ok(events[0][0].includes('Baú de Contribuição no inventário'));assert.equal(events[0][1],'success');
 events.length=0;w.apiPost=async()=>{throw Error('Estoque cheio')};await w.clanClaimMission('mission');assert.deepEqual(events,[['Estoque cheio','error']]);
 }finally{w.close();}
});
