// npm install --prefix /tmp/dro-tutorial-tests jsdom @electric-sql/pglite
// NODE_PATH=/tmp/dro-tutorial-tests/node_modules node --test scripts/tutorial-guide.test.cjs
const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const {JSDOM}=require('jsdom'),{PGlite}=require('@electric-sql/pglite');
const root=path.resolve(__dirname,'..'),read=p=>fs.readFileSync(path.join(root,p),'utf8');
const source=read('backend/src/main/java/com/dro/modules/tutorial/infra/TutorialGuideRepository.java');
let n=0;const ack=source.match(/"""\s*(INSERT INTO[\s\S]*?)"""/)[1].replace(/\?/g,()=>'$'+(++n));
const id='00000000-0000-0000-0000-000000000001',other='00000000-0000-0000-0000-000000000002';
test('read acknowledgements are account scoped, idempotent and independent of tutorial rewards',async()=>{
 const d=new PGlite();try{await d.exec('CREATE TABLE players(id uuid primary key); CREATE TABLE tutorial_progress(player_id uuid,step text);');
 await d.query('INSERT INTO players VALUES ($1),($2)',[id,other]);
 await d.exec(read('backend/src/main/resources/db/migration/V258__trainer_guide_read_topics.sql'));
 await d.query(ack,[id,'COLLECTION']);const first=(await d.query('SELECT read_at FROM trainer_guide_read_topics')).rows[0].read_at;
 await d.query(ack,[id,'COLLECTION']);assert.equal((await d.query('SELECT * FROM trainer_guide_read_topics')).rows.length,1);
 assert.deepEqual((await d.query('SELECT read_at FROM trainer_guide_read_topics')).rows[0].read_at,first);
 assert.equal((await d.query('SELECT * FROM trainer_guide_read_topics WHERE player_id=$1',[other])).rows.length,0);
 assert.equal((await d.query('SELECT * FROM tutorial_progress')).rows.length,0);
 await d.query('DELETE FROM players WHERE id=$1',[id]);assert.equal((await d.query('SELECT * FROM trainer_guide_read_topics')).rows.length,0);
 }finally{await d.close();}
});
function setup(){const dom=new JSDOM('<div id="app"></div>',{url:'https://game.test/#trainer-guide',runScripts:'outside-only'}),w=dom.window;
 w.escapeHtml=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('"','&quot;');w.escapeAttr=w.escapeHtml;
 w.showBottomNav=()=>{};w.navigateTo=()=>{};w.eval(read('game-frontend/assets/js/tutorial.js'));w.eval(read('game-frontend/assets/js/trainer-guide.js'));return dom;}
const initial=()=>({finished:false,totalSteps:6,completedSteps:0,allCompleted:false,canFinish:false,pendingRewards:0,steps:[{step:'SELECT_DIGITAMA',title:'Escolha',description:'Ovo',rewardBits:0,completed:false,rewardClaimed:false}]});
const page=()=>({initial:initial(),readTopics:0,totalTopics:2,topics:[{code:'COLLECTION',group:'Seu progresso',title:'Coleção<script>bad()</script>',description:'Registro automático',route:'collection',available:true,read:false},{code:'TOWER',group:'Desafios',title:'Torre',description:'Time',route:'tower',available:false,unlockHint:'Após missão',read:false}]});
test('unrewarded pending step is not struck through and initial hatch routes to starter',()=>{const dom=setup();try{const w=dom.window;assert.ok(!w.renderTutorialStep(initial().steps[0],true).includes('line-through'));assert.ok(w.renderTutorialStep({step:'HATCH_DIGIMON',title:'Nascimento',completed:false,rewardBits:100},true).includes("navigateTo('starter')"));}finally{dom.window.close();}});
test('guide shows next suggestion, locked topics and safe text',async()=>{const dom=setup();try{const w=dom.window;w.apiGet=async()=>page();await w.renderTrainerGuidePage();const d=w.document;assert.match(d.body.textContent,/Próxima descoberta sugerida/);assert.match(d.body.textContent,/Mais adiante/);assert.equal(d.querySelectorAll('[data-guide-read="TOWER"]').length,0);assert.equal(d.querySelectorAll('script').length,0);}finally{dom.window.close();}});
test('old finished tutorial remains finished while guide is accessible',async()=>{const dom=setup();try{const w=dom.window,p=page();p.initial.finished=true;w.apiGet=async()=>p;await w.renderTrainerGuidePage();assert.match(w.document.body.textContent,/Primeiros passos concluídos/);assert.doesNotMatch(w.document.querySelector('#tutorial-card').textContent,/Finalizar tutorial/);}finally{dom.window.close();}});
test('double click writes one acknowledgement and updates read count',async()=>{const dom=setup();try{const w=dom.window;w.apiGet=async()=>page();let resolve,calls=0;const pending=new Promise(r=>resolve=r);w.apiPost=async url=>{calls++;assert.equal(url,'/tutorial/guide/COLLECTION/acknowledge');return pending;};await w.renderTrainerGuidePage();const b=w.document.querySelector('[data-guide-read]');b.click();b.click();assert.equal(calls,1);const p=page();p.readTopics=1;p.topics[0].read=true;resolve(p);await new Promise(r=>setTimeout(r,0));assert.match(w.document.body.textContent,/1\/2 conteúdos lidos/);}finally{dom.window.close();}});
test('rank guide opens players tab rather than power tab',async()=>{const dom=setup();try{const w=dom.window,p=page();p.topics=[{code:'RANKING',group:'Progresso',title:'Classificação',description:'Conta',route:'ranking',available:true,read:false}];w.apiGet=async()=>p;let target;w.navigateTo=(route,params)=>target={route,params};await w.renderTrainerGuidePage();w.document.querySelector('[data-guide-route]').click();assert.equal(target.route,'ranking');assert.equal(target.params.tab,'players');}finally{dom.window.close();}});
test('late response does not overwrite another page',async()=>{const dom=setup();try{const w=dom.window;let resolve;w.apiGet=()=>new Promise(r=>resolve=r);const render=w.renderTrainerGuidePage();w.document.querySelector('#app').innerHTML='<p>Outra página</p>';resolve(page());await render;assert.equal(w.document.querySelector('#app').textContent,'Outra página');}finally{dom.window.close();}});

test('guide explains the completion achievement and links to rewards page',async()=>{const dom=setup();try{const w=dom.window;w.apiGet=async()=>page();let route;w.navigateTo=value=>route=value;await w.renderTrainerGuidePage();assert.match(w.document.body.textContent,/Treinador bem informado/);assert.match(w.document.body.textContent,/500 Bits/);w.document.querySelector('[data-guide-achievements]').click();assert.equal(route,'achievements');}finally{dom.window.close();}});
