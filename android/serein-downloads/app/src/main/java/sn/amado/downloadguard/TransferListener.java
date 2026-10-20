package sn.amado.downloadguard;

import android.app.*;
import android.content.*;
import android.os.*;
import android.service.notification.*;
import java.util.*;

/** Sends the transfer app's real Cancel action; dismissing a notification is NOT cancellation. */
public class TransferListener extends NotificationListenerService {
    private final Map<String,Long> attempted=new HashMap<>();
    @Override public void onListenerConnected(){State.notifications=true;}
    @Override public void onListenerDisconnected(){State.notifications=false;}
    @Override public void onNotificationPosted(StatusBarNotification sbn){
        if(!State.enabled(this)||sbn==null||!State.apps(this).contains(sbn.getPackageName()))return;
        Notification n=sbn.getNotification();if(n==null||n.extras==null||n.actions==null)return;
        // Inspect text only after verifying the notification belongs to an opted-in app.
        String title=String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TITLE,""));
        String body=String.valueOf(n.extras.getCharSequence(Notification.EXTRA_TEXT,""));
        boolean progress=n.extras.getInt(Notification.EXTRA_PROGRESS_MAX,0)>0||n.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE,false)||Notification.CATEGORY_PROGRESS.equals(n.category);
        boolean ongoing=(n.flags&Notification.FLAG_ONGOING_EVENT)!=0;
        if(!DownloadPolicy.downloadNotification(title,body,progress,ongoing))return;
        DownloadPolicy policy=State.policy(this);
        boolean risk=policy.sensitive(title+" "+body)||State.CONTEXT.risky(sbn.getPackageName(),SystemClock.elapsedRealtime());
        if(!policy.shouldBlock(true,risk,State.strict(this)))return;
        long now=SystemClock.elapsedRealtime();Long last=attempted.get(sbn.getKey());if(last!=null&&now-last<10000)return;
        for(Notification.Action a:n.actions){
            if(a.actionIntent!=null&&a.getRemoteInputs()==null&&DownloadPolicy.cancellationAction(String.valueOf(a.title))){
                attempted.put(sbn.getKey(),now);
                try{
                    a.actionIntent.send();
                    State.status="Annulation demandée à l’application de téléchargement";
                    MonitorService.notice(this,"Annulation demandée", "L’application doit confirmer l’arrêt ; une partie du fichier peut déjà avoir été reçue.");
                }catch(PendingIntent.CanceledException e){State.status="Action Annuler expirée : vérifie le transfert dans l’application";}catch(RuntimeException e){State.status="Annulation refusée : vérifie le transfert dans l’application";}
                return;
            }
        }
        State.status="Transfert sensible détecté sans bouton Annuler utilisable";
        // Never merely hide the notification or claim to have stopped an uncontrollable transfer.
    }
    @Override public void onNotificationRemoved(StatusBarNotification sbn){if(sbn!=null)attempted.remove(sbn.getKey());}
}
