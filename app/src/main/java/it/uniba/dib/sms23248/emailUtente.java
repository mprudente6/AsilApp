package it.uniba.dib.sms23248;

import android.content.Intent;
import android.os.Bundle;
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


public class emailUtente extends Fragment {

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


        return rootView;

    }


}