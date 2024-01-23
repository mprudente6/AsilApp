package it.uniba.dib.sms23248;

import android.content.Context;
import android.content.Intent;
import androidx.core.app.JobIntentService;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MonthlyUpdateJobIntentService extends JobIntentService {

    private static final int JOB_ID = 1000;

    @Override
    protected void onHandleWork(Intent intent) {
        String uid = intent.getStringExtra("UID");

        // Your background task logic goes here, using the UID
        Log.d("BUDGET", "onHandleWork with UID: " + uid);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("RICHIEDENTI_ASILO").document(uid);
        Log.d("BUDGET", "uid: " + uid);
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double currentBudget = documentSnapshot.getDouble("Budget");

                // Update the budget (for example, add 60 to the current budget)
                double updatedBudget = currentBudget + 60.0;
                Log.d("BUDGET", "+ 60: " + updatedBudget);
                // Save the updated budget back to Firestore
                documentReference.update("Budget", updatedBudget)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("BUDGET", "Budget updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("BUDGET", "Failed to update budget: " + e.getMessage());
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("BUDGET", "Failed to fetch current budget: " + e.getMessage());
        });
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MonthlyUpdateJobIntentService.class, JOB_ID, work);
    }
}
