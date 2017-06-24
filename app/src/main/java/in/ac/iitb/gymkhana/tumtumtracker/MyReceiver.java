package in.ac.iitb.gymkhana.tumtumtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent s=new Intent(context,MyService.class);
        context.startService(s);
    }
}
