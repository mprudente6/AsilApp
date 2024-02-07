package it.uniba.dib.sms23248;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class ValutazioneActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private Button submitRatingButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private NetworkChangeReceiver networkChangeReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String connect=getString(R.string.connessione);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valutazione);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        ratingBar = findViewById(R.id.ratingBar);
        submitRatingButton = findViewById(R.id.submitRatingButton);

            // Imposta listener per le modifiche nella RatingBar
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // pulsante di invio abilitato solo dopo aver selezionato le stelle
            enableSubmitButton(rating > 0); // pulsante nascosto se nessuna stella è selezionata
        });

        // al click del pulsante di invio ottieni punteggio
        submitRatingButton.setOnClickListener(view -> {
            float userRating = ratingBar.getRating();

            if (userRating > 0) { // salva dato solo se > 0
                // Call method to submit the rating to Firestore
                if (NetworkUtils.isNetworkAvailable(ValutazioneActivity.this)) {
                submitUserRatingToFirestore(userId, userRating);
                } else {
                    Toast.makeText(ValutazioneActivity.this,connect, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // abilita-disabilita e mostra-nascondi pulsante
    private void enableSubmitButton(boolean enable) {
        submitRatingButton.setEnabled(enable);
        submitRatingButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    // salva nel db
    private void submitUserRatingToFirestore(String userId, float userRating) {
        String errorSearch=getString(R.string.errore_ricerca);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        DocumentReference documentRef = db.collection("VALUTAZIONE").document(userId);

        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // documento esistente: aggiornalo
                    updateExistingDocument(documentRef, userRating);
                } else {
                    // altrimenti: creane uno
                    createNewDocument(documentRef, userRating);
                }
            } else {
                Toast.makeText(getApplicationContext(), errorSearch, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExistingDocument(DocumentReference documentRef, float userRating) {
        String review_registrata=getString(R.string.reviewAggiornata);
        String review_fallita=getString(R.string.reviewFallita);
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("Voto", (int) userRating);

        documentRef.update(ratingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), review_registrata, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),review_fallita, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNewDocument(DocumentReference documentRef, float userRating) {
        String review_saved=getString(R.string.review_saved);
        String reviewNotSaved=getString(R.string.errorSavingReview);

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("ID_RichiedenteAsilo", userId); // salva UID dell'utente loggato come campo
        ratingData.put("Voto", (int) userRating);

        documentRef.set(ratingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),review_saved, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(),reviewNotSaved, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
