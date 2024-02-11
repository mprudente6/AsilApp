package it.uniba.dib.sms23248.SpeseRichiedente;

import android.content.Context;
import android.content.Intent;
import androidx.core.app.JobIntentService;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MonthlyUpdateJobIntentService extends JobIntentService {

    private static final int JOB_ID = 1000;

    @Override
    protected void onHandleWork(Intent intent) {
        String uid = intent.getStringExtra("UID");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("RICHIEDENTI_ASILO").document(uid);
        Log.d("BUDGET", "uid: " + uid);
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double currentBudget = documentSnapshot.getDouble("Budget");

                double updatedBudget = currentBudget + 60.0;
                documentReference.update("Budget", updatedBudget)
                        .addOnSuccessListener(aVoid -> {
                        })
                        .addOnFailureListener(e -> {
                        });
            }
        }).addOnFailureListener(e -> {});
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MonthlyUpdateJobIntentService.class, JOB_ID, work);
    }
}
