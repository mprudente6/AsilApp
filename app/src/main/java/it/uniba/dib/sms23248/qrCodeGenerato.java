package it.uniba.dib.sms23248;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link qrCodeGenerato#newInstance} factory method to
 * create an instance of this fragment.
 */
public class qrCodeGenerato extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public qrCodeGenerato() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment qrCodeGenerato.
     */
    // TODO: Rename and change types and number of parameters
    public static qrCodeGenerato newInstance(String param1, String param2) {
        qrCodeGenerato fragment = new qrCodeGenerato();
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

        Button btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
        btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
        btnCamera.setVisibility(View.INVISIBLE);

        Button btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
        btnGenaraQr.setVisibility(View.INVISIBLE);

        Spinner spinnerLingua = (Spinner) getActivity().findViewById(R.id.languageList);
        spinnerLingua = (Spinner) getActivity().findViewById(R.id.languageList);
        spinnerLingua.setVisibility(View.INVISIBLE);


        View rootView = inflater.inflate(R.layout.fragment_qr_code_generato, container, false);

        ImageView imageQrCode;
        imageQrCode = (ImageView) rootView.findViewById(R.id.qrCode);

        ImageButton chiudi;
        chiudi = (ImageButton) rootView.findViewById(R.id.chiudiFragment);

        chiudi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
                btnCamera = (Button) getActivity().findViewById(R.id.apriContenitore);
                btnCamera.setVisibility(View.VISIBLE);

                Button btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
                btnGenaraQr = (Button) getActivity().findViewById(R.id.generaQrRichiedente);
                btnGenaraQr.setVisibility(View.VISIBLE);

                getFragmentManager().beginTransaction()
                        .remove(qrCodeGenerato.this).commit();
            }
        });

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode("ciao", BarcodeFormat.QR_CODE,300,300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageQrCode.setImageBitmap(bitmap);
        }catch (WriterException e) {
            e.printStackTrace();
        }


        return rootView;
    }
}