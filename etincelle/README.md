# ✦ Étincelle

**Ta prochaine app commence ici.** Une application Android en français qui aide à trouver, préciser et conserver des idées de création d’applications.

## Télécharger

[Installer Étincelle 1.0.0 — APK](https://github.com/axiomatik2026/Shirogane-universe/raw/refs/heads/etincelle-app/etincelle/downloads/Etincelle-1.0.0.apk)

Android 8.0 ou supérieur, avec un Android System WebView à jour. Télécharger l’APK, l’ouvrir et autoriser l’installation depuis l’application utilisée pour le téléchargement si Android le demande. L’APK est signé avec une clé dédiée, sans permission réseau et sans publicité.

## Ce que l’application propose

- **Découvrir** : 30 concepts éditoriaux, 5 catégories, recherche, idée du jour et tirage surprise.
- **Studio** : 150 combinaisons concept × angle, filtres par domaine et difficulté, absence de répétition immédiate lorsqu’une autre combinaison est possible.
- **Fiches** : problème, public, trois fonctions initiales, difficulté, durée indicative de prototype, monétisation à tester, validation et plan de départ.
- **Mes idées** : favoris, notes locales et partage d’une fiche ou du carnet par le menu Android.
- Interface sombre, violet doux et accents menthe. Mise en page adaptée au téléphone et aux grands écrans.

Le moteur fonctionne **entièrement hors ligne**, à partir d’un catalogue rédigé et de cinq angles d’adaptation. Il ne fait pas appel à une IA distante et ne produit pas un nombre illimité de concepts. Les niveaux et durées concernent le concept initial ; une variante peut en changer la complexité. Aucune rentabilité n’est garantie.

## Données et confidentialité

Les favoris et notes restent dans le stockage local de l’application. Aucun compte, suivi publicitaire, analytique, clé API ou permission Internet. La désinstallation ou l’effacement des données supprime le carnet. Utiliser « Partager mon carnet » pour en conserver une copie lisible ; cette version ne permet pas de réimporter cette copie. Le partage n’a lieu qu’après une action de l’utilisateur via le menu système.

## Architecture

Coque Android en Java et interface HTML/CSS/JavaScript intégrée dans les assets de l’APK. Aucun site distant. Le pont JavaScript ne fournit que le partage de texte ; les navigations externes sont bloquées et les accès fichiers génériques désactivés.

```
app/src/main/
  AndroidManifest.xml
  java/com/axiomatik/etincelle/MainActivity.java
  assets/{index.html,style.css,ideas.js,app.js}
  res/{drawable/ic_launcher.xml,values/styles.xml}
scripts/build.sh
tests/ui.cjs
```

## Compiler l’APK sous Linux

Prérequis : JDK 17, Android SDK Platform 35, Build Tools 35.0.0, zip et bash.

```bash
sdkmanager 'platforms;android-35' 'build-tools;35.0.0'
export ANDROID_SDK_ROOT=/chemin/du/sdk
bash scripts/build.sh
```

Résultat : `dist/Etincelle-1.0.0.apk`, signé et vérifié, et `dist/SHA256SUMS.txt`.

La première compilation crée une clé privée dans `.signing/etincelle.jks`, exclue de Git. La clé de l’APK livré est conservée séparément pour le propriétaire, dans l’archive privée de maintenance. Pour publier une mise à jour compatible, réutiliser cette clé avec `ETINCELLE_KEYSTORE`, puis augmenter `versionCode` et `versionName`. Le mot de passe de la clé locale est `android`, alias `etincelle` ; la confidentialité du fichier est indispensable. Ne jamais publier ce fichier. Une compilation avec une autre clé ne pourra pas mettre à jour l’installation existante.

## Tester l’interface

```bash
npm install
npx playwright install chromium
npm test
```

`CHROME_EXECUTABLE_PATH` permet d’utiliser un Chrome installé. Les tests vérifient le catalogue, les filtres et la recherche, les favoris et notes après rechargement, le Studio, les cas sans résultat, l’échappement des notes, le retour et les largeurs 320/393/1000 px. Les captures de contrôle sont écrites dans `/tmp`.

## Vérifications de la version livrée

- Compilation Java et DEX, assemblage des ressources et alignement de l’APK réussis.
- Signature APK v2/v3 vérifiée avec `apksigner`.
- Manifeste : Android minimum API 26, cible API 35, aucune permission demandée.
- Tests Playwright réussis et rendu mobile inspecté.
- Pas de test sur téléphone physique ou émulateur Android dans cet environnement. Le partage Android, les insets et le cycle de vie devront être confirmés sur l’appareil.

Le projet est publié dans le dossier `etincelle` de la branche `etincelle-app` du dépôt Shirogane-universe. La branche `main` n’est pas modifiée.
