package sn.amado.downloadguard;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final int BG=0xff101b29,INK=0xffedf5fc,MINT=0xff70e1bc;
    private LinearLayout body;private TextView state;private Button toggle;private final Handler h=new Handler();
    private final Runnable update=new Runnable(){public void run(){render();h.postDelayed(this,1000);}};
    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        ScrollView s=new ScrollView(this);s.setBackgroundColor(BG);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(22),dp(22),dp(22),dp(22));s.addView(body);setContentView(s);
        s.setOnApplyWindowInsetsListener((v,i)->{body.setPadding(dp(22),dp(22)+i.getSystemWindowInsetTop(),dp(22),dp(22)+i.getSystemWindowInsetBottom());return i;});
        text("SEREIN • DOWNLOADS",13,MINT);text("Filtrer l’enregistrement.",29,INK);state=text("",15,MINT);
        text("Brave • Chrome • SnapTube • Pinterest. Cette application agit sur les commandes de téléchargement reconnues. Elle ne ferme pas les recherches et n’analyse pas visuellement les images.",16,INK);
        toggle=button("Activer la protection",()->{
            if(State.enabled(this)){State.prefs(this).edit().putBoolean("enabled",false).apply();State.CONTEXT.clearAll();State.refreshRuntime(this);}
            else accessConsent();render();
        });
        button("1 • Autoriser l’accessibilité",this::accessConsent);
        button("2 • Annulation par notification (optionnel)",this::notificationConsent);
        button("Choisir les applications",this::chooseApps);button("Modifier les mots et domaines",this::editTerms);
        Switch strict=new Switch(this);strict.setText("Mode strict : tous les téléchargements reconnus");strict.setTextColor(INK);strict.setPadding(0,dp(14),0,dp(14));strict.setChecked(State.strict(this));body.addView(strict);
        strict.setOnCheckedChangeListener((v,yes)->{
            if(!yes){State.prefs(this).edit().putBoolean("strict",false).apply();return;}
            new AlertDialog.Builder(this).setTitle("Bloquer aussi les téléchargements ordinaires ?").setMessage("Le mode strict couvre toutes les commandes Télécharger / Enregistrer l’image reconnues dans les applications choisies, même sans indice sensible. Les fichiers normaux peuvent donc être bloqués aussi. Ce mode ne garantit pas de détecter un bouton masqué ou un téléchargement automatique.")
                .setNegativeButton("Annuler",(d,w)->strict.setChecked(false)).setOnCancelListener(d->strict.setChecked(false)).setPositiveButton("Activer",(d,w)->State.prefs(this).edit().putBoolean("strict",true).apply()).show();
        });
        button("Effacer le contexte temporaire",()->{State.CONTEXT.clearAll();Toast.makeText(this,"Contexte sensible effacé",Toast.LENGTH_SHORT).show();});
        button("Tutoriel et limites",this::help);
        button("Batterie et arrière-plan",()->new AlertDialog.Builder(this).setTitle("Garder la protection active").setMessage("Autorise les notifications et règle Serein Downloads sur Batterie → Sans restriction / activité en arrière-plan. Sur TECNO, vérifie aussi le lancement automatique. Ouvre à nouveau cette application après Forcer l’arrêt.").setPositiveButton("Ouvrir la fiche de l’app",(d,w)->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())))).setNegativeButton("Fermer",null).show());
        text("TEST LOCAL, SANS ENREGISTREMENT",13,MINT);
        EditText context=new EditText(this);context.setTextColor(INK);context.setHintTextColor(0xffaab9c6);context.setHint("Exemple : ecchi_image.jpg ou paysage.jpg");body.addView(context);
        TextView result=text("Ce test vérifie les mots, pas la compatibilité des autres applications.",14,0xffaab9c6);
        button("Tester la règle de téléchargement",()->{DownloadPolicy p=State.policy(this);result.setText(p.shouldBlock(true,p.sensitive(context.getText().toString()),State.strict(this))?"Commande Télécharger : blocage demandé par cette règle":"Commande Télécharger : aucun indice sensible dans ce texte");});
        text("Sans réseau, sans compte ni publicité. Aucune permission d’accès Internet ou aux fichiers. Version 0.1 • prototype à vérifier sur ton téléphone.",13,0xffaab9c6);
    }
    private TextView text(String t,int size,int color){TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(9),0,dp(9));body.addView(v);return v;}
    private Button button(String t,Runnable r){Button b=new Button(this);b.setText(t);b.setAllCaps(false);b.setTextColor(BG);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MINT));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(60));p.setMargins(0,dp(5),0,dp(5));body.addView(b,p);b.setOnClickListener(v->r.run());return b;}
    private void accessConsent(){
        new AlertDialog.Builder(this).setTitle("Lire le contexte et couvrir les boutons Télécharger ?")
            .setMessage("L’accessibilité donne techniquement accès au texte et à la structure des écrans. Serein Downloads limite l’analyse détaillée aux applications que tu choisis ; il vérifie seulement le nom du paquet pour quitter les autres applications. Les champs marqués mot de passe sont ignorés.\n\nUn mot ou domaine sensible rend le contexte de l’application sensible pendant deux minutes, pour reconnaître aussi un menu sans titre. Une nouvelle recherche ordinaire identifiée peut effacer ce contexte. Aucun texte, capture d’écran ou fichier n’est conservé ou envoyé.\n\nQuand une commande de téléchargement est reconnue dans ce contexte, une couverture tactile empêche d’appuyer sur ce bouton. Le mode strict peut couvrir aussi les commandes ordinaires. Tu gardes le contrôle de l’arrêt et de la désinstallation.")
            .setNegativeButton("Refuser",null).setPositiveButton("J’accepte",(d,w)->{
                State.prefs(this).edit().putBoolean("enabled",true).apply();
                if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1);
                try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(Exception e){Toast.makeText(this,"Ouvre les réglages d’accessibilité Android",Toast.LENGTH_LONG).show();}
            }).show();
    }
    private void notificationConsent(){
        new AlertDialog.Builder(this).setTitle("Autoriser la lecture des notifications ?")
            .setMessage("Android accorde un accès large aux notifications. Serein Downloads ne lit les détails que pour les applications choisies et n’agit que sur une notification de transfert détectable. Si un indice sensible est présent et qu’un vrai bouton Annuler est fourni, il envoie cette action à l’application.\n\nCela ne garantit pas l’arrêt : le fichier peut déjà être partiellement ou complètement reçu. Masquer une notification n’annule pas un téléchargement ; Serein Downloads ne se contente pas de la masquer et ne supprime pas de fichiers. Aucun texte de notification n’est enregistré ni transmis.")
            .setNegativeButton("Refuser",null).setPositiveButton("J’accepte, ouvrir Android",(d,w)->{try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(Exception e){Toast.makeText(this,"Ouvre Accès aux notifications dans les réglages Android",Toast.LENGTH_LONG).show();}}).show();
    }
    private void render(){if(state==null)return;state.setText((State.enabled(this)?"Protection demandée":"Protection désactivée")+"\nAccessibilité : "+(State.accessibility?"connectée":State.accessibilityGranted(this)?"autorisée, en attente":"à autoriser")+"\nNotifications de transfert : "+(State.notifications?"connectées":State.notificationGranted(this)?"autorisées, en attente":"non autorisées (optionnel)")+"\nVeille : "+(MonitorService.running?"active":"inactive")+"\n"+State.status);toggle.setText(State.enabled(this)?"Désactiver la protection":"Activer la protection");}
    private void editTerms(){EditText e=new EditText(this);e.setTextColor(INK);e.setMinLines(8);e.setGravity(Gravity.TOP);e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);e.setText(State.prefs(this).getString("terms",DownloadPolicy.DEFAULT_TERMS));ScrollView s=new ScrollView(this);s.addView(e);
        new AlertDialog.Builder(this).setTitle("Indices sensibles").setMessage("Un mot, domaine ou expression par ligne. Maximum 300 expressions de 80 caractères. « Hot » peut aussi concerner un contenu ordinaire.").setView(s).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{StringBuilder b=new StringBuilder();int count=0;for(String raw:e.getText().toString().split("[\\r\\n]+")){String t=raw.trim();if(t.isEmpty())continue;if(t.length()>80||++count>300){Toast.makeText(this,"Liste trop longue",Toast.LENGTH_LONG).show();return;}b.append(t).append('\n');}State.prefs(this).edit().putString("terms",b.toString()).apply();}).show();}
    private void chooseApps(){
        Map<String,String> labels=new HashMap<>();String[][] presets={{"com.brave.browser","Brave"},{"com.android.chrome","Chrome"},{"com.snaptube.premium","SnapTube"},{"com.pinterest","Pinterest"},{"com.pinterest.twa","Pinterest Lite"}};for(String[] p:presets)labels.put(p[0],p[1]);
        Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);PackageManager pm=getPackageManager();for(ResolveInfo r:pm.queryIntentActivities(i,0))if(!getPackageName().equals(r.activityInfo.packageName))labels.put(r.activityInfo.packageName,r.loadLabel(pm).toString());
        Set<String> chosen=State.apps(this);for(String p:chosen)if(!labels.containsKey(p))labels.put(p,p);List<String> packages=new ArrayList<>(labels.keySet());packages.sort((a,b)->labels.get(a).compareToIgnoreCase(labels.get(b)));String[] names=new String[packages.size()];boolean[] checked=new boolean[packages.size()];for(int n=0;n<packages.size();n++){names[n]=labels.get(packages.get(n));checked[n]=chosen.contains(packages.get(n));}
        new AlertDialog.Builder(this).setTitle("Applications ciblées").setMultiChoiceItems(names,checked,(d,n,yes)->{if(yes)chosen.add(packages.get(n));else chosen.remove(packages.get(n));}).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->State.prefs(this).edit().putStringSet("apps",new HashSet<>(chosen)).apply()).show();
    }
    private void help(){new AlertDialog.Builder(this).setTitle("Comment vérifier cette version").setMessage("1. Active la protection puis le service d’accessibilité Serein Downloads.\n2. Vérifie Brave, Chrome, SnapTube et Pinterest dans les applications ciblées.\n3. Ouvre une page dont le texte contient un mot de ta liste, puis son menu Télécharger / Enregistrer l’image : si la commande est exposée à Android, une couverture rouge doit la remplacer. La recherche elle-même reste accessible.\n4. Autorise éventuellement l’accès aux notifications pour tenter d’annuler les transferts déjà lancés par ces applications.\n5. Compare avec un fichier ordinaire et sans contexte sensible ; désactive le mode strict pour autoriser les téléchargements normaux.\n\nLIMITES : aucune analyse visuelle. Une image sans nom ou texte explicite peut passer. Certaines commandes ou notifications sont invisibles à Android, et un téléchargement très rapide peut finir avant détection. Les notifications émises uniquement par un gestionnaire système non sélectionné ne sont pas annulées. Une partie du fichier peut déjà exister. Aucun fichier existant n’est effacé.\n\nLe mode strict bloque aussi les téléchargements ordinaires reconnus. Pour une installation hors boutique, Android peut demander Autoriser les paramètres restreints dans la fiche de cette application avant l’accessibilité. Serein Downloads ne peut pas accorder lui-même cet accès.\n\nSi l’ancien Serein bloque déjà tes recherches, cette nouvelle application ne le désactive pas : leurs réglages sont séparés.").setPositiveButton("Compris",null).show();}
    @Override protected void onResume(){super.onResume();State.refreshRuntime(this);h.post(update);}
    @Override protected void onPause(){h.removeCallbacks(update);super.onPause();}
}
