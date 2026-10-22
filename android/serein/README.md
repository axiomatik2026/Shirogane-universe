# Serein 1.3.1 — correctif de reprise et diagnostic

Android 8+, sans compte ni publicité. Installer `releases/Serein-1.3.1.apk` par-dessus la version précédente compatible, sans désinstaller pour conserver les listes. VersionCode 7, même certificat que les APK 1.0.1 à 1.2.0 distribués. Ouvrir Serein après la mise à jour.


## Correctif 1.3.1 après signalement de panne

La 1.3.0 a été signalée comme ne bloquant plus les sites ni les recherches, surtout après fermeture. La cause exacte sur le téléphone utilisateur reste à confirmer ; cette version corrige des chemins de reprise et de lecture identifiés dans le code, sans prétendre avoir reproduit la panne sur cet appareil.

- Une mise à jour rétablit le VPN précédemment activé, indépendamment de l’option de reprise au démarrage. Une désactivation explicite reste respectée. Rouvrir Serein reprend également un VPN demandé mais arrêté, si l’autorisation est déjà accordée.
- Le service prend en charge le VPN permanent géré par Android. Le réglage doit être choisi par l’utilisateur dans les paramètres VPN. Ne pas activer le blocage des connexions sans VPN, ce tunnel ne transportant que le DNS.
- Une série continue d’événements ne repousse plus indéfiniment la lecture d’une recherche ; la première vérification planifiée conserve son échéance. Si le clavier devient la fenêtre active, le service cherche la fenêtre de l’application choisie parmi les fenêtres interactives accessibles.
- « Vérifier / relancer les protections » affiche l’état réel du VPN, les autorisations et la connexion effective du service d’accessibilité, la veille, ainsi que des compteurs DNS et d’événements locaux. Aucun domaine ou texte consulté n’est enregistré. Les compteurs sont en mémoire et disparaissent après l’arrêt du processus.
- Le bouton de relance réactive la demande VPN, les recherches et la veille en conservant les listes. Il ne peut pas accorder l’accessibilité ou lever les restrictions de batterie : des boutons ouvrent ces réglages. Un état « autorisée mais non connectée » peut nécessiter de désactiver puis réactiver l’interrupteur du service dans Android.

### Validation du correctif

129 assertions JVM passent, dont les décisions de reprise après mise à jour et démarrage, le respect d’une désactivation, et une série de 200 événements ne décalant pas le scan en attente. Compilation Android/DEX et signature compatible avec 1.3.0 vérifiées. Aucun test d’intégration sur téléphone/émulateur de bascule réseau, balayage des applications récentes ou arrêt par Phone Master. Il reste à vérifier le comportement sur le TECNO utilisateur via le nouvel écran d’état. L’ouverture du tunnel n’est pas une preuve que le navigateur lui envoie ses requêtes DNS.

## Liste noire persistante

Coller un lien ou un domaine par ligne. Le domaine entier et ses sous-domaines sont bloqués, pas seulement la page collée. Les entrées sont enregistrées durablement dans les préférences locales, sans serveur. Le VPN relit cette liste à chaque demande DNS et revérifie les réponses en cours avant transmission : aucun redémarrage du service nécessaire après un ajout.

Activer la protection VPN. Son service est indépendant de l’écran Serein, avec notification persistante, `stopWithTask=false` et demande de reprise `START_STICKY`. Retirer Serein des applications récentes ne demande pas son arrêt.

Le tunnel est établi sans test réseau préalable, même en mode avion. La liste est vérifiée avant tout appel réseau. Une coupure ou erreur HTTPS ne supprime plus le tunnel ni l’état activé. La notification indique alors que la liste locale est disponible et le filtre distant en attente. Chaque demande autorisée réessaie le résolveur HTTPS CleanBrowsing au retour du réseau. **Si ce résolveur reste inaccessible, les nouveaux domaines autorisés ne peuvent pas être résolus** : le VPN conserve la liste noire sans promettre une navigation normale pendant cette panne.

## Adresses déjà chargées et recherches hors connexion

Activer séparément l’accessibilité dans les réglages de protection locale et sélectionner les applications concernées, notamment Chrome et Brave. Le service compare aussi les adresses des barres d’adresse reconnues avec la liste noire, entièrement sur le téléphone. Une détection revient à l’accueil Android et peut effacer le champ. Cela peut agir sur une page en cache si sa barre d’adresse est exposée à l’accessibilité. Les mots des recherches restent filtrés localement ; le mode strict facultatif étend la vérification des mots au texte visible.

Vérifier la sélection de Brave sur une installation existante : un nouveau préréglage ne modifie pas une sélection déjà enregistrée. Aucun texte de page, adresse consultée, historique ou capture d’écran n’est conservé ou envoyé par ce service. Seuls les réglages et listes sont enregistrés. Aucune analyse d’image.

Le navigateur intégré sans images est supprimé : bouton, activité et code retirés.

## Réglages et limites

- Autoriser les notifications et l’activité en arrière-plan / batterie sans restriction. Sur certains TECNO, vérifier aussi Phone Master et le lancement automatique.
- La reprise du VPN au redémarrage reste optionnelle dans les réglages, avec autorisation VPN déjà accordée et protection précédemment activée.
- Un arrêt forcé, une révocation VPN/accessibilité ou des restrictions constructeur peuvent arrêter le service. Rouvrir Serein permet de le vérifier et relancer.
- Le DNS sécurisé du navigateur, une IP directe, un proxy ou une connexion déjà établie peuvent contourner le DNS. Une page en cache sans adresse accessible n’est pas garantie bloquée. Après un ajout, recharger la page ou ouvrir un nouvel onglet.
- Ce VPN ne transporte que le DNS : ne pas cocher « Bloquer les connexions sans VPN ». Un autre VPN ne peut pas fonctionner en même temps.
- Hors connexion, les règles restent disponibles et les vérifications locales continuent ; Serein ne charge pas de nouveaux sites.

## Compilation et validation

JDK 17, SDK Android 35, Build Tools 35.0.0 :

```sh
bash run-tests.sh
JAVA_HOME=/chemin/jdk ANDROID_HOME=/chemin/sdk SEREIN_KEYSTORE=/chemin/prive/cle.jks bash build-apk.sh
```

Sortie : `app/build/manual/Serein-1.3.1.apk`. Sans clé fournie, le script génère une clé de test dans `app/build`, exclue de Git. Conserver la clé privée pour les mises à jour ; la CI produit une signature de test distincte de l’APK distribué.

Validation : 129 assertions JVM (30 DNS/politique, 23 HTTPS simulé, 46 recherches, 30 hors connexion/cycle de vie), plus 20 000 entrées aléatoires du parseur. Les tests hors connexion interdisent les connexions réseau dans le processus JVM et vérifient les domaines personnalisés, URL et sous-domaines. Compilation Android/DEX et signature vérifiées. Aucun test sur téléphone ou émulateur de fermeture, redémarrage ou changement de réseau : les callbacks Android et interfaces des navigateurs restent à tester sur appareil.
