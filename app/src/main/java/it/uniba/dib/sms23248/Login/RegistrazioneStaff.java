package it.uniba.dib.sms23248.Login;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

public class  RegistrazioneStaff extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo";
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText email;
    private EditText password;

    private Spinner centroSpinner;

    private EditText cellulare;


    private Button register;
    private TextView loginRedirect;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_registrazione_staff);


        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.confirm_registration);
        loginRedirect = findViewById(R.id.alreadyRegistered);

        centroSpinner = findViewById(R.id.spinner_centro);
        cellulare = findViewById(R.id.cell);

        progressBar=findViewById(R.id.progressBar);



        mAuth = FirebaseAuth.getInstance();

        // fetching dei nomi dei centri disponibili da mettere nello spinner
        db.collection("CENTRI_ACCOGLIENZA")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> centerNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String nome = document.getString("Nome");
                            centerNames.add(nome);
                        }

                         //inizializzazione spinner con adapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, centerNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        centroSpinner.setAdapter(adapter);
                    } else {
                    }
                });


        /*vengono controllate delle condizioni prima della creazione di un nuovo utente Staff, sui valori da assegnare ai campi email, password
        * i campi email e password devono avere valori diversi dal VUOTO
        * la password deve avere un valore con lunghezza di ALMENO 6 caratteri*/
        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // dati degli editText
                String passsLong = getString(R.string.Passwordcaratter);
                String riempiCampi = getString(R.string.Riempicampi);
                String invalidEmail = getString(R.string.Emailinvalid);
                String invalidCell = getString(R.string.Cellulareinvalid);
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();
                String obbligoMail=getString(R.string.email_richiesta);
                String obbligoPassword=getString(R.string.password_richiesta);
                String connect=getString(R.string.connessione);
                String cellulareValue = cellulare.getText().toString().trim();
                String regSuccess=getString(R.string.registraCompletato);
                String regFail=getString(R.string.registrazione_fallita);

                String centroValue = centroSpinner.getSelectedItem().toString();

                if (useremail.isEmpty()) {
                    email.setError(obbligoMail);
                }
                if (userpass.isEmpty()) {
                    password.setError(obbligoPassword);
                }   if (userpass.length() < 6) {
                    password.setError( passsLong);

                }
                if (useremail.isEmpty() || userpass.isEmpty() ||
                        cellulareValue.isEmpty() || centroValue.isEmpty()) {
                    Toast.makeText(RegistrazioneStaff.this, riempiCampi, Toast.LENGTH_SHORT).show();
                } if (!isValidEmail(useremail)) {
                    Toast.makeText(RegistrazioneStaff.this, invalidEmail, Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!isValidPhoneNumber(cellulareValue)) {
                    Toast.makeText(RegistrazioneStaff.this, invalidCell, Toast.LENGTH_SHORT).show();

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    //controllo disponibilità rete
                    if (NetworkUtils.isNetworkAvailable(RegistrazioneStaff.this)) {

                        //registrazione in Firebase Authentication
                    mAuth.createUserWithEmailAndPassword(useremail, userpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String uid = currentUser.getUid();
                                String email=currentUser.getEmail();

                                //inserimento dati in Firebase database
                                DocumentReference documentRuoli = db.collection("RUOLI").document(email);
                                Map<String, Object> ruoli = new HashMap<>();
                                ruoli.put("Ruolo", "Staff");
                                documentRuoli.set(ruoli);

                                DocumentReference documentStaff = db.collection("STAFF").document(uid);

                                Map<String, Object> RichiedenteAsilo = new HashMap<>();
                                RichiedenteAsilo.put("ID_Staff", uid);
                                RichiedenteAsilo.put("Cellulare", cellulareValue);
                                RichiedenteAsilo.put("Centro", centroValue);
                                RichiedenteAsilo.put("Password", userpass);
                                RichiedenteAsilo.put("Email", useremail);
                                RichiedenteAsilo.put("Ruolo", "Staff");

                                documentStaff
                                        .set(RichiedenteAsilo)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(RegistrazioneStaff.this,regSuccess, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegistrazioneStaff.this, AccessoStaff.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(RegistrazioneStaff.this,regFail + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegistrazioneStaff.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }); } else {
                    Toast.makeText(RegistrazioneStaff.this,connect, Toast.LENGTH_LONG).show();
                }
                }
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrazioneStaff.this, AccessoStaff.class));
            }
        });


    }




    private boolean isValidEmail(String email) {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }


    private boolean isValidPhoneNumber(String phoneNumber) {

        String phoneRegex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(phoneRegex);
    }
}