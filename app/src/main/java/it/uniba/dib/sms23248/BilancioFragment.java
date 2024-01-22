package it.uniba.dib.sms23248;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static it.uniba.dib.sms23248.DateConverter.convertDateToTimestamp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import it.uniba.dib.sms23248.R;
import it.uniba.dib.sms23248.SpeseModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BilancioFragment extends Fragment {

    private SpeseModel viewModel;
    private  FirebaseFirestore firestore = FirebaseFirestore.getInstance();
     FirebaseAuth mAuth=FirebaseAuth.getInstance();

     FirebaseUser currentUser=mAuth.getCurrentUser();
     String uid=currentUser.getUid();
    private  DocumentReference documentReference = firestore.collection("RICHIEDENTI_ASILO").document(uid);

    private  TextView budgetTextView;
    private double currentBudget = 60.0; // Initial budget

    private Handler handler = new Handler(Looper.getMainLooper());

    private long updateInterval =  30 * 24 * 60 * 60 * 1000L; // 30 days in milliseconds
    //  FOR TESTING  private long updateInterval =  1 * 60 * 1000L; // 1 minute in milliseconds

    CardView cardView1;
    TextView textSett;
    CardView cardView2;
    TextView textMese;

     ProgressBar loadingIndicator;




  private Button refresh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bilancio, container, false);
        viewModel = new ViewModelProvider(requireActivity().getViewModelStore(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SpeseModel.class);
        budgetTextView = view.findViewById(R.id.textView2);
        cardView1 = view.findViewById(R.id.CardView1);
        textSett = view.findViewById(R.id.textSett);
        cardView2 = view.findViewById(R.id.CardView2);
        textMese = view.findViewById(R.id.textMese);
        loadingIndicator = view.findViewById(R.id.progressBar);
         refresh=view.findViewById(R.id.refresh);


           showLoadingIndicator();
           loadInitialBudget();



           refresh.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   loadInitialBudget();
               }
           });




        QuerySett();
        QueryMese();

        viewModel.getUpdatedBudgetLiveData().observe(getActivity(), new Observer<Double>() {
            @Override
            public void onChanged(Double newBudget) {

                updateBudgetTextView(newBudget);
                loadInitialBudget();

            }
        });


        return view;
    }


    private BroadcastReceiver budgetUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the budget when receiving the broadcast
            String uid = intent.getStringExtra("UID");
            loadInitialBudget();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver to receive the budget update broadcast
        IntentFilter filter = new IntentFilter("ACTION_UPDATE_BUDGET");
        requireContext().registerReceiver(budgetUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver to avoid memory leaks
        requireContext().unregisterReceiver(budgetUpdateReceiver);
    }



     void loadInitialBudget() {
         Log.d("BUDGET", "started");
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Double budget = documentSnapshot.getDouble("Budget");

                            updateBudgetTextView(budget);
                            hideLoadingIndicator();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure to load the initial budget
                    }
                });
    }

    void scheduleBudgetUpdates(String uid) {
        Log.d("BUDGET", "scheduleBudgetUpdates() called");

        // Create the runnable
        Runnable budgetUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("BUDGET", "Delayed task running");
                    // Update budget every 30 days
                    updateBudget(60.0, uid);
                    Log.d("BUDGET", "Delayed ended");
                    handler.postDelayed(this, updateInterval);
                } catch (Exception e) {
                    Log.e("BUDGET", "Exception in delayed task: " + e.getMessage());
                }
            }
        };

        // Post the initial delay explicitly
        handler.postDelayed(budgetUpdateRunnable, updateInterval);
    }


    private void updateBudget(double amount, String uid) {
        currentBudget += amount;
        Log.d("BUDGET", "budget UPDATING");
        documentReference = firestore.collection("RICHIEDENTI_ASILO").document(uid);
        documentReference.update("Budget", currentBudget)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {


                        Log.d("BUDGET", "budget UPDATED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the failure to update the budget
                    }
                });
    }

    private void updateBudgetTextView(Double currentBudget) {
        SpannableString spannableString = new SpannableString(String.format(Locale.getDefault(), "Budget:\n\n        %.2f€", currentBudget));

// Set a larger text size for the currentBudget part
        spannableString.setSpan(new RelativeSizeSpan(1.5f), 8, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Apply the SpannableString to the TextView
        budgetTextView.setText(spannableString);



    }
    public void QuerySett() {
        // Define the start and end dates for the current week
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Set to the first day of the week
        Date startDate = calendar.getTime(); // Start date is the first day of the week

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Move to the last day of the week
        Date endDate = calendar.getTime(); // End date is the last day of the week

        // Convert start and end dates to timestamps
        long startTimeStamp = startDate.getTime();
        long endTimeStamp = endDate.getTime();

        // Reference to your Firestore collection
        DocumentReference parentDocument = firestore.collection("SPESE").document(uid);

        // Get a reference to the subcollection
        parentDocument.collection("Subspese")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle the error
                        Log.e("Fetching", "Error getting prices for the current week", e);
                        return;
                    }

                    Double totSett = 0.00;
                    Log.e("TotSett", "Getting prices for the current week: " + totSett);

                    // Iterate through the documents and filter based on the date range
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Retrieve the date as a String
                        String dateString = document.getString("data");

                        // Convert the date to a timestamp
                        long timeStamp = convertDateToTimestamp(dateString);

                        // Check if the timestamp is within the desired range
                        if (timeStamp >= startTimeStamp && timeStamp <= endTimeStamp) {
                            Double prezzo = document.getDouble("prezzo");
                            if (prezzo != null) {
                                totSett += prezzo;
                                Log.e("TotSett", "Getting prices for the current week: " + totSett);
                            }
                        }
                    }

                    displayTotSett(totSett);
                    // You can further process or display this data as needed
                });
    }

    private void displayTotSett(Double totSett) {

        String text = String.format(Locale.getDefault(), "Spesa settimanale: %.2f€", totSett);

        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf(String.format(Locale.getDefault(), "%.2f", totSett));
        int endIndex = startIndex + String.format(Locale.getDefault(), "%.2f", totSett).length();

        // Apply bold style to the totMese value
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textSett.setText(spannableString);


    }
    public void QueryMese() {
        // Define the start and end dates for the current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day of the month
        Date startDate = calendar.getTime(); // Start date is the first day of the month

        calendar.add(Calendar.MONTH, 1); // Move to the first day of the next month
        calendar.add(Calendar.DAY_OF_MONTH, -1); // Move to the last day of the current month
        Date endDate = calendar.getTime(); // End date is the last day of the month

        // Convert start and end dates to timestamps
        long startTimeStamp = startDate.getTime();
        long endTimeStamp = endDate.getTime();

        // Reference to your Firestore collection
        DocumentReference parentDocument = firestore.collection("SPESE").document(uid);

        // Get a reference to the subcollection
        parentDocument.collection("Subspese")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle the error
                        Log.e("Fetching", "Error getting prices for the current month", e);
                        return;
                    }

                    Double totMese = 0.00;
                    Log.d("TotSett", "Getting prices for the current month: " + totMese);

                    // Iterate through the documents and extract prices
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Retrieve the date as a String
                        String dateString = document.getString("data");

                        // Convert the date to a timestamp
                        long timeStamp = convertDateToTimestamp(dateString);

                        // Check if the timestamp is within the desired range
                        if (timeStamp >= startTimeStamp && timeStamp <= endTimeStamp) {
                            Double prezzo = document.getDouble("prezzo");
                            if (prezzo != null) {
                                totMese += prezzo;
                                Log.d("TotSett", "Getting prices for the current month: " + totMese);
                            }
                        }
                    }

                    displayTotMese(totMese);
                    // You can further process or display this data as needed
                });
    }


    private void displayTotMese(Double totMese) {
        String text = String.format(Locale.getDefault(), "Spesa mensile: %.2f€", totMese);

        SpannableString spannableString = new SpannableString(text);
        int startIndex = text.indexOf(String.format(Locale.getDefault(), "%.2f", totMese));
        int endIndex = startIndex + String.format(Locale.getDefault(), "%.2f", totMese).length();

        // Apply bold style to the totMese value
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textMese.setText(spannableString);
    }

     void showLoadingIndicator() {
        // Show your loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private  void hideLoadingIndicator() {
        // Hide your loading indicator
        loadingIndicator.setVisibility(View.GONE);
    }

}