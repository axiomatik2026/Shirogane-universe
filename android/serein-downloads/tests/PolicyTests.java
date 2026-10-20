import sn.amado.downloadguard.*;
public class PolicyTests {
 static int count;static void check(boolean x,String name){count++;if(!x)throw new AssertionError(name);}
 public static void main(String[] args){
  DownloadPolicy p=new DownloadPolicy(DownloadPolicy.DEFAULT_TERMS);
  for(String s:new String[]{"ecchi_image.jpg","hentai.mp4","HOT classic","ｅｃｃｈｉ","e\u200bcchi","https://www.pornhub.com/test","%68entai-video.webm"})check(p.sensitive(s),"sensitive metadata "+s);
  for(String s:new String[]{"paysage.jpg","Naruto anime","chrome","hotmail.txt","hotspot.mp4","hotel.png"})check(!p.sensitive(s),"ordinary metadata "+s);
  for(String s:new String[]{"Télécharger","Download","Télécharger l’image","Enregistrer l'image","Save image","Download video","Enregistrer la vidéo"})check(DownloadPolicy.downloadAction(s,"",false),"download command "+s);
  for(String s:new String[]{"Enregistrer","Save","Sauvegarder","Téléchargements","Downloads","Ouvrir","Rechercher","Cancel","Download something interesting later"})check(!DownloadPolicy.downloadAction(s,"",false),"not a device download "+s);
  check(!DownloadPolicy.downloadAction("Download","",true),"editable search field not a download button");
  check(DownloadPolicy.downloadAction("","com.snaptube.premium:id/download_button",false),"download icon id");
  check(!DownloadPolicy.downloadAction("","com.snaptube.premium:id/download_tab",false),"downloads navigation tab excluded");
  check(!p.shouldBlock(false,true,false),"sensitive search itself never blocked");
  check(p.shouldBlock(true,true,false),"sensitive download command blocked");
  check(!p.shouldBlock(true,false,false),"ordinary download allowed");
  check(p.shouldBlock(true,false,true),"strict mode explicitly blocks ordinary downloads too");
  for(String s:new String[]{"Annuler","Cancel","Cancel download","Annuler le téléchargement"})check(DownloadPolicy.cancellationAction(s),"real cancel label "+s);
  for(String s:new String[]{"Supprimer","Delete","Cancel subscription","Ouvrir","Effacer tout"})check(!DownloadPolicy.cancellationAction(s),"unrelated/destructive action never used "+s);
  check(DownloadPolicy.downloadNotification("ecchi_image.jpg","30%",true,false),"download filename with progress");
  check(DownloadPolicy.downloadNotification("Video","Téléchargement 20%",false,true),"localized transfer notification");
  check(!DownloadPolicy.downloadNotification("ecchi","Message reçu",false,false),"ordinary notification not a transfer");
  check(!DownloadPolicy.downloadNotification("ecchi.mp4","Upload 20%",true,true),"upload excluded");
  check(!DownloadPolicy.downloadNotification("ecchi.mp4","Envoi en cours",true,true),"French upload excluded");
  RiskSession memory=new RiskSession();memory.mark("pin",1000);check(memory.risky("pin",1001),"context survives popup");check(!memory.risky("chrome",1001),"context isolated by application");check(memory.remaining("pin",1001)==119999,"overlay expiry deadline");check(!memory.risky("pin",121000),"two minute expiry");memory.mark("pin",1000);memory.clear("pin");check(!memory.risky("pin",1001),"normal search can clear context");memory.mark("pin",1000);memory.mark("chrome",1000);memory.clearAll();check(!memory.risky("chrome",1001),"disable clears all context");
  check(!new DownloadPolicy("").sensitive("hentai"),"empty user list respected");
  System.out.println(count+" download filtering assertions passed");
 }
}
