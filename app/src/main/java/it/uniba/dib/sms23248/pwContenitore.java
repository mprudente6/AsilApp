package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class pwContenitore extends Fragment {

    EditText pw;
    Button btnInvia;
    String passwordContenitore;
    String codiceQrContenitore;
    TextView Codice;

    EditText InserisciCodice;

    TextView TitoloCodice;
    TextView TitoloNoCodice;

    static boolean contenitoreAperto = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_pw_contenitore, container, false);

        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.CAMERA")
                == PackageManager.PERMISSION_GRANTED)
        {
            TitoloCodice = rootView.findViewById(R.id.textViewNoPermission);
            TitoloCodice.setVisibility(View.INVISIBLE);

            Codice = rootView.findViewById(R.id.Codice);
            Codice.setVisibility(View.INVISIBLE);

            InserisciCodice = rootView.findViewById(R.id.CodiceContenitore);
            InserisciCodice.setVisibility(View.INVISIBLE);

        } else
        {
            TitoloNoCodice = rootView.findViewById(R.id.textViewPermission);
            TitoloNoCodice.setVisibility(View.INVISIBLE);

        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference Contenitore = db.collection("CONTENITORI").document("0001");



        Contenitore.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                passwordContenitore = documentSnapshot.getString("Password");
                codiceQrContenitore = documentSnapshot.getString("QrCode");
            }
        });

        pw = rootView.findViewById(R.id.PasswordContenitore);


        btnInvia = rootView.findViewById(R.id.buttonInvia);

        btnInvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isTextViewVisible(Codice)){
                        Log.d("CodiceNull ",pw.getText().toString());
                        if (pw.getText().toString().equals(passwordContenitore)){
                            Log.d("Vai  a ","Strumenti biomedicali");
                            contenitoreAperto = true;
                            Intent i = new Intent(pwContenitore.this.getActivity(), SaluteS.class);
                            startActivity(i);
                        }
                    }
                    else{

                        InserisciCodice = rootView.findViewById(R.id.CodiceContenitore);
                        Log.d("CodiceNoNull ",InserisciCodice.getText().toString());
                        if (InserisciCodice.getText().toString().equals(codiceQrContenitore) && pw.getText().toString().equals(passwordContenitore)){
                            Log.d("Vai  a ","Strumenti biomedicali");
                            contenitoreAperto = true;
                            Intent i = new Intent(pwContenitore.this.getActivity(), SaluteS.class);
                            startActivity(i);
                        }
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        return rootView;

    }
    private boolean isTextViewVisible(TextView textView) {
        if (textView == null) {
            return false;
        }

        // Ottieni i limiti globali del TextView
        Rect rect = new Rect();
        textView.getGlobalVisibleRect(rect);

        // Verifica se i limiti globali del TextView sono visibili sullo schermo
        return rect.width() > 0 && rect.height() > 0;
    }


}