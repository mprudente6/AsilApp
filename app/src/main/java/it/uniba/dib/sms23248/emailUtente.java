package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        View rootView = inflater.inflate(R.layout.fragment_email_utente, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        EditText Email = rootView.findViewById(R.id.EditEmail);

        Button Invia = rootView.findViewById((R.id.buttonInvia));



        Invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Controllo per ricercare un utente tramite l'email verificando se presente nel database
                HomeS.UID = "";
                db.collection("RICHIEDENTI_ASILO")
                        .whereEqualTo("Email", Email.getText().toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            HomeS.UID = document.getId();
                                            Intent i = new Intent(emailUtente.this.getActivity(), SaluteS.class);
                                            startActivity(i);
                                        }
                                    } else {
                                        // La query non ha restituito alcun risultato
                                        Toast.makeText(emailUtente.this.getActivity(), R.string.noUtente, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });


        return rootView;

    }


}