package it.uniba.dib.sms23248;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CartellaClinicaFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout dataLayout;
    private Button submitButton;

    private String documentId;
    private final String userId = HomeS.UID; // Replace with the actual user ID

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

    private void fetchUserDataFromFirestore() {
        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            documentId = documentSnapshot.getId();
            Map<String, Object> userData = documentSnapshot.getData();

            for (Map.Entry<String, Object> entry : getDefaultFields().entrySet()) {
                String field = entry.getKey();
                Object value = userData != null ? userData.get(field) : null;

                if (!"ID_RichiedenteAsilo".equals(field)) {  // Exclude "ID_RichiedenteAsilo" field from display
                    TextView textView = new TextView(requireContext());
                    textView.setText(getDisplayNameForField(field));
                    textView.setTypeface(null, Typeface.BOLD);
                    textView.setPadding(6, 6, 6, 2);
                    textView.setTextSize(15);

                    EditText editText = new EditText(requireContext());
                    editText.setText(value != null ? value.toString() : "");
                    editText.setHint(getDisplayNameForField(field));
                    editText.getBackground().mutate().setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
                    int bottomPaddingInDp = 6;
                    int bottomPaddingInPx = (int) (bottomPaddingInDp * getResources().getDisplayMetrics().density);
                    editText.setPadding(editText.getPaddingLeft(), editText.getPaddingTop(), editText.getPaddingRight(), bottomPaddingInPx);

                    dataLayout.addView(textView);
                    dataLayout.addView(editText);
                }

                submitButton.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            // Handle failure
        });
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

    private Map<String, Object> getDefaultFields() {
        // Define the fields you want to display (modify as needed)
        String[] fields = {"Allergie", "Altezza", "Anamnesi", "Diagnosi", "GruppoSanguigno", "NoteMediche", "Peso", "ID_RichiedenteAsilo"};

        Map<String, Object> defaultFields = new HashMap<>();
        for (String field : fields) {
            defaultFields.put(field, userId); // Set the value of "ID_RichiedenteAsilo" to userId
        }

        return defaultFields;
    }

    private void updateDataInFirestore() {
        hideKeyboard();

        DocumentReference userRef = db.collection("CARTELLA_CLINICA_UTENTI").document(userId);

        Map<String, Object> updatedData = new HashMap<>();

        for (int i = 0; i < dataLayout.getChildCount(); i += 2) {
            if (dataLayout.getChildAt(i + 1) instanceof EditText) {
                EditText editText = (EditText) dataLayout.getChildAt(i + 1);

                String field = editText.getHint().toString();
                // Check and modify the field value based on specific conditions
                if ("Note Mediche".equals(field)) {
                    field = "NoteMediche";
                } else if ("Gruppo Sanguigno".equals(field)) {
                    field = "GruppoSanguigno";
                }

                String updatedValue = editText.getText().toString();

                // Save null value if the field is empty during creation
                updatedData.put(field, updatedValue.isEmpty() ? null : updatedValue);
            }
        }

        // Include "ID_RichiedenteAsilo" field with the userId value
        updatedData.put("ID_RichiedenteAsilo", userId);

        // Use the set() method to save all fields and their values
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, update the document fields
                    updateExistingDocument(userRef, updatedData);
                } else {
                    // Document does not exist, create a new one
                    createNewDocument(userRef, updatedData);
                }
            } else {
                // Handle error
                Toast.makeText(requireContext(), "Errore nella ricerca del documento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewDocument(DocumentReference userRef, Map<String, Object> updatedData) {
        // Use set() method to create a new document with the updated fields
        userRef.set(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Nuova cartella clinica creata con successo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(requireContext(), "Errore nella creazione della cartella clinica", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingDocument(DocumentReference userRef, Map<String, Object> updatedData) {
        // Use update() method to update the document fields
        userRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Cartella clinica aggiornata con successo!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
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