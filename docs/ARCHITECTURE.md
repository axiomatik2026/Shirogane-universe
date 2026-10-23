# Architecture et décisions — SHE:SELF v1.0.0

## 1. Couches

- **UI responsive** (`web/index.html`, `style.css`) : mêmes écrans sur navigateur et Android.
- **Domaine** (`web/app.js`) : banques de questions et de micro-actions, calcul de 10 axes, personnalisation selon les priorités et contraintes, bilan, carnet.
- **Persistance** (`localStorage`) : données enregistrées localement sous une clé versionnée ; export/import JSON volontaire.
- **Conteneur Android** (`MainActivity.java`) : WebView de ressources uniquement locales, pont JS strictement limité à l'export JSON, sélecteur Android pour importer/exporter.
- **CI** : contrôle syntaxique de JS puis compilation et publication d'un APK de test via GitHub Actions.

## 2. Contrat de données

`sheself_v1` : `{ version: 1, profile, settings, answers, tasks, journal, createdAt }`.
Aucune information ne quitte l'appareil. Le JSON d'export n'est pas chiffré. Pas de synchronisation ni de création de compte.

## 3. Confidentialité, limites et risques

- Aucune permission `INTERNET`, aucun appel à une API ni SDK analytique.
- `allowUniversalAccessFromFileURLs=false`, `allowFileAccessFromFileURLs=false` pour limiter les privilèges du document local ; `allowFileAccess=true` uniquement pour charger les ressources de l'APK.
- L'import JSON est plafonné à 2 Mo côté web et est soumis à confirmation de remplacement.
- Contrôles psychométriques exploratoires, ne pas présenter les profils comme des catégories biologiques de femmes.
- Modèle de menace restant : des applications malveillantes avec accès au terminal, sauvegardes d'exports, appareils compromis, pertes des données si application supprimée.

## 4. Améliorations avant sortie publique

1. Tests automatiques de calcul du score (sens de chaque item, inversions, équité des pondérations).
2. Études volontaires : entretiens cognitifs, validation de la structure factorielle, fidélité test/retest, mesure des biais selon âge/langue/culture.
3. Accessibilité : test lecteur d'écran, contraste WCAG, contenu en langue locale.
4. Migrations de schéma de données, support multilingue, options de chiffrement local.
5. APK/AAB de production signé avec une clé gérée de façon sécurisée et politique de confidentialité accessible.
