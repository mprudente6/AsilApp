package it.uniba.dib.sms23248;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class HomeS extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public static String UID;
    private TextView benvenuto;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button aggiungiUtente;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_s);

        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();

            return;
        }



        mAuth = FirebaseAuth.getInstance();
        benvenuto=findViewById(R.id.Benvenuto);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        Button saluteButton = findViewById(R.id.btnSaluteS);

        saluteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });

        aggiungiUtente=findViewById(R.id.btnAggiungiUtente);

        aggiungiUtente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeS.this, RegistrazioneRichiedenteAsilo.class);
                startActivity(intent);
            }
        });

        Button admin=findViewById(R.id.btnAmministrazione);

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeS.this, Amministrazione.class);


                startActivity(intent);
            }

        });


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference documentStaff = db.collection("STAFF").document(uid);

            documentStaff.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the value of the "Nome" field
                            String email = document.getString("Email");

                            // Use the retrieved value as needed
                            benvenuto.setText(  email );
                        }
                    }
                }
            });
        }
    }

    private void openSaluteScreen() {
        Intent intent = new Intent(this, SaluteS.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout,menu );
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();
        if (itemId==R.id.logout){
            showLogoutConfirmationDialog();
        }
        return super.onOptionsItemSelected(item);
    }
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Sei sicuro di voler uscire?");

        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent intent = new Intent(HomeS.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showLogoutConfirmationDialog();
    }

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        String stringScansione = "Scansiona QR code utente";
        options.setPrompt(stringScansione);
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        UID = " ";
        if (ContextCompat.checkSelfPermission(HomeS.this, "android.permission.CAMERA")
                == PackageManager.PERMISSION_GRANTED) {
            // I permessi sono già stati concessi, puoi procedere con l'utilizzo della fotocamera.
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeS.this);
                builder.setTitle("Result");
                Log.d("Result", result.getContents());

                UID = result.getContents();
                openSaluteScreen();
                /*if (result.getContents().equals(qrCodeContenitore)){

                    Fragment fragmentPwContenitore = null;
                    fragmentPwContenitore = new pwContenitore();
                    getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

                }
                else{
                    //builder.setMessage(result.getContents());
                    builder.setMessage("UTENTE NON TROVATO");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }*/

            }
        } else {
            // Richiedi i permessi della fotocamera se non sono stati concessi
            Fragment fragmentPwContenitore = null;
            fragmentPwContenitore = new pwContenitore();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    });
}
