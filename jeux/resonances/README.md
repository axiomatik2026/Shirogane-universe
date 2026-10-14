# Shirogane Universe — Résonances

Démo tactique 2D en français, pour souris, clavier et écran tactile. Sept Ama Girls jouables, sélection de quatre héroïnes, trois rencontres, Résonance collective, dialogues d’Amado et Rebecca, codex et sauvegarde locale des missions terminées.

## Jouer

Ouvrir `dist/index.html` dans un navigateur, ou télécharger `dist/shirogane-resonances.html`, version autonome hors ligne. Aucun serveur, compte ou dépendance requis pour jouer localement. L’hébergement Sites peut demander la connexion du propriétaire.

## Contrôles

- Cliquer/toucher une carte ou un personnage pour le sélectionner.
- Déplacer : 1 PA, puis choisir une case accessible.
- Frapper : 1 PA, puis choisir un adversaire à portée.
- Technique : 2 PA. Les techniques globales s’activent immédiatement.
- Garde : 1 PA, absorbe 14 dégâts. Fin du tour : réinitialise les PA après les ennemis.
- À 100 %, Résonance collective : dégâts de zone et soins, sans PA.
- 1–4 : sélectionner ; D : déplacer ; A : frapper ; S : technique ; G : garde ; E : fin du tour.

La sauvegarde locale conserve l’équipe et les missions terminées, pas le combat en cours. Aucun suivi ni données envoyées à un serveur.

## Développement

`node test.cjs` vérifie les règles et 105 rencontres (35 formations × 3 missions).

`node --check dist/game.js` et `node --check dist/engine.js` vérifient la syntaxe.

`node bundle.cjs` régénère la version autonome après toute modification.

Le moteur `engine.js` est indépendant de l’interface. Les miniatures SVG sont dessinées par code d’après les descriptions fournies par Amado. Aucun fichier PDF original n’est inclus ni publié. Les propriétés intellectuelles de l’univers et des personnages restent celles de leur auteur.

## Adaptation et sources

Mission originale, après l’arrivée d’Amavelle ; pas une adaptation littérale d’un chapitre.

- Souffle 1–4, chapitre 12, pages PDF 67–72 : duos de Résonance et Intermonde neutre, vide, sans paysage terrestre.
- Souffle 1–4, chapitres 73–77 : essences humaines et complémentarité.
- Souffle 9–12, chapitres 665–666 : coordination physique et conceptuelle des sept armes.
- Descriptions visuelles des neuf personnages, tenues et artefacts fournies le 14 octobre 2026.

Grille, chiffres, boss, dialogues et effets simplifiés : adaptations ludiques originales. La grille visualise le combat, elle n’affirme pas un sol matériel dans l’Intermonde. La robe longue ouverte d’Amarilys dévoile sa jupe courte, suivant l’interprétation proposée pendant la préparation.

## Vérification

Tests automatisés des règles et du parcours d’interface avec DOM simulé. Le contrôle visuel dans un navigateur n’était pas disponible dans l’environnement de création ; le rendu sur appareils réels reste à confirmer.
