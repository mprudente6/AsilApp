package it.uniba.dib.sms23248;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeR extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView benvenuto;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_r);

        mAuth = FirebaseAuth.getInstance();
        benvenuto=findViewById(R.id.Benvenuto);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        ImageView customMenuIcon = findViewById(R.id.avatar);
        customMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference documentRichiedenteAsilo = db.collection("RICHIEDENTI_ASILO").document(uid);

            documentRichiedenteAsilo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the value of the "Nome" field
                            String nome = document.getString("Nome");

                            // Use the retrieved value as needed
                            benvenuto.setText("Benvenuto, " + nome + "!");
                        }
                    }
                }
            });

            Button btnInformazioni = findViewById(R.id.btnInformazioniCentro);
            btnInformazioni.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomeR.this, CentroAccoglienza.class);
                    startActivity(i);
                }
            });

            Button btnMappa = findViewById(R.id.btnApriMappa);
            btnMappa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomeR.this, MappaCentro.class);
                    startActivity(i);
                }
            });

            Button btnMedia = findViewById(R.id.btnMedia);
            btnMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomeR.this, MediaRichiedente.class);
                    startActivity(i);
                }
            });

            Button btnGeneraQR = findViewById(R.id.generaQrRichiedente);
            btnGeneraQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragmentQrCode = null;
                    fragmentQrCode = new qrCodeGenerato();
                    getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutQrCode, fragmentQrCode)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                }
            });

            Button saluteButton = findViewById(R.id.btnSaluteR);

            saluteButton.setOnClickListener(view -> openSaluteScreen());
        }
    }

    private void openSaluteScreen() {
        Intent intent = new Intent(this, SaluteR.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.logout) {
            showLogoutConfirmationDialog();
            return true;
        } else if (itemId == R.id.profilo) {
            openProfiloScreen();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Handle custom ImageView click event
    public void onCustomImageClick(View view) {
        showPopupMenu(view);
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_bar); // Create a separate menu resource file

        // Set up the item click listener for the PopupMenu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.logout) {
                    showLogoutConfirmationDialog();
                    return true;
                } else if (itemId == R.id.profilo) {
                    openProfiloScreen();
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
    }
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Sei sicuro di voler uscire?");

        builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent intent = new Intent(HomeR.this, MainActivity.class);
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

    private void openProfiloScreen() {
        Intent intent = new Intent(this, ProfiloActivity.class);
        startActivity(intent);
    }
}