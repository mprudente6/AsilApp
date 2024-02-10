package it.uniba.dib.sms23248;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AnagraficaFragment extends Fragment {


    private FirebaseFirestore db;
    private LinearLayout personalDataLayout;

    private final String userId =  HomeS.UID;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anagrafica, container, false);

        db = FirebaseFirestore.getInstance();
        TextView anag=view.findViewById(R.id.titleAnagrafica);
        personalDataLayout = view.findViewById(R.id.personalDataLayout);

        fetchUserDataFromFirestore(view);

        return view;
    }

    // legge e mostra (con una chiamata a addDataToLayout) la scheda anagrafica del richiedente asilo di cui si è scansionato il QR code
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

                            List<String> orderedFields = Arrays.asList("Nome", "Cognome","Genere","Email","Cellulare","DataNascita", "LuogoNascita");

                            for (String field : orderedFields) {
                                Object value = userData.get(field);
                                // mostra solo i campi con valori non vuoti
                                if (value != null) {
                                    String displayName = getDisplayNameForField(field);
                                    addDataToLayout(view, displayName, value);
                                }
                            }
                        }
                    }
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 20, 6, 0);
        fieldTextView.setLayoutParams(params);

        fieldTextView.setTextSize(18);

        TextView valueTextView = new TextView(view.getContext());

        valueTextView.setText(value.toString());
        valueTextView.setTextSize(16);
        valueTextView.setPadding(20, 5, 8, 6);

        personalDataLayout.addView(fieldTextView);
        personalDataLayout.addView(valueTextView);
    }


}