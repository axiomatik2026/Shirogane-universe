const assert=require('node:assert/strict');
const E=require('./dist/engine.js');
let checks=0;const test=(name,fn)=>{fn();checks++;console.log('OK',name)};
test('Invalid formations are rejected',()=>assert.throws(()=>E.create(0,['amanda','amanda','amada','amalia'])));
test('Blocked movement never spends AP',()=>{const s=E.create();const a=s.allies[0];assert.ok(E.act(s,a.id,'move',{x:1,y:1}));assert.equal(a.ap,2)});
test('Out of range attacks never spend AP or deal damage',()=>{const s=E.create(),hp=s.enemies[0].hp;assert.ok(E.act(s,'amanda','attack','e2'));assert.equal(s.allies[0].ap,2);assert.equal(s.enemies[0].hp,hp)});
test('AP cannot go negative',()=>{const s=E.create();E.act(s,'amanda','guard');E.act(s,'amanda','guard');assert.ok(E.act(s,'amanda','guard'));assert.equal(s.allies[0].ap,0)});
test('Echo adapts to consecutive attacks from same actor',()=>{const s=E.create(1);let h=s.allies[1],e=s.enemies[0];h.x=4;h.y=3;E.act(s,h.id,'attack',e.id);let hp=e.hp;E.act(s,h.id,'attack',e.id);assert.equal(hp-e.hp,7)});
test('Anchors protect boss, destroying them exposes boss',()=>{const s=E.create(2),h=s.allies[1],e=s.enemies[0];h.x=4;h.y=3;E.act(s,h.id,'attack',e.id);assert.equal(e.hp,157);s.enemies.slice(1).forEach(e=>e.hp=0);E.act(s,h.id,'attack',e.id);assert.equal(e.hp,143)});
test('Silence prevents enemy damage for one turn',()=>{const s=E.create(0,['amavelle','amarisa','amanda','amandine']);s.enemies.forEach((e,i)=>{e.x=2;e.y=i+1});E.act(s,'amavelle','skill');const hp=s.allies.map(a=>a.hp);E.end(s);assert.deepEqual(s.allies.map(a=>a.hp),hp);assert.ok(s.enemies.every(e=>e.silence===0));assert.ok(s.allies.every(a=>a.ap===2))});
test('Healing caps at max HP and does not revive',()=>{const s=E.create();s.allies[0].hp=0;s.allies[1].hp-=2;E.act(s,'amada','skill');assert.equal(s.allies[0].hp,0);assert.equal(s.allies[1].hp,s.allies[1].maxhp)});
test('Resonance requires charge and consumes it',()=>{const s=E.create();assert.ok(E.act(s,'amanda','resonance'));s.charge=100;assert.equal(E.act(s,'amanda','resonance'),null);assert.equal(s.charge,0)});
test('All 35 formations can complete all three missions',()=>{
 for(let a=0;a<4;a++)for(let b=a+1;b<5;b++)for(let c=b+1;c<6;c++)for(let d=c+1;d<7;d++)for(let stage=0;stage<3;stage++){
  let s=E.create(stage,[a,b,c,d].map(i=>E.heroes[i].id)),steps=0;
  while(s.status==='playing'&&s.round<35&&steps++<500){
   for(const h of s.allies.filter(h=>h.hp>0)){
    if(s.status!=='playing')break;
    if(s.charge>=100)E.act(s,h.id,'resonance');
    if(s.status!=='playing')break;
    if(h.ap===2&&((h.id==='amada'&&s.allies.some(x=>x.hp>0&&x.hp<x.maxhp-16))||h.id==='amalia'||h.id==='amarisa'||h.id==='amavelle')){E.act(s,h.id,'skill');continue;}
    while(h.ap>0&&s.status==='playing'){
     const targets=s.enemies.filter(e=>e.hp>0).sort((a,b)=>(a.name==='Ancrage'?-100:0)-(b.name==='Ancrage'?-100:0)+E.dist(h,a)-E.dist(h,b));
     const target=targets.find(e=>E.dist(h,e)<=h.range);
     if(target)E.act(s,h.id,'attack',target.id);
     else{const moves=E.reachable(s,h).sort((a,b)=>E.dist(a,targets[0])-E.dist(b,targets[0]));E.act(s,h.id,moves.length?'move':'guard',moves[0]);}
    }
   }
   if(s.status==='playing')E.end(s);
  }
  assert.equal(s.status,'won',`Formation ${[a,b,c,d]} mission ${stage}`);
 }
});
console.log(checks+' rule tests passed, including 105 complete encounters.');
