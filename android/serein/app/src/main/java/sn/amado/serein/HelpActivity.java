package sn.amado.serein;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Offline tutorials, privacy notice and local rule test. No WebView or network. */
public class HelpActivity extends Activity {
    private LinearLayout body;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);getWindow().setStatusBarColor(MainActivity.BG);getWindow().setNavigationBarColor(MainActivity.BG);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(MainActivity.BG);body=new LinearLayout(this);body.setOrientation(1);
        int p=MainActivity.dp(this,22);body.setPadding(p,p,p,p);scroll.addView(body);setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((v,i)->{body.setPadding(p,p+i.getSystemWindowInsetTop(),p,p+i.getSystemWindowInsetBottom());return i;});
        button("Accueil Serein",()->{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();});
        button("Configurer les recherches",()->startActivity(new Intent(this,GuardSettingsActivity.class)));
        button("Lire la confidentialité",()->new AlertDialog.Builder(this).setTitle("Confidentialité").setMessage(read("privacy-fr.txt")).setPositiveButton("Fermer",null).show());
        text("TEST LOCAL DE TA LISTE",15,MainActivity.MINT);
        EditText input=new EditText(this);input.setSingleLine();input.setHint("Un mot ou une expression");input.setTextColor(MainActivity.INK);input.setHintTextColor(0xffaab9c6);body.addView(input);
        TextView result=text("Le test ne quitte pas Serein et n’enregistre pas la saisie.",14,0xffaab9c6);
        button("Tester sans connexion",()->{
            String terms=getSharedPreferences("serein",0).getString("search_terms",SearchPolicy.DEFAULT_TERMS);
            boolean blocked=new SearchPolicy(terms).blocked(input.getText().toString());
            result.setText(blocked?"Mot présent dans la liste : cette règle déclenche un blocage si l’application expose son texte à Serein.":"Aucune correspondance dans ta liste. Ce résultat ne concerne pas le filtre DNS.");
        });
        text(read("tutorials-fr.txt"),16,MainActivity.INK);
    }
    private TextView text(String t,int size,int color){TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setTextIsSelectable(true);v.setPadding(0,MainActivity.dp(this,12),0,MainActivity.dp(this,12));body.addView(v);return v;}
    private void button(String label,Runnable action){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextColor(MainActivity.BG);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(MainActivity.MINT));body.addView(b,new LinearLayout.LayoutParams(-1,MainActivity.dp(this,58)));b.setOnClickListener(v->action.run());}
    private String read(String asset){
        try(InputStream in=getAssets().open(asset);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }catch(IOException e){return "Le document n’a pas pu être ouvert.";}
    }
}
