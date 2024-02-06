package it.uniba.dib.sms23248;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import it.uniba.dib.sms23248.SpeseRichiedente.Spese;

public class ProfiloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        // Find the 'Scheda anagrafica' button by its ID
        ImageView schedaAnagrafica = findViewById(R.id.btnSchedaUser);
        ImageView bilancioSpese = findViewById(R.id.btnSpeseRichiedente);

        // Set a click listener for the 'Scheda anagrafica' button
        schedaAnagrafica.setOnClickListener(view -> {
            // Create an intent to navigate to AnagraficaActivity
            Intent intent = new Intent(ProfiloActivity.this, AnagraficaActivity.class);

            // Start the AnagraficaActivity
            startActivity(intent);
        });

        bilancioSpese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfiloActivity.this, Spese.class);

                startActivity(intent);
            }
        });
        // Find the 'Valuta l'app' text view by its ID
        CardView valutazione = findViewById(R.id.valutazioneApp);

        // Set a click listener for the 'Valuta l'app' text view
        valutazione.setOnClickListener(view -> {
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