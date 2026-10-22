package sn.amado.serein;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/** Local, deterministic search policy. No screenshots, uploads or browsing log. */
public final class SearchPolicy {
    public static final String DEFAULT_TERMS="ecchi\nhentai\nhot\nhot classic\nsexy\nnsfw\nporn\nporno\npornographique\npornography\nxxx\nnude\nnudes\nnaked\nrule34\nrule 34\nr34\nlewd\nahegao\noppai\nboobs\ntits\ntopless\n18+";
    public static final Set<String> DEFAULT_APPS=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        "com.pinterest","com.pinterest.twa","com.instagram.android","com.google.android.youtube",
        "com.reddit.frontpage","com.brave.browser","com.android.chrome","org.mozilla.firefox","com.facebook.katana",
        "com.facebook.lite","com.twitter.android","com.zhiliaoapp.musically","com.ss.android.ugc.trill")));
    public static String normalize(String text){
        if(text==null)return "";
        String s=Normalizer.normalize(text,Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
        return s.replaceAll("[\\p{M}\\p{Cf}]","").replaceAll("\\s+"," ").trim();
    }
    private final List<Pattern> patterns=new ArrayList<>();
    public SearchPolicy(String terms){
        Set<String> seen=new HashSet<>();
        for(String raw:terms.split("[\\r\\n]+")){
            String term=normalize(raw);if(term.isEmpty()||term.length()>80||!seen.add(term))continue;
            String phrase=Pattern.quote(term).replace(" ","\\E\\s+\\Q");
            patterns.add(Pattern.compile("(?<![\\p{L}\\p{N}])"+phrase+"(?![\\p{L}\\p{N}])"));
            if(patterns.size()>=300)break;
        }
    }
    public boolean blocked(String query){
        String text=normalize(query);if(text.length()>4096)text=text.substring(0,4096);
        for(Pattern p:patterns)if(p.matcher(text).find())return true;
        return false;
    }
    public static boolean searchMetadata(String id,String hint,String description){
        String meta=normalize((id==null?"":id)+" "+(hint==null?"":hint)+" "+(description==null?"":description));
        // Resource IDs commonly identify collapsed search/result headers too.
        for(String token:new String[]{"search","query","recherche","rechercher","chercher","buscar","url_bar","urlbar","omnibox","address_bar","location_view"})
            if(meta.contains(token))return true;
        return false;
    }
    public boolean shouldBlock(String text,boolean password,boolean visible,boolean searchField,boolean strict){
        return !password && visible && (searchField||strict) && blocked(text);
    }
}
