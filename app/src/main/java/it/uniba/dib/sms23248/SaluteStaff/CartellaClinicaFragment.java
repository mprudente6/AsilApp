package it.uniba.dib.sms23248.SaluteStaff;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.uniba.dib.sms23248.HomeS;
import it.uniba.dib.sms23248.R;

public class CartellaClinicaFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout dataLayout;
    private MaterialButton submitButton;
    private final String userId = HomeS.UID; // UID del richiedente asilo di cui si è scansionato il QR code

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

    // legge e mostra i dati di cartella clinica dal db
    private void fetchUserDataFromFirestore() {
        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> userData = documentSnapshot.getData();

            List<String> orderedFields = Arrays.asList("Anamnesi", "Diagnosi", "Gruppo sanguigno", "Allergie", "Altezza", "Peso", "Note mediche");

            for (String field : orderedFields) {
                Object value = userData.get(field);

                if (!"ID_RichiedenteAsilo".equals(field)) {
                    TextView textView = new TextView(requireContext());
                    textView.setText(field);
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setPadding(6, 6, 6, 2);
                    textView.setTextSize(15);

                    EditText editText = new EditText(requireContext());
                    editText.setText(value != null ? value.toString() : "");
                    editText.setHint(field);
                    editText.getBackground().mutate().setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
                    int bottomPaddingInDp = 6;
                    int bottomPaddingInPx = (int) (bottomPaddingInDp * getResources().getDisplayMetrics().density);
                    editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingRight(), bottomPaddingInPx);

                    dataLayout.addView(textView);
                    dataLayout.addView(editText);
                }
            }
            submitButton.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {

        });
    }

    private Map<String, Object> getDefaultFields() {
        // nomi campi da mostrare
        String[] fields = {"Allergie", "Altezza", "Anamnesi", "Diagnosi", "GruppoSanguigno", "NoteMediche", "Peso", "ID_RichiedenteAsilo"};

        Map<String, Object> defaultFields = new HashMap<>();
        for (String field : fields) {
            defaultFields.put(field, userId);
        }

        return defaultFields;
    }

    // aggiorna salvando i dati modificati nel db
    private void updateDataInFirestore() {
        hideKeyboard();

        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        Map<String, Object> updatedData = new HashMap<>();

        for (int i = 0; i < dataLayout.getChildCount(); i += 2) {
            if (dataLayout.getChildAt(i + 1) instanceof EditText) {
                EditText editText = (EditText) dataLayout.getChildAt(i + 1);

                String field = editText.getHint().toString();
                // sistema leggibilità intestazioni
                if ("Note Mediche".equals(field)) {
                    field = "NoteMediche";
                } else if ("Gruppo Sanguigno".equals(field)) {
                    field = "GruppoSanguigno";
                }

                String updatedValue = editText.getText().toString();

                // salva i campi vuoti con valore null
                updatedData.put(field, updatedValue.isEmpty() ? null : updatedValue);
            }
        }

        updatedData.put("ID_RichiedenteAsilo", userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // aggiorna campi
                    updateExistingDocument(userRef, updatedData);
                } else {
                    // crea documento
                    createNewDocument(userRef, updatedData);
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.errore_ricerca), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // crea nel db: cartella clinica per il richiedente asilo con valore UID equivalente a userId
    private void createNewDocument(DocumentReference userRef, Map<String, Object> updatedData) {
        userRef.set(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), getString(R.string.new_medicalFolder), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), getString(R.string.errorUpdatefolder), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingDocument(DocumentReference userRef, Map<String, Object> updatedData) {
        userRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), getString(R.string.successUpdate), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), getString(R.string.errorUpdate), Toast.LENGTH_SHORT).show();
                });
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (requireActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(requireActivity().getCurrentFocus()).getWindowToken(), 0);
        }
    }
}