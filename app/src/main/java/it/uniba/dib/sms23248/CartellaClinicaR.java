package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class CartellaClinicaR extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;
    private final Map<String, View> addedViewsMap = new HashMap<>();

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_cartella_clinica_r, container, false);

        db = FirebaseFirestore.getInstance();
        personalDataLayout = view.findViewById(R.id.personalDataLayout);


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


            fetchUserDataFromFirestore();


        return view;
    }

    private void shareData() {
        String subject = "CARTELLA CLINICA";
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

        for (int i = 0; i < personalDataLayout.getChildCount(); i += 2) {
            TextView fieldTextView = (TextView) personalDataLayout.getChildAt(i);
            TextView valueTextView = (TextView) personalDataLayout.getChildAt(i + 1);

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

    private void fetchUserDataFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

            // Set up a real-time listener for the user document
            userRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    // Handle error
                    showToast("Errore durante l'ascolto delle modifiche della cartella clinica");
                    return;
                }
                List<String> orderedFields = Arrays.asList("Anamnesi", "Diagnosi", "GruppoSanguigno", "Allergie", "Altezza", "Peso", "NoteMediche");

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> userData = documentSnapshot.getData();

                    // Iterate through the ordered fields
                    for (String field : orderedFields) {
                        Object value = userData.get(field);

                        // Check if the value for the field exists
                        if (value != null) {
                            // Update the corresponding view
                            updateDataInView(field, value);
                        }
                    }

                    // Check if all fields are empty (excluding "Utente")
                    if (allFieldsAreEmpty(userData, "Utente")) {
                        // Display the message if all fields are empty
                        displayMessage("La tua cartella clinica non è stata ancora compilata.\n\nVedrai i tuoi dati dopo la prima visita.");
                    }
                } else {
                    // Document does not exist, show toast message
                    showToast("Cartella clinica non disponibile. Lo Staff non ha ancora caricato la tua cartella clinica!");
                }
            });
        } else {
            // User not logged in
            showToast("Utente non autenticato");
        }
    }


    // Check if all fields are empty (excluding specified fields)
    private boolean allFieldsAreEmpty(Map<String, Object> userData, String... excludedFields) {
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            String field = entry.getKey();

            // Skip the excluded fields
            if (!isFieldExcluded(field, excludedFields)) {
                Object value = entry.getValue();
                if (value != null && !value.toString().isEmpty()) {
                    return false; // At least one non-excluded field is not empty
                }
            }
        }
        return true;
    }

    // Check if a field should be excluded
    private boolean isFieldExcluded(String field, String... excludedFields) {
        for (String excludedField : excludedFields) {
            if (field.equals(excludedField)) {
                return true;
            }
        }
        return false;
    }

    // Method to update data in the layout
    private void updateDataInView(String field, Object value) {

        for (int i = 0; i < personalDataLayout.getChildCount(); i += 2) {
            TextView existingFieldTextView = (TextView) personalDataLayout.getChildAt(i);


            if (existingFieldTextView.getText().toString().equals(field)) {

                TextView valueTextView = (TextView) personalDataLayout.getChildAt(i + 1);
                valueTextView.setText(value != null ? value.toString() : "");
                return;
            }
        }


        TextView fieldTextView = new TextView(requireContext());
        fieldTextView.setText(field);
        fieldTextView.setTypeface(null, Typeface.BOLD);
        fieldTextView.setPadding(26, 15, 6, 6);
        fieldTextView.setTextSize(18);

        TextView valueTextView = new TextView(requireContext());
        valueTextView.setText(value != null ? value.toString() : "");
        valueTextView.setTextSize(16);
        valueTextView.setPadding(26, 6, 6, 6);

        personalDataLayout.addView(fieldTextView);
        personalDataLayout.addView(valueTextView);
    }

    // Check if all fields are empty
    private boolean allFieldsAreEmpty(Map<String, Object> userData) {
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            Object value = entry.getValue();
            if (value != null && !value.toString().isEmpty()) {
                return false; // At least one field is not empty
            }
        }
        return true;
    }


    // Method to update data in the layout



    private boolean allFieldsAreEmpty(String... fields) {
        for (String field : fields) {
            if (field != null && !field.isEmpty()) {
                return false; // At least one field is not empty
            }
        }
        return true;
    }






    private String getValueFromMap(Map<String, Object> map, String key) {
        return map != null && map.containsKey(key) ? map.get(key).toString() : "";
    }



    private String getDisplayNameForField(String field) {
        // Modify this function to handle display names for fields with more than one word
        switch (field) {
            case "GruppoSanguigno":
                return "Gruppo Sanguigno";
            case "NoteMediche":
                return "Note Mediche";
            // Add more cases as needed for other fields
            default:
                return field;
        }
    }

    private void addDataToLayout(String field, Object value) {
        // Check if the field view already exists
        if (addedViewsMap.containsKey(field)) {
            // Field view already exists, update the corresponding value view
            TextView valueTextView = (TextView) addedViewsMap.get(field);
            valueTextView.setText(value != null ? value.toString() : "");
        } else {
            // Field view doesn't exist, create and add both field and value views
            // Clear the existing views in the layout
            personalDataLayout.removeAllViews();

            // Field view
            TextView fieldTextView = new TextView(requireContext());
            fieldTextView.setText(field);
            fieldTextView.setTypeface(null, Typeface.BOLD);
            fieldTextView.setPadding(26, 6, 6, 6);
            fieldTextView.setTextSize(15);

            // Value view
            TextView valueTextView = new TextView(requireContext());
            valueTextView.setText(value != null ? value.toString() : "");
            valueTextView.setTextSize(15);
            valueTextView.setPadding(26, 6, 6, 6);

            // Add views to the layout
            personalDataLayout.addView(fieldTextView);
            personalDataLayout.addView(valueTextView);

            // Store the views in the map
            addedViewsMap.put(field, valueTextView);
        }
    }



    // Update the displayMessage method
    private void displayMessage(String message) {
        // Clear the existing views in the layout
        personalDataLayout.removeAllViews();

        TextView messageTextView = new TextView(requireContext());
        messageTextView.setText(message);
        messageTextView.setTextSize(16);
        messageTextView.setGravity(Gravity.CENTER);

        int textColor = ContextCompat.getColor(requireContext(), R.color.grey);
        messageTextView.setTextColor(textColor);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        int padding = (int) getResources().getDimension(R.dimen.padding);
        messageTextView.setPadding(padding, padding, padding, padding);

        messageTextView.setLayoutParams(layoutParams);

        personalDataLayout.addView(messageTextView);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}