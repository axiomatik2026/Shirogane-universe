package sn.amado.serein;

import android.content.*;
import android.net.VpnService;

/** Restore previously enabled protection using Android's permitted boot/update events. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(intent==null || !(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())||Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())))return;
        GuardRuntimeService.ensure(context);
        SharedPreferences p=context.getSharedPreferences("serein",0);
        if(p.getBoolean("vpn_enabled",false) && p.getBoolean("resume_vpn_on_boot",false) && VpnService.prepare(context)==null){
            try{context.startForegroundService(new Intent(context,ShieldService.class));}catch(RuntimeException ignored){}
        }
    }
}
