package sn.amado.downloadguard;

import java.text.Normalizer;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

/** Pure local rules: detects text metadata, never claims visual image recognition. */
public final class DownloadPolicy {
    public static final String DEFAULT_TERMS="ecchi\nhentai\nhot\nhot classic\nsexy\nnsfw\nporn\nporno\npornographique\npornography\nxxx\nnude\nnudes\nnaked\nrule34\nrule 34\nr34\nlewd\nahegao\noppai\nboobs\ntits\ntopless\npornhub\nxvideos\nxnxx\nxhamster\nnhentai\nhanime";
    public static final Set<String> DEFAULT_APPS=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "com.brave.browser","com.android.chrome","com.snaptube.premium","com.pinterest","com.pinterest.twa")));
    public static String normalize(String raw){
        if(raw==null)return "";
        return Normalizer.normalize(raw,Normalizer.Form.NFKD).toLowerCase(Locale.ROOT)
            .replaceAll("[\\p{M}\\p{Cf}]","").replace('’','\'').replaceAll("\\s+"," ").trim();
    }
    private final List<Pattern> words=new ArrayList<>();
    public DownloadPolicy(String terms){
        Set<String> seen=new HashSet<>();
        for(String s:terms.split("[\\r\\n]+")){
            s=normalize(s);if(s.isEmpty()||s.length()>80||!seen.add(s))continue;
            words.add(Pattern.compile("(?<![\\p{L}\\p{N}])"+Pattern.quote(s).replace(" ","\\E\\s+\\Q")+"(?![\\p{L}\\p{N}])"));
            if(words.size()>=300)break;
        }
    }
    public boolean sensitive(String raw){
        String s=normalize(raw);if(s.length()>24000)s=s.substring(0,24000);
        try{s=URLDecoder.decode(s,"UTF-8");}catch(Exception ignored){}
        // Filenames often separate terms with underscore, dot or hyphen.
        s=s.replace('_',' ');
        for(Pattern p:words)if(p.matcher(s).find())return true;
        return false;
    }
    public static boolean downloadAction(String label,String id,boolean editable){
        if(editable)return false;
        String s=normalize(label),view=normalize(id);
        // Pinterest's generic Save/Enregistrer saves to a board, not to the device.
        if(s.equals("save")||s.equals("enregistrer")||s.equals("sauvegarder"))return false;
        if(s.length()<96 && (s.matches("(?:download|telecharger)(?:\\s+(?:image|video|file|photo|picture|audio|l'image|la video|le fichier|l'audio|to device))?")
            ||s.matches("(?:save|enregistrer|sauvegarder) (?:image|video|file|photo|picture|l'image|la video|le fichier|sur l'appareil|to device)")))return true;
        if(!s.isEmpty() && !(s.equals("download")||s.equals("telecharger")))return false;
        return view.matches(".*(?:[:/_]|^)(?:download|save_image|save_video|download_image|download_video)_(?:button|btn|action)(?:$|[_/].*)")
            ||view.endsWith(":id/download")||view.endsWith(":id/download_button")||view.endsWith(":id/btn_download");
    }
    public static boolean cancellationAction(String label){
        String s=normalize(label);
        return s.equals("cancel")||s.equals("annuler")||s.equals("cancel download")||s.equals("annuler le telechargement")||s.equals("annuler telechargement");
    }
    public static boolean downloadNotification(String title,String body,boolean progress,boolean ongoing){
        String s=normalize(title+" "+body);
        if(s.contains("upload")||s.contains("envoi")||s.contains("sending")||s.contains("mise en ligne"))return false;
        return (progress||ongoing)&&(s.contains("download")||s.contains("telecharg")||s.matches(".*\\.(?:mp4|webm|mkv|jpg|jpeg|png|gif|zip)(?:\\s|$).*"));
    }
    public boolean shouldBlock(boolean recognizedDownload,boolean risky,boolean strict){return recognizedDownload&&(risky||strict);}
}
