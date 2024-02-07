package it.uniba.dib.sms23248;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartellaClinicaFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout dataLayout;
    private Button submitButton;

    private String documentId;
    private final String userId = HomeS.UID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartella_clinica, container, false);

        db = FirebaseFirestore.getInstance();
        dataLayout = view.findViewById(R.id.personalDataLayout);
        submitButton = view.findViewById(R.id.confirmButton);

        fetchUserDataFromFirestore();

        submitButton.setOnClickListener(Bview -> updateDataInFirestore());

        View mainLayout = view.findViewById(R.id.cartella_clinica_layout);

        mainLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (requireActivity().getCurrentFocus() instanceof EditText) {
                    hideKeyboard();
                }

                v.performClick();
            }
            return false;
        });

        return view;
    }

    private class EditTextWatcher implements TextWatcher {
        private EditText editText;

        EditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            // Calcola l'altezza necessaria per visualizzare tutto il testo
            int lineCount = editText.getLineCount();
            int lineHeight = editText.getLineHeight();
            int height = lineHeight * lineCount;

            // Imposta l'altezza dell'EditText
            ViewGroup.LayoutParams layoutParams = editText.getLayoutParams();
            layoutParams.height = height;
            editText.setLayoutParams(layoutParams);
        }
    }

    private void fetchUserDataFromFirestore() {
        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            documentId = documentSnapshot.getId();
            Map<String, Object> userData = documentSnapshot.getData();
            List<String> orderedFields = Arrays.asList("Anamnesi", "Diagnosi", "GruppoSanguigno", "Allergie", "Altezza", "Peso", "NoteMediche", "ID_RichiedenteAsilo");

            for (String field : orderedFields) {
                Object value = userData != null ? userData.get(field) : null;

                if (!"ID_RichiedenteAsilo".equals(field)) {
                    TextView textView = new TextView(requireContext());
                    textView.setText(getDisplayNameForField(field));
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setPadding(25, 30, 25, 2);
                    textView.setTextSize(18);

                    MultiAutoCompleteTextView multiAutoCompleteTextView = new MultiAutoCompleteTextView(requireContext());
                    multiAutoCompleteTextView.setText(value != null ? value.toString() : "");
                    multiAutoCompleteTextView.setHint(getDisplayNameForField(field));
                    multiAutoCompleteTextView.setTextSize(16);
                    multiAutoCompleteTextView.setPadding(25, 0, 25, 20); // Modifica il padding come desiderato
                    multiAutoCompleteTextView.setMaxLines(5); // Imposta il numero massimo di righe
                    multiAutoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.topMargin = 20; // Imposta la distanza tra il testo precedente e il MultiAutoCompleteTextView

                    Log.d("Field Name", field); // Aggiungi log per verificare il nome del campo
                    Log.d("Field Value", value != null ? value.toString() : "null"); // Aggiungi log per verificare il valore del campo

                    dataLayout.addView(textView);
                    LinearLayout multiAutoCompleteTextViewLayout = new LinearLayout(requireContext());
                    multiAutoCompleteTextViewLayout.setOrientation(LinearLayout.VERTICAL); // Orientamento verticale
                    multiAutoCompleteTextViewLayout.addView(multiAutoCompleteTextView);
                    dataLayout.addView(multiAutoCompleteTextViewLayout, layoutParams);

                }
            }

            submitButton.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {

        });
    }




    private String getDisplayNameForField(String field) {

        switch (field) {
            case "GruppoSanguigno":
                return "Gruppo Sanguigno";
            case "NoteMediche":
                return "Note Mediche";

            default:
                return field;
        }
    }

    private void updateDataInFirestore() {
        hideKeyboard();

        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        Map<String, Object> updatedData = new HashMap<>();

        for (int i = 0; i < dataLayout.getChildCount(); i += 2) {
            View view = dataLayout.getChildAt(i + 1);
            String field = ((TextView) dataLayout.getChildAt(i)).getText().toString();
            field = field.replace(" ", "");
            Log.d("Field Name", field); // Aggiungi questo log per verificare il nome del campo
            Log.d("Child View", view.toString());

            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                if (viewGroup.getChildCount() > 0 && viewGroup.getChildAt(0) instanceof MultiAutoCompleteTextView) {
                    MultiAutoCompleteTextView multiAutoCompleteTextView = (MultiAutoCompleteTextView) viewGroup.getChildAt(0);
                    String updatedValue = multiAutoCompleteTextView.getText().toString();
                    Log.d("Field value", updatedValue);
                    updatedData.put(field, updatedValue.isEmpty() ? null : updatedValue);
                }
            }
        }

        updatedData.put("ID_RichiedenteAsilo", userId);

        Log.d("Updated Data", updatedData.toString()); // Aggiungi questo log per verificare i dati aggiornati

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    updateExistingDocument(userRef, updatedData);
                } else {
                    createNewDocument(userRef, updatedData);
                }
            } else {
                Toast.makeText(requireContext(), "Errore nella ricerca del documento", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createNewDocument(DocumentReference userRef, Map<String, Object> updatedData) {

        userRef.set(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Nuova cartella clinica creata con successo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(requireContext(), "Errore nella creazione della cartella clinica", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingDocument(DocumentReference userRef, Map<String, Object> updatedData) {

        userRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Cartella clinica aggiornata con successo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(requireContext(), "Errore nell'aggiornamento della cartella clinica", Toast.LENGTH_SHORT).show();
                });
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (requireActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus()).getWindowToken(), 0);
        }
    }
}
