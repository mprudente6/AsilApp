package it.uniba.dib.sms23248;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class Contenitore extends AppCompatActivity {

    Button btnCamera;
    Button brnMostraQr;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference Contenitore = db.collection("CONTENITORI").document("0001");

    String qrCodeContenitore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contenitore);

        scanCode();
        Contenitore.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                qrCodeContenitore  = documentSnapshot.getString("QrCode");
            }
        });

    }

    public void scanCode()
    {
        ScanOptions options = new ScanOptions();
        String stringScansione = getString(R.string.scansione);
        options.setPrompt(stringScansione);
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }
    //Controllo per verificare se sono stati accettati o meno i permessi della fotocamera
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if (ContextCompat.checkSelfPermission(Contenitore.this, "android.permission.CAMERA")
                == PackageManager.PERMISSION_GRANTED) {
            if(result.getContents() !=null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Contenitore.this);
                builder.setTitle("Result");

                //Controllo per verificare se èstato scansionato un codice presente nel database

                if (result.getContents().equals(qrCodeContenitore)){

                    Fragment fragmentPwContenitore = null;
                    fragmentPwContenitore = new pwContenitore();
                    getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

                }
                else{
                    //builder.setMessage(result.getContents());
                    builder.setMessage(getString(R.string.noContenitore));
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                            Intent intent = new Intent(Contenitore.this, SaluteS.class);
                            startActivity(intent);
                        }
                    }).show();
                }

            }
        } else {

            Fragment fragmentPwContenitore = null;
            fragmentPwContenitore = new pwContenitore();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }


    });
}
