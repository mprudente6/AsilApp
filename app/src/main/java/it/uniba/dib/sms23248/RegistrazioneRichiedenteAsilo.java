package it.uniba.dib.sms23248;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegistrazioneRichiedenteAsilo extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo";
    private FirebaseAuth mAuth;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseFirestore dbS = FirebaseFirestore.getInstance();
    private EditText email;
    private EditText password;
    private EditText nome;
    private EditText cognome;
    private Spinner genderSpinner;

    private EditText cellulare;
    private EditText luogonascita;
    private EditText datadinascita;

    private Button register;
    private TextView loginRedirect;

    private ProgressBar progressBar;

    FirebaseUser currentUserS;
    String uidStaff;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione_richiedente_asilo);

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
        Log.e(TAG, "StaffUser: "+uidStaff);

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
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();
                String nomeValue = nome.getText().toString().trim();
                String cognomeValue = cognome.getText().toString().trim();
                String cellulareValue = cellulare.getText().toString().trim();
                String luogonascitaValue = luogonascita.getText().toString().trim();
                String nascitaValue = datadinascita.getText().toString().trim();
                String genereValue = genderSpinner.getSelectedItem().toString();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String currentUid=currentUser.getUid();
                Log.e(TAG, "Current UID: " + currentUid);

                if (useremail.isEmpty()) {
                    email.setError("Email required");
                }
                if (userpass.isEmpty()) {
                    password.setError("Password required");
                }
                if (userpass.length() < 6) {
                    password.setError( "La password deve essere di almeno 6 caratteri!");

                }
                if (useremail.isEmpty() || userpass.isEmpty() || nomeValue.isEmpty() || cognomeValue.isEmpty() ||
                        cellulareValue.isEmpty() || luogonascitaValue.isEmpty() || nascitaValue.isEmpty() || genereValue.isEmpty()) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Riempire tutti i campi!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(useremail)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Email non valida!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidPhoneNumber(cellulareValue)) {
                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Cellulare non valido!", Toast.LENGTH_SHORT).show();
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(useremail, userpass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Get the UID of the newly created user
                                    String uid = task.getResult().getUser().getUid();
                                    Log.e(TAG, "User UID: " + uid);
                                    // Check if the user is a Staff member

                                    DocumentReference documentStaff = db.collection("STAFF").document(currentUid);

                                    documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    String role = document.getString("Ruolo");
                                                    if ("Staff".equals(role)) {
                                                        // Continue with the registration process
                                                        registerUser(uid);
                                                    } else {
                                                        progressBar.setVisibility(View.GONE);  // Dismiss progress bar
                                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, "You don't have permission to register users.", Toast.LENGTH_SHORT).show();
                                                        Log.e(TAG, "User doesn't have permission to register");
                                                    }
                                                } else {
                                                    progressBar.setVisibility(View.GONE);  // Dismiss progress bar
                                                    Log.e(TAG, "User document doesn't exist");
                                                    // Handle the case where the user document doesn't exist
                                                }
                                            } else {
                                                progressBar.setVisibility(View.GONE);  // Dismiss progress bar
                                                Log.e(TAG, "Error getting document: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    progressBar.setVisibility(View.GONE);  // Dismiss progress bar
                                    Toast.makeText(RegistrazioneRichiedenteAsilo.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Firebase authentication failed: " + task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }

    private void registerUser(String uid) {
        String useremail = email.getText().toString().trim();
        String nomeValue = nome.getText().toString().trim();
        String cognomeValue = cognome.getText().toString().trim();
        String cellulareValue = cellulare.getText().toString().trim();
        String luogonascitaValue = luogonascita.getText().toString().trim();
        String nascitaValue = datadinascita.getText().toString().trim();
        String genereValue = genderSpinner.getSelectedItem().toString();


        // Get the reference to the "RichiedenteAsilo" document using the provided UID
        DocumentReference documentRichiedenteAsilo = db.collection("RICHIEDENTI_ASILO").document(uid);

        // Get the staff document to retrieve the Centro field
        DocumentReference documentStaff = db.collection("STAFF").document(uidStaff);



        DocumentReference documentRuoli = db.collection("RUOLI").document(useremail);

        // Retrieve the Centro field from the Staff document
        documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot staffDocument = task.getResult();
                    if (staffDocument.exists()) {
                        // Retrieve Centro from the Staff document
                        String centroValue = staffDocument.getString("Centro");

                        Map<String, Object> ruoli = new HashMap<>();
                        ruoli.put("Ruolo", "RichiedenteAsilo");
                        documentRuoli.set(ruoli);

                        // Create a map with the data you want to store
                        Map<String, Object> richiedenteAsilo = new HashMap<>();
                        richiedenteAsilo.put("ID_RichiedenteAsilo", uid);
                        richiedenteAsilo.put("Nome", nomeValue);
                        richiedenteAsilo.put("Cognome", cognomeValue);
                        richiedenteAsilo.put("Cellulare", cellulareValue);
                        richiedenteAsilo.put("LuogoNascita", luogonascitaValue);
                        richiedenteAsilo.put("DataNascita", nascitaValue);
                        richiedenteAsilo.put("Genere", genereValue);
                        richiedenteAsilo.put("Password", useremail);
                        richiedenteAsilo.put("Email", useremail);
                        richiedenteAsilo.put("Centro", centroValue);
                        richiedenteAsilo.put("Ruolo", "RichiedenteAsilo");

                        documentRichiedenteAsilo.set(richiedenteAsilo)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getStaffCredentialsAndSignIn(uidStaff);
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                        // Add a delay before starting the HomeS activity
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent= new Intent(RegistrazioneRichiedenteAsilo.this, HomeS.class);
                                                // Remove transition animation

                                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                startActivity(intent);
                                                Log.d(TAG, "Registration successful");


                                            }
                                        }, 3000); // Delay of 1000 milliseconds (1 second)
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Registration failed: " + e.getMessage());
                                    }
                                });
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Staff document doesn't exist");
                        // Handle the case where the Staff document doesn't exist
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error getting Staff document: " + task.getException().getMessage());
                }
            }
        });
    }


    public void showDatePicker() {
        final Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        // Apply the custom style
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

    // Email validation method
    private boolean isValidEmail(String email) {
        // Use a simple regex for email validation
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }

    // Phone number validation method
    private boolean isValidPhoneNumber(String phoneNumber) {
        // Use a simple regex for phone number validation
        String phoneRegex = "^[+]?[0-9]{10,13}$";
        return phoneNumber.matches(phoneRegex);
    }
    private void getStaffCredentialsAndSignIn(String uid) {
        // Reference to the "Staff" collection with the user UID as the document ID
        DocumentReference documentStaff = db.collection("STAFF").document(uidStaff);

        // Retrieve email and password fields from the "Staff" collection
        documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Retrieve email and password from the document
                        String staffEmail = document.getString("Email");
                        String staffPassword = document.getString("Password");

                        // Sign in the Staff member
                        mAuth.signInWithEmailAndPassword(staffEmail, staffPassword)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success
                                            Log.d(TAG, "Sign in success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                Log.d(TAG, "User signed in: " + user.getUid());
                                            }
                                            // Navigate to the desired activity
                                            startActivity(new Intent(RegistrazioneRichiedenteAsilo.this, HomeS.class));
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "Sign in failed", task.getException());
                                            Toast.makeText(RegistrazioneRichiedenteAsilo.this, "Sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Log.e(TAG, "Staff document doesn't exist");
                        // Handle the case where the Staff document doesn't exist
                    }
                } else {
                    Log.e(TAG, "Error getting Staff document: " + task.getException().getMessage());
                }
            }
        });
    }


}