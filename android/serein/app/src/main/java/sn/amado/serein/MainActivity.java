package sn.amado.serein;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
    static final int BG=Color.rgb(13,23,33), INK=Color.rgb(233,242,248), MINT=Color.rgb(112,225,188);
    LinearLayout body; TextView status, detail; Button toggle; Handler handler=new Handler();
    final Runnable refresh=new Runnable(){ public void run(){ renderStatus(); handler.postDelayed(this,1200); }};
    static int dp(Context c,int n){return (int)(n*c.getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b){ super.onCreate(b); build(); }
    TextView text(String t,int size,int color){ TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setPadding(0,dp(this,9),0,dp(this,9));body.addView(v);return v; }
    Button button(String t,Runnable action){ Button v=new Button(this);v.setText(t);v.setAllCaps(false);v.setTextColor(BG);v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MINT));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(this,56));p.setMargins(0,dp(this,8),0,dp(this,8));body.addView(v,p);v.setOnClickListener(w->action.run());return v; }
    void build(){
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
        body=new LinearLayout(this);body.setOrientation(1);int p=dp(this,24);body.setPadding(p,p,p,p);scroll.addView(body);setContentView(scroll);scroll.setOnApplyWindowInsetsListener((v,i)->{body.setPadding(p,p+i.getSystemWindowInsetTop(),p,p+i.getSystemWindowInsetBottom());return i;});
        text("S E R E I N",14,MINT);text("Reprends ton espace.",32,INK);
        text("Moins de tentations. Une navigation choisie.",16,0xff9aafbf);
        status=text("Protection désactivée",24,INK);detail=text("",14,0xff9aafbf);
        toggle=button("Activer la protection",()->{
            if(ShieldService.running || ShieldService.starting){stopService(new Intent(this,ShieldService.class));return;}
            new AlertDialog.Builder(this).setTitle("Activer le filtre DNS ?").setMessage("Serein utilise le créneau VPN Android pour filtrer les domaines. Les requêtes DNS sont chiffrées via HTTPS et envoyées au filtre adulte CleanBrowsing. Si ce serveur devient inaccessible, le VPN sera arrêté et une alerte indiquera que la protection est désactivée. Serein ne stocke pas ton historique. Un autre VPN ne peut pas fonctionner en même temps. Le filtre ne reconnaît pas les photos dans les réseaux sociaux.").setNegativeButton("Annuler",null).setPositiveButton("Activer",(d,w)->{
                if(Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},8);
                Intent consent=android.net.VpnService.prepare(this);if(consent!=null)startActivityForResult(consent,7);else startShield();
            }).show();
        });
        button("Ouvrir le navigateur sans images",()->startActivity(new Intent(this,BrowserActivity.class)));
        text("01   FILTRE ADULTE",13,MINT);text("Domaines pornographiques et hentai connus, complétés par le filtre adulte CleanBrowsing.",16,INK);
        text("02   ZÉRO IMAGE PAR DÉFAUT",13,MINT);text("Le navigateur Serein désactive les images, vidéos, scripts et fenêtres externes. Les sites complexes peuvent moins bien fonctionner.",16,INK);
        text("03   RECHERCHES DANS LES APPLICATIONS",13,MINT);
        text("Bloque les mots sensibles dans les recherches accessibles de Pinterest et des applications choisies. L’accès d’accessibilité doit être activé séparément.",15,INK);
        button("Configurer le blocage des recherches",()->startActivity(new Intent(this,GuardSettingsActivity.class)));
        button("Modifier ma liste noire",this::editDomains);
        button("Réglages VPN Android",()->{try{startActivity(new Intent(Settings.ACTION_VPN_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});
        text("Ce que Serein peut faire",20,INK);
        text("Le VPN filtre les noms de domaine, pas le contenu de chaque image. Une photo suggestive sur un site autorisé, une image enregistrée ou une publication dans une autre app peut rester visible. Pour éviter leurs images, utilise le navigateur Serein ou évite ces applications.",14,0xffaab9c6);
        text("Le DNS sécurisé propre à un navigateur, les adresses IP directes et certains proxys peuvent contourner ce filtre. Désactive le DNS sécurisé personnalisé de tes autres navigateurs. Ce n’est pas un verrou inviolable.",14,0xffaab9c6);
        text("Sur certains téléphones TECNO, autorise Serein à fonctionner en arrière-plan. Vérifie l’état après un redémarrage : l’activation est manuelle. Ne coche pas « Bloquer les connexions sans VPN » : Serein ne transporte que le DNS.",14,0xffaab9c6);
        text("Version 1.1.0 • Android 8+ • Sans compte ni publicité",12,0xff758b9d);
    }
    void editDomains(){
        EditText input=new EditText(this);input.setTextColor(INK);input.setHint("exemple.com\nautresite.com");input.setHintTextColor(0xffaab9c6);input.setMinLines(5);input.setGravity(Gravity.TOP);input.setText(getSharedPreferences("serein",0).getString("domains",""));
        new AlertDialog.Builder(this).setTitle("Domaines à bloquer").setMessage("Un domaine par ligne. Les sous-domaines seront bloqués aussi.").setView(input).setNegativeButton("Annuler",null).setPositiveButton("Enregistrer",(d,w)->{
            StringBuilder clean=new StringBuilder();for(String s:input.getText().toString().split("[\\s,;]+")){if(s.isEmpty())continue;String h=Policy.domain(s);if(h.isEmpty()||!h.contains(".")){Toast.makeText(this,"Domaine invalide : "+s,Toast.LENGTH_LONG).show();return;}clean.append(h).append('\n');}
            getSharedPreferences("serein",0).edit().putString("domains",clean.toString()).apply();Toast.makeText(this,"Liste enregistrée",Toast.LENGTH_SHORT).show();
        }).show();
    }
    void renderStatus(){
        if(status==null)return;
        boolean on=ShieldService.running, busy=ShieldService.starting;
        status.setText(on?"Filtrage DNS actif":busy?"Vérification du filtre…":"Protection désactivée");
        status.setTextColor(on?MINT:INK);
        toggle.setText(on?"Désactiver la protection":busy?"Annuler l’activation":"Activer la protection");
        detail.setText(!ShieldService.problem.isEmpty()?ShieldService.problem:on?"Filtre adulte + liste noire • connexion HTTPS":busy?"Test du serveur avant l’activation du VPN. Internet reste disponible.":"Active le filtre. Tu peux garder ton navigateur habituel.");
    }
    void startShield(){try{startForegroundService(new Intent(this,ShieldService.class));}catch(Exception e){Toast.makeText(this,"Activation impossible : "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==7&&result==RESULT_OK)startShield();}
    @Override protected void onResume(){super.onResume();handler.post(refresh);}
    @Override protected void onPause(){handler.removeCallbacks(refresh);super.onPause();}
}
