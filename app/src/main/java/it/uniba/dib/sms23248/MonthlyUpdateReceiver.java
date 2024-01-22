package it.uniba.dib.sms23248;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class MonthlyUpdateReceiver extends BroadcastReceiver {
    Double currentBudget;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("BUDGET", "on Receive");

        String uid = intent.getStringExtra("UID");

        Intent updateBudgetIntent = new Intent("ACTION_UPDATE_BUDGET");
        updateBudgetIntent.putExtra("UID", uid);
        context.sendBroadcast(updateBudgetIntent);
    }


}