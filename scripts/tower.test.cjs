// npm install --prefix /tmp/dro-tower-tests jsdom @electric-sql/pglite
// NODE_PATH=/tmp/dro-tower-tests/node_modules node --test scripts/tower.test.cjs
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {JSDOM} = require('jsdom');
const {PGlite} = require('@electric-sql/pglite');
const root = path.resolve(__dirname, '..');
const read = p => fs.readFileSync(path.join(root, p), 'utf8');
const player = '00000000-0000-0000-0000-000000000001';
const request = '00000000-0000-0000-0000-000000000002';
const java = read('backend/src/main/java/com/dro/modules/tower/infra/TowerRepository.java');
const sql = java.match(/"""\s*(INSERT INTO[\s\S]*?)"""/)[1].replace(/\?/g, (()=>{let i=0; return () => '$'+(++i);})());
const params = [player,1,1,new Date('2026-11-23T12:00:30Z'),request,1,true,1,100,'ENERGY_CELL','Célula de Energia',1,'Time','Koromon'];
async function db() {
  const db = new PGlite();
  await db.exec('CREATE TABLE players(id uuid primary key,bits int not null default 0); CREATE TABLE items(player_id uuid,quantity int CHECK(quantity <= 1));');
  await db.query('INSERT INTO players VALUES ($1, 20)', [player]);
  await db.exec(read('backend/src/main/resources/db/migration/V253__infinite_tower.sql'));
  await db.exec(read('backend/src/main/resources/db/migration/V254__tower_result_member_health.sql'));
  return db;
}
test('real migration and repository upsert persist one row per account', async()=>{
 const d = await db(); try {
   await d.query(sql,params); const next = [...params]; next[2]=2; next[6]=false; next[8]=0; next[9]=null; next[10]=null; next[11]=0;
   await d.query(sql,next);
   const rows=(await d.query('SELECT * FROM player_tower_progress')).rows;
   assert.equal(rows.length,1); assert.equal(Number(rows[0].highest_floor),1); assert.equal(Number(rows[0].attempts),2); assert.equal(rows[0].last_won,false);
 } finally {await d.close();}
});
test('database failure rolls back wallet, inventory and floor together',async()=>{
 const d = await db(); try {
   await assert.rejects(d.transaction(async tx=>{
     await tx.query(sql,params);
     await tx.query('UPDATE players SET bits = bits + 100 WHERE id = $1',[player]);
     await tx.query('INSERT INTO items VALUES ($1, 2)',[player]);
   }));
   assert.equal((await d.query('SELECT * FROM player_tower_progress')).rows.length,0);
   assert.equal((await d.query('SELECT bits FROM players')).rows[0].bits,20);
   assert.equal((await d.query('SELECT * FROM items')).rows.length,0);
 } finally {await d.close();}
});
test('progress survives absence of mission history and invalid negative progress rejected',async()=>{
 const d=await db(); try {
   await d.query(sql,params);
   await assert.rejects(d.query('UPDATE player_tower_progress SET highest_floor = -1'));
   assert.equal(Number((await d.query('SELECT highest_floor FROM player_tower_progress')).rows[0].highest_floor),1);
   await d.query('DELETE FROM players WHERE id = $1',[player]);
   assert.equal((await d.query('SELECT * FROM player_tower_progress')).rows.length,0);
 }finally{await d.close();}
});
const initial = () => ({highestFloor:0,attempts:0,serverTime:new Date().toISOString(),availableAt:null,nextFloor:{number:1,kind:'COMUM',enemyName:'Koromon',profile:'Equilibrado',stats:{hp:40,attack:8,defense:4},reward:{bits:100,itemCode:'ENERGY_CELL',itemName:'Célula de Energia',quantity:1}},lastResult:null});
const teams = [{id:'team1',name:'Meu time',digimonIds:['d1']},{id:'team2',name:'Outro time',digimonIds:['d2']}];
async function ui(options={}) {
 const dom=new JSDOM('<div id="app"></div>',{url:'https://game.test/#tower',runScripts:'outside-only'}); const w=dom.window;
 w.showBottomNav=()=>{};w.escapeHtml=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;'); w.escapeAttr=w.escapeHtml;w.formatStage=v=>v;
 w.apiGet=async p=>p.endsWith('/tower')?(options.state||initial()):p==='/mission-teams'?(options.teams||teams):[{id:'d1',name:'Agumon<script>bad()</script>',level:8,stage:'ROOKIE'},{id:'d2',name:'Gabumon',level:9,stage:'ROOKIE'}];
 w.apiPost=options.post||(async()=>initial());
 w.eval(read('game-frontend/assets/js/tower.js'));await w.renderTowerPage();return dom;
}
test('tower renders preview, escapes names and switches formation',async()=>{
 const dom=await ui();try{const d=dom.window.document;assert.match(d.body.textContent,/Andar 1/);assert.match(d.body.textContent,/100 Bits/);assert.equal(d.querySelectorAll('script').length,0);
 const select=d.querySelector('select');select.value='team2';select.dispatchEvent(new dom.window.Event('change'));assert.match(d.querySelector('#tower-members').textContent,/Gabumon/);
 }finally{dom.window.close();}
});
test('empty teams provide creation link',async()=>{const dom=await ui({teams:[]});try{assert.equal(dom.window.document.querySelector('#tower-challenge'),null);assert.match(dom.window.document.body.textContent,/Criar meu primeiro time/);}finally{dom.window.close();}});
test('server clock drives cooldown even when client differs',async()=>{
 const state=initial();state.serverTime='2026-01-01T00:00:00Z';state.availableAt='2026-01-01T00:00:30Z';const dom=await ui({state});try{assert.equal(dom.window.document.querySelector('button').disabled,true);assert.match(dom.window.document.querySelector('button').textContent,/30s/);}finally{dom.window.close();}
});
test('double click submits once and renders automatic reward',async()=>{
 let calls=0,resolve; const promise=new Promise(r=>resolve=r);const dom=await ui({post:async(p,body)=>{calls++;assert.equal(p,'/players/me/tower/challenge');assert.equal(body.expectedAttempts,0);return promise;}});
 try {const d=dom.window.document;d.querySelector('#tower-challenge').click();d.querySelector('#tower-challenge').click();assert.equal(calls,1);
 const updated=initial();updated.highestFloor=1;updated.attempts=1;updated.nextFloor.number=2;updated.lastResult={floor:1,won:true,rounds:2,teamName:'Meu time',enemyName:'Koromon',reward:updated.nextFloor.reward};resolve(updated);await new Promise(r=>setTimeout(r,0));assert.match(d.body.textContent,/Vitória!/);assert.match(d.body.textContent,/Recompensa adicionada automaticamente/);
 }finally{dom.window.close();}
});
test('network failure keeps same request ID on retry',async()=>{
 const ids=[];const dom=await ui({post:async(p,b)=>{ids.push(b.requestId);throw new Error('Sem conexão');}});try{const d=dom.window.document;d.querySelector('#tower-challenge').click();await new Promise(r=>setTimeout(r,0));assert.match(d.querySelector('#tower-error').textContent,/Sem conexão/);d.querySelector('#tower-challenge').click();await new Promise(r=>setTimeout(r,0));assert.equal(ids.length,2);assert.equal(ids[0],ids[1]);}finally{dom.window.close();}
});
test('navigation away ignores late responses',async()=>{
 let resolve;const response=new Promise(r=>resolve=r);const dom=await ui({post:()=>response});try{const d=dom.window.document;d.querySelector('#tower-challenge').click();d.querySelector('#app').innerHTML='<p>Outra página</p>';resolve(initial());await new Promise(r=>setTimeout(r,0));assert.equal(d.querySelector('#app').textContent,'Outra página');}finally{dom.window.close();}
});


// Queries taken from the repository, including the one-statement snapshot read.
const memberInsert = java.match(/"""\s*(INSERT INTO player_tower_last_members[\s\S]*?)"""/)[1].replace(/\?/g, (()=>{let i=0; return () => '$'+(++i);})());
const snapshotSelect = java.match(/"""\s*(SELECT p\.\*[\s\S]*?)"""/)[1].replace('?', '$1');
test('member HP snapshots survive reload and retain formation order', async()=>{
 const d=await db();try {
   await d.query(sql,params);
   await d.query(memberInsert,[player,1,'00000000-0000-0000-0000-000000000004','Gabumon',0,100]);
   await d.query(memberInsert,[player,0,'00000000-0000-0000-0000-000000000003','Agumon',73,120]);
   const rows=(await d.query(snapshotSelect,[player])).rows;
   assert.deepEqual(rows.map(r=>[r.digimon_name,r.remaining_hp,r.max_hp]),[['Agumon',73,120],['Gabumon',0,100]]);
   assert.ok(rows.every(r=>r.request_id===request));
   await assert.rejects(d.query(memberInsert,[player,2,'00000000-0000-0000-0000-000000000005','Patamon',121,120]));
 }finally{await d.close();}
});
test('older results without snapshots still load',async()=>{
 const d=await db();try{
   await d.query(sql,params);
   const rows=(await d.query(snapshotSelect,[player])).rows;
   assert.equal(rows.length,1);assert.equal(rows[0].digimon_id,null);assert.equal(rows[0].last_won,true);
 }finally{await d.close();}
});
test('failed snapshot replacement rolls back to previous result and HP',async()=>{
 const d=await db();try{
   await d.query(sql,params);
   await d.query(memberInsert,[player,0,'00000000-0000-0000-0000-000000000003','Agumon',73,120]);
   await assert.rejects(d.transaction(async tx=>{
     const next=[...params];next[2]=2;await tx.query(sql,next);
     await tx.query('DELETE FROM player_tower_last_members WHERE player_id = $1',[player]);
     await tx.query(memberInsert,[player,0,'00000000-0000-0000-0000-000000000003','Agumon',-1,120]);
   }));
   const row=(await d.query(snapshotSelect,[player])).rows[0];
   assert.equal(Number(row.attempts),1);assert.equal(row.remaining_hp,73);
 }finally{await d.close();}
});
test('result details display each member HP and defeated status safely',async()=>{
 const state=initial();state.lastResult={floor:1,won:true,rounds:3,teamName:'Time',enemyName:'Koromon',reward:state.nextFloor.reward,
 members:[{digimonId:'d1',name:'Agumon<script>bad()</script>',remainingHp:73,maxHp:120},{digimonId:'d2',name:'Gabumon',remainingHp:0,maxHp:100}]};
 const dom=await ui({state});try{
   const d=dom.window.document,details=d.querySelector('.tower-health-details');
   assert.match(details.querySelector('summary').textContent,/HP restante/);
   assert.equal(details.querySelectorAll('article').length,2);
   assert.match(details.textContent,/73 \/ 120/);assert.match(details.textContent,/Derrotado/);
   assert.equal(details.querySelectorAll('script').length,0);
   assert.equal(details.querySelector('progress').value,73);assert.equal(details.querySelector('progress').max,120);
   assert.equal(details.querySelector('.is-defeated progress').value,0);
 }finally{dom.window.close();}
});
test('old result explains that HP details start with the next fight',async()=>{
 const state=initial();state.lastResult={floor:1,won:false,rounds:1,teamName:'Time',enemyName:'Koromon',reward:{bits:0},members:[]};
 const dom=await ui({state});try{assert.match(dom.window.document.body.textContent,/Detalhes de HP disponíveis a partir do próximo combate/);}finally{dom.window.close();}
});
