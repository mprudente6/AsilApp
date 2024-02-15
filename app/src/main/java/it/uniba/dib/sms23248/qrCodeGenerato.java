package it.uniba.dib.sms23248;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class qrCodeGenerato extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public qrCodeGenerato() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        mAuth = FirebaseAuth.getInstance();

        CardView btnGenaraQr = getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr = getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr.setVisibility(View.INVISIBLE);

        CardView btnMedia = getActivity().findViewById(R.id.cardMedia);
        btnMedia = getActivity().findViewById(R.id.cardMedia);
        btnMedia.setVisibility(View.INVISIBLE);

        ImageView btnApriMappa = getActivity().findViewById(R.id.btnApriMappa);
        btnApriMappa = getActivity().findViewById(R.id.btnApriMappa);
        btnApriMappa.setVisibility(View.INVISIBLE);

        CardView mappacard = getActivity().findViewById(R.id.cardMap);
        mappacard = getActivity().findViewById(R.id.cardMap);
        mappacard.setVisibility(View.INVISIBLE);

        CardView btnInformazioniCentro = getActivity().findViewById(R.id.cardInfoCentro);
        btnInformazioniCentro = getActivity().findViewById(R.id.cardInfoCentro);
        btnInformazioniCentro.setVisibility(View.INVISIBLE);

        CardView btnInformazioniSalute = getActivity().findViewById(R.id.cardSaluteR);
        btnInformazioniSalute = getActivity().findViewById(R.id.cardSaluteR);
        btnInformazioniSalute.setVisibility(View.INVISIBLE);

        View rootView = inflater.inflate(R.layout.fragment_qr_code_generato, container, false);

        ImageView imageQrCode;
        imageQrCode = rootView.findViewById(R.id.qrCode);

        ImageButton chiudi;
        chiudi = rootView.findViewById(R.id.chiudiFragment);

        chiudi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView btnGenaraQr = getActivity().findViewById(R.id.generaQrRichiedente);
                btnGenaraQr = getActivity().findViewById(R.id.generaQrRichiedente);
                btnGenaraQr.setVisibility(View.VISIBLE);

                CardView btnMedia = getActivity().findViewById(R.id.cardMedia);
                btnMedia = getActivity().findViewById(R.id.cardMedia);
                btnMedia.setVisibility(View.VISIBLE);

                ImageView btnApriMappa = getActivity().findViewById(R.id.btnApriMappa);
                btnApriMappa = getActivity().findViewById(R.id.btnApriMappa);
                btnApriMappa.setVisibility(View.VISIBLE);

                CardView mappacard = getActivity().findViewById(R.id.cardMap);
                mappacard = getActivity().findViewById(R.id.cardMap);
                mappacard.setVisibility(View.VISIBLE);

                CardView btnInformazioniCentro = getActivity().findViewById(R.id.cardInfoCentro);
                btnInformazioniCentro = getActivity().findViewById(R.id.cardInfoCentro);
                btnInformazioniCentro.setVisibility(View.VISIBLE);

                CardView btnInformazioniSalute = getActivity().findViewById(R.id.cardSaluteR);
                btnInformazioniSalute = getActivity().findViewById(R.id.cardSaluteR);
                btnInformazioniSalute.setVisibility(View.VISIBLE);

                getFragmentManager().beginTransaction()
                        .remove(qrCodeGenerato.this).commit();
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            //Generazione Codice QR sulla base dell UID dell'utente loggato come Richiedente Asilo
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(uid, BarcodeFormat.QR_CODE, 300, 300);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    imageQrCode.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }

            }

        return rootView;
    }
}