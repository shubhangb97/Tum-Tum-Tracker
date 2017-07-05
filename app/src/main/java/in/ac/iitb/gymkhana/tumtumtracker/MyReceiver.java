package in.ac.iitb.gymkhana.tumtumtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent s=new Intent(context,MyService.class);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        s.putExtra("ndeftag",tag)
        context.startService(s);
    }
}
