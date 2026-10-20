# Serein — Android

Application Android en français pour réduire l’exposition aux contenus adultes.

## Correctif 1.0.1

La première version utilisait exclusivement le port DNS 853 et un filtre familial qui peut aussi bloquer des sites généralistes. Cette version utilise HTTPS sur le port 443 et le filtre adulte, qui laisse passer les domaines mixtes comme Reddit. Les URL ne sont plus bloquées pour un mot adulte inclus dans un mot ordinaire, par exemple « Pornic ».

Le transport a été testé avec des connexions simulées ; l’environnement de compilation n’a pas permis de valider une résolution DNS réelle. La cause exacte du problème signalé sur téléphone n’a pas été confirmée. Le navigateur intégré reste un navigateur texte limité : les sites qui exigent JavaScript ou une connexion peuvent ne pas fonctionner. Pour la navigation normale, utiliser Chrome ou un autre navigateur habituel avec le filtre VPN activé.

**Installation de ce correctif : désinstaller la version 1.0 puis installer la version 1.0.1.** La clé de signature de 1.0 n’était plus disponible. Une sauvegarde privée de la nouvelle clé est conservée hors de GitHub pour les prochaines mises à jour.

## Installer

Télécharger `releases/Serein-1.0.1.apk`, ouvrir le fichier sur Android 8 ou plus récent et autoriser cette source si Android le demande. Dans Serein, activer la protection et accepter la demande VPN. Ouvrir ensuite le navigateur sans images.

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

Sortie : `app/build/manual/Serein-1.0.1.apk`. La clé est créée dans `app/build/serein-local.jks` (exclue de Git). Le script utilise les outils Android officiels et les modules javac/jar du JDK sans dépendances Maven. Le projet inclut également des fichiers Gradle pour Android Studio.

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
