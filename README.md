# SHE:SELF — Ta vie, à ton image

**Application Android et web, hors ligne, sans compte ni publicité**, qui produit une boussole de préférences à partir d'un questionnaire original et construit un programme personnel de micro-actions et un journal privé.

## Télécharger l'APK

Dans l'onglet [Actions](../../actions/workflows/sheself-android.yml), sélectionner la dernière exécution **SHESELF Android APK** réussie, puis télécharger l'artifact **SHESELF-Android-v1-debug-APK** et extraire `app-debug.apk`.

> **Attention :** l'APK produit automatiquement est une version *debug*, adaptée au test et signée avec une clé de développement générée par le runner. Il ne constitue pas une version de production signée durablement ou publiable telle quelle sur Google Play.

## Architecture

```text
web/                             Application web source unique
  index.html                     Entrée / balisage
  app.js                         Questionnaire, calcul, personnalisation, carnet
  style.css                      Interface responsive
  icon.svg                       Logo vectoriel
  manifest.webmanifest, sw.js    Installation et cache PWA en HTTPS
android/
  app/build.gradle               Version Android / build / source web partagée
  app/src/main/AndroidManifest.xml
  app/src/main/java/com/sheself/app/MainActivity.java
                                   WebView offline + import/export Android
  app/src/main/res/               Nom, thème, icône lanceur
.github/workflows/               Tests et génération automatique APK
scripts/                          Validation du projet
 docs/                            Recherche, architecture et précautions
```

## Développement web

Ouvrir `web/index.html` dans le navigateur ou utiliser un serveur statique pour tester l'installation PWA. Un serveur HTTPS est nécessaire au service worker. Aucune police distante obligatoire.

## Développement Android

Prérequis : Android SDK 35, JDK 17, Gradle 8.10.2. Dans `android/` : `gradle :app:assembleDebug`. Les ressources `web/` sont embarquées dans l'APK automatiquement ; il n'y a pas deux versions du questionnaire à maintenir.

**APK minimum Android 7.0 (API 24), cible API 35.** Le mode hors-ligne ne demande pas de permission Internet. L'export et l'import JSON utilisent le sélecteur de documents Android ; sur navigateur web, le téléchargement JSON standard est conservé.

## Données / transparence

Les préférences, réponses, tâches et notes sont conservées dans `localStorage` de la WebView Android (ou du navigateur). Elles sont privées **tant que le système/appareil lui-même reste protégé** ; elles ne sont pas chiffrées par l'application. Aucune donnée ne part vers une API. Aucun diagnostic clinique : les 10 dimensions et questions originales ne sont pas des instruments psychométriques validés. Voir `docs/RECHERCHE_ET_CONCEPTION.md` et `docs/ARCHITECTURE.md`.

## Livrable

Version 1.0.0, octobre 2026. Source sous licence à définir avant diffusion commerciale.
