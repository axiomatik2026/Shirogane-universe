# Shirogane Universe — Wiki Android

Une encyclopédie hors connexion construite à partir des dix tomes PDF fournis par l’auteur.

## Installer sur Android

L’APK complète est fournie en téléchargement privé dans la conversation. Elle contient le texte intégral des tomes et n’est pas publiée dans ce dépôt public.

Android 8.0 ou version ultérieure. Ouvrir l’APK et autoriser l’installation depuis le navigateur ou le gestionnaire de fichiers si Android le demande. L’application utilise Android System WebView et ne demande aucune permission réseau.

## Contenu

- 177 dossiers : 62 personnages, 14 arcs, 36 combats, 17 mondes et lieux, 19 artefacts, 15 lois et pouvoirs, 14 factions et menaces.
- 1 259 chapitres et bonus issus de 10 tomes, représentant 8 791 pages PDF.
- Synthèses, relations cliquables et références par tome, chapitre et page ; la version privée ajoute les passages d’apparence, de pouvoirs et d’évolution.
- Recherche dans les fiches et le texte intégral, lecture par page, favoris, marques de lecture, taille de texte et thèmes clair/sombre.
- Les noms et limites des arcs sont des regroupements éditoriaux de lecture. Les résultats de combats sont indiqués uniquement lorsqu’ils sont documentés.

Le dossier `dist/` contient une édition publique avec les synthèses et les références. Les passages originaux, le corpus de lecture et l’APK restent privés. Exécuter les scripts avec les PDF de l’auteur pour régénérer localement le corpus complet.

## Structure

- `dist/` : application web autonome et corpus JavaScript.
- `android/` : source de l’enveloppe Android.
- `scripts/extract.py` : extraction des PDF avec PyMuPDF.
- `scripts/compile_wiki.py` : synthèses éditoriales, références et index des dossiers.
- Les binaires, les PDF et les clés de signature sont exclus du dépôt public.

## Régénérer le corpus

```sh
python3 -m pip install pymupdf
SHIROGANE_PDF_DIR=/chemin/vers/les/pdf python3 scripts/extract.py
python3 scripts/compile_wiki.py
```

Les PDF doivent être nommés `Tome_1.pdf` à `Tome_9.pdf` et `Tome_10_Spin_off.pdf`. Le fichier intermédiaire `corpus.json` n’est pas publié ; les textes extraits nécessaires à l’application se trouvent dans `dist/data/`.

## Compiler Android

Installer JDK 17, Android SDK Platform 35 et Build Tools 35.0.0, puis :

```sh
ANDROID_SDK_ROOT=/chemin/du/sdk bash android/scripts/build.sh
```

La clé de signature est privée et exclue de Git. Conserver la même clé pour les mises à jour Android. Les clés sont créées avec les valeurs de développement indiquées dans le script ; une publication sur un store doit utiliser une configuration de signature adaptée.

## Validation de la version 1.0.0

Syntaxe JavaScript ; intégrité des 1 259 chapitres ; résolution des références et des liens ; tests DOM des fiches, de la navigation, des favoris, du lecteur, du spin-off et de la recherche intégrale. APK compilée, alignement vérifié et signature v2/v3 vérifiée avec les outils Android. Installation sur appareil physique non vérifiée dans cet environnement.

Aucune image générée. Les textes, noms et concepts appartiennent à leur auteur.
