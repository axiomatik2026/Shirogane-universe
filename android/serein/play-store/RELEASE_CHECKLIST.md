# Serein — dossier candidat Google Play

**Statut : préparé pour revue, non téléversé et non publié.**

Date des vérifications : 20 octobre 2026. Prix demandé : achat unique de **2,00 USD**, sans abonnement, sans publicité et sans achat intégré. Le tarif est à configurer dans Play Console ; il n’est pas activé par la présence du fichier `monetization.json`.

## Obstacles actuellement ouverts

1. **Compte et pays marchand inconnus.** La liste officielle Google affiche le Sénégal comme éligible à l’inscription développeur mais non à l’inscription marchand. Un compte marchand sénégalais ne permet donc pas de mettre en œuvre cette vente via Google Play. Il faut un éditeur réellement établi dans un pays pris en charge ; ne pas inventer d’adresse ou de pays de résidence. Référence : https://support.google.com/googleplay/android-developer/answer/9306917?hl=en.
2. **Type de compte.** La page Google « Choose a developer account type » demande un compte d’organisation pour les applications approuvées utilisant VpnService. Vérifier l’identité de l’organisation et son D-U-N-S lorsqu’il est requis. La version actuelle de Serein utilise cette API. Référence : https://support.google.com/googleplay/android-developer/answer/13634885?hl=en.
3. **Service DNS à autoriser pour la distribution commerciale.** La page des filtres gratuits CleanBrowsing les présente comme destinés aux parents et non aux organisations. L’usage personnel déjà testé ne vaut pas autorisation de redistribuer un service dans une app payante. Confirmer par écrit un accord adapté ou choisir un fournisseur autorisant cet usage. Aucun message n’a été envoyé à CleanBrowsing. Références : https://cleanbrowsing.org/filters/ et https://cleanbrowsing.org/solutions/app-developers/.
4. **Identité et support à renseigner.** Nom légal de l’éditeur, adresse/contact requis, e-mail support choisi, URL publique de confidentialité et profil de paiement. Aucun e-mail personnel n’a été publié automatiquement.
5. **Revue des autorisations.** Déclarations AccessibilityService, VpnService et foreground services à déposer avec descriptions exactes et vidéo de démonstration propre. La présence de dialogues de consentement dans le code ne constitue pas une approbation Google.
6. **Qualité à valider sur plusieurs appareils.** Les tests locaux ne remplacent pas les essais réels (réseaux Wi-Fi/mobile, refus d’autorisation, coupures, économie d’énergie, fermeture et redémarrage). La démonstration Pinterest fournie par l’utilisateur valide un cas sur son téléphone, pas toutes les applications.
7. **Visuels de boutique manquants.** Icône haute résolution, visuel promotionnel et captures propres de l’application. Aucune image n’a été générée, et la vidéo personnelle de démonstration n’a pas été publiée.

## Éléments préparés

- Version Android 1.3.0-rc1, versionCode 5, compileSdk/targetSdk 36, avec tutoriels et notice de confidentialité fonctionnelle accessibles hors ligne.
- Fiche française dans `listing-fr.json`, proposition tarifaire dans `monetization.json`.
- Brouillons de déclarations dans `PERMISSIONS_AND_DATA.md` et texte de confidentialité à compléter dans `PRIVACY_POLICY_DRAFT.md`.
- Script reproductible `build-bundle.sh` et App Bundle candidat signé avec la clé locale existante.
- APK candidat pour essais. La version personnelle validée 1.2.0 reste dans le dossier releases de la branche stable.

## Avant la première soumission

1. Confirmer l’éditeur réel, le pays du compte et le profil marchand éligible.
2. Confirmer le contrat ou les conditions de distribution commerciale du filtrage DNS et leur coût. Réévaluer l’adéquation d’un achat unique à ce coût récurrent, sans changer le prix demandé sans décision de l’éditeur.
3. Décider Play App Signing. La clé de signature actuelle ne doit jamais être commitée. Si Google génère une autre clé de signature finale, les APK installés manuellement pourront nécessiter une désinstallation avant la version boutique. L’AAB peut utiliser une clé de téléversement distincte ; ne pas supposer que la signature d’un AAB devient la clé des APK servis par Google.
4. Renseigner les coordonnées de support et publier une politique de confidentialité publique, cohérente avec la notice intégrée. Valider les catégories de données DNS/IP dans Data safety. Ne pas cocher « aucune donnée collectée » par défaut.
5. Créer l’app dans Play Console comme **payante**, prix de base demandé 2,00 USD, puis vérifier les conversions locales/taxes et les pays de distribution. Aucune bibliothèque Play Billing n’est nécessaire pour un achat payé avant téléchargement ; ne pas ajouter d’abonnement.
6. Remplir les déclarations d’accessibilité et de VPN, les services au premier plan, le questionnaire de classification et le public cible réel. Le candidat propose un usage adulte de maîtrise de sa navigation ; ne pas le déclarer comme outil destiné au handicap ou promettre un contrôle parental inviolable.
7. Préparer une vidéo dédiée montrant les consentements, autorisations manuelles, blocage de recherche et désactivation. Ne pas réutiliser les captures personnelles contenant applications, messages, identité ou informations sensibles sans autorisation spécifique.
8. Exécuter les tests internes/fermés nécessaires au compte. Google impose notamment 12 testeurs inscrits pendant 14 jours continus pour certains nouveaux comptes personnels ; cela ne résout pas la condition de compte d’organisation applicable à VpnService. Référence : https://support.google.com/googleplay/android-developer/answer/14151465?hl=en.
9. Téléverser le candidat, traiter les erreurs de console, lire le rapport de pré-lancement, puis soumettre pour revue. Ne pas présenter ce dossier comme une publication effective ou une garantie d’approbation.

## API cible

Le candidat cible Android 16 (API 36), conformément à l’exigence applicable aux nouvelles applications depuis le 31 août 2026. Référence : https://developer.android.com/google/play/requirements/target-sdk. Les comportements conditionnés par cette cible doivent être vérifiés sur appareil ; la compilation et la structure AAB ne remplacent pas cette validation.

## Vérifications techniques

Les signatures, la structure AAB et la reconstruction d’un APK à partir du bundle sont vérifiées lors de la préparation. L’absence de bibliothèque native évite un paquet natif embarqué à aligner pour les pages mémoire 16 Ko ; cela ne dispense pas des essais Android.

Les 115 assertions existantes et 20 000 entrées aléatoires de parseurs sont conservées. Le test local du tutoriel n’effectue aucune requête réseau et ne conserve pas la saisie.
