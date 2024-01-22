package it.uniba.dib.sms23248;

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

public class ValutazioneActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private Button submitRatingButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valutazione);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // Initialize RatingBar and Submit Button
        ratingBar = findViewById(R.id.ratingBar);
        submitRatingButton = findViewById(R.id.submitRatingButton);

        // Set listener for changes in RatingBar
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Enable the submit button once the user rates
            enableSubmitButton(rating > 0); // Hide button if no stars are selected
        });

        // Set click listener for submit button
        submitRatingButton.setOnClickListener(view -> {
            float userRating = ratingBar.getRating();

            if (userRating > 0) {
                // Call method to submit the rating to Firestore
                submitUserRatingToFirestore(userId, userRating);
            }
        });
    }

    // Method to enable the submit button once the user rates
    private void enableSubmitButton(boolean enable) {
        submitRatingButton.setEnabled(enable);
        submitRatingButton.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    // Method to submit the user's rating data to Firestore
    private void submitUserRatingToFirestore(String userId, float userRating) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Get the document reference based on the UID
        DocumentReference documentRef = db.collection("VALUTAZIONE").document(userId);

        // Check if the document exists in the collection based on the UID
        documentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, update the 'Voto' field
                    updateExistingDocument(documentRef, userRating);
                } else {
                    // Document does not exist, create a new one
                    createNewDocument(documentRef, userRating);
                }
            } else {
                // Handle error
                Toast.makeText(getApplicationContext(), "Errore nella ricerca del documento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update an existing document
    private void updateExistingDocument(DocumentReference documentRef, float userRating) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("Voto", (int) userRating);

        documentRef.update(ratingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message using Toast
                        Toast.makeText(getApplicationContext(), "Recensione aggiornata con successo!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle error
                        Toast.makeText(getApplicationContext(), "Errore nell'aggiornamento della recensione", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to create a new document in the Firestore collection
    private void createNewDocument(DocumentReference documentRef, float userRating) {
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("Utente", userId);
        ratingData.put("Voto", (int) userRating);

        documentRef.set(ratingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show success message using Toast
                        Toast.makeText(getApplicationContext(), "Recensione salvata con successo!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle error
                        Toast.makeText(getApplicationContext(), "Errore nel salvataggio della recensione", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
