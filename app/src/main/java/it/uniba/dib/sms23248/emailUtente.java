package it.uniba.dib.sms23248;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link emailUtente#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class emailUtente extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment emailUtente.
     */
    // TODO: Rename and change types and number of parameters
    public static emailUtente newInstance(String param1, String param2) {


        emailUtente fragment = new emailUtente();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public emailUtente() {
        // Required empty public constructor
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

        View rootView = inflater.inflate(R.layout.fragment_email_utente, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        EditText Email = rootView.findViewById(R.id.EditEmail);

        Button Invia = rootView.findViewById((R.id.buttonInvia));

        Invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeS.UID = "";
                db.collection("RICHIEDENTI_ASILO")
                        .whereEqualTo("Email", Email.getText().toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("UID", document.getId() );
                                        HomeS.UID = document.getId();
                                        Intent i = new Intent(emailUtente.this.getActivity(), SaluteS.class);
                                        startActivity(i);
                                    }
                                } else {
                                }
                            }
                        });
            }
        });


        // Inflate the layout for this fragment
        return rootView;
    }
}