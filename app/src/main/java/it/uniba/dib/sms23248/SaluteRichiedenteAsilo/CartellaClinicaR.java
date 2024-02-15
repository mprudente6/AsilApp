package it.uniba.dib.sms23248.SaluteRichiedenteAsilo;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

public class CartellaClinicaR extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_cartella_clinica_r, container, false);
        String connessione=getString(R.string.connessione);

        db = FirebaseFirestore.getInstance();
        personalDataLayout = view.findViewById(R.id.personalDataLayout);


        Button shareButton = view.findViewById(R.id.shareButton);

        // condividi cartella clinica su Gmail o Whatsapp
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    shareData();
                } else {
                    Toast.makeText(requireContext(),connessione, Toast.LENGTH_LONG).show();
                }
            }
        });



        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchUserDataFromFirestore();
    }


    private void shareData() {
        String subject = "CARTELLA CLINICA";
        String text = prepareTextToShare();

        // intent per la condivisione di dati testuali su altre app
        List<Intent> targetedShareIntents = new ArrayList<>();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        List<ResolveInfo> resInfo = requireContext().getPackageManager().queryIntentActivities(shareIntent, 0);

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
            if (valueTextView!=null) {
                String value = valueTextView.getText().toString();
                textBuilder.append(field).append(": ").append(value).append("\n");
            } else{
                Toast.makeText(requireContext(),getString(R.string.errore_condivisione), Toast.LENGTH_SHORT).show();
            }
        }

        return textBuilder.toString();
    }

    private void fetchUserDataFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

            // cattura errore
            userRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    // Errore
                    showToast("Errore durante l'ascolto delle modifiche della cartella clinica");
                    return;
                }
                List<String> orderedFields = Arrays.asList("Anamnesi", "Diagnosi", "Gruppo sanguigno", "Allergie", "Altezza", "Peso", "Note mediche");

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Map<String, Object> userData = documentSnapshot.getData();

                    for (String field : orderedFields) {
                        Object value = userData.get(field);

                        // mostra solo i campi con valori non vuoti
                        if (value != null) {
                            updateDataInView(field, value);
                        }
                    }

                    if (allFieldsAreEmpty(userData, "Utente")) {
                        // se tutti i campi sono vuoti
                        displayMessage("La tua cartella clinica non è stata ancora compilata.\n\nVedrai i tuoi dati dopo la prima visita.");
                    }
                } else {
                    // se la cartella clinica non è stata ancora creata
                    showToast("Cartella clinica non disponibile. Lo Staff non ha ancora caricato la tua cartella clinica!");
                }
            });
        } else {
            showToast("Utente non autenticato");
        }
    }


    // controlla che tutti i campi siano vuoti (oltre gli esclusi)
    private boolean allFieldsAreEmpty(Map<String, Object> userData, String... excludedFields) {
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            String field = entry.getKey();

            if (!isFieldExcluded(field, excludedFields)) {
                Object value = entry.getValue();
                if (value != null && !value.toString().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    // controlla se il campo è escluso dalla visualizzazione (perché un dato sensibile)
    private boolean isFieldExcluded(String field, String... excludedFields) {
        for (String excludedField : excludedFields) {
            if (field.equals(excludedField)) {
                return true;
            }
        }
        return false;
    }

    // aggiungi i dati da mostrare nella cartella clinica
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

    // mostra messaggio: se non è stata ancora creata la cartella clinica per l'utente loggato
    private void displayMessage(String message) {
        // rimuovi le views esistenti in quel layout
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

        // mostra messaggio come TextView
        personalDataLayout.addView(messageTextView);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}