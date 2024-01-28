package it.uniba.dib.sms23248;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AnagraficaFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;

    private final String userId =  HomeS.UID;   //"1qRWhwM51WP3VjEfMnc4NejOzBh2";  Da sostituire con UID di Richiedente Asilo di cui si è scansionato QR CODE

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anagrafica, container, false);

        db = FirebaseFirestore.getInstance();
        personalDataLayout = view.findViewById(R.id.personalDataLayout);

        fetchUserDataFromFirestore(view);

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Your logic for back press goes here
                boolean contenitoreAperto = pwContenitore.contenitoreAperto;
                if (contenitoreAperto) {
                    // The boolean variable is true, perform the desired action
                    // For example, navigate to another activity and call a method
                    Log.d("SONO QUI","PRECISAMENTE QUI");
                    openScanCode();
                }
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);


        return view;
    }

    private void fetchUserDataFromFirestore(View view) {
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
                                    addDataToLayout(view, displayName, value);
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

    private void addDataToLayout(View view, String field, Object value) {
        TextView fieldTextView = new TextView(view.getContext());
        fieldTextView.setText(field);
        fieldTextView.setTypeface(null, Typeface.BOLD);
        fieldTextView.setPadding(26, 6, 6, 6);
        fieldTextView.setTextSize(15);

        TextView valueTextView = new TextView(view.getContext());

        valueTextView.setText(value.toString());
        valueTextView.setTextSize(15);
        valueTextView.setPadding(26, 6, 6, 6);

        personalDataLayout.addView(fieldTextView);
        personalDataLayout.addView(valueTextView);
    }

    private void openScanCode() {
        // Create an Intent to navigate to another activity
        Intent intent = new Intent(AnagraficaFragment.this.getActivity(), HomeS.class);
        // Start the other activity
        startActivity(intent);
    }
}