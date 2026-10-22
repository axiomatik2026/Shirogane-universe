package sn.amado.serein;

import java.net.IDN;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Pure policy used by the VPN and accessible browser address fields. No browsing logs. */
public final class Policy {
    private static final SearchPolicy SEARCH=new SearchPolicy(SearchPolicy.DEFAULT_TERMS);
    private static final Set<String> BASE = new HashSet<>(Arrays.asList(
        "pornhub.com","xvideos.com","xnxx.com","xhamster.com","youporn.com",
        "redtube.com","spankbang.com","porn.com","rule34.xxx","rule34.paheal.net",
        "e621.net","e926.net","nhentai.net","nhentai.to","hentaihaven.xxx",
        "hanime.tv","hentai.tv","hentaifox.com","hitomi.la","exhentai.org",
        "e-hentai.org","gelbooru.com","danbooru.donmai.us","sankakucomplex.com",
        "onlyfans.com","fansly.com","chaturbate.com","stripchat.com","bongacams.com"));
    public static String domain(String raw) {
        if (raw == null) return "";
        try {
            String s=raw.trim().toLowerCase(Locale.ROOT);
            URI u=new URI(s.contains("://") ? s : "https://"+s);
            String h=u.getHost();
            if(h==null) return "";
            h=IDN.toASCII(h).toLowerCase(Locale.ROOT);
            while(h.endsWith(".")) h=h.substring(0,h.length()-1);
            return h;
        } catch(Exception e) { return ""; }
    }
    public static boolean blocked(String host, String custom) {
        String h=domain(host);
        if(h.isEmpty()) return true;
        Set<String> all=new HashSet<>(BASE);
        for(String s:custom.split("[\\s,;]+")) { String d=domain(s); if(!d.isEmpty()) all.add(d); }
        for(String d:all) if(h.equals(d)||h.endsWith("."+d)) return true;
        return false;
    }
    /** Only a recognizable address in an address bar, never arbitrary page prose. */
    public static boolean blockedAddress(String text,String custom){
        String host=domain(text);return host.contains(".")&&blocked(host,custom);
    }
    public static boolean addressMetadata(String id,String hint,String description){
        String meta=SearchPolicy.normalize((id==null?"":id)+" "+(hint==null?"":hint)+" "+(description==null?"":description));
        for(String token:new String[]{"url_bar","urlbar","omnibox","address_bar","location_view","mozac_browser_toolbar_url_view","barre d'adresse","address and search"})
            if(meta.contains(token))return true;
        return false;
    }
    public static boolean allowedUrl(String url, String custom) {
        try {
            URI u=new URI(url);
            if(!"https".equalsIgnoreCase(u.getScheme()) || u.getUserInfo()!=null || blocked(u.getHost(),custom)) return false;
            String content=(u.getRawPath()==null?"":u.getRawPath())+" "+(u.getRawQuery()==null?"":u.getRawQuery());
            content=URLDecoder.decode(content,StandardCharsets.UTF_8.name());
            if(SEARCH.blocked(content))return false;
            return true;
        } catch(Exception e) { return false; }
    }
}
