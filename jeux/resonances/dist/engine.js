/* Shirogane Universe — deterministic tactical rules, browser and Node. */
(function(root){
'use strict';
const heroes=[
 {id:'amanda',name:'Amanda',role:'Vérité humaine',color:'#c33b60',hair:'#a82342',eye:'#d82b48',dress:'#a82245',weapon:'Véritas',skill:'Vérité révélée',desc:'22 dégâts, ignore les protections. Révèle la cible : +8 dégâts reçus pendant 2 tours.',hp:86,atk:16,range:2,move:3,pair:'amandine'},
 {id:'amandine',name:'Amandine',role:'Liberté émotionnelle',color:'#b98724',hair:'#eab949',eye:'#edb332',dress:'#242738',weapon:'Héliona',skill:'Flèche solaire',desc:'28 dégâts à distance. Portée 6 cases.',hp:72,atk:14,range:5,move:4,pair:'amanda'},
 {id:'amada',name:'Amada',role:'Vie humaine',color:'#34866e',hair:'#398564',eye:'#258a69',dress:'#397f60',weapon:'Lifebloom',skill:'Floraison vivante',desc:'Soigne toute l’équipe de 20 PV et immobilise les ennemis proches pendant leur prochain tour.',hp:80,atk:13,range:3,move:3,pair:'amalia'},
 {id:'amalia',name:'Amalia',role:'Harmonie humaine',color:'#4274be',hair:'#264e9d',eye:'#387ce0',dress:'#202b64',weapon:'Samsara',skill:'Cycle protecteur',desc:'Accorde 15 points de protection et soigne 8 PV à chaque alliée.',hp:78,atk:13,range:4,move:3,pair:'amada'},
 {id:'amarilys',name:'Amarilys',role:'Perfection humaine',color:'#8d708e',hair:'#e5e7f2',eye:'#b92d4d',dress:'#272433',weapon:'Seijun',skill:'Coupe parfaite',desc:'38 dégâts à 2 cases, ignore les protections. Une ouverture, un seul geste.',hp:76,atk:20,range:1,move:4,pair:'amarisa'},
 {id:'amarisa',name:'Amarisa',role:'Logique humaine',color:'#8050b1',hair:'#694093',eye:'#a36cdc',dress:'#613683',weapon:'Logion',skill:'Grille logique',desc:'Inflige 10 dégâts à tous les ennemis et les révèle pendant 2 tours : +8 dégâts reçus.',hp:68,atk:14,range:4,move:3,pair:'amarilys'},
 {id:'amavelle',name:'Amavelle',role:'Silence humain',color:'#695480',hair:'#735087',eye:'#c73554',dress:'#292433',weapon:'Silentium',skill:'Champ de silence',desc:'Inflige 12 dégâts à tous les ennemis et empêche leurs attaques au prochain tour.',hp:94,atk:21,range:1,move:3,pair:null}
];
const chapters=[
 {name:'Le seuil blanc',label:'01 · Première résonance',rule:'Les fragments avancent vers l’alliée la plus proche. Leurs intentions sont visibles.',objective:'Neutraliser les trois fragments.',enemies:[['Fragment',4,1,38],['Fragment',5,3,38],['Fragment',4,5,38]]},
 {name:'L’écho des gestes',label:'02 · La contradiction',rule:'L’Écho réduit de moitié les dégâts d’une même héroïne lorsqu’elle le frappe deux fois de suite. Alternez vos attaquantes.',objective:'Déjouer l’Écho et ses fragments.',enemies:[['Écho',5,3,115],['Fragment',4,0,38],['Fragment',5,6,38]]},
 {name:'Une seule cohérence',label:'03 · Le cœur de la faille',rule:'Le Noyau réduit les dégâts de 80 % tant qu’un Ancrage subsiste. Détruisez les deux cristaux avant de le frapper.',objective:'Briser les Ancrages, puis neutraliser le Noyau.',enemies:[['Noyau',5,3,160],['Ancrage',4,0,48],['Ancrage',4,6,48]]}
];
const clone=x=>JSON.parse(JSON.stringify(x));
const dist=(a,b)=>Math.abs(a.x-b.x)+Math.abs(a.y-b.y);
function create(stage=0,team=['amanda','amandine','amada','amalia']){
 if(!Number.isInteger(stage)||!chapters[stage]||team.length!==4||new Set(team).size!==4||team.some(id=>!heroes.some(h=>h.id===id)))throw Error('Formation invalide');
 return {stage,round:1,charge:0,status:'playing',log:['L’Intermonde attend votre premier mouvement.'],allies:team.map((id,i)=>({...clone(heroes.find(h=>h.id===id)),x:i%2,y:1+Math.floor(i/2)*3,maxhp:heroes.find(h=>h.id===id).hp,ap:2,shield:0})),enemies:chapters[stage].enemies.map((e,i)=>({id:'e'+i,name:e[0],x:e[1],y:e[2],hp:e[3],maxhp:e[3],atk:e[0]==='Noyau'?17:e[0]==='Écho'?14:10,root:0,silence:0,reveal:0,last:null})),events:[]};
}
function note(s,t){s.log.push(t);s.log=s.log.slice(-20)}
function living(s){return s.allies.filter(h=>h.hp>0)}
function occupied(s,x,y){return [...s.allies,...s.enemies].some(h=>h.hp>0&&h.x===x&&h.y===y)}
function reachable(s,h){const seen=new Set([h.x+','+h.y]),q=[[h.x,h.y,0]],out=[];for(let i=0;i<q.length;i++){let [x,y,d]=q[i];if(d>=h.move)continue;for(let [a,b] of [[x+1,y],[x-1,y],[x,y+1],[x,y-1]]){let k=a+','+b;if(a<0||b<0||a>6||b>6||seen.has(k)||occupied(s,a,b))continue;seen.add(k);q.push([a,b,d+1]);out.push({x:a,y:b});}}return out;}
function damage(s,e,n,actor,ignore=false){if(e.hp<=0)return;if(e.reveal)n+=8;if(e.name==='Noyau'&&s.enemies.some(x=>x.name==='Ancrage'&&x.hp>0))n=Math.max(1,Math.round(n*.2));if(e.name==='Écho'&&e.last===actor&&!ignore)n=Math.round(n/2);e.last=actor;e.hp=Math.max(0,e.hp-n);s.events.push({id:e.id,text:'−'+n});}
function check(s){if(s.enemies.every(e=>e.hp<=0))s.status='won';else if(living(s).length===0)s.status='lost';}
function act(s,id,kind,target){
 s.events=[];const h=s.allies.find(a=>a.id===id);if(s.status!=='playing'||!h||h.hp<=0)return 'Choisissez une héroïne active.';
 if(kind==='resonance'){
  if(s.charge<100)return 'La Résonance nécessite 100 points.';
  s.charge=0;s.enemies.forEach(e=>damage(s,e,32,'resonance',true));living(s).forEach(a=>a.hp=Math.min(a.maxhp,a.hp+12));note(s,'Résonance collective ! 32 dégâts et 12 PV restaurés.');check(s);return null;
 }
 let cost=kind==='skill'?2:1;if(h.ap<cost)return 'Pas assez de points d’action. Choisissez une sœur ou terminez le tour.';
 if(kind==='move'){
  if(!target||!reachable(s,h).some(p=>p.x===target.x&&p.y===target.y))return 'Cette case est inaccessible.';
  h.x=target.x;h.y=target.y;note(s,h.name+' se repositionne.');
 }else if(kind==='guard'){h.shield+=14;note(s,h.name+' se protège (+14).');}
 else if(kind==='attack'||kind==='skill'){
  const global=kind==='skill'&&['amada','amalia','amarisa','amavelle'].includes(id);
  const e=s.enemies.find(e=>e.id===target&&e.hp>0);
  const range=kind==='skill'?(id==='amandine'?6:2):h.range;
  if(!global&&(!e||dist(h,e)>range))return 'Cible hors de portée. Déplacez-vous d’abord.';
  if(kind==='attack'){damage(s,e,h.atk,id);note(s,h.name+' frappe avec '+h.weapon+'.');}
  else {
   if(id==='amanda'){damage(s,e,22,id,true);e.reveal=2;}
   if(id==='amandine')damage(s,e,28,id);
   if(id==='amarilys')damage(s,e,38,id,true);
   if(id==='amada'){living(s).forEach(a=>{a.hp=Math.min(a.maxhp,a.hp+20);s.events.push({id:a.id,text:'+20'});});s.enemies.filter(e=>dist(e,h)<=4).forEach(e=>e.root=1);}
   if(id==='amalia')living(s).forEach(a=>{a.shield+=15;a.hp=Math.min(a.maxhp,a.hp+8);});
   if(id==='amarisa')s.enemies.forEach(e=>{damage(s,e,10,id);e.reveal=2;});
   if(id==='amavelle')s.enemies.forEach(e=>{damage(s,e,12,id);e.silence=1;});
   note(s,h.name+' · '+h.skill+'.');
  }
 }else return 'Action inconnue.';
 h.ap-=cost;s.charge=Math.min(100,s.charge+(kind==='move'?5:12)+(living(s).some(a=>a.id===h.pair&&dist(a,h)<=3)?6:0));check(s);return null;
}
function intent(s,e){if(e.silence)return 'Silence · aucune attaque';if(e.name==='Ancrage')return 'Maintient le bouclier';let a=living(s).sort((a,b)=>dist(e,a)-dist(e,b))[0];return a?'Vise '+a.name+' · '+e.atk+' dégâts':'—';}
function end(s){
 if(s.status!=='playing')return;s.events=[];
 for(const e of s.enemies.filter(e=>e.hp>0)){
  if(e.reveal)e.reveal--;if(e.silence){e.silence--;e.root=0;note(s,e.name+' ne peut pas attaquer.');continue;}if(e.name==='Ancrage')continue;
  let target=living(s).sort((a,b)=>dist(e,a)-dist(e,b))[0];if(!target)break;
  if(!e.root){for(let i=0;i<2&&dist(e,target)>1;i++){let options=[[e.x+1,e.y],[e.x-1,e.y],[e.x,e.y+1],[e.x,e.y-1]].map(([x,y])=>({x,y})).filter(p=>p.x>=0&&p.x<7&&p.y>=0&&p.y<7&&!occupied(s,p.x,p.y)).sort((a,b)=>dist(a,target)-dist(b,target));if(options[0]&&dist(options[0],target)<dist(e,target)){e.x=options[0].x;e.y=options[0].y;}else break;}}
  e.root=0;if(dist(e,target)<=(e.name==='Noyau'?4:1)){let absorbed=Math.min(target.shield,e.atk);target.shield-=absorbed;const n=e.atk-absorbed;target.hp=Math.max(0,target.hp-n);s.events.push({id:target.id,text:n?'−'+n:'Protégée'});note(s,e.name+' → '+target.name+' : '+n+' dégâts.');}
 }
 check(s);if(s.status==='playing'){s.round++;living(s).forEach(h=>h.ap=2);note(s,'Tour '+s.round+' · vos points d’action sont restaurés.');}
}
root.Shiro={heroes,chapters,create,dist,reachable,act,end,intent};
if(typeof module!=='undefined')module.exports=root.Shiro;
})(typeof window==='undefined'?globalThis:window);
