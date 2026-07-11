(() => {
  const header = document.querySelector('.site-header');
  const menuButton = document.querySelector('.menu-toggle');
  const nav = document.querySelector('.main-nav');
  const navLinks = [...document.querySelectorAll('.main-nav a')];

  const setMenu = (open) => {
    menuButton.setAttribute('aria-expanded', String(open));
    nav.classList.toggle('open', open);
    document.body.classList.toggle('menu-open', open);
  };

  menuButton.addEventListener('click', () => setMenu(menuButton.getAttribute('aria-expanded') !== 'true'));
  navLinks.forEach((link) => link.addEventListener('click', () => setMenu(false)));

  const updateHeader = () => header.classList.toggle('scrolled', window.scrollY > 18);
  updateHeader();
  window.addEventListener('scroll', updateHeader, { passive: true });

  const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        revealObserver.unobserve(entry.target);
      }
    });
  }, { threshold: 0.12 });

  document.querySelectorAll('.reveal').forEach((item, index) => {
    item.style.transitionDelay = `${Math.min((index % 4) * 70, 210)}ms`;
    revealObserver.observe(item);
  });

  const sections = [...document.querySelectorAll('main section[id]')];
  const updateActiveNav = () => {
    const marker = window.scrollY + Math.min(window.innerHeight * 0.42, 360);
    let active = sections[0]?.id;
    sections.forEach((section) => {
      if (section.offsetTop <= marker) active = section.id;
    });
    navLinks.forEach((link) => link.classList.toggle('active', link.getAttribute('href') === `#${active}`));
  };
  updateActiveNav();
  window.addEventListener('scroll', updateActiveNav, { passive: true });

  const characters = [
    {
      id:'amado', name:'Amado Shirogane', concept:'Raison · Équilibre', role:'Axe central', group:['fondations'], symbol:'R', color:'#e9edf7', tier:'Public',
      summary:'La Raison incarnée : un père, un arbitre et un gardien qui apprend que la cohérence ne suffit pas toujours à gouverner l’humain.',
      facts:[['Nature','Entité conceptuelle'],['Fonction','Gardien de l’Équilibre'],['Artefact','Logos'],['Affiliation','Famille Shirogane']],
      appearance:'Cheveux rouges, regard rubis et lunettes blanches totalement opaques à monture noire dans sa forme iconique. Il porte fréquemment un costume formel ; Logos se manifeste comme une épée conceptuelle marquée d’un rubis.',
      personality:'Calme, analytique et tranchant. Amado préfère démontrer, stabiliser et arbitrer plutôt que dominer. Son apparente froideur masque une responsabilité familiale immense et une réelle capacité à revoir son jugement.',
      sourceNote:'Apparence iconique et fonction confirmées dans le corpus. Les détails secondaires peuvent varier selon l’incarnation et la période.',
      quote:'Tu dessines des formes que tu refuses de rencontrer.', quoteAuthor:'— Amado, phrase rapportée par Iris',
      anecdotes:['Logos n’est pas seulement une arme : sa forme de capybara transforme régulièrement les crises cosmiques en scènes domestiques.','Amado formule souvent des phrases simples comme des lois, ce qui irrite Iris autant que cela l’oblige à réfléchir.'],
      relations:['Noésis Absoluta','Rebecca Shirogane','Amadeus','Les Ama Girls'],
      spoilers:['Son héritage de Raison se combine au Sens de Noésis à travers Amadeus.','À la fin de l’œuvre, sa fonction d’arbitre laisse davantage de place aux choix humains de sa famille.']
    },
    {
      id:'noesis', name:'Noésis Absoluta', concept:'Sens', role:'Déesse primordiale', group:['fondations','primordiales'], symbol:'S', color:'#ff6b37', tier:'Public',
      summary:'Le Sens devenu conscience : la présence qui exige une direction, tranche les faux pourquoi et refuse que l’existence se contente d’être cohérente.',
      facts:[['Nature','Déesse primordiale'],['Fonction','Incarnation du Sens'],['Artefact','Téganon'],['Affiliation','Famille Shirogane']],
      appearance:'Très grande femme aux longs cheveux noirs, marqués d’une mèche orange foncé sur le côté gauche et souvent attachés en chignon désordonné. Ses yeux sont orange ; sa tenue emblématique est un kimono noir et orange.',
      personality:'Autoritaire, vulgaire, paresseuse, gourmande et ironique. Derrière la provocation se trouve une juge redoutablement lucide, capable de transformer une dispute familiale en démonstration métaphysique.',
      sourceNote:'Voix, concept et marqueurs visuels sont stables. Le site conserve sa caractérisation quotidienne autant que son rang cosmique.',
      quote:'Le sens précède ta question.', quoteAuthor:'— Noésis Absoluta',
      anecdotes:['Téganon, sa poêle emblématique, résume parfaitement son mélange de trivialité domestique et d’autorité cosmique.','Elle peut traiter un procès universel avec la même énergie qu’une dispute concernant un repas ou un capybara récalcitrant.'],
      relations:['Amado Shirogane','Amadeus','Nulla Absentia','Iris Marthory-Vaucluse'],
      spoilers:['Son opposition au Nihilisme devient un conflit récurrent qui dépasse largement une simple rivalité.','Elle préside l’un des jugements les plus importants de la fin de série.']
    },
    {
      id:'rebecca', name:'Rebecca Shirogane', concept:'Création · Imagination', role:'Créatrice', group:['fondations'], symbol:'I', color:'#ff4c8d', tier:'Public',
      summary:'L’impulsion créatrice qui donne aux structures une chaleur, une émotion et le droit de devenir autre chose qu’une fonction parfaite.',
      facts:[['Nature','Entité créatrice'],['Fonction','Imagination / Artiste'],['Énergie','Encre de Genèse'],['Affiliation','Famille Shirogane']],
      appearance:'Son apparence varie selon les incarnations. Une représentation récurrente la montre avec des cheveux bruns attachés en haute queue-de-cheval, des yeux ambre-orange, des lunettes fines et une veste rouge sur un haut beige.',
      personality:'Inventive, vive, affective et parfois épuisée par sa propre capacité à créer. Rebecca agit par intuition, recommence, rature et assume progressivement que chaque création peut devenir autonome.',
      sourceNote:'La fonction est parfaitement confirmée ; la bible canonique signale que ses descriptions physiques varient selon les périodes.',
      quote:'Amaelle est la clé !', quoteAuthor:'— Rebecca, lorsqu’elle identifie la faille du jugement',
      anecdotes:['Son vieux fauteuil abîmé et ses idées flottant comme des lucioles forment l’une de ses images quotidiennes les plus reconnaissables.','Iris lui reproche de renverser de l’encre partout, ce qui résume leur opposition entre invention spontanée et forme parfaite.'],
      relations:['Amado Shirogane','Les Ama Girls','Nulla Absentia','Iris Marthory-Vaucluse'],
      spoilers:['Son épuisement créateur produit une conséquence autonome majeure.','Sa réparation finale ne consiste pas à effacer les erreurs, mais à rendre le monde de nouveau habitable.']
    },
    {
      id:'nyxara', name:'Nyxara Abyssgarde', concept:'Néant primordial', role:'Déesse originelle', group:['primordiales'], symbol:'V', color:'#aa71ff', tier:'Public',
      summary:'Le Néant originel sous une forme libre, joueuse et maternelle : une absence capable de caprice, d’attachement et d’une tendresse aussi inquiétante que sincère.',
      facts:[['Nature','Déesse originelle'],['Fonction','Néant / Zéro'],['Rang','Primordial'],['Affiliation','Extérieure aux lois ordinaires']],
      appearance:'Longs cheveux blond cendré et ondulés, yeux dorés profonds, longue robe noire aux reflets mauves dans sa représentation graphique la plus stable.',
      personality:'Joueuse, maternelle et terrifiante sans perdre sa chaleur. Nyxara est dangereuse parce qu’elle est libre : elle peut aimer, plaisanter et abolir une structure dans le même mouvement.',
      sourceNote:'Le noyau psychologique est solidement attesté. La description visuelle correspond à sa représentation graphique de référence.',
      quote:'Nyxara joue avec le Zéro comme une enfant capricieuse.', quoteAuthor:'— Iris, à propos de Nyxara',
      anecdotes:['Une version de Nyxara privée de jeu et d’affection est jugée encore plus dangereuse que l’originale.','Son côté “mère poule” ne diminue jamais la menace métaphysique qu’elle représente.'],
      relations:['Amavelle Shirogane','Iris Marthory-Vaucluse','Orphéa Aurora','Barbatos'],
      spoilers:['Sa position face aux crises finales montre qu’elle ne se réduit jamais à une alliée ou une ennemie classique.']
    },
    {
      id:'orphea', name:'Orphéa Aurora', concept:'Temps absolu', role:'Souveraine temporelle', group:['primordiales'], symbol:'T', color:'#e6c269', tier:'Public',
      summary:'La souveraine du Temps Absolu : majestueuse, expressive et suffisamment théâtrale pour transformer une mesure cosmique en scène de famille.',
      facts:[['Nature','Déesse'],['Fonction','Temps absolu'],['Domaine','Univers du Temps Absolu'],['Autorité','Fonctions temporelles']],
      appearance:'Femme divine aux très longs cheveux blancs purs et soyeux, yeux dorés éclatants et longue robe blanche aux plis fluides, faite d’un tissu éthéré évoquant la lumière du temps.',
      personality:'Majestueuse, expressive, dramatique, espiègle et boudeuse. Orphéa aime observer, comparer et mettre en scène, parfois au point d’oublier que la continuité paie le prix de chaque distraction.',
      sourceNote:'Robe blanche, yeux dorés et présence solennelle sont attestés ; sa voix théâtrale est récurrente.',
      quote:'Le Temps est infini, après tout.', quoteAuthor:'— Orphéa Aurora',
      anecdotes:['Elle a déjà abrité Logos sous forme de capybara derrière son trône pendant qu’un groupe entier le poursuivait.','Ses procès sont sérieux, mais rarement dépourvus de distractions ou de mise en scène.'],
      relations:['Arion Valerius','Chronosia','Chroniel','Elythra','Virelya'],
      spoilers:['Son règne est interrompu par une souveraineté fondée sur l’instant parfait.','Son retour affirme que le Temps n’existe pas seulement pour conserver : il doit permettre de devenir.']
    },
    {
      id:'iris', name:'Iris Marthory-Vaucluse', concept:'Esthétique', role:'Architecte des corps', group:['fondations','primordiales'], symbol:'E', color:'#e7b9d8', tier:'Palier III',
      summary:'La première création stable et l’Esthéticienne de la Conscience, chargée de donner au vivant une forme capable d’accueillir une âme, une douleur et une histoire.',
      facts:[['Nature','Entité primordiale'],['Fonction','Esthétique / Forme'],['Domaine','Désert de l’Idéal'],['Titre','Première création stable']],
      appearance:'Cheveux blanc-argenté et posture toujours maîtrisée. Lors de son séjour terrestre, elle porte un yukata bleu nuit et conserve une fleur rose offerte par une enfant dans ses cheveux.',
      personality:'Narcissique, théâtrale, sévère et obsédée par les proportions. Elle aime sa solitude et critique tout, mais son rapport à l’imperfection humaine devient progressivement plus tendre et complexe.',
      sourceNote:'Le corpus confirme sa fonction, ses cheveux blanc-argenté et plusieurs tenues ; son apparence peut varier avec le contexte.',
      quote:'Ta posture au sol est profondément offensante. Tu t’allonges comme un meuble fatigué.', quoteAuthor:'— Iris à Amadeus',
      anecdotes:['Elle garde une fleur humaine simplement parce qu’une enfant la lui a offerte — un geste minuscule qui marque une évolution immense.','Même l’épuisement doit, selon elle, respecter un minimum de composition.'],
      relations:['La Conscience','Rebecca Shirogane','Amadeus','Amado Shirogane'],
      spoilers:['Elle intervient directement dans le jugement final du Doute.','Son travail concerne aussi la conservation de formes rejetées et de futurs corporels alternatifs.']
    },
    {
      id:'cael', name:'Cael Virelios', concept:'Doute', role:'Antagoniste méthodique', group:['antagonistes'], symbol:'D', color:'#8fb4c6', tier:'Palier IV',
      summary:'Le Doute méthodique : il ne se contente pas d’opposer une réponse à une autre, il exige que toute certitude prouve qu’elle mérite d’exister.',
      facts:[['Nature','Incarnation du Doute'],['Fonction','Juge / contradicteur'],['Artefacts','Aporia et Balance'],['Principe','Il ne tue pas']],
      appearance:'Ses yeux bleu clair sont attestés. L’édition publique insiste davantage sur sa présence froide, Aporia et la Balance que sur une description corporelle exhaustive.',
      personality:'Froid, méthodique, austère et convaincu que l’émotion corrompt le jugement. Son paradoxe central est de posséder malgré lui une compassion qu’il refuse d’intégrer à sa doctrine.',
      sourceNote:'Fonction, artefacts, yeux et principe moral sont confirmés. Plusieurs détails physiques restent volontairement indéterminés sans invention.',
      quote:'L’ordre sans humanité face à l’instabilité pure… La mesure penche déjà.', quoteAuthor:'— Cael Virelios',
      anecdotes:['Il analyse les micro-ajustements d’une bataille comme un juge observant une démonstration.','Sa doctrine condamne l’attachement, alors que son comportement révèle parfois exactement l’inverse.'],
      relations:['Amado Shirogane','Noésis Absoluta','Nyra Velmire','Noxelyra Apex'],
      spoilers:['Sur Terre, il nourrit discrètement des chats et des chiens errants, puis soigne un oiseau blessé.','Sa propre preuve finit par devenir l’élément central de son jugement.']
    },
    {
      id:'ama-girls', name:'Les Ama Girls', concept:'Sept concepts humains', role:'Sororité conceptuelle', group:['ama-girls'], symbol:'7', color:'#d71943', tier:'Public',
      summary:'Sept sœurs, sept fonctions et une même décision : ne jamais devenir des absolus vides de choix, même lorsque l’humanité rend leur existence plus douloureuse.',
      facts:[['Membres','Amanda, Amandine, Amada, Amalia, Amarilys, Amarisa, Amavelle'],['Tenue','Maid of Coherence'],['Lien','Famille Shirogane'],['Noyau','Choix et humanité']],
      appearance:'Le groupe associe sept signatures chromatiques — rouge, or solaire, vert, bleu, blanc, violet et silence sombre — à une tenue commune structurée appelée Maid of Coherence.',
      personality:'Elles se disputent, plaisantent, se protègent et débattent constamment de leur propre fonction. Leur force collective vient moins de l’uniformité que de leur capacité à rester différentes.',
      sourceNote:'Composition et concepts sont confirmés. Certaines appellations évoluent selon les périodes ; les fiches individuelles précisent chaque sœur.',
      quote:'La nuance est notre force.', quoteAuthor:'— Amarisa, au nom de la logique humaine du groupe',
      anecdotes:['Elles ont traversé trois corridors temporels à une vitesse absurde pour poursuivre Logos, qui refusait de prendre un bain.','Elles accueillent Chronosia comme une sœur en quelques secondes, sans procédure ni débat cosmologique.'],
      relations:['Amado Shirogane','Rebecca Shirogane','Amaelle','Amadeus'],
      spoilers:['Chacune affronte une version absolue d’elle-même et gagne non par perfection, mais par intégration de son humanité.']
    },
    {
      id:'amanda', name:'Amanda Shirogane', concept:'Vérité · Axiome', role:'Aînée structurante', group:['ama-girls'], symbol:'A', color:'#d92c49', tier:'Public',
      summary:'La présence la plus posée du groupe, pour qui la vérité n’est utile que si elle reste capable de reconnaître l’expérience humaine.',
      facts:[['Concept','Vérité / Axiome'],['Artefact','Veritas'],['Groupe','Ama Girls'],['Signature','Rouge rubis']],
      appearance:'Longs cheveux rouges tombant sur les épaules, posture droite et regard froid. Sa présence évoque un verdict avant même qu’elle ne parle.',
      personality:'Calme, responsable et naturellement directrice. Amanda observe avant d’intervenir et cherche à transmettre la vérité sans la réduire à une arme.',
      sourceNote:'Chevelure rouge, Veritas et rôle de premier plan dans le groupe sont confirmés.',
      quote:'Nous avons évolué.', quoteAuthor:'— Amanda Shirogane',
      anecdotes:['Même lorsqu’un événement devient complètement absurde, elle tente d’en préserver le sens de la responsabilité.','Son calme laisse parfois apparaître un sourire discret lorsque la famille s’agrandit.'],
      relations:['Les Ama Girls','Amado Shirogane','Amaelle','Chronosia'],
      spoilers:['Elle choisit une vie d’historienne en Égypte après le retrait cosmique.']
    },
    {
      id:'amandine', name:'Amandine Shirogane', concept:'Liberté', role:'Énergie solaire', group:['ama-girls'], symbol:'L', color:'#f0bd4c', tier:'Public',
      summary:'La Liberté sous sa forme la plus directe : solaire, bruyante, combative et incapable de laisser une règle abstraite décider seule de la vie des autres.',
      facts:[['Concept','Liberté'],['Artefact','Heliona'],['Groupe','Ama Girls'],['Signature','Or solaire']],
      appearance:'Longue chevelure blonde traversée d’énergie solaire. Sa silhouette et ses mouvements dégagent une impression d’élan permanent.',
      personality:'Franche, explosive, festive et protectrice. Elle transforme rapidement une hésitation en mouvement et supporte très mal les fonctions qui interdisent de choisir.',
      sourceNote:'Chevelure blonde, énergie solaire et tempérament direct sont largement attestés.',
      quote:'Il refuse de prendre un bain.', quoteAuthor:'— Amandine, justification d’une poursuite multi-cosmique',
      anecdotes:['Elle formule des conseils de défense à Amaelle avec une précision tellement excessive qu’ils deviennent de véritables flashbacks comiques.','Elle attribue volontiers la responsabilité d’un désastre cosmique au capybara qui l’a provoqué.'],
      relations:['Les Ama Girls','Amaelle','Chronosia','Logos'],
      spoilers:['Elle choisit le football au Brésil et vise la sélection nationale.']
    },
    {
      id:'amada', name:'Amada Shirogane', concept:'Nature · Vie', role:'Gardienne du vivant', group:['ama-girls'], symbol:'N', color:'#66c781', tier:'Public',
      summary:'La Nature incarnée comme affection : elle protège moins par automatisme biologique que parce qu’elle s’attache réellement à ce qui peut grandir.',
      facts:[['Concept','Nature / Vie'],['Artefact','Lifebloom'],['Groupe','Ama Girls'],['Signature','Vert émeraude']],
      appearance:'Longs cheveux verts évoquant une cascade vivante. Lifebloom apparaît fréquemment près de son poignet ou de sa main.',
      personality:'Douce, réservée, protectrice et profondément attachée aux êtres fragiles. Sa tranquillité ne doit pas être confondue avec de la passivité.',
      sourceNote:'Cheveux verts, Lifebloom et fonction liée au vivant sont confirmés.',
      quote:'On reste ensemble.', quoteAuthor:'— Amada Shirogane',
      anecdotes:['Parmi les projets de vacances du groupe, son premier réflexe est simplement : cuisiner.','Elle peut protéger une fleur par affection plutôt que par obligation conceptuelle — distinction essentielle de son humanité.'],
      relations:['Les Ama Girls','Amaelle','Rebecca Shirogane','Lifebloom'],
      spoilers:['Elle choisit la protection environnementale en Nouvelle-Zélande.']
    },
    {
      id:'amalia', name:'Amalia Shirogane', concept:'Harmonie', role:'Médiatrice cyclique', group:['ama-girls'], symbol:'H', color:'#64a5e8', tier:'Public',
      summary:'L’Harmonie qui accepte la dissonance : elle ne cherche pas un monde sans erreurs, mais un rythme capable de les intégrer sans écraser les personnes.',
      facts:[['Concept','Harmonie'],['Artefact','Samsara'],['Groupe','Ama Girls'],['Signature','Bleu profond']],
      appearance:'Cheveux bleu profond et présence calme. Samsara tourne régulièrement en orbite autour d’elle comme un cycle visible.',
      personality:'Empathique, réfléchie et chaleureuse. Amalia perçoit les relations comme des rythmes et comprend que l’équilibre vivant comporte du désordre.',
      sourceNote:'Chevelure bleue, Samsara et rapport cyclique à l’harmonie sont attestés.',
      quote:'L’harmonie n’est pas l’absence d’erreur. C’est la joie qu’on trouve dans le mouvement.', quoteAuthor:'— Amalia Shirogane',
      anecdotes:['Face à Chronosia, elle résume immédiatement la position familiale par deux mots : “On l’adopte.”','Elle observe les villes humaines comme des compositions où le chaos peut devenir un rythme.'],
      relations:['Les Ama Girls','Chronosia','Lian Yue','Amaelle'],
      spoilers:['Elle devient mangaka au Japon et publie Shirogane Universe à l’intérieur même de l’histoire.']
    },
    {
      id:'amarilys', name:'Amarilys Shirogane', concept:'Perfection', role:'Lame de la nuance', group:['ama-girls'], symbol:'P', color:'#e6e7ed', tier:'Public',
      summary:'La Perfection qui découvre que choisir une cicatrice, pardonner une faute ou conserver un manque peut être plus humain qu’une correction totale.',
      facts:[['Concept','Perfection'],['Artefact','Seijun'],['Groupe','Ama Girls'],['Signature','Argent / blanc']],
      appearance:'Cheveux blancs et yeux rouges dans sa forme iconique. Son allure est nette, tranchante et volontairement maîtrisée.',
      personality:'Fiére, exigeante, directe et étonnamment lucide sur le danger des absolus. Son goût de la beauté ne supprime jamais son sens du choix.',
      sourceNote:'Cheveux blancs, regard rouge, Seijun et présence tranchante sont confirmés. Les marqueurs tardifs sont masqués par défaut.',
      quote:'Et si tu ne choisis pas… tu n’es pas libre.', quoteAuthor:'— Amarilys Shirogane',
      anecdotes:['Elle considère qu’une poursuite cosmique pour imposer un bain à Logos est “absolument” justifiée.','Même au repos, son idée d’une activité légère reste l’entraînement.'],
      relations:['Les Ama Girls','Seijun','Chronosia','Amaelle'],
      spoilers:['Elle conserve volontairement l’absence de son œil gauche et ses cicatrices comme mémoire, esthétisme et orgueil.','Elle choisit l’escrime humaine en Russie avec une ambition olympique.']
    },
    {
      id:'amarisa', name:'Amarisa Shirogane', concept:'Logique', role:'Analyste', group:['ama-girls'], symbol:'L', color:'#aa78e8', tier:'Public',
      summary:'La Logique qui trouve de la valeur dans les paradoxes humains, non parce qu’ils sont corrects, mais parce qu’ils révèlent une expérience que le calcul seul ne suffit pas à épuiser.',
      facts:[['Concept','Logique'],['Artefact','Logion'],['Groupe','Ama Girls'],['Signature','Améthyste']],
      appearance:'Longues tresses violettes, yeux améthyste et lunettes. Logion flotte souvent ouvert à ses côtés.',
      personality:'Analytique, précise, froide en apparence et dotée d’un humour sec. Elle calcule vite, mais son évolution tient à sa capacité à considérer la nuance comme une force.',
      sourceNote:'Tresses violettes, yeux améthyste, lunettes et Logion sont des marqueurs stables.',
      quote:'Statistiquement, ça commence à devenir suspect.', quoteAuthor:'— Amarisa Shirogane',
      anecdotes:['Elle réagit aux révélations familiales comme à un problème probabiliste, ce qui les rend souvent encore plus drôles.','Son projet de vacances idéal consiste simplement à étudier.'],
      relations:['Les Ama Girls','Logion','Chronosia','Amaelle'],
      spoilers:['Elle structure une transition métaphysique décisive en séparant abstraction du concept, corps et âme.','Elle choisit une carrière d’astronaute à la NASA.']
    },
    {
      id:'amavelle', name:'Amavelle Shirogane', concept:'Silence', role:'Présence silencieuse', group:['ama-girls'], symbol:'S', color:'#a9a1bd', tier:'Public',
      summary:'Le Silence comme espace de transformation : peu de mots, une attention intense et la conviction que la stabilité n’a de valeur que si elle laisse encore la possibilité de changer.',
      facts:[['Concept','Silence'],['Artefact','Silentium'],['Groupe','Ama Girls'],['Signature','Noir / argent']],
      appearance:'Le corpus public insiste davantage sur sa présence, Silentium et ses tonalités sombres que sur une description physique totalement stable. Sa silhouette est généralement sobre et tranchante.',
      personality:'Silencieuse, observatrice, douce sans être fragile et capable de formuler en quelques mots le cœur philosophique d’une scène.',
      sourceNote:'Concept, artefact et tempérament sont confirmés ; plusieurs détails corporels restent partiels pour éviter de fixer une version non attestée.',
      quote:'C’est douloureux. Mais c’est vivant.', quoteAuthor:'— Amavelle Shirogane',
      anecdotes:['Lorsqu’on lui demande ses projets de vacances, elle répond simplement : “Chats.”','Son silence n’est jamais une absence de position : il prépare souvent la phrase qui recentre tout le groupe.'],
      relations:['Les Ama Girls','Nyxara Abyssgarde','Silentium','Amaelle'],
      spoilers:['Elle devient poète au Moyen-Orient après le retrait cosmique.']
    }
  ];

  const normalize = (value) => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
  const grid = document.getElementById('character-grid');
  const searchInput = document.getElementById('character-search');
  const filterButtons = [...document.querySelectorAll('.filter-chip')];
  const countLabel = document.getElementById('character-count');
  const emptyState = document.getElementById('wiki-empty');
  const spoilerSwitch = document.getElementById('spoiler-switch');
  let currentFilter = 'all';
  let spoilersEnabled = sessionStorage.getItem('shirogane-spoilers') === 'true';

  const setSpoilerMode = (enabled) => {
    spoilersEnabled = enabled;
    sessionStorage.setItem('shirogane-spoilers', String(enabled));
    spoilerSwitch.setAttribute('aria-pressed', String(enabled));
    spoilerSwitch.querySelector('small').textContent = enabled ? 'Activé' : 'Désactivé';
    document.body.classList.toggle('spoilers-enabled', enabled);
    if (dialog.open) renderSpoilers(activeCharacter);
  };

  const cardTemplate = (character) => `
    <button class="character-card wiki-card" type="button" data-character="${character.id}" style="--concept:${character.color};--concept-glow:${character.color}">
      <span class="card-spoiler-level">${character.tier}</span>
      <span class="character-symbol${character.symbol === '7' ? ' seven' : ''}" aria-hidden="true">${character.symbol}</span>
      <span class="character-meta"><span>${character.concept}</span><span>${character.role}</span></span>
      <h3>${character.name}</h3>
      <p>${character.summary}</p>
      <span class="character-line"></span>
      <span class="card-action">Ouvrir la fiche ↗</span>
    </button>`;

  const renderCards = () => {
    const query = normalize(searchInput.value.trim());
    const visible = characters.filter((character) => {
      const matchesFilter = currentFilter === 'all' || character.group.includes(currentFilter);
      const haystack = normalize([character.name, character.concept, character.role, character.summary, ...character.relations].join(' '));
      return matchesFilter && (!query || haystack.includes(query));
    });
    grid.innerHTML = visible.map(cardTemplate).join('');
    countLabel.textContent = `${visible.length} dossier${visible.length > 1 ? 's' : ''}`;
    emptyState.hidden = visible.length !== 0;
    grid.querySelectorAll('[data-character]').forEach((card) => card.addEventListener('click', () => openProfile(card.dataset.character)));
  };

  searchInput.addEventListener('input', renderCards);
  filterButtons.forEach((button) => button.addEventListener('click', () => {
    currentFilter = button.dataset.filter;
    filterButtons.forEach((item) => item.classList.toggle('active', item === button));
    renderCards();
  }));
  spoilerSwitch.addEventListener('click', () => setSpoilerMode(!spoilersEnabled));
  document.addEventListener('keydown', (event) => {
    if (event.key === '/' && !/input|textarea/i.test(document.activeElement.tagName)) {
      event.preventDefault();
      searchInput.focus();
    }
  });

  const dialog = document.getElementById('character-dialog');
  const dialogClose = document.getElementById('dialog-close');
  const identity = dialog.querySelector('.profile-identity');
  const profileSymbol = document.getElementById('profile-symbol');
  const profileKicker = document.getElementById('profile-kicker');
  const profileName = document.getElementById('profile-name');
  const profileConcept = document.getElementById('profile-concept');
  const profileBadges = document.getElementById('profile-badges');
  const profileSummary = document.getElementById('profile-summary');
  const profileFacts = document.getElementById('profile-facts');
  const profileRelations = document.getElementById('profile-relations');
  const profileAppearance = document.getElementById('profile-appearance');
  const profilePersonality = document.getElementById('profile-personality');
  const profileSourceNote = document.getElementById('profile-source-note');
  const profileQuote = document.getElementById('profile-quote');
  const profileQuoteAuthor = document.getElementById('profile-quote-author');
  const profileAnecdotes = document.getElementById('profile-anecdotes');
  const profileSpoilers = document.getElementById('profile-spoilers');
  const profileSpoilerList = document.getElementById('profile-spoiler-list');
  const spoilerLockCopy = document.getElementById('spoiler-lock-copy');
  const revealSpoilersButton = document.getElementById('reveal-profile-spoilers');
  const profileLink = document.getElementById('profile-link');
  const tabs = [...dialog.querySelectorAll('.profile-tab')];
  const panels = [...dialog.querySelectorAll('.profile-panel')];
  let activeCharacter = null;

  const setTab = (tabName) => {
    tabs.forEach((tab) => tab.classList.toggle('active', tab.dataset.tab === tabName));
    panels.forEach((panel) => panel.classList.toggle('active', panel.dataset.panel === tabName));
  };
  tabs.forEach((tab) => tab.addEventListener('click', () => setTab(tab.dataset.tab)));

  const renderSpoilers = (character, force = false) => {
    const revealed = spoilersEnabled || force;
    profileSpoilers.classList.toggle('revealed', revealed);
    spoilerLockCopy.textContent = revealed ? 'Révélations tardives affichées.' : 'Cette partie est masquée pour préserver la découverte.';
    profileSpoilerList.innerHTML = character.spoilers.map((item) => `<li>${item}</li>`).join('');
  };

  revealSpoilersButton.addEventListener('click', () => renderSpoilers(activeCharacter, true));

  const openProfile = (id, updateHash = true) => {
    const character = characters.find((item) => item.id === id);
    if (!character) return;
    activeCharacter = character;
    identity.style.setProperty('--profile-color', character.color);
    dialog.style.setProperty('--profile-color', character.color);
    profileSymbol.textContent = character.symbol;
    profileKicker.textContent = `DOSSIER · ${character.role}`;
    profileName.textContent = character.name;
    profileConcept.textContent = character.concept;
    profileBadges.innerHTML = [...character.group.map((item) => item.replace('-', ' ')), character.tier].map((item) => `<span>${item}</span>`).join('');
    profileSummary.textContent = character.summary;
    profileFacts.innerHTML = character.facts.map(([term, value]) => `<div><dt>${term}</dt><dd>${value}</dd></div>`).join('');
    profileRelations.innerHTML = character.relations.map((item) => `<span>${item}</span>`).join('');
    profileAppearance.textContent = character.appearance;
    profilePersonality.textContent = character.personality;
    profileSourceNote.textContent = character.sourceNote;
    profileQuote.textContent = `« ${character.quote} »`;
    profileQuoteAuthor.textContent = character.quoteAuthor;
    profileAnecdotes.innerHTML = character.anecdotes.map((item) => `<li>${item}</li>`).join('');
    renderSpoilers(character);
    setTab('overview');
    profileLink.classList.remove('copied');
    profileLink.textContent = 'Copier le lien de la fiche';
    if (!dialog.open) dialog.showModal();
    document.body.classList.add('dialog-open');
    if (updateHash) history.replaceState(null, '', `#fiche-${character.id}`);
  };

  const closeProfile = () => {
    dialog.close();
    document.body.classList.remove('dialog-open');
    const fallback = window.scrollY > document.getElementById('personnages').offsetTop - 300 ? '#personnages' : '#top';
    history.replaceState(null, '', fallback);
  };
  dialogClose.addEventListener('click', closeProfile);
  dialog.addEventListener('cancel', (event) => { event.preventDefault(); closeProfile(); });
  dialog.addEventListener('click', (event) => {
    const rect = dialog.getBoundingClientRect();
    const outside = event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom;
    if (outside) closeProfile();
  });

  profileLink.addEventListener('click', async () => {
    const url = `${location.origin}${location.pathname}#fiche-${activeCharacter.id}`;
    try {
      await navigator.clipboard.writeText(url);
      profileLink.textContent = 'Lien copié';
      profileLink.classList.add('copied');
    } catch {
      profileLink.textContent = url;
    }
  });

  renderCards();
  setSpoilerMode(spoilersEnabled);

  const requestedProfile = location.hash.match(/^#fiche-(.+)$/)?.[1];
  if (requestedProfile) requestAnimationFrame(() => openProfile(requestedProfile, false));

  const canvas = document.getElementById('starfield');
  const ctx = canvas.getContext('2d');
  const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  let stars = [];
  let frameId;

  const resize = () => {
    const ratio = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.floor(window.innerWidth * ratio);
    canvas.height = Math.floor(window.innerHeight * ratio);
    canvas.style.width = `${window.innerWidth}px`;
    canvas.style.height = `${window.innerHeight}px`;
    ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
    const count = Math.min(150, Math.floor((window.innerWidth * window.innerHeight) / 9000));
    stars = Array.from({ length: count }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      radius: Math.random() * 1.25 + 0.15,
      alpha: Math.random() * 0.7 + 0.15,
      speed: Math.random() * 0.08 + 0.015
    }));
  };

  const draw = () => {
    ctx.clearRect(0, 0, window.innerWidth, window.innerHeight);
    for (const star of stars) {
      star.y -= star.speed;
      if (star.y < -3) {
        star.y = window.innerHeight + 3;
        star.x = Math.random() * window.innerWidth;
      }
      ctx.beginPath();
      ctx.arc(star.x, star.y, star.radius, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(235, 238, 246, ${star.alpha})`;
      ctx.fill();
    }
    if (!reducedMotion) frameId = requestAnimationFrame(draw);
  };

  resize();
  draw();
  window.addEventListener('resize', () => {
    cancelAnimationFrame(frameId);
    resize();
    draw();
    updateActiveNav();
  }, { passive: true });
})();
