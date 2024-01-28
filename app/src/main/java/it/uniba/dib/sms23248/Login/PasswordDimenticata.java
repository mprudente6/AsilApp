package it.uniba.dib.sms23248.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.uniba.dib.sms23248.HomeR;
import it.uniba.dib.sms23248.HomeS;
import it.uniba.dib.sms23248.R;

public class PasswordDimenticata extends AppCompatActivity {
    private EditText email;
    private EditText newpassword;
    private Button accedi;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_dimenticata);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = findViewById(R.id.ResetEmail);
        newpassword = findViewById(R.id.ResetPassword);
        accedi = findViewById(R.id.UpdateButton);

        accedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInAndUpdate();
            }
        });
    }

    private void signInAndUpdate() {

        String userEmail = email.getText().toString().trim();
        String newPassword = newpassword.getText().toString().trim();
        String Signfail=getString(R.string.loginFallito);
        String autfallita=getString(R.string.autenticaFallito);
        String riempiCampi=getString(R.string.riempiCampi);

        if (!userEmail.isEmpty() && !newPassword.isEmpty()) {
            mAuth.signInWithEmailAndPassword(userEmail, newPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    updatePasswordAndRedirect(user.getUid(), newPassword, userEmail);
                                } else {
                                    // Handle the case where the user is unexpectedly null
                                    Toast.makeText(PasswordDimenticata.this, Signfail, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(PasswordDimenticata.this, autfallita, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Handle empty email or password
            Toast.makeText(PasswordDimenticata.this,riempiCampi, Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePasswordAndRedirect(String uid, String newPassword, String userEmail) {
        // Assuming your collection for user roles is named "RUOLI"
        DocumentReference userRoleRef = db.collection("RUOLI").document(userEmail);

        userRoleRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String fallitoRitrovamento=getString(R.string.fallitoRitrovamento);
                String sconosciuto=getString(R.string.userSconosciuto);
                String NotFound=getString(R.string.NotFound);
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String role = document.getString("Ruolo");

                        if ("RichiedenteAsilo".equals(role)) {
                            // Update password in RICHIEDENTI_ASILO collection
                            updatePasswordInCollection("RICHIEDENTI_ASILO", uid, newPassword);
                            // Redirect to HomeR activity
                            startActivity(new Intent(PasswordDimenticata.this, HomeR.class));
                            finish();
                        } else if ("Staff".equals(role)) {
                            // Update password in STAFF collection
                            updatePasswordInCollection("STAFF", uid, newPassword);
                            // Redirect to HomeS activity
                            startActivity(new Intent(PasswordDimenticata.this, HomeS.class));
                            finish();
                        } else {
                            // Handle other roles if needed
                            Toast.makeText(PasswordDimenticata.this,sconosciuto, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle the case where the document does not exist
                        Toast.makeText(PasswordDimenticata.this,NotFound, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle failures
                    Toast.makeText(PasswordDimenticata.this,fallitoRitrovamento, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePasswordInCollection(String collectionName, String uid, String newPassword) {
        String passUpdate=getString(R.string.passAggiorna);
        String updateFail=getString(R.string.aggiornaFail);
        DocumentReference userDocRef = db.collection(collectionName).document(uid);
        userDocRef.update("Password", newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password in Firestore updated successfully
                            Toast.makeText(PasswordDimenticata.this, passUpdate, Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle failure to update password in Firestore
                            Toast.makeText(PasswordDimenticata.this,updateFail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}