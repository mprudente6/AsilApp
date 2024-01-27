package it.uniba.dib.sms23248;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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

        // MULTILINGUA


        /*Spinner spinner = (Spinner) findViewById(R.id.languageList);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.opzione_lingua,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLang = parent.getItemAtPosition(position).toString();
                if(selectedLang.equals("ITA")){
                    Log.d("ITA","ITA");
                    Locale locale = new Locale("it");
                    Locale.setDefault(locale);
                    Resources resources = Contenitore.this.getResources();
                    Configuration config = resources.getConfiguration();
                    config.setLocale(locale);
                    resources.updateConfiguration(config,resources.getDisplayMetrics());

                } else if (selectedLang.equals("ENG")){
                    Log.d("ENG","ENG");
                    Locale locale = new Locale("en");
                    Locale.setDefault(locale);
                    Resources resources = Contenitore.this.getResources();
                    Configuration config = resources.getConfiguration();
                    config.setLocale(locale);
                    resources.updateConfiguration(config,resources.getDisplayMetrics());
                    finish();
                    startActivity(getIntent());
                }
                if (selectedLang.equals("Scegli la lingua")){

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            };
        });*/


        /*
        brnMostraQr = (Button) findViewById(R.id.generaQrRichiedente);

        brnMostraQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragmentQrCode = null;
                fragmentQrCode = new qrCodeGenerato();
                getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutQrCode, fragmentQrCode)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            }
        });*/

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
    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if (ContextCompat.checkSelfPermission(Contenitore.this, "android.permission.CAMERA")
                == PackageManager.PERMISSION_GRANTED) {
            // I permessi sono già stati concessi, puoi procedere con l'utilizzo della fotocamera.
            if(result.getContents() !=null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(Contenitore.this);
                builder.setTitle("Result");


                Log.d("qrContenitore1",qrCodeContenitore);

                if (result.getContents().equals(qrCodeContenitore)){

                    Fragment fragmentPwContenitore = null;
                    fragmentPwContenitore = new pwContenitore();
                    getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

                }
                else{
                    //builder.setMessage(result.getContents());
                    builder.setMessage("CONTENITORE NON TROVATO");
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
            // Richiedi i permessi della fotocamera se non sono stati concessi
            Fragment fragmentPwContenitore = null;
            fragmentPwContenitore = new pwContenitore();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutPwContenitore, fragmentPwContenitore)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }


    });
}
