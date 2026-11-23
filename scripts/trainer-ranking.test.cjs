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
 CREATE TABLE players(id uuid primary key,username varchar(50),user_type text,clan_id uuid);
 CREATE TABLE clans(id uuid primary key,name text,tag text,active boolean);
 CREATE TABLE digimons(player_id uuid,is_bot boolean);
 CREATE TABLE achievement_definitions(code varchar(60) primary key,active boolean default true);
 CREATE TABLE player_achievements(player_id uuid,achievement_code varchar(60),completed_at timestamptz,claimed_at timestamptz);
 CREATE TABLE digimon_collection_entries(player_id uuid,digimon_info_id bigint,rarity text);
 CREATE TABLE player_tower_progress(player_id uuid primary key,highest_floor bigint);
 INSERT INTO achievement_definitions(code) VALUES ('FIRST_HATCH'),('WORLD_500'),('HATCH_50'),('STAGE_MEGA');
 `);
 await d.exec(read('backend/src/main/resources/db/migration/V255__trainer_ranking.sql'));
 await d.exec(read('backend/src/main/resources/db/migration/V256__trainer_search_profile_and_history.sql'));
 await d.exec(read('backend/src/main/resources/db/migration/V257__trainer_weekly_progress_and_daily_movement.sql'));
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
 w.escapeAttr=w.escapeHtml;
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

const positional=sql=>{let i=0;return sql.replace(/\?/g,()=>'$'+(++i));};
const searchSql=positional(block('SELECT [*] FROM trainer_ranking_entries'));
const profileSql=positional(block('SELECT e[.]\\*'));
const historySql=block('INSERT INTO trainer_ranking_daily_history');
test('literal case-insensitive search preserves global position and never treats wildcard characters specially',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 const rows=(await d.query(searchSql,['beTA',10,0])).rows;assert.equal(rows.length,1);assert.equal(Number(rows[0].position),2);
 assert.equal((await d.query(searchSql,['%',10,0])).rows.length,0);
 assert.equal((await d.query(searchSql,["' OR true --",10,0])).rows.length,0);
 }finally{await d.close();}
});
test('profile exposes score snapshot and current active clan but hides excluded users',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 const clan='00000000-0000-0000-0000-000000000099';await d.query("INSERT INTO clans VALUES ($1,'Tamers','TMR',true)",[clan]);
 await d.query('UPDATE players SET clan_id=$1 WHERE id=$2',[clan,ids[0]]);
 let row=(await d.query(profileSql,[ids[0]])).rows[0];assert.equal(row.clan_name,'Tamers');assert.equal(Number(row.total_score),415);
 assert.equal(row.email,undefined);assert.equal(row.password,undefined);
 await d.query('UPDATE players SET trainer_ranking_excluded=true WHERE id=$1',[ids[0]]);
 assert.equal((await d.query(profileSql,[ids[0]])).rows.length,0);
 }finally{await d.close();}
});
test('daily history is immutable within same rule/day and changes version after reweighting',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);await d.exec(historySql);
 let rows=(await d.query('SELECT * FROM trainer_ranking_daily_history WHERE player_id=$1',[ids[0]])).rows;
 assert.equal(rows.length,1);assert.equal(Number(rows[0].total_score),415);
 const firstVersion=rows[0].rules_version;
 await d.query('UPDATE player_tower_progress SET highest_floor=20 WHERE player_id=$1',[ids[0]]);
 await rebuild(d);await d.exec(historySql);
 rows=(await d.query('SELECT * FROM trainer_ranking_daily_history WHERE player_id=$1',[ids[0]])).rows;
 assert.equal(rows.length,1);assert.equal(Number(rows[0].total_score),415);
 await d.exec("UPDATE achievement_definitions SET trainer_points=50 WHERE code='FIRST_HATCH'");await rebuild(d);await d.exec(historySql);
 rows=(await d.query('SELECT * FROM trainer_ranking_daily_history WHERE player_id=$1',[ids[0]])).rows;
 assert.equal(rows.length,2);assert.ok(rows.some(r=>r.rules_version!==firstVersion));
 assert.equal((await d.query("SELECT COUNT(*) n FROM trainer_ranking_daily_history WHERE snapshot_date <> (CURRENT_TIMESTAMP AT TIME ZONE 'America/Sao_Paulo')::date")).rows[0].n,0);
 }finally{await d.close();}
});
test('history insertion rolls back together with the published ranking',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 await assert.rejects(d.transaction(async tx=>{await tx.exec(historySql);await tx.exec('DELETE FROM trainer_ranking_entries');throw Error('rollback');}));
 assert.equal((await d.query('SELECT * FROM trainer_ranking_daily_history')).rows.length,0);
 assert.equal((await d.query('SELECT * FROM trainer_ranking_entries')).rows.length,2);
 }finally{await d.close();}
});
test('clicking a player opens safe public profile and Escape closes it',async()=>{
 const dom=ui();try{const w=dom.window;
 w.apiGet=async p=>{assert.equal(p,'/ranking/players/'+ids[0]);return{ranking:{playerName:'Alpha<script>',position:42,totalScore:415,collectionScore:90,achievementScore:125,towerScore:200,collectionEntries:4,masteredSpecies:1,completedAchievements:2,highestFloor:10},clanName:'Tamers<script>',clanTag:'TMR',updatedAt:'2026-11-23T12:00:00Z'};};
 await w.openTrainerProfile(ids[0]);const overlay=w.document.querySelector('#trainer-profile-overlay');
 assert.match(overlay.textContent,/415/);assert.match(overlay.textContent,/Tamers/);assert.equal(overlay.querySelectorAll('script').length,0);
 overlay.dispatchEvent(new w.KeyboardEvent('keydown',{key:'Escape'}));assert.equal(w.document.querySelector('#trainer-profile-overlay'),null);
 }finally{dom.window.close();}
});
test('typing a new search invalidates an older response and retains global pagination',async()=>{
 const dom=ui();try{const w=dom.window;w.showBottomNav=()=>{};w.document.body.innerHTML='<div id="app"></div>';
 w.eval(read('game-frontend/assets/js/ranking.js'));
 let resolveOld;const old=new Promise(r=>resolveOld=r);const calls=[];
 w.apiGet=async(p,params)=>{calls.push({...params});if(calls.length===1)return old;return{entries:[],me:null,myStatus:'PENDING',updatedAt:null,totalPlayers:10,matchingPlayers:0,hasNext:false};};
 const first=w.renderRankingPage({tab:'players'});const input=w.document.querySelector('#rank-search');assert.equal(input.disabled,false);
 w.rankOnSearchInput('Beta');await new Promise(r=>setTimeout(r,350));
 resolveOld({entries:[],me:null,myStatus:'PENDING',updatedAt:null,totalPlayers:999,hasNext:false});await first;
 assert.equal(calls[1].search,'Beta');assert.match(w.document.querySelector('#rank-content').textContent,/0 encontrados/);assert.doesNotMatch(w.document.querySelector('#rank-content').textContent,/999/);
 }finally{dom.window.close();}
});

const progressJava=read('backend/src/main/java/com/dro/modules/ranking/infra/TrainerRankingProgressRepository.java');
const progressSql=[...progressJava.matchAll(/jdbc[.]update\("""\s*([\s\S]*?)"""\)/g)].map(m=>m[1]);
assert.equal(progressSql.length,6);
async function publishAt(d,when,fail=false){
 await d.transaction(async tx=>{
   await tx.exec('DELETE FROM trainer_ranking_entries');await tx.exec(insert);await tx.exec(update);
   await tx.query('UPDATE trainer_ranking_state SET updated_at=$1 WHERE id=1',[when]);
   await tx.exec(historySql);for(const sql of progressSql)await tx.exec(sql);
   if(fail)throw Error('simulated publish failure');
 });
}
async function ranked(d,id=ids[0]){return(await d.query('SELECT * FROM trainer_ranking_entries WHERE player_id=$1',[id])).rows[0];}
test('weekly gain counts changes once and resets at Monday midnight Sao Paulo',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-22T15:00:00Z');
 assert.equal(Number((await ranked(d)).weekly_gain),0);
 await d.query('UPDATE player_tower_progress SET highest_floor=20 WHERE player_id=$1',[ids[0]]);
 await publishAt(d,'2026-11-23T02:59:59Z');assert.equal(Number((await ranked(d)).weekly_gain),200);
 await publishAt(d,'2026-11-23T03:00:00Z');assert.equal(Number((await ranked(d)).weekly_gain),0);
 assert.equal((await d.query('SELECT week_start::text AS week_start FROM trainer_ranking_state')).rows[0].week_start,'2026-11-23');
 await d.query('UPDATE player_tower_progress SET highest_floor=21 WHERE player_id=$1',[ids[0]]);
 await publishAt(d,'2026-11-23T04:00:00Z');assert.equal(Number((await ranked(d)).weekly_gain),20);
 await publishAt(d,'2026-11-23T04:05:00Z');assert.equal(Number((await ranked(d)).weekly_gain),20);
 }finally{await d.close();}
});
test('daily movement reports up, down, same and new with valid yesterday record',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');
 await publishAt(d,'2026-11-24T12:00:00Z');assert.equal((await ranked(d)).movement,'SAME');
 await d.query('INSERT INTO player_tower_progress VALUES ($1,40)',[ids[1]]);
 const newcomer='00000000-0000-0000-0000-000000000006';await d.query("INSERT INTO players(id,username,user_type) VALUES ($1,'New','PLAYER')",[newcomer]);
 await d.query('INSERT INTO player_tower_progress VALUES ($1,5)',[newcomer]);
 await publishAt(d,'2026-11-24T13:00:00Z');
 assert.equal((await ranked(d,ids[1])).movement,'UP');assert.equal(Number((await ranked(d,ids[1])).position_change),1);
 assert.equal((await ranked(d)).movement,'DOWN');assert.equal(Number((await ranked(d)).position_change),-1);
 assert.equal((await ranked(d,newcomer)).movement,'NEW');
 assert.equal(Number((await ranked(d,newcomer)).weekly_gain),100);assert.equal((await ranked(d,newcomer)).weekly_baseline_kind,'NEW_ENTRY');
 }finally{await d.close();}
});
test('missing yesterday is unavailable rather than a fabricated new entry',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');await publishAt(d,'2026-11-25T12:00:00Z');
 assert.equal((await ranked(d)).movement,'UNAVAILABLE');assert.equal((await ranked(d)).position_change,null);
 }finally{await d.close();}
});
test('weight change rebases the week and prevents administrative points from counting as progress',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');
 await d.exec("UPDATE achievement_definitions SET trainer_points=50 WHERE code='FIRST_HATCH'");
 await publishAt(d,'2026-11-24T12:00:00Z');assert.equal(Number((await ranked(d)).weekly_gain),0);
 assert.equal((await ranked(d)).movement,'UNAVAILABLE');
 await d.query('UPDATE player_tower_progress SET highest_floor=11 WHERE player_id=$1',[ids[0]]);
 await publishAt(d,'2026-11-24T13:00:00Z');assert.equal(Number((await ranked(d)).weekly_gain),20);
 assert.equal((await d.query('SELECT * FROM trainer_ranking_weeks')).rows.length,2);
 }finally{await d.close();}
});
test('existing account excluded when week opened gets observed baseline upon joining',async()=>{
 const d=await database();try{await seed(d);await d.query('INSERT INTO player_tower_progress VALUES ($1,30)',[ids[3]]);
 await publishAt(d,'2026-11-23T12:00:00Z');await d.query('UPDATE players SET trainer_ranking_excluded=false WHERE id=$1',[ids[3]]);
 await publishAt(d,'2026-11-23T13:00:00Z');const row=await ranked(d,ids[3]);
 assert.equal(Number(row.total_score),600);assert.equal(Number(row.weekly_gain),0);assert.equal(row.weekly_baseline_kind,'RETURNING');
 }finally{await d.close();}
});
test('first rollout can use real history earlier in the same week without reconstructing older days',async()=>{
 const d=await database();try{await seed(d);await rebuild(d);
 await d.exec("UPDATE trainer_ranking_state SET updated_at='2026-11-23T12:00:00Z'");await d.exec(historySql);
 await d.query('UPDATE player_tower_progress SET highest_floor=15 WHERE player_id=$1',[ids[0]]);
 await publishAt(d,'2026-11-25T12:00:00Z');const row=await ranked(d);assert.equal(Number(row.weekly_gain),100);
 assert.equal(new Date(row.weekly_baseline_at).toISOString(),'2026-11-23T12:00:00.000Z');
 assert.equal((await d.query("SELECT * FROM trainer_ranking_daily_history WHERE snapshot_date<'2026-11-23'")).rows.length,0);
 }finally{await d.close();}
});
test('weekly ordering uses gain then permanent position; ties stay deterministic',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');
 await d.query('INSERT INTO player_tower_progress VALUES ($1,5)',[ids[1]]);await publishAt(d,'2026-11-23T13:00:00Z');
 assert.equal(Number((await ranked(d,ids[1])).weekly_position),1);assert.equal(Number((await ranked(d)).weekly_position),2);
 await d.query('UPDATE player_tower_progress SET highest_floor=15 WHERE player_id=$1',[ids[0]]);await publishAt(d,'2026-11-23T14:00:00Z');
 assert.equal(Number((await ranked(d)).weekly_position),1);assert.equal(Number((await ranked(d,ids[1])).weekly_position),2);
 }finally{await d.close();}
});
test('failed week rollover keeps old score, comparisons and baseline metadata atomically',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-22T12:00:00Z');
 const old=(await d.query('SELECT * FROM trainer_ranking_state')).rows[0];
 await assert.rejects(publishAt(d,'2026-11-23T03:00:00Z',true));
 const current=(await d.query('SELECT * FROM trainer_ranking_state')).rows[0];assert.deepEqual(current,old);
 assert.equal((await d.query('SELECT * FROM trainer_ranking_weeks')).rows.length,1);
 }finally{await d.close();}
});

test('weekly search keeps weekly and global positions distinct',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');
 await d.query('INSERT INTO player_tower_progress VALUES ($1,5)',[ids[1]]);await publishAt(d,'2026-11-23T13:00:00Z');
 const query=[...java.matchAll(/"""\s*([\s\S]*?)"""/g)].map(m=>m[1]).find(q=>q.includes('ORDER BY weekly_position'));
 const rows=(await d.query(positional(query),['alpha',10,0])).rows;
 assert.equal(rows.length,1);assert.equal(Number(rows[0].position),1);assert.equal(Number(rows[0].weekly_position),2);
 }finally{await d.close();}
});
test('a recorded empty day permits a real new entry comparison next day',async()=>{
 const d=await database();try{await seed(d);await d.exec('UPDATE players SET trainer_ranking_excluded=true');
 await publishAt(d,'2026-11-23T12:00:00Z');assert.equal((await d.query('SELECT * FROM trainer_ranking_entries')).rows.length,0);
 await d.query('UPDATE players SET trainer_ranking_excluded=false WHERE id=$1',[ids[0]]);await publishAt(d,'2026-11-24T12:00:00Z');
 assert.equal((await ranked(d)).movement,'NEW');assert.equal(Number((await ranked(d)).weekly_gain),0);
 }finally{await d.close();}
});
test('negative corrections do not generate negative weekly points',async()=>{
 const d=await database();try{await seed(d);await publishAt(d,'2026-11-23T12:00:00Z');
 await d.query('UPDATE player_tower_progress SET highest_floor=0 WHERE player_id=$1',[ids[0]]);await publishAt(d,'2026-11-23T13:00:00Z');
 assert.equal(Number((await ranked(d)).weekly_gain),0);
 }finally{await d.close();}
});
test('weekly UI displays weekly gain and position while retaining global context',()=>{
 const dom=ui();try{const w=dom.window;
 const entry={playerId:ids[0],playerName:'Alpha',position:42,totalScore:415,weeklyPosition:7,weeklyGain:200,weeklyBaselineAt:'2026-11-23T12:00:00Z',weeklyBaselineKind:'EXISTING',collectionScore:90,achievementScore:125,towerScore:200};
 w.document.body.innerHTML=w.trainerRankingContent({mode:'weekly',entries:[entry],me:entry,myStatus:'RANKED',updatedAt:'2026-11-24T12:00:00Z',totalPlayers:100,weekStart:'2026-11-23',weekBaselineAt:'2026-11-23T12:00:00Z'});
 assert.match(w.document.body.textContent,/#7/);assert.match(w.document.body.textContent,/#42 no geral/);assert.match(w.document.body.textContent,/pontos na semana/);assert.match(w.document.body.textContent,/23\/11\/2026/);
 assert.match(w.document.querySelector('.trainer-rank-total').textContent,/200/);
 }finally{dom.window.close();}
});
test('all daily movement labels are rendered without inventing a comparison',()=>{
 const dom=ui();try{const w=dom.window;
 for(const [status,delta,label] of [['UP',3,'↑ 3'],['DOWN',-2,'↓ 2'],['SAME',0,'—'],['NEW',null,'Nova entrada'],['UNAVAILABLE',null,'Sem base de ontem']])assert.ok(w.trainerMovement({movement:status,positionChange:delta}).includes(label));
 }finally{dom.window.close();}
});
test('weekly toggle sends mode and preserves search',async()=>{
 const dom=ui();try{const w=dom.window;w.showBottomNav=()=>{};w.document.body.innerHTML='<div id="app"></div>';
 w.eval(read('game-frontend/assets/js/ranking.js'));const requests=[];
 w.apiGet=async(p,params)=>{requests.push(params);return{mode:params.mode,entries:[],me:null,myStatus:'PENDING',updatedAt:null,totalPlayers:0,matchingPlayers:0,hasNext:false};};
 await w.renderRankingPage({tab:'players'});w.document.querySelector('#rank-search').value='Alpha';w.rankSwitchTrainerMode('weekly');await new Promise(r=>setTimeout(r,0));
 assert.equal(requests.at(-1).mode,'weekly');assert.equal(requests.at(-1).search,'Alpha');assert.equal(requests.at(-1).page,0);
 assert.match(w.document.querySelector('#rank-content').textContent,/primeira base semanal/);
 }finally{dom.window.close();}
});
