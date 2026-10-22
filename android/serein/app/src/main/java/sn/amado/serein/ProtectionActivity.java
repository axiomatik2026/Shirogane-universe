package sn.amado.serein;

import android.app.*;
import android.content.*;
import android.net.VpnService;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Live service observations, separate from saved user preferences. No browsing content logged. */
public class ProtectionActivity extends Activity {
    private LinearLayout body; private TextView state;
    private final Handler handler=new Handler();
    private final Runnable refresh=new Runnable(){public void run(){render();handler.postDelayed(this,1000);}};
    private SharedPreferences prefs(){return getSharedPreferences("serein",0);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(MainActivity.BG);
        body=new LinearLayout(this);body.setOrientation(1);int p=MainActivity.dp(this,22);body.setPadding(p,p,p,p);scroll.addView(body);setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,i)->{body.setPadding(p,p+i.getSystemWindowInsetTop(),p,p+i.getSystemWindowInsetBottom());return i;});
        text("État réel des protections",25);state=text("",16);
        button("Activer / relancer les protections",()->new AlertDialog.Builder(this).setTitle("Relancer Serein ?")
            .setMessage("Active le VPN, le blocage local des recherches et la veille en arrière-plan avec tes listes actuelles. Le service d’accessibilité doit être autorisé séparément : il lit les champs de recherche et adresses accessibles des applications sélectionnées, et peut effacer un champ ou revenir à l’accueil. Aucun texte n’est enregistré. Un autre VPN ne peut pas être actif en même temps.")
            .setNegativeButton("Annuler",null).setPositiveButton("Relancer",(d,w)->{
                prefs().edit().putBoolean("search_guard",true).putBoolean("guard_background",true).commit();
                if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0)requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},91);
                GuardRuntimeService.ensure(this);
                stopService(new Intent(this,ShieldService.class));
                handler.postDelayed(()->requestVpn(),400);
            }).show());
        button("1 · Vérifier l’accessibilité",()->{
            new AlertDialog.Builder(this).setTitle("Reconnecter le blocage des mots")
                .setMessage("Ouvre Serein — blocage des recherches. Si Android l’indique activé mais que cet écran affiche « non connecté », désactive puis réactive son interrupteur. Android peut demander d’autoriser les paramètres restreints dans la fiche Serein.")
                .setNegativeButton("Fermer",null).setPositiveButton("Ouvrir",(d,w)->{
                    try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).putExtra(Intent.EXTRA_COMPONENT_NAME,new ComponentName(this,SearchGuardService.class)));}
                    catch(Exception e){startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
                }).show();
        });
        button("2 · Autoriser l’arrière-plan",()->{
            new AlertDialog.Builder(this).setTitle("Réglages TECNO / Android")
                .setMessage("Dans Batterie, choisis Sans restriction / Autoriser l’activité en arrière-plan. Dans Phone Master, autorise aussi le lancement automatique si cette option existe. Une fermeture normale de la fenêtre ne doit pas demander l’arrêt des services.")
                .setNegativeButton("Fermer",null).setPositiveButton("Fiche Serein",(d,w)->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())))).show();
        });
        button("3 · Réglages VPN permanent",()->{
            new AlertDialog.Builder(this).setTitle("Reprise gérée par Android")
                .setMessage("Serein prend maintenant en charge « VPN permanent / Toujours actif ». Tu peux l’activer dans la roue dentée de Serein. Ne coche pas « Bloquer les connexions sans VPN » : ce service transporte seulement le DNS. Ce réglage concerne le VPN, pas le service de lecture des recherches.")
                .setNegativeButton("Fermer",null).setPositiveButton("Ouvrir",(d,w)->startActivity(new Intent(Settings.ACTION_VPN_SETTINGS))).show();
        });
        button("Applications et mots bloqués",()->startActivity(new Intent(this,GuardSettingsActivity.class)));
        text("Les compteurs ci-dessus comptent les opérations depuis le dernier lancement du processus. Aucun domaine ni mot consulté n’est conservé. Si les compteurs restent à zéro après une recherche ou l’ouverture d’un site, le service ne reçoit pas ces opérations : une autorisation, la sélection des applications ou le DNS sécurisé du navigateur est à vérifier.",14);
        text("Pour tester : vérifie d’abord un site ordinaire, puis un domaine de ta liste dans un nouvel onglet. Ferme ensuite la fenêtre Serein et refais le test. Un arrêt forcé ou une restriction du téléphone peut interrompre les services.",14);
    }
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(MainActivity.INK);t.setTextSize(size);t.setPadding(0,16,0,16);body.addView(t);return t;}
    private void button(String name,Runnable action){Button b=new Button(this);b.setText(name);b.setAllCaps(false);body.addView(b);b.setOnClickListener(v->action.run());}
    private void requestVpn(){Intent consent=VpnService.prepare(this);if(consent!=null)startActivityForResult(consent,92);else startVpn();}
    private void startVpn(){try{startForegroundService(new Intent(this,ShieldService.class));}catch(RuntimeException e){Toast.makeText(this,"VPN non démarré : "+e.getClass().getSimpleName(),Toast.LENGTH_LONG).show();}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==92&&result==RESULT_OK)startVpn();}
    private void render(){
        boolean permitted=GuardRuntimeService.accessibilityEnabled(this);
        boolean keywords=prefs().getBoolean("search_guard",false);
        long age=SearchGuardService.lastEvent==0?-1:(SystemClock.elapsedRealtime()-SearchGuardService.lastEvent)/1000;
        state.setText("VPN : "+(ShieldService.running?"tunnel établi":ShieldService.starting?"démarrage":"arrêté")+
            "\nFiltre distant : "+(ShieldService.remoteReady?"réponse reçue":"non confirmé")+
            "\nDNS reçus : "+ShieldService.requests.get()+" · bloqués : "+ShieldService.blocked.get()+"\nRéponses distantes : "+ShieldService.replies.get()+" · erreurs : "+ShieldService.errors.get()+
            (ShieldService.lastError.isEmpty()?"":"\nDernière erreur DNS : "+ShieldService.lastError)+
            "\n"+ShieldService.lastLifecycle+
            "\n\nMots / liens : "+(keywords?"demandés":"désactivés")+
            "\nAccessibilité : "+(!permitted?"autorisation manquante":SearchGuardService.connected?"connectée":"autorisée mais non connectée")+
            "\nVeille : "+(GuardRuntimeService.running?"service au premier plan":"arrêtée")+
            "\nApplications choisies : "+prefs().getStringSet("search_apps",SearchPolicy.DEFAULT_APPS).size()+
            "\nÉvénements reçus : "+SearchGuardService.events+" · fenêtres lues : "+SearchGuardService.scans+"\nDétections : "+SearchGuardService.blocks+(age<0?"":" · dernier événement il y a "+age+" s")+
            (SearchGuardService.lastError.isEmpty()?"":"\nDernière erreur locale : "+SearchGuardService.lastError)+
            (ShieldService.problem.isEmpty()?"":"\n\n"+ShieldService.problem)+(GuardRuntimeService.problem.isEmpty()?"":"\n"+GuardRuntimeService.problem));
    }
    @Override protected void onResume(){super.onResume();handler.post(refresh);}
    @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
