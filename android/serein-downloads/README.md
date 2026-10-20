# Serein Downloads — prototype Android 0.1.0

Application distincte de Serein, dédiée aux **commandes de téléchargement**. Android 8 ou plus récent. Brave, Chrome, SnapTube et Pinterest sont sélectionnés par défaut lorsqu'ils sont installés.

## Installation

1. Installer `Serein-Downloads-0.1.apk` depuis le dossier `releases` de cette branche.
2. Ouvrir Serein Downloads, lire les explications et activer son service dans les paramètres d'accessibilité Android.
3. Vérifier les applications sélectionnées. L'accès aux notifications est facultatif, avec un consentement séparé.
4. Tester d'abord avec un téléchargement ordinaire, puis un élément dont le texte contient un mot filtré. Les versions et langues des applications peuvent changer leurs boutons accessibles.

Sur certains appareils, Android demande d'autoriser les paramètres restreints dans la fiche de l'application avant l'accessibilité. Les réglages de batterie peuvent aussi limiter son fonctionnement. Le service affiche une notification persistante avec une commande pour désactiver la protection. Fermer l'écran de réglages ne désactive pas le service ; un arrêt forcé Android peut l'arrêter.

L'ancien Serein conserve ses propres réglages : installer cette application ne désactive pas son blocage des recherches.

## Fonctionnement et limites

- Le service lit les textes et identifiants accessibles dans les applications choisies. Un indice sensible (mot ou domaine) fait couvrir les commandes de téléchargement reconnues par une zone tactile qui intercepte l'appui. Les recherches ne sont pas fermées.
- Un contexte sensible reste disponible pendant deux minutes pour les menus sans titre. Une nouvelle recherche ordinaire reconnue peut l'effacer. L'utilisateur peut aussi le vider dans les réglages.
- « Enregistrer » dans un tableau Pinterest n'est pas assimilé au téléchargement d'une image sur le téléphone.
- L'accès facultatif aux notifications permet d'envoyer une véritable action « Annuler » fournie par l'application lorsqu'un transfert sensible est identifié. L'application source doit accepter cette demande. Des octets peuvent déjà avoir été reçus. Aucune notification n'est simplement effacée pour simuler un arrêt.
- Le mode strict couvre **toutes les commandes de téléchargement reconnues**, même pour des fichiers ordinaires. Il est désactivé par défaut et demande une confirmation.
- **Aucune analyse visuelle des images ou vidéos.** Un contenu sensible sans texte révélateur, une commande non accessible, un téléchargement automatique, très rapide ou sans action d'annulation peuvent passer. Ce prototype ne garantit pas l'interception de tous les téléchargements dans les quatre applications.
- Aucune suppression de fichier, inspection des fichiers privés d'autres applications, capture d'écran, connexion serveur ou collecte de l'historique. Les préférences restent locales. Le contexte temporaire ne contient que le nom technique de l'application et une échéance, sans texte de page.

## Autorisations

Service d'accessibilité pour lire les commandes et poser les protections tactiles ; écoute des notifications facultative ; notifications et service de premier plan pour le fonctionnement local ; redémarrage après mise à jour ou démarrage du téléphone. Aucune permission Internet, stockage, caméra ou microphone.

## Compilation et vérification

JDK 17, plateforme Android 36 et Build Tools 36.0.0 :

```sh
bash run-tests.sh
JAVA_HOME=/chemin/jdk ANDROID_HOME=/chemin/sdk SEREIN_KEYSTORE=/chemin/prive/cle.jks bash build-apk.sh
```

La clé doit avoir l'alias `serein` et les mots de passe attendus par le script local. Sans clé fournie, le script génère une clé de test locale dans `app/build`, exclue de Git. Conserver la même clé privée pour les mises à jour. La CI produit une signature de test distincte de l'APK distribué : ses sorties ne peuvent pas mettre à jour celui-ci directement.

Les tests JVM vérifient les mots filtrés, faux positifs courants, commandes, notifications et expiration du contexte. L'APK a été compilé et sa signature vérifiée. **Aucun test d'intégration sur téléphone avec les quatre applications n'a encore été effectué.**
