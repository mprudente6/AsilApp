package it.uniba.dib.sms23248.Amministrazione;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms23248.HomeS;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;
import it.uniba.dib.sms23248.SpeseRichiedente.SpeseModel;
import it.uniba.dib.sms23248.SpeseRichiedente.MonthlyUpdateReceiver;

public class RegistrazioneRichiedenteAsilo extends AppCompatActivity {


    private FirebaseAuth mAuth;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText email;
    private EditText password;
    private EditText nome;
    private EditText cognome;
    private Spinner genderSpinner;

    private EditText cellulare;
    private EditText luogonascita;
    private EditText datadinascita;

    private Button register;


    private ProgressBar progressBar;

    FirebaseUser currentUserS;
    String uidStaff;

    SpeseModel sharedViewModel;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_richiedente_asilo);
        sharedViewModel = new ViewModelProvider(this).get(SpeseModel.class);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.confirm_registration);

        nome = findViewById(R.id.nome);
        cognome = findViewById(R.id.cognome);
        genderSpinner = findViewById(R.id.spinner_genere);
        cellulare = findViewById(R.id.cell);
        luogonascita = findViewById(R.id.luogoNascita);
        progressBar=findViewById(R.id.progressBar);



        mAuth = FirebaseAuth.getInstance();
        currentUserS = mAuth.getCurrentUser();
        uidStaff = currentUserS != null ? currentUserS.getUid() : null;


        datadinascita = findViewById(R.id.dataNascita);
        ImageView calendar = findViewById(R.id.calnedarIcon);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDatePicker();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String obbligoMail=getString(R.string.email_richiesta);
                String obbligoPassword=getString(R.string.password_richiesta);
                String noConnection=getString(R.string.not_connection);


                String passsLong = getString(R.string.Passwordcaratter);
                String riempiCampi = getString(R.string.Riempicampi);
                String invalidEmail = getString(R.string.Emailinvalid);
                String invalidCell = getString(R.string.Cellulareinvalid);
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();
                String nomeValue = nome.getText().toString().trim();
                String cognomeValue = cognome.getText().toString().trim();
                String cellulareValue = cellulare.getText().toString().trim();
                String luogonascitaValue = luogonascita.getText().toString().trim();
                String nascitaValue = datadinascita.getText().toString().trim();
                String genereValue = genderSpinner.getSelectedItem().toString();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String staffUid=currentUser.getUid();

                if (useremail.isEmpty()) {
                    email.setError(obbligoMail);
                }
                if (userpass.isEmpty()) {
                    password.setError(obbligoPassword);
                }
                if (userpass.length() < 6) {
                    password.setError( passsLong);

                }
                if (useremail.isEmpty() || userpass.isEmpty() || nomeValue.isEmpty() || cognomeValue.isEmpty() ||
                        cellulareValue.isEmpty() || luogonascitaValue.isEmpty() || nascitaValue.isEmpty() || genereValue.isEmpty()) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, riempiCampi, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(useremail)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, invalidEmail, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidPhoneNumber(cellulareValue)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, invalidCell, Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                if (NetworkUtils.isNetworkAvailable(RegistrazioneRichiedenteAsilo.this)) {
                    mAuth.createUserWithEmailAndPassword(useremail, userpass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        String uid = task.getResult().getUser().getUid();

                                        registerUser(uid);


                                    } else {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }); } else {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, noConnection, Toast.LENGTH_LONG).show();
                }
              }
        });
      }

    private void registerUser(String uid) {
        String useremail = email.getText().toString().trim();
        String userpass=password.getText().toString().trim();
        String nomeValue = nome.getText().toString().trim();
        String cognomeValue = cognome.getText().toString().trim();
        String cellulareValue = cellulare.getText().toString().trim();
        String luogonascitaValue = luogonascita.getText().toString().trim();
        String nascitaValue = datadinascita.getText().toString().trim();
        String genereValue = genderSpinner.getSelectedItem().toString();

        DocumentReference documentRichiedenteAsilo = db.collection("RICHIEDENTI_ASILO").document(uid);

        DocumentReference documentStaff = db.collection("STAFF").document(uidStaff);



        DocumentReference documentRuoli = db.collection("RUOLI").document(useremail);

        documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot staffDocument = task.getResult();
                    if (staffDocument.exists()) {
                        String regSuccess=getString(R.string.registraCompletato);
                        String regFail=getString(R.string.registrazione_fallita);                     String centroValue = staffDocument.getString("Centro");

                        Map<String, Object> ruoli = new HashMap<>();
                        ruoli.put("Ruolo", "RichiedenteAsilo");
                        documentRuoli.set(ruoli);

                        DocumentReference documentCartellaClinica = db.collection("CARTELLA_CLINICA_UTENTI").document(uid);
                        Map<String, Object> cartella = new HashMap<>();
                        cartella.put("Utente", uid);
                        documentCartellaClinica.set(cartella);

                        DocumentReference documentTerapieUtenti = db.collection("TERAPIE_UTENTI").document(uid);
                        Map<String, Object> terapia = new HashMap<>();
                        cartella.put("Utente", uid);
                        documentTerapieUtenti.set(terapia);

                        DocumentReference documentValutazione = db.collection("VALUTAZIONE").document(uid);
                        Map<String, Object> valutazione = new HashMap<>();
                        valutazione.put("Utente", uid);
                        documentValutazione.set(valutazione);

                        DocumentReference documentSpese = db.collection("SPESE").document(uid);
                        Map<String, Object> spese = new HashMap<>();
                        spese.put("Utente", uid);
                        documentSpese.set(spese);


                        DocumentReference documentParametriUtenti = db.collection("PARAMETRI_UTENTI").document(uid);
                        Map<String, Object> parametri = new HashMap<>();
                        parametri.put("Utente", uid);
                        documentParametriUtenti.set(parametri);


                        Map<String, Object> richiedenteAsilo = new HashMap<>();
                        richiedenteAsilo.put("ID_RichiedenteAsilo", uid);
                        richiedenteAsilo.put("Nome", nomeValue);
                        richiedenteAsilo.put("Cognome", cognomeValue);
                        richiedenteAsilo.put("Cellulare", cellulareValue);
                        richiedenteAsilo.put("LuogoNascita", luogonascitaValue);
                        richiedenteAsilo.put("DataNascita", nascitaValue);
                        richiedenteAsilo.put("Genere", genereValue);
                        richiedenteAsilo.put("Password", userpass);
                        richiedenteAsilo.put("Email", useremail);
                        richiedenteAsilo.put("Centro", centroValue);
                        richiedenteAsilo.put("Budget", 60);
                        richiedenteAsilo.put("Ruolo", "RichiedenteAsilo");



                        documentRichiedenteAsilo.set(richiedenteAsilo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, regSuccess, Toast.LENGTH_SHORT).show();
                                        getStaffCredentialsAndSignIn(uidStaff);



                                        scheduleMonthlyAlarm(uid);


                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, regFail + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }




                    else {
                        progressBar.setVisibility(View.GONE);

                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    public void showDatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.DatePickerStyle,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        datadinascita.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }


    private boolean isValidEmail(String email) {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }


    private boolean isValidPhoneNumber(String phoneNumber) {

        String phoneRegex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(phoneRegex);
    }


    private void getStaffCredentialsAndSignIn(String uid) {
        String Autfail=getString(R.string.autenticaFallito);
        DocumentReference documentStaff = db.collection("STAFF").document(uidStaff);

        documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String staffEmail = document.getString("Email");
                        String staffPassword = document.getString("Password");


                        mAuth.signInWithEmailAndPassword(staffEmail, staffPassword)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {


                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                            }
                                            Intent intent= new Intent(RegistrazioneRichiedenteAsilo.this, HomeS.class);
                                            startActivity(intent);
                                        } else {

                                            Toast.makeText(RegistrazioneRichiedenteAsilo.this, Autfail + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private void scheduleMonthlyAlarm(String uid) {

        Intent intent = new Intent(this, MonthlyUpdateReceiver.class);



        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uid.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }


        long startTime = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY * 30;


        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    AlarmManager.INTERVAL_DAY * 30,
                    pendingIntent
            );
        }
    }
}