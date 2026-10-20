package sn.amado.serein;

import android.app.*;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.accessibility.AccessibilityManager;
import android.widget.*;
import java.util.*;

/** Independent, explicit consent and settings for on-device accessibility filtering. */
public class GuardSettingsActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout body;
    private TextView state;
    private Button toggle;
    private final Handler handler=new Handler();
    private final Runnable refresh=new Runnable(){public void run(){render();handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle b){
        super.onCreate(b);prefs=getSharedPreferences("serein",0);
        getWindow().setStatusBarColor(MainActivity.BG);getWindow().setNavigationBarColor(MainActivity.BG);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(MainActivity.BG);
        body=new LinearLayout(this);body.setOrientation(1);int p=MainActivity.dp(this,22);body.setPadding(p,p,p,p);scroll.addView(body);setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,i)->{body.setPadding(p,p+i.getSystemWindowInsetTop(),p,p+i.getSystemWindowInsetBottom());return i;});
        text("SEREIN • RECHERCHES",13,MainActivity.MINT);
        text("Protège aussi tes applications.",28,MainActivity.INK);
        state=text("",19,MainActivity.MINT);
        text("Détecte les mots bloqués dans les champs de recherche accessibles des applications sélectionnées. Une détection renvoie à l’accueil Android ; le champ est effacé si l’application le permet.",15,MainActivity.INK);
        toggle=button("Activer le blocage des recherches",()->{
            if(prefs.getBoolean("search_guard",false)){prefs.edit().putBoolean("search_guard",false).apply();render();}
            else consent();
        });
        button("Choisir les applications",this::chooseApps);
        button("Modifier les mots bloqués",this::editTerms);
        Switch strict=new Switch(this);strict.setText("Mode strict : texte visible aussi");strict.setTextColor(MainActivity.INK);strict.setPadding(0,p,0,p);strict.setChecked(prefs.getBoolean("search_strict",false));body.addView(strict);
        strict.setOnCheckedChangeListener((v,yes)->{
            if(!yes){prefs.edit().putBoolean("search_strict",false).apply();return;}
            new AlertDialog.Builder(this).setTitle("Étendre la détection ?").setMessage("Dans les applications choisies, Serein vérifiera aussi le texte visible des publications, descriptions et suggestions. Un mot comme « hot » dans un contenu ordinaire pourra donc fermer l’application. Aucun texte n’est enregistré ni transmis.").setNegativeButton("Annuler",(d,w)->strict.setChecked(false)).setOnCancelListener(d->strict.setChecked(false)).setPositiveButton("Activer",(d,w)->prefs.edit().putBoolean("search_strict",true).apply()).show();
        });
        text("Les mots « ecchi », « hentai », « hot », « hot classic », « sexy », « nsfw » et d’autres sont préremplis. Ils sont modifiables. « Hot » peut aussi bloquer une recherche innocente contenant ce mot.",14,0xffaab9c6);
        text("Aucune analyse d’image : une photo seule ou une vidéo sans mot détectable peut passer. Certaines applications cachent leurs champs à Android. Si Pinterest n’est pas détecté, essaie le mode strict. Le filtre de recherches reste indépendant du VPN DNS.",14,0xffaab9c6);
        button("Réglages d’accessibilité Android",()->{
            if(!prefs.getBoolean("search_guard",false))consent();else openAccessibility();
        });
        text("Si Android indique « Paramètre restreint », ouvre Réglages → Applications → Serein → menu ⋮ → Autoriser les paramètres restreints, puis reviens activer ce service. Selon le téléphone, cette option peut être ailleurs. Serein ne peut pas accorder lui-même cette autorisation.",14,0xffaab9c6);
        button("Retour à Serein",this::finish);
    }
    private TextView text(String t,int size,int color){TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,MainActivity.dp(this,10),0,MainActivity.dp(this,10));body.addView(v);return v;}
    private Button button(String t,Runnable r){Button v=new Button(this);v.setText(t);v.setAllCaps(false);v.setTextColor(MainActivity.BG);v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.MINT));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,MainActivity.dp(this,58));p.setMargins(0,MainActivity.dp(this,7),0,MainActivity.dp(this,7));body.addView(v,p);v.setOnClickListener(w->r.run());return v;}
    private void consent(){
        new AlertDialog.Builder(this).setTitle("Autoriser la lecture des recherches ?")
            .setMessage("Cette fonction utilise le service d’accessibilité Android. Android lui donne accès au contenu textuel de l’écran et à des actions de navigation. Serein limite sa vérification aux applications que tu sélectionnes et aux champs de recherche identifiables ; le mode strict optionnel inclut leur texte visible.\n\nQuand un mot bloqué apparaît, Serein peut effacer le champ et revenir à l’accueil Android. Aucun texte de recherche ni capture d’écran n’est enregistré ou envoyé ; les mots de passe signalés par Android sont ignorés. Tu peux désactiver cette fonction ici ou dans les réglages Android.\n\nLe DNS reste un service distinct : ses requêtes sont envoyées au résolveur déclaré dans Serein.")
            .setNegativeButton("Refuser",null).setPositiveButton("J’accepte, ouvrir les réglages",(d,w)->{
                prefs.edit().putBoolean("search_guard",true).apply();openAccessibility();
            }).show();
    }
    private void openAccessibility(){
        try{Intent i=new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);i.putExtra(":settings:fragment_args_key",new ComponentName(this,SearchGuardService.class).flattenToString());startActivity(i);}
        catch(Exception e){Toast.makeText(this,"Ouvre Réglages → Accessibilité → Serein",Toast.LENGTH_LONG).show();}
    }
    private boolean systemEnabled(){
        AccessibilityManager am=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        for(AccessibilityServiceInfo i:am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            ServiceInfo s=i.getResolveInfo().serviceInfo;
            if(getPackageName().equals(s.packageName)&&SearchGuardService.class.getName().equals(s.name))return true;
        }
        return false;
    }
    private void render(){
        boolean yes=prefs.getBoolean("search_guard",false),system=systemEnabled();
        int n=prefs.getStringSet("search_apps",SearchPolicy.DEFAULT_APPS).size();
        String terms=prefs.getString("search_terms",SearchPolicy.DEFAULT_TERMS).trim();
        state.setText(!yes?"Blocage des recherches désactivé":!system?"Autorisation d’accessibilité à activer":n==0?"Aucune application sélectionnée":terms.isEmpty()?"Aucun mot bloqué":SearchGuardService.connected?"Blocage des recherches actif":"Service autorisé • connexion en cours");
        toggle.setText(yes?"Désactiver le blocage des recherches":"Activer le blocage des recherches");
    }
    private void editTerms(){
        EditText input=new EditText(this);input.setTextColor(MainActivity.INK);input.setMinLines(8);input.setGravity(Gravity.TOP);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);input.setText(prefs.getString("search_terms",SearchPolicy.DEFAULT_TERMS));
        ScrollView scroll=new ScrollView(this);scroll.addView(input);
        new AlertDialog.Builder(this).setTitle("Mots ou expressions bloqués").setMessage("Un mot ou une expression par ligne. Les mots entiers sont détectés, sans tenir compte des majuscules ni des accents. Maximum 300 expressions de 80 caractères.").setView(scroll).setNeutralButton("Liste par défaut",(d,w)->prefs.edit().putString("search_terms",SearchPolicy.DEFAULT_TERMS).apply()).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{
            String raw=input.getText().toString();StringBuilder clean=new StringBuilder();int count=0;
            for(String term:raw.split("[\\r\\n]+")){term=term.trim();if(term.isEmpty())continue;if(term.length()>80||++count>300){Toast.makeText(this,"Maximum 300 expressions de 80 caractères",Toast.LENGTH_LONG).show();return;}clean.append(term).append('\n');}
            prefs.edit().putString("search_terms",clean.toString()).apply();
        }).show();
    }
    private void chooseApps(){
        PackageManager pm=getPackageManager();Intent launch=new Intent(Intent.ACTION_MAIN);launch.addCategory(Intent.CATEGORY_LAUNCHER);
        Map<String,String> labels=new HashMap<>();
        for(ResolveInfo r:pm.queryIntentActivities(launch,0)){
            String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;
            labels.put(pkg,r.loadLabel(pm).toString());
        }
        Set<String> chosen=new HashSet<>(prefs.getStringSet("search_apps",SearchPolicy.DEFAULT_APPS));
        // Keep named presets available even if package visibility omits an installed app.
        String[][] presets={{"com.pinterest","Pinterest"},{"com.pinterest.twa","Pinterest Lite"},{"com.instagram.android","Instagram"},{"com.google.android.youtube","YouTube"},{"com.reddit.frontpage","Reddit"},{"com.android.chrome","Chrome"},{"org.mozilla.firefox","Firefox"},{"com.facebook.katana","Facebook"},{"com.facebook.lite","Facebook Lite"},{"com.twitter.android","X / Twitter"},{"com.zhiliaoapp.musically","TikTok"},{"com.ss.android.ugc.trill","TikTok (autre version)"}};
        for(String[] pair:presets)if(!labels.containsKey(pair[0]))labels.put(pair[0],pair[1]+" (si installé)");
        for(String pkg:chosen)if(!labels.containsKey(pkg))labels.put(pkg,pkg);
        List<String> packages=new ArrayList<>(labels.keySet());packages.sort((a,b)->labels.get(a).compareToIgnoreCase(labels.get(b)));
        String[] names=new String[packages.size()];boolean[] checked=new boolean[packages.size()];
        for(int i=0;i<packages.size();i++){names[i]=labels.get(packages.get(i));checked[i]=chosen.contains(packages.get(i));}
        new AlertDialog.Builder(this).setTitle("Applications à surveiller").setMultiChoiceItems(names,checked,(d,which,yes)->{if(yes)chosen.add(packages.get(which));else chosen.remove(packages.get(which));})
            .setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{prefs.edit().putStringSet("search_apps",new HashSet<>(chosen)).apply();render();}).show();
    }
    @Override protected void onResume(){super.onResume();handler.post(refresh);}
    @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
