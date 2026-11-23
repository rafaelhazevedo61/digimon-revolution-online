// npm install --prefix /tmp/dro-trainer-tests jsdom @electric-sql/pglite
// NODE_PATH=/tmp/dro-trainer-tests/node_modules node --test scripts/trainer-ranking.test.cjs
const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const {PGlite}=require('@electric-sql/pglite'),{JSDOM}=require('jsdom');
const root=path.resolve(__dirname,'..');
const read=p=>fs.readFileSync(path.join(root,p),'utf8');
const java=read('backend/src/main/java/com/dro/modules/ranking/infra/TrainerRankingRepository.java');
const block=prefix=>java.match(new RegExp('"""\\s*('+prefix+'[\\s\\S]*?)"""'))[1];
const insert=block('INSERT INTO trainer_ranking_entries'),update=block('UPDATE trainer_ranking_state');
const ids=Array.from({length:5},(_,i)=>'00000000-0000-0000-0000-'+String(i+1).padStart(12,'0'));
async function database(){
 const d=new PGlite();
 await d.exec(`
 CREATE TABLE players(id uuid primary key,username varchar(50),user_type text);
 CREATE TABLE digimons(player_id uuid,is_bot boolean);
 CREATE TABLE achievement_definitions(code varchar(60) primary key,active boolean default true);
 CREATE TABLE player_achievements(player_id uuid,achievement_code varchar(60),completed_at timestamptz,claimed_at timestamptz);
 CREATE TABLE digimon_collection_entries(player_id uuid,digimon_info_id bigint,rarity text);
 CREATE TABLE player_tower_progress(player_id uuid primary key,highest_floor bigint);
 INSERT INTO achievement_definitions(code) VALUES ('FIRST_HATCH'),('WORLD_500'),('HATCH_50'),('STAGE_MEGA');
 `);
 await d.exec(read('backend/src/main/resources/db/migration/V255__trainer_ranking.sql'));
 for(let i=0;i<ids.length;i++)await d.query('INSERT INTO players(id,username,user_type) VALUES ($1,$2,$3)',[ids[i],['Alpha','Beta','Admin','Teste','Bot'][i],i===2?'ADMIN':'PLAYER']);
 await d.query('UPDATE players SET trainer_ranking_excluded=true WHERE id=$1',[ids[3]]);
 await d.query('INSERT INTO digimons VALUES ($1,true)',[ids[4]]);
 return d;
}
async function rebuild(d){await d.transaction(async tx=>{await tx.exec('DELETE FROM trainer_ranking_entries');await tx.exec(insert);await tx.exec(update);});}
async function seed(d){
 for(const rarity of ['COMMON','RARE','EPIC','LEGENDARY'])await d.query('INSERT INTO digimon_collection_entries VALUES ($1,1,$2)',[ids[0],rarity]);
 await d.query("INSERT INTO player_achievements VALUES ($1,'FIRST_HATCH',now(),null),($1,'WORLD_500',now(),null),($1,'HATCH_50',null,null)",[ids[0]]);
 await d.query('INSERT INTO player_tower_progress VALUES ($1,10)',[ids[0]]);
}
test('scores count registrations, mastery, completed unclaimed achievements and highest floor',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 const rows=(await d.query('SELECT * FROM trainer_ranking_entries ORDER BY position')).rows;
 assert.equal(rows.length,2);assert.equal(rows[0].player_name,'Alpha');
 assert.equal(Number(rows[0].collection_score),90);assert.equal(Number(rows[0].achievement_score),125);
 assert.equal(Number(rows[0].tower_score),200);assert.equal(Number(rows[0].total_score),415);
 assert.equal(Number(rows[0].completed_achievements),2);
 assert.equal(Number(rows[1].total_score),0);
 }finally{await d.close();}
});
test('rebuild is idempotent and claiming does not add points',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 await d.query('UPDATE player_achievements SET claimed_at=now() WHERE player_id=$1 AND completed_at IS NOT NULL',[ids[0]]);
 await rebuild(d);assert.equal(Number((await d.query('SELECT total_score FROM trainer_ranking_entries WHERE player_id=$1',[ids[0]])).rows[0].total_score),415);
 }finally{await d.close();}
});
test('ties are deterministic and ranks unique',async()=>{
 const d=await database();try{await rebuild(d);let rows=(await d.query('SELECT player_name,position FROM trainer_ranking_entries ORDER BY position')).rows;
 assert.deepEqual(rows.map(r=>r.player_name),['Alpha','Beta']);assert.deepEqual(rows.map(r=>Number(r.position)),[1,2]);
 await rebuild(d);assert.deepEqual((await d.query('SELECT player_name FROM trainer_ranking_entries ORDER BY position')).rows.map(r=>r.player_name),['Alpha','Beta']);
 }finally{await d.close();}
});
test('snapshot and metadata roll back together on failure',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 await assert.rejects(d.transaction(async tx=>{await tx.exec('DELETE FROM trainer_ranking_entries');await tx.exec("UPDATE trainer_ranking_state SET total_players=999");throw Error('failure');}));
 assert.equal(Number((await d.query('SELECT total_players FROM trainer_ranking_state')).rows[0].total_players),2);
 assert.equal((await d.query('SELECT * FROM trainer_ranking_entries')).rows.length,2);
 }finally{await d.close();}
});
test('inactive achievement does not score and test-account exclusion takes effect next refresh',async()=>{
 const d=await database();try{await seed(d);await d.exec("UPDATE achievement_definitions SET active=false WHERE code='WORLD_500'");
 await rebuild(d);assert.equal(Number((await d.query('SELECT total_score FROM trainer_ranking_entries WHERE player_id=$1',[ids[0]])).rows[0].total_score),315);
 await d.query('UPDATE players SET trainer_ranking_excluded=true WHERE id=$1',[ids[0]]);await rebuild(d);
 assert.equal((await d.query('SELECT * FROM trainer_ranking_entries')).rows.length,1);
 }finally{await d.close();}
});
function ui(){
 const dom=new JSDOM('<div id="rank-content"></div><div id="rank-pagination"></div>',{runScripts:'outside-only',url:'https://game.test'});
 const w=dom.window;w.getPlayerId=()=>ids[0];w.escapeHtml=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;');
 w.eval(read('game-frontend/assets/js/trainer-ranking.js'));return dom;
}
test('renders own rank, breakdown, safe names and empty first snapshot',()=>{
 const dom=ui();try{const w=dom.window;
 const entry={playerId:ids[0],playerName:'Alpha<script>bad()</script>',position:42,totalScore:415,collectionScore:90,achievementScore:125,towerScore:200,collectionEntries:4,masteredSpecies:1,completedAchievements:2,highestFloor:10};
 w.document.body.innerHTML=w.trainerRankingContent({entries:[entry],me:entry,myStatus:'RANKED',updatedAt:'2026-11-23T12:00:00Z',totalPlayers:100});
 assert.match(w.document.body.textContent,/#42/);assert.match(w.document.body.textContent,/415/);assert.equal(w.document.querySelectorAll('script').length,0);
 w.document.body.innerHTML=w.trainerRankingContent({entries:[],me:null,myStatus:'PENDING',updatedAt:null,totalPlayers:0});
 assert.match(w.document.body.textContent,/Aguardando a primeira atualização/);
 }finally{dom.window.close();}
});
test('existing ranking tabs remain and players uses paged endpoint with personal position',async()=>{
 const dom=ui();try{const w=dom.window;w.eval(read('game-frontend/assets/js/ranking.js'));
 w.apiGet=async(p,params)=>{assert.equal(p,'/ranking/players');assert.equal(params.page,0);return{entries:[],me:null,myStatus:'EXCLUDED',updatedAt:null,totalPlayers:0,hasNext:false};};
 await w.rankSwitchTab('players');
 // rankSwitchTab starts the asynchronous request without awaiting it.
 await new Promise(r=>setTimeout(r,0));
 assert.match(w.document.getElementById('rank-content').textContent,/não participa/);
 const source=read('game-frontend/assets/js/ranking.js');for(const name of ['Poder','Rebirth','Arena','Clãs','Jogadores'])assert.ok(source.includes(name));
 }finally{dom.window.close();}
});
