package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class ParametriMediciR extends Fragment {
    private LinearLayout parametriLayout;
    private FirebaseFirestore firestore;
    private String userId;

    private Map<String, TextView> addedViewsMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametri_medici_r, container, false);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            showToast("User not logged in");
        }

        parametriLayout = view.findViewById(R.id.parametriLayout);

        // Find views by ID
        Button shareButton = view.findViewById(R.id.shareButton);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    shareData();
                } else {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            fetchUserData();
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
        }

        return view;
    }


    private void shareData() {
        String subject = "PARAMETRI MEDICI";
        String text = prepareTextToShare();

        // Get the list of apps that can handle the share intent
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        List<ResolveInfo> resInfo = requireContext().getPackageManager().queryIntentActivities(shareIntent, 0);

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

        for (int i = 0; i < parametriLayout.getChildCount(); i += 2) {
            TextView fieldTextView = (TextView) parametriLayout.getChildAt(i);
            TextView valueTextView = (TextView) parametriLayout.getChildAt(i + 1);

            String field = fieldTextView.getText().toString();
            if (valueTextView!=null) {
                String value = valueTextView.getText().toString();
                textBuilder.append(field).append(": ").append(value).append("\n");
            } else{
                Toast.makeText(requireContext(),"Non ci sono dati da condividere", Toast.LENGTH_SHORT).show();
            }
        }

        return textBuilder.toString();
    }

    private void fetchUserData() {
        firestore.collection("PARAMETRI_UTENTI")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document exists, display data
                            displayUserData(document);
                        } else {
                            // Document does not exist, show toast message
                            showToast("Siamo spiacenti, i tuoi parametri medici non sono ancora stati caricati!");
                        }
                    } else {
                        // Handle error
                        showToast("Errore nella ricerca dei tuoi parametri medici!");
                    }
                });
    }

    private void displayUserData(DocumentSnapshot document) {
        // Display the fetched data in the parametri layout
        // You can customize this part based on your data structure

        for (Map.Entry<String, Object> entry : document.getData().entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            // Exclude specific fields
            if (!field.equals("Utente") && !field.equals("ID_RichiedenteAsilo") && !field.equals("DataVisita")) {
                // Update the corresponding view if the field exists
                updateDataInView(field, value);
            }
        }
    }

    private void updateDataInView(String field, Object value) {
        // Check if the field view already exists
        if (addedViewsMap.containsKey(field)) {
            // Field view already exists, update the corresponding value view
            TextView valueTextView = addedViewsMap.get(field);
            valueTextView.setText(formatValue(field, value));
        } else {
            // Field view doesn't exist, create and add both field and value views
            // Field view
            TextView fieldTextView = new TextView(requireContext());
            fieldTextView.setText(getDisplayNameForField(field));
            fieldTextView.setTypeface(null, Typeface.BOLD);
            fieldTextView.setPadding(26, 6, 6, 6);
            fieldTextView.setTextSize(15);

            // Value view
            TextView valueTextView = new TextView(requireContext());
            valueTextView.setText(formatValue(field, value));
            valueTextView.setTextSize(15);
            valueTextView.setPadding(26, 6, 6, 6);

            // Add views to the layout
            parametriLayout.addView(fieldTextView);
            parametriLayout.addView(valueTextView);

            // Store the views in the map
            addedViewsMap.put(field, valueTextView);
        }
    }

    private String formatValue(String field, Object value) {
        if (value == null) {
            return "N/A";
        } else if (value instanceof Double) {
            return String.format("%.2f %s", value, getMeasurementUnit(field));
        } else {
            return value + " " + getMeasurementUnit(field);
        }
    }

    private String getMeasurementUnit(String field) {
        switch (field) {
            case "TemperaturaCorporea":
                return "°C";
            case "FrequenzaCardiaca":
                return "bpm";
            case "PressioneMax":
                return "mmHg";
            case "PressioneMin":
                return "mmHg";
            case "Glucosio":
                return "mg/dl";
            case "Saturazione":
                return "%";
            default:
                return "";
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String getDisplayNameForField(String field) {
        // Modify this function to handle display names for fields with more than one word
        switch (field) {
            case "TemperaturaCorporea":
                return "Temperatura Corporea";
            case "FrequenzaCardiaca":
                return "Frequenza Cardiaca";
            case "PressioneMax":
                return "Pressione Massima";
            case "PressioneMin":
                return "Pressione Minima";
            case "Glucosio":
                return "Glicemia";
            default:
                return field;
        }
    }
}