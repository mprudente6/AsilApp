package it.uniba.dib.sms23248.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.uniba.dib.sms23248.HomeS;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

public class AccessoStaff extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button login;
    private TextView registerRedirect;

    private TextView forgotPassword;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesso_staff);

        email = findViewById(R.id.Logemail);
        password = findViewById(R.id.Logpassword);
        login = findViewById(R.id.loginButton);
        registerRedirect = findViewById(R.id.notyetRegistered);
        forgotPassword = findViewById(R.id.password_persa);

        String connessione=getString(R.string.connessione);



        mAuth = FirebaseAuth.getInstance();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String incorrectPass = getString(R.string.IncorrectPass);
                String invalidEmail = getString(R.string.Emailinvalid);
                String permissionS = getString(R.string.PermessiS);
                String obbligoMail=getString(R.string.email_richiesta);
                String obbligoPassword=getString(R.string.password_richiesta);

                if (NetworkUtils.isNetworkAvailable(AccessoStaff.this)) {
                String useremail = email.getText().toString().trim();
                String userpass = password.getText().toString().trim();

                if (useremail.isEmpty()) {
                    email.setError(obbligoMail);
                    return; // Exit the method if email is empty
                }
                if (userpass.isEmpty()) {
                    password    .setError(obbligoPassword);
                    return; // Exit the method if email is empty
                }

                // Call getUserRole to retrieve user role
                getUserRole(useremail, new UserRoleCallback() {
                    @Override
                    public void onSuccess(String userRole) {
                        String logpositivo=getString(R.string.login_con_successo);
                        // Check if the retrieved role is "Staff"
                        if ("Staff".equals(userRole)) {
                            // Continue with login process
                            if (!useremail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(useremail).matches()) {
                                if (!userpass.isEmpty()) {
                                    mAuth.signInWithEmailAndPassword(useremail, userpass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            Toast.makeText(AccessoStaff.this,logpositivo, Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(AccessoStaff.this, HomeS.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AccessoStaff.this, incorrectPass, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    password.setError(obbligoPassword);
                                }
                            } else if (useremail.isEmpty()) {
                                email.setError(obbligoMail);
                            } else {
                                email.setError(invalidEmail);
                            }
                        } else {
                            // User doesn't have the required role, show error message
                            Toast.makeText(AccessoStaff.this, permissionS, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Handle the failure, for example, show an error message
                        Toast.makeText(AccessoStaff.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
                } else {
                    // No internet connection, show a message to the user
                    Toast.makeText(AccessoStaff.this,connessione, Toast.LENGTH_LONG).show();
                }
            }
        });

        registerRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccessoStaff.this, RegistrazioneStaff.class));
            }
        });



        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String invio_reset=getString(R.string.invio_reset);
                String resetFallito=getString(R.string.resetFallito);

                String Emailfirst = getString(R.string.ResetPass);

                String userEmail = email.getText().toString().trim();

                if (!userEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    if (NetworkUtils.isNetworkAvailable(AccessoStaff.this)) {
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password reset email sent successfully
                                        Toast.makeText(AccessoStaff.this, invio_reset, Toast.LENGTH_SHORT).show();

                                        // Delay the Intent by 6 seconds
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Handle successful password reset, for example, navigate to another activity
                                                Intent intent = new Intent(AccessoStaff.this, PasswordDimenticata.class);
                                                startActivity(intent);
                                                finish(); // Optional: Close the current activity if needed
                                            }
                                        }, 10000); // 6000 milliseconds = 6 seconds
                                    } else {
                                        // Password reset email sending failed
                                        Toast.makeText(AccessoStaff.this, resetFallito, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // No internet connection, show a message to the user
                    Toast.makeText(AccessoStaff.this, connessione, Toast.LENGTH_LONG).show();
                }
                } else {
                    Toast.makeText(AccessoStaff.this, Emailfirst, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void getUserRole(String useremail, final UserRoleCallback callback) {
        String EmailinonEsiste = getString(R.string.EmailnonEsiste);
        db.collection("RUOLI").document(useremail)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userRole = documentSnapshot.getString("Ruolo");
                            callback.onSuccess(userRole);
                        } else {
                            callback.onFailure(EmailinonEsiste);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    final String erroreRecupero=getString(R.string.erroreRecupero);
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(erroreRecupero + e.getMessage());
                    }
                });
    }

    interface UserRoleCallback {
        void onSuccess(String userRole);
        void onFailure(String errorMessage);
    }

}
