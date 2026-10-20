package sn.amado.serein;

import java.net.IDN;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Pure policy shared by the VPN and the restricted browser. No browsing logs. */
public final class Policy {
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
    public static boolean allowedUrl(String url, String custom) {
        try {
            URI u=new URI(url);
            if(!"https".equalsIgnoreCase(u.getScheme()) || u.getUserInfo()!=null || blocked(u.getHost(),custom)) return false;
            String decoded=URLDecoder.decode(url,StandardCharsets.UTF_8.name()).toLowerCase(Locale.ROOT);
            for(String word:Arrays.asList("porn","porno","hentai","nsfw","nudes","nude","xxx","rule34","sextape","sexy","erotique","érotique")) {
                if(decoded.contains(word)) return false;
            }
            return true;
        } catch(Exception e) { return false; }
    }
}
