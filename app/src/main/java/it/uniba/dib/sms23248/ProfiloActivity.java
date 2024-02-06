package it.uniba.dib.sms23248;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.uniba.dib.sms23248.SpeseRichiedente.Spese;

public class ProfiloActivity extends AppCompatActivity {
    TextView nomeUtente;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

         nomeUtente=findViewById(R.id.textnomeUtente);
        ImageView schedaAnagrafica = findViewById(R.id.btnSchedaUser);
        ImageView bilancioSpese = findViewById(R.id.btnSpeseRichiedente);

        ProfileName();

        schedaAnagrafica.setOnClickListener(view -> {

            Intent intent = new Intent(ProfiloActivity.this, AnagraficaActivity.class);


            startActivity(intent);
        });

        bilancioSpese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfiloActivity.this, Spese.class);

                startActivity(intent);
            }
        });

        CardView valutazione = findViewById(R.id.valutazioneApp);


        valutazione.setOnClickListener(view -> {

            Intent intent = new Intent(ProfiloActivity.this, ValutazioneActivity.class);


            startActivity(intent);
        });
    }


    public void goToHomeScreen() {
        Intent intent = new Intent(this, HomeR.class);
        startActivity(intent);
        finish();
    }

    private void ProfileName(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser= mAuth.getCurrentUser();
        String uid= currentUser.getUid();

        Task<DocumentSnapshot> Utente= db.collection("RICHIEDENTI_ASILO").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                String nome = document.getString("Nome");
                                String cognome = document.getString("Cognome");


                                nomeUtente.setText( nome+" "+cognome );
                            }
                        }
                    }
                });
    }
}