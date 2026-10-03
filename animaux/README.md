# Échos — les voix du vivant

Application Android en français : touche l’avatar emoji d’un animal pour entendre son cri.

## Installation

[ Télécharger Échos 1.1.0 (APK) ](https://github.com/axiomatik2026/Shirogane-universe/raw/refs/heads/animaux-app/animaux/downloads/Echos-1.1.0.apk)

Android 8.0 ou supérieur. Ouvrir l’APK téléchargé et autoriser l’installation depuis le navigateur si Android le demande.

## Fonctionnalités

35 animaux : les 15 d’origine, plus éléphant, tigre, ours, bourdon, chameau, âne, chèvre, corbeau, cerf, dauphin, singe hurleur, paon, manchot, pigeon, otarie, serpent à sonnette, oie, dindon, baleine et lapin. Deux nouvelles catégories : Marins et Insectes. Recherche avec ou sans accents, catégories, favoris conservés localement, arrêt du son et remplacement immédiat de la lecture quand on touche un autre animal. Les sons et l’interface sont inclus : aucune connexion, compte, publicité ou permission réseau.

Les avatars utilisent les emojis de l’appareil. Le catalogue couvre une sélection de 35 animaux, pas toutes les espèces. Les 15 sons initiaux sont des effets synthétiques SFXMint sous CC0, dont l’exactitude zoologique n’est pas vérifiée. Les 20 ajouts utilisent des enregistrements Wikimedia Commons sous CC0, domaine public, CC BY ou CC BY-SA. Les crédits, licences et modifications des extraits sont consultables hors connexion dans l’application. Voir [SOURCES.md](SOURCES.md). Catalogue ludique, pas un outil scientifique.

## Compilation

JDK 17, Android SDK Platform 35 et Build Tools 35.0.0 requis.

```bash
export ANDROID_SDK_ROOT=/chemin/du/sdk
bash scripts/build.sh
```

L’APK signé est créé dans `dist/Echos-1.1.0.apk`. La clé privée de signature est exclue de Git : la conserver pour les mises à jour, puis augmenter les versions du manifeste. Une nouvelle clé impose de désinstaller l’ancienne application. `ECHOS_KEYSTORE` permet d’indiquer le chemin de la clé existante. Alias `echos`, mot de passe local `android` : le fichier de clé doit rester privé.

## Vérification

```bash
npm install
npx playwright install chromium
npm test
```

Les tests vérifient la lecture audio dans Chromium, le remplacement et l’arrêt, les favoris après rechargement, la recherche et l’absence de débordement à 320/393/1000 px. Le décodage des 35 fichiers, la signature APK v2/v3 et le manifeste sont vérifiés lors de la livraison. Aucun test sur téléphone physique ou émulateur Android : la lecture native doit être confirmée sur l’appareil.

## Structure et confidentialité

Coque Java Android, WebView local et pont MediaPlayer limité aux 35 identifiants du catalogue. Aucun accès réseau, fichiers externes, analytique ou collecte. Les favoris restent sur l’appareil et sont supprimés lors de la désinstallation. La lecture s’arrête lorsque l’application passe en arrière-plan.

Le projet et l’APK sont publiés dans la branche `animaux-app` du dépôt `Shirogane-universe`.

## Mise à jour 1.1.0

20 animaux ajoutés. Version Android 2, même clé de signature que 1.0.0 : installer par-dessus pour conserver les favoris. Les fichiers CC BY-SA adaptés restent sous leur licence source ; leur inclusion ne change pas la licence du reste de l’application. `audio-provenance.json` conserve les sources, transformations et empreintes des nouveaux extraits.
