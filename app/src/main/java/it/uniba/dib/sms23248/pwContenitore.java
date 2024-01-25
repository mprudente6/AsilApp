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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link pwContenitore#newInstance} factory method to
 * create an instance of this fragment.
 */
public class pwContenitore extends Fragment {

    EditText pw;
    Button btnInvia;
    String passwordContenitore;
    String codiceQrContenitore;
    TextView Codice;

    EditText InserisciCodice;

    TextView TitoloCodice;
    TextView TitoloNoCodice;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public pwContenitore() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment pwContenitore.
     */
    // TODO: Rename and change types and number of parameters
    public static pwContenitore newInstance(String param1, String param2) {
        pwContenitore fragment = new pwContenitore();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Button btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
        btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
        btnCamera.setVisibility(View.INVISIBLE);

        Button btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr.setVisibility(View.INVISIBLE);



        View rootView = inflater.inflate(R.layout.fragment_pw_contenitore, container, false);

        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.CAMERA")
                == PackageManager.PERMISSION_GRANTED)
        {
            TitoloCodice = (TextView) rootView.findViewById(R.id.textViewNoPermission);
            TitoloCodice.setVisibility(View.INVISIBLE);

            Codice = (TextView) rootView.findViewById(R.id.Codice);
            Codice.setVisibility(View.INVISIBLE);

            InserisciCodice = (EditText) rootView.findViewById(R.id.CodiceContenitore);
            InserisciCodice.setVisibility(View.INVISIBLE);

        } else
        {
            TitoloNoCodice = (TextView) rootView.findViewById(R.id.textViewPermission);
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

        pw = (EditText)rootView.findViewById(R.id.PasswordContenitore);


        btnInvia = (Button) rootView.findViewById(R.id.buttonInvia);

        btnInvia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(isTextViewVisible(Codice)){
                        Log.d("CodiceNull ",pw.getText().toString());
                        if (pw.getText().toString().equals(passwordContenitore)){
                            Log.d("Vai  a ","Strumenti biomedicali");
                            Intent i = new Intent(pwContenitore.this.getActivity(), SaluteS.class);
                            startActivity(i);
                        }
                    }
                    else{

                        InserisciCodice = (EditText) rootView.findViewById(R.id.CodiceContenitore);
                        Log.d("CodiceNoNull ",InserisciCodice.getText().toString());
                        if (InserisciCodice.getText().toString().equals(codiceQrContenitore) && pw.getText().toString().equals(passwordContenitore)){
                            Log.d("Vai  a ","Strumenti biomedicali");
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