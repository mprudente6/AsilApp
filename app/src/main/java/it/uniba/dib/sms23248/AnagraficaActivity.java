package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnagraficaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;
    private FirebaseUser currentUser;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anagrafica);



        // Find views by ID
        Button shareButton = findViewById(R.id.shareButton);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.isNetworkAvailable(AnagraficaActivity.this)) {
                    shareData();
                } else {
                    Toast.makeText(AnagraficaActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        personalDataLayout = findViewById(R.id.personalDataLayout);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (NetworkUtils.isNetworkAvailable(AnagraficaActivity.this)) {
            fetchUserDataFromFirestore();
        } else {
            Toast.makeText(AnagraficaActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void shareData() {
        String subject = "SCHEDA DATI ANAGRAFICI";
        String text = prepareTextToShare();

        // Get the list of apps that can handle the share intent
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (packageName.contains("com.whatsapp") || packageName.contains("com.google.android.gm")) {
                    Intent targeted = new Intent(Intent.ACTION_SEND);
                    targeted.setType("text/plain");
                    targeted.putExtra(Intent.EXTRA_SUBJECT, subject);
                    targeted.putExtra(Intent.EXTRA_TEXT, text);
                    targeted.setPackage(packageName);
                    targetedShareIntents.add(targeted);
                }
            }

            if (!targetedShareIntents.isEmpty()) {
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Scegli l'app con cui condividere i tuoi dati");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Intent[]{}));
                startActivity(chooserIntent);
            } // else: No WhatsApp o Gmail: il sistema mostra 'Nessuna app è in grado di eseguire questa azione.'
        }
    }

    private String prepareTextToShare() {
        StringBuilder textBuilder = new StringBuilder();

        for (int i = 0; i < personalDataLayout.getChildCount(); i += 2) {
            TextView fieldTextView = (TextView) personalDataLayout.getChildAt(i);
            TextView valueTextView = (TextView) personalDataLayout.getChildAt(i + 1);

            String field = fieldTextView.getText().toString();
            String value = valueTextView.getText().toString();

            textBuilder.append(field).append(": ").append(value).append("\n");
        }

        return textBuilder.toString();
    }

    private void fetchUserDataFromFirestore() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DocumentReference userRef = db.collection("RICHIEDENTI_ASILO").document(userId);

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();

                            if (userData != null) {
                                for (Map.Entry<String, Object> entry : userData.entrySet()) {
                                    String field = entry.getKey();
                                    Object value = entry.getValue();

                                    // Fields to exclude from display
                                    if (!field.equals("Budget") && !field.equals("Centro") && !field.equals("ID_RichiedenteAsilo") && !field.equals("Password") && !field.equals("Ruolo")) {
                                        String displayName = getDisplayNameForField(field);
                                        addDataToLayout(displayName, value);
                                    }
                                }
                            }
                        }
                    } else {
                        // Handle failure
                    }
                }
            });
        }
    }

    private String getDisplayNameForField(String field) {
        switch (field) {
            case "DataNascita":
                return "Data di nascita";
            case "LuogoNascita":
                return "Luogo di nascita";
            default:
                return field;
        }
    }

    private void addDataToLayout(String field, Object value) {
        TextView fieldTextView = new TextView(this);
        fieldTextView.setText(field);
        fieldTextView.setTypeface(null, Typeface.BOLD);
        fieldTextView.setPadding(26, 6, 6, 6);
        fieldTextView.setTextSize(15);

        TextView valueTextView = new TextView(this);

        valueTextView.setText(value.toString());
        valueTextView.setTextSize(15);
        valueTextView.setPadding(26, 6, 6, 6);

        personalDataLayout.addView(fieldTextView);
        personalDataLayout.addView(valueTextView);
    }
    @Override
    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}