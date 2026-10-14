const fs=require('node:fs');
let html=fs.readFileSync('dist/index.html','utf8');
html=html.replace('<link rel="stylesheet" href="style.css">','<style>'+fs.readFileSync('dist/style.css','utf8')+'</style>');
html=html.replace('<script src="engine.js" defer></script><script src="game.js" defer></script>','');
html=html.replace('</body>','<script>'+fs.readFileSync('dist/engine.js','utf8')+'</script><script>'+fs.readFileSync('dist/game.js','utf8')+'</script></body>');
fs.writeFileSync('dist/shirogane-resonances.html',html);
console.log('Standalone demo written: '+Buffer.byteLength(html)+' bytes');
