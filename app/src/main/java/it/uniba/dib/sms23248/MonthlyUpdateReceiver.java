package it.uniba.dib.sms23248;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MonthlyUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String uid = intent.getStringExtra("UID");

        // Enqueue the work using JobIntentService
        Intent serviceIntent = new Intent(context, MonthlyUpdateJobIntentService.class);
        serviceIntent.putExtra("UID", uid);
        MonthlyUpdateJobIntentService.enqueueWork(context, serviceIntent);
    }
}
