package sn.amado.downloadguard;

import java.util.*;

/** Only expiry timestamps are kept, never page text. Context expires after 2 min. */
public final class RiskSession {
    private final Map<String,Long> until=new HashMap<>();
    public static final long TTL=120000;
    public synchronized void mark(String pkg,long now){until.put(pkg,now+TTL);}
    public synchronized boolean risky(String pkg,long now){Long end=until.get(pkg);if(end==null)return false;if(now>=end){until.remove(pkg);return false;}return true;}
    public synchronized long remaining(String pkg,long now){return risky(pkg,now)?until.get(pkg)-now:0;}
    public synchronized void clear(String pkg){until.remove(pkg);}
    public synchronized void clearAll(){until.clear();}
}
