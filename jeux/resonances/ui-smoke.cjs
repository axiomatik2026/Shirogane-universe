// Interface smoke test with a small DOM substitute, not a browser rendering test.
const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const elements=new Map();
function scan(html){for(const m of html.matchAll(/\bid="([^"]+)"/g))if(!elements.has(m[1]))elements.set(m[1],new Element(m[1]));}
class Element{
 constructor(id){this.id=id;this.style={};this.dataset={};this.open=false;this.disabled=false;this.classList={add(){},remove(){}};this._html='';}
 set innerHTML(s){this._html=s;scan(s)}get innerHTML(){return this._html}
 querySelectorAll(){return []}setAttribute(){}showModal(){this.open=true}close(){this.open=false}
}
scan(fs.readFileSync('dist/index.html','utf8'));
const ctx={console,document:{getElementById:id=>elements.get(id)||null,addEventListener(){}},localStorage:{getItem(){return null},setItem(){}},setTimeout:()=>1,clearTimeout(){}};ctx.window=ctx;vm.createContext(ctx);
vm.runInContext(fs.readFileSync('dist/engine.js','utf8'),ctx);
vm.runInContext(fs.readFileSync('dist/game.js','utf8'),ctx);
assert.ok(elements.get('arena').innerHTML.includes('viewBox="0 0 120 170"'));
elements.get('start').onclick();
for(let i=0;i<3;i++)elements.get('nextLine').onclick();
assert.equal(vm.runInContext('s.status',ctx),'playing');
for(const id of ['amanda','amandine','amada','amalia','amarilys','amarisa','amavelle']){
 vm.runInContext(`s=Shiro.create(0,['${id}',...heroes.map(h=>h.id).filter(x=>x!=='${id}').slice(0,3)]);selected='${id}';render();`,ctx);
 assert.ok(!elements.get('arena').innerHTML.includes('NaN'));
 elements.get('skill').onclick();
 vm.runInContext('s=null;render()',ctx);
}
for(const f of ['help()','sources()','codex()','teamPicker()']){vm.runInContext(f,ctx);assert.ok(elements.get('modal').open);elements.get('closeModal').onclick();}
for(let i=0;i<3;i++){vm.runInContext(`stage=${i};s=Shiro.create(stage);s.status='won';render();`,ctx);assert.ok(elements.get('missionInfo').innerHTML.includes('LOI DU DOMAINE'));}
console.log('OK initial screen, dialogue-to-combat, seven techniques, three missions, codex, help, source notes, formation picker.');
