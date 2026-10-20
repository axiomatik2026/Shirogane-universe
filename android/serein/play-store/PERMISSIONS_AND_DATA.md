# Brouillons de déclarations — ne pas soumettre sans revue de l’éditeur

## AccessibilityService

- Outil destiné au handicap : **non**, `isAccessibilityTool=false`.
- Fonction : appliquer la liste de mots choisie par l’utilisateur aux champs de recherche accessibles des applications sélectionnées ; mode strict optionnel sur le texte visible.
- Données accessibles : texte et structure des nœuds de la fenêtre active dans les applications choisies. Les champs marqués mot de passe sont ignorés. Aucun screenshot ni geste arbitraire n’est demandé.
- Actions : effacer le champ éditable si possible, puis action système Accueil ; si celle-ci échoue, avertissement et tentative de retour à Serein.
- Logique : règles locales déterministes définies par l’utilisateur ; aucun agent autonome, modèle distant ou planification d’actions.
- Consentement : information séparée dans le parcours d’activation, refus possible, puis activation manuelle dans Android. Le mode strict demande une confirmation supplémentaire.
- Transmission et conservation : pas d’envoi ou journal des textes analysés. Préférences locales seulement.
- Désactivation : accessible dans l’application, la notification et les réglages Android. Aucun verrou contre la désinstallation.

Joindre une vidéo de revue montrant exactement ce parcours. Référence : https://support.google.com/googleplay/android-developer/answer/10964491?hl=en.

## VpnService

- Usage : filtrage de domaines via DNS pour la navigation choisie par l’utilisateur. Vérifier l’éligibilité de cet usage dans la déclaration Google ; ne pas le présenter comme VPN d’anonymisation ou contrôle parental garanti.
- Le tunnel local route le DNS vers le résolveur virtuel ; le trafic Web ordinaire ne traverse pas un serveur VPN Serein.
- Les requêtes DNS sont transférées par HTTPS avec validation des certificats vers le filtre adulte CleanBrowsing.
- Destinataire : CleanBrowsing, qui reçoit domaines et IP de connexion. Ce tiers doit être nommé dans la politique et les déclarations.
- Défaillance : prétest avant activation ; après plusieurs erreurs de transport, le VPN s’arrête avec avertissement et Internet peut continuer sans filtre. La protection locale des recherches reste distincte.
- Activation explicite via dialogue et demande VPN Android. Arrêt possible dans l’application et Android. Une seule application VPN à la fois.

Référence : https://support.google.com/googleplay/android-developer/answer/12564964?hl=en.

## Foreground services

Type actuel : `specialUse` déclaré pour le VPN de filtrage et la veille de recherches locales. Expliquer le besoin de protection visible en continu avec notification lorsque l’utilisateur navigue dans d’autres applications. La veille n’utilise ni son artificiel, ni verrou de veille, ni requête périodique réseau. Vérifier l’acceptation de cet usage lors de la déclaration ; l’exécution reste sous le contrôle d’Android et de l’utilisateur.

## Data safety : éléments factuels à qualifier

| Élément | Traitement réel | Point de déclaration à confirmer |
| --- | --- | --- |
| Texte accessible des recherches/publications | Mémoire locale transitoire, sans journal ni transmission par l’accessibilité | Traitement local ; ne pas confondre avec une recherche Web volontaire |
| Listes de mots/domaines/apps, préférences | Stockage privé local jusqu’à effacement/désinstallation | Données locales ; sauvegarde Android automatique désactivée |
| Domaines demandés au DNS | Transmis par HTTPS à CleanBrowsing pour résolution/filtrage | Historique de navigation/données réseau selon la classification Google ; confirmer collecte et statut du tiers dans le contrat |
| IP de connexion du résolveur | Visible du service DNS par le fonctionnement du réseau | Qualifier identifiants réseau/localisation éventuelle selon traitement effectif du fournisseur |
| Navigation et requêtes d’un moteur de recherche | Envoyées aux sites ou au moteur choisis dans le navigateur intégré | Décrire le traitement de navigation ; ne pas promettre qu’aucune recherche ne quitte jamais l’appareil |
| Diagnostics/mesure d’audience | Aucun SDK de suivi ou de publicité ajouté | Refaire cette revue si un SDK est ajouté |
| Achat de l’application | Géré par la boutique, aucun compte Serein ni paiement intégré | Séparer les pratiques de la boutique de celles de l’éditeur |

Aucune réponse définitive du formulaire Data safety n’a été envoyée. Les catégories, finalités, caractère obligatoire et partage/exemptions doivent être validés contre les conditions du fournisseur et la version exacte du formulaire.
