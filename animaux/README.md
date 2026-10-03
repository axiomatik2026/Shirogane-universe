# Échos — les voix du vivant

Application Android en français : touche l’avatar emoji d’un animal pour entendre son cri.

## Installation

[ Télécharger Échos 1.0.0 (APK) ](https://github.com/axiomatik2026/Shirogane-universe/raw/refs/heads/animaux-app/animaux/downloads/Echos-1.0.0.apk)

Android 8.0 ou supérieur. Ouvrir l’APK téléchargé et autoriser l’installation depuis le navigateur si Android le demande.

## Fonctionnalités

15 animaux : chien, chat, loup, lion, grenouille, hibou, oiseau, vache, cheval, cochon, mouton, poule, coq, canard et chauve-souris. Recherche avec ou sans accents, catégories, favoris conservés localement, arrêt du son et remplacement immédiat de la lecture quand on touche un autre animal. Les sons et l’interface sont inclus : aucune connexion, compte, publicité ou permission réseau.

Les avatars utilisent les emojis de l’appareil. La première version couvre une sélection de 15 animaux, pas toutes les espèces. Les effets sonores sont déclarés CC0 par SFXMint ; leur adéquation zoologique est indiquée comme non vérifiée par le fournisseur. Voir [SOURCES.md](SOURCES.md). Catalogue ludique, pas un outil scientifique.

## Compilation

JDK 17, Android SDK Platform 35 et Build Tools 35.0.0 requis.

```bash
export ANDROID_SDK_ROOT=/chemin/du/sdk
bash scripts/build.sh
```

L’APK signé est créé dans `dist/Echos-1.0.0.apk`. La clé privée de signature est exclue de Git : la conserver pour les mises à jour, puis augmenter les versions du manifeste. Une nouvelle clé impose de désinstaller l’ancienne application. `ECHOS_KEYSTORE` permet d’indiquer le chemin de la clé existante. Alias `echos`, mot de passe local `android` : le fichier de clé doit rester privé.

## Vérification

```bash
npm install
npx playwright install chromium
npm test
```

Les tests vérifient la lecture audio dans Chromium, le remplacement et l’arrêt, les favoris après rechargement, la recherche et l’absence de débordement à 320/393/1000 px. Le décodage des 15 fichiers, la signature APK v2/v3 et le manifeste sont vérifiés lors de la livraison. Aucun test sur téléphone physique ou émulateur Android : la lecture native doit être confirmée sur l’appareil.

## Structure et confidentialité

Coque Java Android, WebView local et pont MediaPlayer limité aux 15 identifiants du catalogue. Aucun accès réseau, fichiers externes, analytique ou collecte. Les favoris restent sur l’appareil et sont supprimés lors de la désinstallation. La lecture s’arrête lorsque l’application passe en arrière-plan.

Le projet et l’APK sont publiés dans la branche `animaux-app` du dépôt `Shirogane-universe`.
