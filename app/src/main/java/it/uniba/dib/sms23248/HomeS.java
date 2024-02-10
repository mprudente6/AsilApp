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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import it.uniba.dib.sms23248.Amministrazione.Amministrazione;
import it.uniba.dib.sms23248.Amministrazione.RegistrazioneRichiedenteAsilo;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class HomeS extends AppCompatActivity {

    private FirebaseAuth mAuth;

    public static String UID;
    private TextView benvenuto;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CardView aggiungiUtente;


    public Boolean userExist = false;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_s);
        String connesso=getString(R.string.connessione);

        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(),connesso, Toast.LENGTH_LONG).show();

            return;
        }
        boolean contenitoreAperto = pwContenitore.contenitoreAperto;
        if (contenitoreAperto) {
            //pwContenitore.contenitoreAperto = false;
            contenitoreAperto = pwContenitore.contenitoreAperto;
        }

        mAuth = FirebaseAuth.getInstance();
        benvenuto=findViewById(R.id.Benvenuto);


        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        CardView saluteButton = findViewById(R.id.btnSaluteS);

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

        CardView admin=findViewById(R.id.btnAmministrazione);

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
        String sicuro = getString(R.string.VuoiUscire);
        String si = getString(R.string.Si);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage(sicuro);

        builder.setPositiveButton(si, new DialogInterface.OnClickListener() {
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

        FragmentManager fragmentManager = getSupportFragmentManager(); // Ottieni il FragmentManager
        Fragment fragment = fragmentManager.findFragmentById(R.id.frameLayoutEmailUtente); // Trova il Fragment
        if (fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit(); // Rimuovi il Fragment
            RelativeLayout layoutHomeS = findViewById(R.id.LayoutHomeS);
            layoutHomeS.setVisibility(View.VISIBLE);
        }else
        {
            showLogoutConfirmationDialog();
        }

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

                UID = result.getContents();

                db.collection("RICHIEDENTI_ASILO").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (result.getContents().equals(document.getId()))
                                {
                                    userExist = true;
                                    break;
                                }
                            }
                            if (userExist == true){

                                openSaluteScreen();

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
                            }
                        }
                    }
                });


            }
        } else {
            // Richiedi i permessi della fotocamera se non sono stati concessi
            RelativeLayout layoutHomeS = findViewById(R.id.LayoutHomeS);
            layoutHomeS.setVisibility(View.GONE);
            Fragment emailUtente = null;
            emailUtente = new emailUtente();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutEmailUtente, emailUtente, "emailUtente")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    });
}
