package sn.amado.serein;

/** Local lifecycle decisions deliberately have no dependency on connectivity. */
public final class LocalGuardState {
    public enum State { DISABLED, PERMISSION_REQUIRED, NO_RULES, CONNECTING, ACTIVE }
    public static boolean shouldRun(boolean enabled,boolean background,boolean permission,int apps,int terms){
        return enabled && background && permission && apps>0 && terms>0;
    }
    public static State state(boolean enabled,boolean permission,boolean connected,int apps,int terms){
        if(!enabled)return State.DISABLED;
        if(!permission)return State.PERMISSION_REQUIRED;
        if(apps<=0||terms<=0)return State.NO_RULES;
        return connected?State.ACTIVE:State.CONNECTING;
    }
    public static boolean shouldResumeVpn(boolean enabled,boolean resumeOnBoot,boolean packageUpdate){
        return enabled&&(packageUpdate||resumeOnBoot);
    }
    public static int countTerms(String terms){
        if(terms==null)return 0;int count=0;
        for(String s:terms.split("[\\r\\n]+"))if(!s.trim().isEmpty())count++;
        return count;
    }
}
