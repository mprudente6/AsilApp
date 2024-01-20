package it.uniba.dib.sms23248;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CentroAccoglienza extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference centro = db.collection("CENTRI_ACCOGLIENZA").document("C001");

    Double latitude;
    Double longitude;
    Double zoomlevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_accoglienza);

    }

    @Override
    protected void onStart() {
        super.onStart();
        centro.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Ottieni il valore del campo "Descrizione"
                    String NomeDatabase = documentSnapshot.getString("Nome");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView Nome = findViewById(R.id.Nome);

                    // Inserisci il valore nel TextView
                    String NomeDaInserire = NomeDatabase;
                    Nome.setText(NomeDaInserire);

                    // Ottieni il valore del campo "Descrizione"
                    String Descrizione = documentSnapshot.getString("Descrizione");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView TestoDescrizione = findViewById(R.id.Description);

                    // Inserisci il valore nel TextView
                    String DescrizioneDaInserire = Descrizione;
                    TestoDescrizione.setText(DescrizioneDaInserire);

                    // Ottieni il valore del campo "SitoWeb"
                    String SitoWeb = documentSnapshot.getString("Sito web");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView TestoSitoWeb = findViewById(R.id.Link);

                    // Inserisci il valore nel TextView
                    String SitoWebDaInserire = SitoWeb;
                    TestoSitoWeb.setText(SitoWebDaInserire);

                    // Ottieni il valore del campo "Indirizzo"
                    String Indirizzo = documentSnapshot.getString("Indirizzo");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView TestoIndirizzo = findViewById(R.id.Indirizzo);

                    // Inserisci il valore nel TextView
                    String IndirizzoDaInserire = Indirizzo;
                    TestoIndirizzo.setText(IndirizzoDaInserire);

                    // Ottieni il valore del campo "Telefono"
                    String Telefono = documentSnapshot.getString("Telefono");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView TestoTelefono = findViewById(R.id.Tel);

                    // Inserisci il valore nel TextView
                    String TelefonoDaInserire = Telefono;
                    TestoTelefono.setText(TelefonoDaInserire);

                    // Ottieni il valore del campo "Email"
                    String Email = documentSnapshot.getString("Email");
                    // Ottieni l'istanza di TextView usando l'ID definito nel tuo layout XML
                    TextView TestoEmail = findViewById(R.id.Email);

                    // Inserisci il valore nel TextView
                    String EmailDaInserire = Email;
                    TestoEmail.setText(EmailDaInserire);

                    latitude = documentSnapshot.getDouble("latitude");
                    longitude = documentSnapshot.getDouble("longitude");
                    zoomlevel = documentSnapshot.getDouble("zoomlevel");

                }
            }
        });
    }
}