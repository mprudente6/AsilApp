package it.uniba.dib.sms23248.SpeseRichiedente;

import static it.uniba.dib.sms23248.SpeseRichiedente.DateConverter.convertDateToTimestamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import it.uniba.dib.sms23248.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkUtils;
import it.uniba.dib.sms23248.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BilancioFragment extends Fragment {


    private NetworkChangeReceiver networkChangeReceiver;

    private SpeseModel viewModel;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
     FirebaseAuth mAuth=FirebaseAuth.getInstance();

     FirebaseUser currentUser=mAuth.getCurrentUser();
     String uid=currentUser.getUid();
    private final DocumentReference documentReference = firestore.collection("RICHIEDENTI_ASILO").document(uid);

    private  TextView budgetTextView;

    CardView cardView1;
    TextView textSett;
    CardView cardView2;
    TextView textMese;

     ProgressBar loadingIndicator;




  private Button refresh;
   TextView budget;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_bilancio, container, false);
        viewModel = new ViewModelProvider(requireActivity().getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SpeseModel.class);
        budgetTextView = view.findViewById(R.id.textView2);
        cardView1 = view.findViewById(R.id.CardView1);
        textSett = view.findViewById(R.id.textSett);
        cardView2 = view.findViewById(R.id.CardView2);
        textMese = view.findViewById(R.id.textMese);
        loadingIndicator = view.findViewById(R.id.progressBar);
         refresh=view.findViewById(R.id.refresh);
         budget=view.findViewById(R.id.textBilancio);



           showLoadingIndicator();

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            loadBudget();
            QuerySett();
            QueryMese();
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
        }



           refresh.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   loadBudget();
               }
           });






        viewModel.getUpdatedBudgetLiveData().observe(getActivity(), new Observer<Double>() {
            @Override
            public void onChanged(Double newBudget) {
                loadBudget();



            }
        });


        return view;
    }



     void loadBudget() {
         Log.d("BUDGET", "started");
         documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
             @Override
             public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                 if (e != null) {

                     Log.e("BUDGET", "Error fetching budget", e);
                     return;
                 }

                 if (documentSnapshot != null && documentSnapshot.exists()) {
                     Double budget = documentSnapshot.getDouble("Budget");

                     updateBudgetTextView(budget);
                     hideLoadingIndicator();
                 }
             }
         });
     }




    private void updateBudgetTextView(Double currentBudget) {
        String budgetString = String.format(Locale.getDefault(), "%.2f€", currentBudget);

        budget.setText(budgetString);



    }
    public void QuerySett() {


        Calendar calendar = Calendar.getInstance();

// Imposta il calendario al primo giorno della settimana
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());


// Ottieni la data di inizio settimana
        Date startDate = calendar.getTime();

// Spostati in avanti di 6 giorni per ottenere la fine della settimana
        calendar.add(Calendar.DAY_OF_WEEK, 6);

// Ottieni la data di fine settimana
        Date endDate = calendar.getTime();


        long startTimeStamp = startDate.getTime();
        long endTimeStamp = endDate.getTime();


        DocumentReference parentDocument = firestore.collection("SPESE").document(uid);


        parentDocument.collection("Subspese")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {

                        Log.e("Fetching", "Error getting prices for the current week", e);
                        return;
                    }

                    Double totSett = 0.00;



                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String dateString = document.getString("data");


                        long timeStamp = convertDateToTimestamp(dateString);


                        if (timeStamp >= startTimeStamp && timeStamp <= endTimeStamp) {
                            Double prezzo = document.getDouble("prezzo");
                            if (prezzo != null) {
                                totSett += prezzo;
                                Log.e("TotSett", "Getting prices for the current week: " + totSett);
                            }
                        }
                    }

                    displayTotSett(totSett);

                });
    }

    private void displayTotSett(Double totSett) {

        String spesaSett = getString(R.string.Spesasettimana);



        String text = String.format(Locale.getDefault(), spesaSett+" "+"%.2f€", totSett);

        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf(String.format(Locale.getDefault(), "%.2f", totSett));
        int endIndex = startIndex + String.format(Locale.getDefault(), "%.2f", totSett).length();


        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textSett.setText(spannableString);


    }
    public void QueryMese() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Imposta al primo giorno del mese
        Date startDate = calendar.getTime(); // La data di inizio è il primo giorno del mese

        calendar.add(Calendar.MONTH, 1); // Sposta al primo giorno del mese successivo
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Sposta all'ultimo giorno del mese corrente
        Date endDate = calendar.getTime(); // La data di fine è l'ultimo giorno del mese



        long startTimeStamp = startDate.getTime();
        long endTimeStamp = endDate.getTime();


        DocumentReference parentDocument = firestore.collection("SPESE").document(uid);


        parentDocument.collection("Subspese")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {

                        Log.e("Fetching", "Error getting prices for the current month", e);
                        return;
                    }

                    Double totMese = 0.00;
                    Log.d("TotSett", "Getting prices for the current month: " + totMese);


                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String dateString = document.getString("data");


                        long timeStamp = convertDateToTimestamp(dateString);


                        if (timeStamp >= startTimeStamp && timeStamp <= endTimeStamp) {
                            Double prezzo = document.getDouble("prezzo");
                            if (prezzo != null) {
                                totMese += prezzo;
                                Log.d("TotSett", "Getting prices for the current month: " + totMese);
                            }
                        }
                    }

                    displayTotMese(totMese);

                });
    }


    private void displayTotMese(Double totMese) {
        String spesaMese = getString(R.string.SpesaMensile);
        String text = String.format(Locale.getDefault(), spesaMese+" "+"%.2f€", totMese);

        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf(String.format(Locale.getDefault(), "%.2f", totMese));
        int endIndex = startIndex + String.format(Locale.getDefault(), "%.2f", totMese).length();


        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textMese.setText(spannableString);
    }

     void showLoadingIndicator() {

        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private  void hideLoadingIndicator() {

        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkChangeReceiver != null) {
            getActivity().unregisterReceiver(networkChangeReceiver);
        }
    }

}