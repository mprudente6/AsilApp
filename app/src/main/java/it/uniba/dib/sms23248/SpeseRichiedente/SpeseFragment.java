package it.uniba.dib.sms23248.SpeseRichiedente;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;


import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SpeseFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView textView;
    private PieChart pieChart;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser= mAuth.getCurrentUser();
    String uid=currentUser.getUid();
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spese, container, false);
        String connessione = getString(R.string.connessione);

        db = FirebaseFirestore.getInstance();
        textView=view.findViewById(R.id.textView1);
        pieChart = view.findViewById(R.id.pieChart);

        pieChart.getDescription().setEnabled(false);

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            fetchSubSpese();
        } else {
            Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
        }


        return view;
    }

    private void fetchSubSpese() {

        db.collection("SPESE").document(uid).collection("Subspese")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {

                        Log.e("FirestoreListener", "Error: ", error);
                        return;
                    }

                    if (value != null) {
                        List<DocumentSnapshot> documents = value.getDocuments();

                        calculatePercentagesAndDisplayChart(documents);
                    }
                });
    }

    private void calculatePercentagesAndDisplayChart(List<DocumentSnapshot> documents) {
        String connessione = getString(R.string.connessione);
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, Float> tipoTotalPrice = new HashMap<>();
        int currentMonth = getCurrentMonth();


        for (DocumentSnapshot document : documents) {

            String  dataF = document.getString("data");
             Long timestampMillis= DateConverter.convertDateToTimestamp(dataF);

            Date date = new Date(timestampMillis);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if (calendar.get(Calendar.MONTH) == currentMonth) {
                String tipo = document.getString("tipo");
                float price = document.getDouble("prezzo").floatValue();

                if (tipoTotalPrice.containsKey(tipo)) {
                    tipoTotalPrice.put(tipo, tipoTotalPrice.get(tipo) + price);
                } else {
                    tipoTotalPrice.put(tipo, price);
                }
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        float totalPrices = 0f;

        for (Map.Entry<String, Float> entry : tipoTotalPrice.entrySet()) {
            totalPrices += entry.getValue();
        }


        for (Map.Entry<String, Float> entry : tipoTotalPrice.entrySet()) {
            String tipo = entry.getKey();
            float totalPrice = entry.getValue();
            float percentage = (totalPrice / totalPrices) * 100;

            entries.add(new PieEntry(percentage, tipo + "\n€" + String.format("%.2f", totalPrice)));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(16f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        String centerText = getString(R.string.SpeseGraf);
        pieChart.setCenterText(centerText);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleRadius(30f);
        pieChart.animateY(1000, Easing.EaseInOutCubic);
        pieChart.invalidate();
        pieChart.setCenterTextSize(16f);
        Legend legend = pieChart.getLegend();


        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setXEntrySpace(10f);
        legend.setYOffset(60f);

    }


    private int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MONTH);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkChangeReceiver != null) {
            getActivity().unregisterReceiver(networkChangeReceiver);
        }
    }

}