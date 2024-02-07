package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class AnagraficaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;
    private FirebaseUser currentUser;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anagrafica);
        String connect=getString(R.string.connessione);



        // Find views by ID
        Button shareButton = findViewById(R.id.shareButton);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.isNetworkAvailable(AnagraficaActivity.this)) {
                    shareData();
                } else {
                    Toast.makeText(AnagraficaActivity.this,connect, Toast.LENGTH_LONG).show();
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        personalDataLayout = findViewById(R.id.personalDataLayout);
        currentUser = FirebaseAuth.getInstance().getCurrentUser(); // utente loggato


            fetchUserDataFromFirestore();

    }

    private void shareData() {
        String subject = "SCHEDA DATI ANAGRAFICI";
        String text = prepareTextToShare();

        // intent per la condivisione di dati testuali su altre app
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(shareIntent, 0);

        if (!resInfo.isEmpty()) {
            for (ResolveInfo resolveInfo : resInfo) {
                String packageName = resolveInfo.activityInfo.packageName;
                // filtra tra la lista di app solo Gmail e Whatsapp
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
                                List<String> orderedFields = Arrays.asList("Nome", "Cognome","Genere","Email","Cellulare","DataNascita", "LuogoNascita");

                                for (String field : orderedFields) {
                                    Object value = userData.get(field);
                                    if (value != null) {
                                        String displayName = getDisplayNameForField(field);
                                        addDataToLayout(displayName, value);
                                    }
                                }
                            }
                        }
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
        MaterialTextView fieldTextView = new MaterialTextView(this);
        fieldTextView.setText(field);
        fieldTextView.setTypeface(null, Typeface.BOLD);
        fieldTextView.setPadding(28, 8, 8, 8);
        fieldTextView.setTextSize(16);

        MaterialTextView valueTextView = new MaterialTextView(this);
        valueTextView.setText(value.toString());
        valueTextView.setTextSize(18);
        valueTextView.setPadding(28, 8, 8, 8);

        personalDataLayout.addView(fieldTextView);
        personalDataLayout.addView(valueTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}