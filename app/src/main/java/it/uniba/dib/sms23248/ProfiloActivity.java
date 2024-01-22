package it.uniba.dib.sms23248;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ProfiloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        // Find the 'Scheda anagrafica' button by its ID
        View schedaAnagraficaButton = findViewById(R.id.schedaAnagraficaButton);

        // Set a click listener for the 'Scheda anagrafica' button
        schedaAnagraficaButton.setOnClickListener(view -> {
            // Create an intent to navigate to AnagraficaActivity
            Intent intent = new Intent(ProfiloActivity.this, AnagraficaActivity.class);

            // Start the AnagraficaActivity
            startActivity(intent);
        });

        // Find the 'Valuta l'app' text view by its ID
        View valutaLAppTextView = findViewById(R.id.valutaAppTextView);

        // Set a click listener for the 'Valuta l'app' text view
        valutaLAppTextView.setOnClickListener(view -> {
            // Create an intent to navigate to ValutazioneActivity
            Intent intent = new Intent(ProfiloActivity.this, ValutazioneActivity.class);

            // Start the ValutazioneActivity
            startActivity(intent);
        });
    }

    // Method to go back to Home Screen
    public void goToHomeScreen() {
        Intent intent = new Intent(this, HomeR.class);
        startActivity(intent);
        finish(); // Finish this activity when going back to Home Screen
    }
}