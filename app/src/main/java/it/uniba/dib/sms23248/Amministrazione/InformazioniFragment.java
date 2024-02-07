package it.uniba.dib.sms23248.Amministrazione;



import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InformazioniFragment extends Fragment {

    private static final String TAG = "InformazioniFragment";

    EditText editNome;
    EditText editIndirizzo;
    EditText editLink;
    EditText editEmail;
    EditText editTel;
    EditText editDescr;

    View view;
    Button saveButton;


    private final FirebaseFirestore dbS = FirebaseFirestore.getInstance();
    DocumentReference documentStaff;


NetworkChangeReceiver networkChangeReceiver;

    String connesso=getString(R.string.connessione);
    String uid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_informazioni, container, false);
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), connesso, Toast.LENGTH_LONG).show();

            return view;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editIndirizzo = view.findViewById(R.id.editIndirizzo);
        editTel = view.findViewById(R.id.editTel);
        editLink = view.findViewById(R.id.editLink);
        editDescr = view.findViewById(R.id.editDescription);
        saveButton = view.findViewById(R.id.saveBtn);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            documentStaff = dbS.collection("STAFF").document(uid);
            fetchInformazioniCentro();
            saveButton.setOnClickListener(v -> {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    salvaInformazioniFirestore();
                } else {
                    Toast.makeText(requireContext(),connesso, Toast.LENGTH_LONG).show();
                }

            });
        } else {

            Log.d(TAG, "Current user is null");
        }



        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkChangeReceiver, intentFilter);


        return view;
    }



    private void fetchInformazioniCentro() {
        String errorStaffCollection=getString(R.string.errStaffcollection);
        CollectionReference centroAccoglienzaCollection = dbS.collection("CENTRI_ACCOGLIENZA");
        documentStaff.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String centroValue = documentSnapshot.getString("Centro");
                        Log.e(TAG, "Centro : " + centroValue);
                        if (centroValue != null) {
                            centroAccoglienzaCollection.whereEqualTo("Nome", centroValue)
                                    .limit(1) // prendi solo un documento
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            DocumentSnapshot matchingDocument = queryDocumentSnapshots.getDocuments().get(0);
                                            String name = matchingDocument.getString("Nome");
                                            String address = matchingDocument.getString("Indirizzo");
                                            String link = matchingDocument.getString("Sito web");
                                            String email = matchingDocument.getString("Email");
                                            String tel = matchingDocument.getString("Telefono");
                                            String descr = matchingDocument.getString("Descrizione");

                                            editNome.setText(name);
                                            editIndirizzo.setText(address);
                                            editLink.setText(link);
                                            editEmail.setText(email);
                                            editTel.setText(tel);
                                            editDescr.setText(descr);

                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error: " + e.getMessage()));
                        }
                       }
                })
                .addOnFailureListener(e -> Log.e(TAG, errorStaffCollection + e.getMessage()));
    }


    private void salvaInformazioniFirestore() {
        String aggiornaDati= getString(R.string.datiAggiornati);
        CollectionReference centroAccoglienzaCollection = dbS.collection("CENTRI_ACCOGLIENZA");
        String nome = editNome.getText().toString();
        String indirizzo = editIndirizzo.getText().toString();
        String link = editLink.getText().toString();
        String email = editEmail.getText().toString();
        String tel = editTel.getText().toString();
        String descr = editDescr.getText().toString();

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), connesso, Toast.LENGTH_LONG).show();
            return;
        }

        documentStaff.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String centroValue = documentSnapshot.getString("Centro");
                        if (centroValue != null) {
                            centroAccoglienzaCollection.whereEqualTo("Nome", centroValue)
                                    .limit(1)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            DocumentSnapshot matchingDocument = queryDocumentSnapshots.getDocuments().get(0);
                                            DocumentReference documentRef = matchingDocument.getReference();
                                            Map<String, Object> newData = new HashMap<>();
                                            newData.put("Nome", nome);
                                            newData.put("Indirizzo", indirizzo);
                                            newData.put("Sito web", link);
                                            newData.put("Email", email);
                                            newData.put("Telefono", tel);
                                            newData.put("Descrizione", descr);

                                            documentRef.update(newData)
                                                    .addOnSuccessListener(aVoid -> {

                                                        Toast.makeText(getContext(), aggiornaDati, Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> Log.e(TAG, "Errore: " + e.getMessage()));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Error: " + e.getMessage()));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error: " + e.getMessage()));
    }


    @Override
    public void onDestroyView() {

        if (networkChangeReceiver != null) {
            requireContext().unregisterReceiver(networkChangeReceiver);
        }
        super.onDestroyView();
    }


}
