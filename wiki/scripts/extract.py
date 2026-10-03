import fitz,pathlib,re,json,collections,os
ROOT=pathlib.Path(__file__).resolve().parents[1]; OUT=ROOT/'dist/data';OUT.mkdir(parents=True,exist_ok=True)
books=[]; allchap=[]
for n in range(1,11):
 file=f'Tome_{n}.pdf' if n<10 else 'Tome_10_Spin_off.pdf'; doc=fitz.open(str(pathlib.Path(os.environ.get('SHIROGANE_PDF_DIR','upload'))/file)); chapters=[]; cur=None; sub=0
 for page,p in enumerate(doc,1):
  text=p.get_text().strip();lines=text.splitlines(); title=[]
  if lines and re.match(r'^\d+: chap',lines[0],re.I):
   for b in p.get_text('dict')['blocks']:
    for l in b.get('lines',[]):
     for s in l['spans']:
      if s['size']>=23:title.append(s['text'])
   heading=' '.join(title).strip();heading=re.sub(r'^\d+:\s*','',heading)
   if n==10 and re.match(r'Chapitre 1\s*:',heading):sub+=1
   cur={'id':f't{n}-c{len(chapters)+1}','tome':n,'title':heading,'start':page,'end':page,'part':sub,'pages':[]};chapters.append(cur)
  if cur:
   cur['end']=page;cur['pages'].append({'page':page,'text':text})
 book={'id':n,'name':'Tome '+str(n)+(' · Spin-off' if n==10 else ''),'file':file,'pages':len(doc),'chapters':[ {k:v for k,v in c.items() if k!='pages'} for c in chapters]}
 books.append(book);allchap.extend(chapters)
 (OUT/f'tome-{n}.js').write_text('window.SH_BOOKS['+str(n)+']='+json.dumps(chapters,ensure_ascii=False,separators=(',',':'))+';')
(ROOT/'corpus.json').write_text(json.dumps(allchap,ensure_ascii=False))
(OUT/'catalog.js').write_text('window.SH_BOOKS={};window.SH_CATALOG='+json.dumps(books,ensure_ascii=False,separators=(',',':'))+';')
print('Chapters:',len(allchap),'Pages:',sum(b['pages'] for b in books))
print('\n'.join(f'{b["name"]}: {len(b["chapters"])} chapitres' for b in books))
# names frequent multiword sequences for editorial review
freq=collections.Counter()
for c in allchap:
 t=' '.join(p['text'] for p in c['pages']);freq.update(re.findall(r'\b[A-ZÀ-Ý][a-zà-ÿ]+(?: [A-ZÀ-Ý][a-zà-ÿ]+){1,2}\b',t))
print(freq.most_common(100))
