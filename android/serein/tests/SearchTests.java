import sn.amado.serein.*;
public class SearchTests {
 static int count;
 static void check(boolean ok,String label){count++;if(!ok)throw new AssertionError(label);}
 public static void main(String[] args){
  SearchPolicy p=new SearchPolicy(SearchPolicy.DEFAULT_TERMS);
  for(String q:new String[]{"ecchi","ECCHI","anime ecchi","hot classic","hot classique","hentai anime","sexy","nsfw","nudes","18+","e\u200bcchi","ｅｃｃｈｉ","écchi","rule 34"})check(p.blocked(q),"sensitive query "+q);
  for(String q:new String[]{"anime","Naruto","Dakar météo","informatique","classic music","hotmail","hotspot","hotdog","hotels Dakar","classicCodex","github"})check(!p.blocked(q),"ordinary query "+q);
  check(p.shouldBlock("ecchi",false,true,true,false),"Pinterest search text");
  check(!p.shouldBlock("ecchi",true,true,true,true),"password never checked");
  check(!p.shouldBlock("ecchi",false,false,true,true),"hidden text never checked");
  check(!p.shouldBlock("ecchi",false,true,false,false),"ordinary non-search text ignored");
  check(p.shouldBlock("ecchi",false,true,false,true),"strict mode checks visible text");
  check(!p.shouldBlock("anime",false,true,true,true),"normal search remains accessible");
  check(SearchPolicy.searchMetadata("com.pinterest:id/search_bar_text","",""),"Pinterest header metadata");
  check(SearchPolicy.searchMetadata("","Rechercher des idées",""),"French hint");
  check(SearchPolicy.searchMetadata("com.android.chrome:id/url_bar","",""),"browser address field");
  check(SearchPolicy.searchMetadata("com.instagram.android:id/action_bar_search_edit_text","",""),"Instagram search metadata");
  check(!SearchPolicy.searchMetadata("message_body","Message",""),"message not mistaken for search");
  check(SearchPolicy.DEFAULT_APPS.contains("com.pinterest"),"Pinterest preset");
  check(!SearchPolicy.DEFAULT_APPS.contains("com.android.settings"),"system settings excluded");
  check(!SearchPolicy.DEFAULT_APPS.contains("sn.amado.serein"),"Serein itself excluded");
  SearchPolicy custom=new SearchPolicy("hot classic\nmon expression\na+b\na+b");
  check(custom.blocked("HOT   CLASSIC"),"multiword spaces normalized");
  check(custom.blocked("mon expression"),"custom phrase");
  check(custom.blocked("a+b"),"regex characters treated literally");
  check(!custom.blocked("aaab"),"custom term not interpreted as regex");
  check(!custom.blocked("hot"),"custom phrase does not block lone word");
  check(!new SearchPolicy("").blocked("ecchi"),"user can empty blocklist");
  check(!p.blocked(null),"null text safe");
  System.out.println(count+" search guard assertions passed");
 }
}
