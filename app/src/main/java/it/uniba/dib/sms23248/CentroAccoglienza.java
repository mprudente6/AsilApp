package it.uniba.dib.sms23248;

import static android.content.ContentValues.TAG;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CentroAccoglienza extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();


    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_accoglienza);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (NetworkUtils.isNetworkAvailable(CentroAccoglienza.this)) {
            retrieveCentroFromRichiedente(uid);
        } else {
            Toast.makeText(CentroAccoglienza.this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void retrieveCentroFromRichiedente(String currentUserUid) {
        db.collection("RICHIEDENTI_ASILO")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String centro = documentSnapshot.getString("Centro");
                        if (centro != null) {
                            retrieveCentroAccoglienzaDocument(centro);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving Centro from STAFF collection", e);
                });
    }

    private void retrieveCentroAccoglienzaDocument(String centro) {
        db.collection("CENTRI_ACCOGLIENZA")
                .whereEqualTo("Nome", centro)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for updates in Centro Accoglienza document", e);
                        return;
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot centroAccoglienzaDoc = queryDocumentSnapshots.getDocuments().get(0);

                        // Retrieve fields from Centro Accoglienza document
                        String nome = centroAccoglienzaDoc.getString("Nome");
                        String descrizione = centroAccoglienzaDoc.getString("Descrizione");
                        String sitoWeb = centroAccoglienzaDoc.getString("Sito web");
                        String indirizzo = centroAccoglienzaDoc.getString("Indirizzo");
                        String telefono = centroAccoglienzaDoc.getString("Telefono");
                        String email = centroAccoglienzaDoc.getString("Email");


                        // Update UI with retrieved data
                        updateUI(nome, descrizione, sitoWeb, indirizzo, telefono, email);
                    }
                });
    }


    private void updateUI(String nome, String descrizione, String sitoWeb, String indirizzo, String telefono, String email) {
        // Update your TextViews or other UI elements with the retrieved data
        TextView Nome = findViewById(R.id.Nome);
        Nome.setText(nome);

        TextView TestoDescrizione = findViewById(R.id.Description);
        TestoDescrizione.setText(descrizione);

        TextView TestoSitoWeb = findViewById(R.id.Link);
        TestoSitoWeb.setText(sitoWeb);

        TextView TestoIndirizzo = findViewById(R.id.Indirizzo);
        TestoIndirizzo.setText(indirizzo);

        TextView TestoTelefono = findViewById(R.id.Tel);
        TestoTelefono.setText(telefono);

        TextView TestoEmail = findViewById(R.id.Email);
        TestoEmail.setText(email);
    }

    @Override
    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
