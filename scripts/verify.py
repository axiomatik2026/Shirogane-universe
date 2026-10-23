#!/usr/bin/env python3
"""Contrôles statiques du projet ; ne remplace pas un test sur appareil Android."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
web=root/'web'
manifest=(root/'android/app/src/main/AndroidManifest.xml').read_text()
js=(web/'app.js').read_text()
for f in ['index.html','app.js','style.css','icon.svg','manifest.webmanifest','sw.js']:
    assert (web/f).is_file(), f
assert 'android.permission.INTERNET' not in manifest
assert 'setDomStorageEnabled(true)' in (root/'android/app/src/main/java/com/sheself/app/MainActivity.java').read_text()
assert 'window.SheSelfAndroid.exportJson(content)' in js
assert len(re.findall(r"(?:scene|scale)\('",js)) >= 30
print('Validation structure, offline et pont export : OK')
