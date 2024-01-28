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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.uniba.dib.sms23248.HomeR;
import it.uniba.dib.sms23248.NetworkUtils;
import it.uniba.dib.sms23248.R;

public class AccessoRichiedenteAsilo extends AppCompatActivity {

    private static final String TAG = "AccessoRichiedenteAsilo"; // Add this line to define the TAG
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button login;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView forgotPassword;
    FirebaseUser currentUserS;

    String uidS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accesso_richiedente_asilo);

        email=findViewById(R.id.Logemail);
        password=findViewById(R.id.Logpassword);
        login=findViewById(R.id.loginButton);

        forgotPassword=findViewById(R.id.password_persa);

        mAuth = FirebaseAuth.getInstance();



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String incorrectPass = getString(R.string.IncorrectPass);
                String invalidEmail = getString(R.string.Emailinvalid);
                String permissionR = getString(R.string.PermessiR);
                String obbligoMail=getString(R.string.email_richiesta);
                String obbligoPassword=getString(R.string.password_richiesta);
                String logpositivo=getString(R.string.login_con_successo);
                String connession=getString(R.string.connessione);




                if (NetworkUtils.isNetworkAvailable(AccessoRichiedenteAsilo.this)) {
                    String useremail = email.getText().toString().trim();
                    String userpass = password.getText().toString().trim();

                    if (useremail.isEmpty()) {
                        email.setError(obbligoMail);
                        return; // Exit the method if email is empty
                    }

                    if (userpass.isEmpty()) {
                        password.setError(obbligoPassword);
                        return; // Exit the method if email is empty
                    }

                    // Call getUserRole to retrieve user role
                    getUserRole(useremail, new UserRoleCallback() {
                        @Override
                        public void onSuccess(String userRole) {

                            // Check if the retrieved role is "RichiedenteAsilo"
                            if ("RichiedenteAsilo".equals(userRole)) {
                                // Continue with login process
                                if (!useremail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(useremail).matches()) {
                                    if (!userpass.isEmpty()) {
                                        mAuth.signInWithEmailAndPassword(useremail, userpass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {
                                                Toast.makeText(AccessoRichiedenteAsilo.this,logpositivo, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(AccessoRichiedenteAsilo.this, HomeR.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AccessoRichiedenteAsilo.this, incorrectPass, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(AccessoRichiedenteAsilo.this, permissionR, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Handle the failure, for example, show an error message
                            Toast.makeText(AccessoRichiedenteAsilo.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // No internet connection, show a message to the user
                    Toast.makeText(AccessoRichiedenteAsilo.this,connession, Toast.LENGTH_LONG).show();
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String connession=getString(R.string.connessione);
                String Emailfirst = getString(R.string.ResetPass);
                String invio_reset=getString(R.string.invio_reset);
                String userEmail = email.getText().toString().trim();
                String resetFallito=getString(R.string.resetFallito);

                if (!userEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    if (NetworkUtils.isNetworkAvailable(AccessoRichiedenteAsilo.this)) {
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password reset email sent successfully
                                        Toast.makeText(AccessoRichiedenteAsilo.this,invio_reset, Toast.LENGTH_SHORT).show();

                                        // Delay the Intent by 6 seconds
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Handle successful password reset, for example, navigate to another activity
                                                Intent intent = new Intent(AccessoRichiedenteAsilo.this, PasswordDimenticata.class);
                                                startActivity(intent);
                                                finish(); // Optional: Close the current activity if needed
                                            }
                                        }, 6000); // 6000 milliseconds = 6 seconds
                                    } else {
                                        // Password reset email sending failed
                                        Toast.makeText(AccessoRichiedenteAsilo.this,resetFallito, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    } else {
                        // No internet connection, show a message to the user
                        Toast.makeText(AccessoRichiedenteAsilo.this,connession,Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AccessoRichiedenteAsilo.this, Emailfirst, Toast.LENGTH_LONG).show();
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


