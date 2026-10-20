# Serein — Android

Application Android en français pour réduire l’exposition aux contenus adultes.

## Mise à jour 1.2.0 : protection locale hors connexion et arrière-plan

Installer par-dessus 1.1.0 ou 1.0.1 : même clé de signature, versionCode 4. Ouvrir Serein une fois après la mise à jour. Dans les réglages de recherches, vérifier l’accessibilité, laisser **Maintenir la protection en arrière-plan** activé et autoriser les notifications.

La détection des mots reste **entièrement locale**, sans connexion réseau, requête DNS ni serveur. Elle peut donc agir en mode avion sur les champs et contenus déjà présents dans une application. Les publications qui ne sont pas chargées hors ligne ne sont évidemment pas téléchargées par Serein. Le filtre de sites par DNS est distinct et nécessite un réseau pour résoudre les domaines lorsque la navigation est en ligne.

Une veille au premier plan accompagne désormais la protection de recherches, avec une notification indiquant l’état réel de connexion au service d’accessibilité. Elle tourne dans le même processus que le service local, sans requête réseau, minuterie périodique, verrou de veille ni son artificiel. L’activité Serein peut être quittée ou retirée des applications récentes. Le manifeste utilise `stopWithTask=false`, et la veille comme le VPN demandent une reprise Android via `START_STICKY` après une suppression de processus éligible. Les réglages de mots et d’applications sont restaurés depuis les préférences locales.

La notification permet de revenir aux réglages ou de désactiver le blocage des recherches. Les choix de l’utilisateur sont respectés : pas de relance forcée après un arrêt explicite ou une révocation. Sans accès d’accessibilité, sans mot ou sans application sélectionnée, la veille ne prétend pas fournir une protection active.

Un récepteur système traite le redémarrage et la fin d’une mise à jour. La veille locale est relancée si elle avait été activée et si Android confirme toujours l’autorisation d’accessibilité. La reprise du VPN au démarrage est **optionnelle et désactivée par défaut**, et exige une autorisation VPN déjà accordée ainsi qu’un état VPN précédemment activé. En cas d’erreur de réseau, le comportement de restauration de l’accès Internet introduit en 1.0.1 reste appliqué ; cela n’arrête pas la protection locale.

### Réglages Android à effectuer sur l’appareil

Dans Serein, ouvrir **Régler la batterie / arrière-plan**, puis choisir **Sans restriction / Autoriser l’activité en arrière-plan** dans les paramètres Android de l’application. Sur certains TECNO, il faut aussi vérifier le lancement automatique ou les restrictions Phone Master. L’app propose les écrans Android officiels ; elle ne modifie pas ces autorisations à la place de l’utilisateur.

**Limites** : aucune application ne peut garantir de continuer après « Forcer l’arrêt », un arrêt demandé dans le gestionnaire Android, une désactivation de l’accessibilité ou un arrêt agressif du constructeur. Après de tels événements, ouvrir Serein pour vérifier et relancer la protection. La reprise après manque de mémoire ou redémarrage dépend des règles Android et du téléphone ; elle n’est pas une garantie d’exécution permanente.

### Validation 1.2.0

- 16 assertions de décisions de cycle de vie et de fonctionnement hors connexion, dont détection de mots avec une interdiction effective de toutes les connexions réseau dans le processus de test JVM.
- 99 assertions existantes (30 DNS/politique, 23 HTTPS simulé, 46 règles de recherches) et 20 000 entrées aléatoires de parseurs.
- Compilation Android et DEX, signature vérifiée et identique à 1.1.0.
- Pas de test Android réel ou émulateur de fermeture, Doze, interruption du processus ou redémarrage effectué pendant cette mise à jour. Le test de connectivité interdite porte sur la logique locale, pas sur les callbacks du système Android.

## Historique : mise à jour 1.1.0 : recherches dans les applications

Installer cet APK **par-dessus Serein 1.0.1**, sans désinstaller : même signature, versionCode 3. Les utilisateurs de la première version 1.0 doivent toujours désinstaller celle-ci d’abord.

Dans Serein, ouvrir **Configurer le blocage des recherches**, choisir les applications, accepter l’information dédiée, puis activer **Serein — blocage des recherches** dans les réglages d’accessibilité Android. La protection DNS et la protection de recherches ont des activations séparées. Le service ne s’active pas automatiquement lors de la mise à jour.

Cette fonction utilise `AccessibilityService` et uniquement les événements des applications sélectionnées. Elle analyse localement le texte des champs de recherche identifiables. Un en-tête de recherche réduit de Pinterest peut aussi être détecté par sa position dans le quart supérieur de l’écran ; ce repli peut aussi détecter un titre de publication sensible situé dans cette zone. Si un mot bloqué est détecté, le service tente d’effacer le champ éditable puis revient à l’accueil Android. Si l’action Accueil échoue, il avertit l’utilisateur et tente de ramener Serein au premier plan.

La liste initiale contient notamment ecchi, hentai, hot, hot classic, sexy, nsfw et nude. Elle est modifiable. Les mots entiers et expressions sont comparés sans tenir compte des majuscules, accents, caractères de largeur différente ou certains caractères invisibles. Les variantes d’un mot, autres langues et formulations détournées ne sont pas toutes couvertes. Le mot « hot » peut aussi bloquer une recherche innocente qui contient ce mot.

Le **mode strict**, optionnel et accompagné d’une confirmation, étend l’analyse au texte visible dans les applications choisies, y compris publications, descriptions et suggestions. Il peut donc fermer une application sur un mot présent dans une publication ordinaire. Sans ce mode, les champs non identifiés comme recherche sont ignorés, sauf le repli Pinterest décrit plus haut.

**Limites** : aucune reconnaissance ou suppression individuelle des images ; un contenu sans texte détectable peut rester visible. Les premiers résultats peuvent brièvement apparaître avant l’action de retour à l’accueil. Les applications qui ne rendent pas leurs champs accessibles peuvent ne pas être filtrées. Une mise à jour d’une application peut modifier ses identifiants et casser la détection. Le service peut être désactivé par Android ou l’utilisateur. Les clones d’applications et autres profils Android peuvent nécessiter leur propre installation/activation. L’état réel doit être confirmé sur chaque téléphone.

**Confidentialité** : le service dispose techniquement d’un accès au contenu textuel accessible de l’écran dans les applications sélectionnées. Il ne prend aucune capture d’écran, ne transmet ni ne conserve les recherches ou textes d’écran, et ignore les champs marqués mot de passe par Android. Seuls les réglages (liste de mots et applications, activation) sont sauvegardés. La fonctionnalité est déclarée comme n’étant pas un outil destiné au handicap (`isAccessibilityTool=false`) ; elle reste sous le contrôle explicite de l’utilisateur. Le DNS, séparément, conserve son usage déclaré de CleanBrowsing.

Android peut demander d’abord **Autoriser les paramètres restreints** dans la fiche de l’application installée hors boutique, avant d’autoriser l’accessibilité. Cette autorisation doit être accordée manuellement par l’utilisateur. Serein ne contourne pas ce réglage.

### Validation 1.1.0

- Compilation Android, génération DEX et vérification de signature de l’APK.
- Même certificat que l’APK 1.0.1 remis précédemment.
- 46 assertions sur les recherches sensibles/ordinaires, limites de mots, normalisation Unicode, mots personnalisés et exclusions des mots de passe, textes invisibles et champs ordinaires.
- 30 assertions DNS/politique de navigation et 23 assertions de transport HTTPS, plus 20 000 entrées aléatoires dans les parseurs.
- Pas de test d’intégration effectué dans les applications Pinterest/Instagram/YouTube réelles ni sur téléphone/émulateur. Les tests de règles ne prouvent pas que les champs d’une version donnée d’une application seront exposés à Android.

## Historique : correctif 1.0.1

La première version utilisait exclusivement le port DNS 853 et un filtre familial qui peut aussi bloquer des sites généralistes. Cette version utilise HTTPS sur le port 443 et le filtre adulte, qui laisse passer les domaines mixtes comme Reddit. Les URL ne sont plus bloquées pour un mot adulte inclus dans un mot ordinaire, par exemple « Pornic ».

Le transport a été testé avec des connexions simulées ; l’environnement de compilation n’a pas permis de valider une résolution DNS réelle. La cause exacte du problème signalé sur téléphone n’a pas été confirmée. Le navigateur intégré reste un navigateur texte limité : les sites qui exigent JavaScript ou une connexion peuvent ne pas fonctionner. Pour la navigation normale, utiliser Chrome ou un autre navigateur habituel avec le filtre VPN activé.

**Installation de ce correctif : désinstaller la version 1.0 puis installer la version 1.0.1.** La clé de signature de 1.0 n’était plus disponible. Une sauvegarde privée de la nouvelle clé est conservée hors de GitHub pour les prochaines mises à jour.

## Installer

Télécharger `releases/Serein-1.2.0.apk`, ouvrir le fichier sur Android 8 ou plus récent et autoriser cette source si Android le demande. Dans Serein, activer la protection et accepter la demande VPN. Ouvrir ensuite le navigateur sans images.

Cette version est signée avec une clé locale de test. Elle n'est pas distribuée par Google Play. Une compilation GitHub Actions utilise une autre clé : une installation issue d'une autre clé demande de désinstaller l'ancienne app, ce qui efface les réglages. Conserver sa clé locale hors du dépôt pour créer des mises à jour compatibles.

## Ce qui fonctionne

- VPN local de filtrage DNS : seul le trafic UDP destiné au résolveur virtuel `10.111.0.2` traverse l’interface ; le reste de la connexion suit le réseau habituel.
- Requêtes DNS transférées par DNS-over-HTTPS (port 443) au **Adult Filter de CleanBrowsing**, avec vérification du certificat et du nom du serveur. Le transport est lié explicitement au réseau Wi-Fi/mobile hors VPN pour éviter une boucle DNS. Aucun résolveur non filtré ne sert de secours.
- Liste locale de domaines adultes et hentai, et liste personnelle modifiable. Les sous-domaines sont inclus.
- Navigateur texte : pages HTTPS reconstruites depuis du texte échappé et des liens validés. Le HTML distant, les images, SVG, vidéos, scripts, styles distants, iframes et téléchargements ne sont pas conservés. Une recherche ajoute `safe=active` ; les mots adultes de la liste locale sont refusés.
- Notification et état visible du service. Aucun historique de navigation enregistré par l’application, aucun compte, aucune publicité.

## Limites importantes

Le filtrage DNS ne reconnaît pas les images. Une photo suggestive ou explicite sur Instagram, WhatsApp, un domaine autorisé ou dans la galerie n'est pas masquée individuellement. Pour naviguer sans ces images, utiliser le navigateur texte de Serein. Celui-ci peut aussi retirer des images utiles et des fonctionnalités des sites.

Le filtre adulte est un service tiers : CleanBrowsing reçoit les noms de domaine résolus et l’adresse IP de connexion. Les sites visités reçoivent les requêtes HTTP habituelles. Consulter leurs politiques de confidentialité. Serein ne prétend pas anonymiser le trafic.

La couverture des catégories adultes dépend de CleanBrowsing et des listes locales ; elle n'est pas exhaustive. Le VPN n'est pas un verrou inviolable : désinstallation, désactivation, autre VPN, DNS-over-HTTPS personnalisé, DNS privé, caches existants, IP directe et proxys peuvent réduire ou contourner son effet. Désactiver le DNS sécurisé personnalisé des autres navigateurs. Le navigateur Serein n'est pas défini automatiquement comme navigateur par défaut.

Le serveur est testé avant de créer le VPN : si ce test échoue, la protection n’est pas activée et le réseau reste disponible. Après trois erreurs de transport pendant l’utilisation, le VPN est arrêté et une alerte indique explicitement que la protection est désactivée. Internet redevient alors accessible sans filtrage. Le transport TUN implémente le DNS UDP sur IPv4 ; le trafic Internet IPv6 normal est autorisé en dehors du tunnel. Le DNS TCP vers le résolveur virtuel n'est pas implémenté. Les paquets fragmentés sont refusés.

L’activation est manuelle après un redémarrage. Certains téléphones, dont des TECNO, nécessitent d’autoriser l’exécution en arrière-plan. Le mode VPN permanent n’est pas annoncé et **« Bloquer les connexions sans VPN » ne doit pas être activé**, car l’app ne transporte pas tout le trafic. Une seule app VPN peut être active à la fois.

## Compiler

JDK 17, Android SDK plateforme 35 et build-tools 35.0.0 ; outils `zip`, `sed`, `find`.

```sh
export JAVA_HOME=/chemin/vers/jdk-17
export ANDROID_HOME=/chemin/vers/android-sdk
bash run-tests.sh
bash build-apk.sh
```

Sortie : `app/build/manual/Serein-1.2.0.apk`. La clé est créée dans `app/build/serein-local.jks` (exclue de Git). Le script utilise les outils Android officiels et les modules javac/jar du JDK sans dépendances Maven. Le projet inclut également des fichiers Gradle pour Android Studio.

La construction automatisée est décrite dans le workflow racine `.github/workflows/serein-android.yml`. Le binaire livré a été construit localement ; une exécution CI n'est pas nécessaire pour le télécharger.

## Validation de cette version

- Compilation de toutes les sources Android et génération DEX.
- Vérification cryptographique de l’APK signé et inspection du manifeste.
- 30 assertions sur les domaines adultes et ordinaires, recherches, paquets tronqués, fragments, NXDOMAIN, ports et checksum IPv4.
- 23 assertions de transport HTTPS : encodage de requête, serveur adulte, réponses HTTP, type MIME, taille, identifiant et question DNS. Les connexions de ces tests sont simulées.
- 20 000 entrées aléatoires dans les parseurs DNS/IP.
- Pas de test d’installation, d’interface, de réseau réel ni d’autonomie effectué sur téléphone ou émulateur. Les protections effectives doivent être confirmées sur l’appareil.

## Sources techniques

- Android VPN : https://developer.android.com/develop/connectivity/vpn
- Android foreground services : https://developer.android.com/develop/background-work/services/fgs/service-types
- CleanBrowsing Family Filter : https://cleanbrowsing.org/filters/
- CleanBrowsing paramètres réseau : https://cleanbrowsing.org/support/troubleshooting/firewall-dns-requirements
