package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class ParametriMediciR extends Fragment {

    private FirebaseFirestore firestore;
    private String userId;
    private List<BarChart> barCharts;

    boolean allChartsEmpty = true;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//    String formattedCurrentDate = dateFormat.format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametri_medici_r, container, false);
        String connect=getString(R.string.connessione);
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            showToast("User not logged in");
        }

        barCharts = new ArrayList<>();
        barCharts.add(view.findViewById(R.id.barChartTemperaturaCorporea));
        barCharts.add(view.findViewById(R.id.barChartFrequenzaCardiaca));
        barCharts.add(view.findViewById(R.id.barChartPressioneMax));
        barCharts.add(view.findViewById(R.id.barChartPressioneMin));
        barCharts.add(view.findViewById(R.id.barChartSaturazione));
        barCharts.add(view.findViewById(R.id.barChartGlucosio));

        for (int i = 0; i < barCharts.size(); i++) {
            BarChart barChart = barCharts.get(i);
            if (barChart != null) {
                barChart.setNoDataText(getString(R.string.noChartData) + "\n" + getFieldName(i));
                barChart.setVisibility(View.GONE);
            }
        }

        Button shareButton = view.findViewById(R.id.shareButton);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    shareData();
                } else {
                    Toast.makeText(requireContext(),connect, Toast.LENGTH_LONG).show();
                }
            }
        });

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            fetchUserDataFromFirestore(userId);
        } else {
            Toast.makeText(requireContext(),connect, Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private String getFieldName(int index) {
        String[] fieldNames = {"Temperatura Corporea", "Frequenza Cardiaca", "Pressione Massima", "Pressione Minima", "Saturazione", "Glicemia"};
        return fieldNames[index];
    }

    private void fetchUserDataFromFirestore(String uid) {
        firestore.collection("PARAMETRI_UTENTI")
                .whereEqualTo("ID_RichiedenteAsilo", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, List<BarEntry>> fieldEntriesMap = new HashMap<>();
                        List<String> dateValues = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateValue = document.getString("DataVisita");
                            dateValues.add(dateValue);

                            Map<String, Object> data = document.getData();

                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String fieldName = entry.getKey();
                                Object fieldValue = entry.getValue();

                                if (!fieldName.equals("Utente") && !fieldName.equals("ID_RichiedenteAsilo") && !fieldName.equals("DataVisita")) {
                                    float value = ((Number) fieldValue).floatValue();

                                    if (value != 0) {
                                        List<BarEntry> entries = fieldEntriesMap.get(fieldName);
                                        if (entries == null) {
                                            entries = new ArrayList<>();
                                            fieldEntriesMap.put(fieldName, entries);
                                            allChartsEmpty = false;
                                        }
                                        entries.add(new BarEntry(entries.size() + 1, value));
                                    }
                                }
                            }
                        }

                        setupBarCharts(fieldEntriesMap, dateValues);
                    } else {
                        showToast("Error fetching data");
                    }
                });
    }

    private void setupBarCharts(Map<String, List<BarEntry>> fieldEntriesMap, List<String> dateValues) {
        int index = 0;

        for (Map.Entry<String, List<BarEntry>> entry : fieldEntriesMap.entrySet()) {
            String fieldName = entry.getKey();
            List<BarEntry> entries = entry.getValue();

            BarDataSet dataSet = new BarDataSet(entries, getDisplayName(fieldName));
            dataSet.setColors(getColor(fieldName));
            dataSet.setDrawValues(true);
            dataSet.setValueTextSize(12f);
            dataSet.setValueFormatter(new CustomValueFormatter(fieldName));

            BarData barData = new BarData(dataSet);

            BarChart barChart = barCharts.get(index++);
            customizeBarChart(barChart, dateValues);

            barChart.setData(barData);

            barChart.setVisibility(View.VISIBLE);

            barChart.invalidate();
        }

        if (allChartsEmpty) {
            displayMessage();
        }

        allChartsEmpty = true;
    }

    private static class CustomValueFormatter extends ValueFormatter {
        private final String fieldName;

        public CustomValueFormatter(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getBarLabel(BarEntry barEntry) {
            switch (fieldName) {
                case "TemperaturaCorporea":
                    return barEntry.getY() + " °C";
                case "FrequenzaCardiaca":
                    return barEntry.getY() + " BPM";
                case "PressioneMax":
                    return barEntry.getY() + " mmHg";
                case "PressioneMin":
                    return barEntry.getY() + " mmHg";
                case "Saturazione":
                    return barEntry.getY() + " %";
                case "Glucosio":
                    return barEntry.getY() + " mg/dL";
                default:
                    return String.valueOf(barEntry.getY());
            }
        }
    }

    private void shareData() {
        String subject = "MISURAZIONE PARAMETRI MEDICI - ULTIMA VISITA";

        fetchLastDocumentFromFirestore(userId, new FirestoreCallback() {
            @Override
            public void onDataFetched(String text) {
                if (text.isEmpty()) {
                    showToast("Non ci sono dati da condividere");
                } else {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);

                    List<Intent> targetedShareIntents = new ArrayList<>();
                    List<ResolveInfo> resInfo = requireContext().getPackageManager().queryIntentActivities(shareIntent, 0);

                    if (!resInfo.isEmpty()) {
                        for (ResolveInfo resolveInfo : resInfo) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            if (packageName.contains("com.whatsapp") || packageName.contains("com.google.android.gm")) {
                                Intent targeted = new Intent(Intent.ACTION_SEND);
                                targeted.setType("text/plain");
                                targeted.putExtra(Intent.EXTRA_SUBJECT, subject);
                                targeted.putExtra(Intent.EXTRA_TEXT, text);
                                targeted.setPackage(packageName);
                                targetedShareIntents.add(targeted);
                            }
                        }

                        if (!targetedShareIntents.isEmpty()) {
                            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Scegli l'app con cui condividere i tuoi dati");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Intent[]{}));
                            startActivity(chooserIntent);
                        } else {
                            showToast("Nessuna app è in grado di eseguire questa azione.");
                        }
                    } else {
                        showToast("Nessuna app è in grado di eseguire questa azione.");
                    }
                }
            }
        });
    }

    private void displayMessage() {
        RelativeLayout relativeLayout = requireView().findViewById(R.id.chartContainer);

        for (BarChart barChart : barCharts) {
            if (barChart != null) {
                relativeLayout.removeView(barChart);
            }
        }

        Button shareButton = requireView().findViewById(R.id.shareButton);
        shareButton.setVisibility(View.GONE);

        TextView messageTextView = requireView().findViewById(R.id.messageTextView);
        messageTextView.setText(getString(R.string.noParametri));
        messageTextView.setVisibility(View.VISIBLE);
    }

    private void customizeBarChart(BarChart barChart, List<String> dateValues) {
        barChart.getDescription().setEnabled(false);

        barChart.getXAxis().setValueFormatter(new DateAxisValueFormatter(dateValues));

        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        barChart.getXAxis().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(16f);
        legend.setTypeface(Typeface.DEFAULT_BOLD);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        legend.setYOffset(12f);
        legend.setXOffset(10f);
    }

    private String getDisplayName(String fieldName) {
        switch (fieldName) {
            case "TemperaturaCorporea":
                return "Temperatura Corporea";
            case "FrequenzaCardiaca":
                return "Frequenza Cardiaca";
            case "PressioneMax":
                return "Pressione Massima";
            case "PressioneMin":
                return "Pressione Minima";
            case "Saturazione":
                return "Saturazione";
            case "Glucosio":
                return "Glicemia";
            default:
                return fieldName;
        }
    }

    private int getColor(String fieldName) {
        switch (fieldName) {
            case "TemperaturaCorporea":
                return ColorTemplate.JOYFUL_COLORS[0];
            case "FrequenzaCardiaca":
                return ColorTemplate.LIBERTY_COLORS[1];
            case "PressioneMax":
                return ColorTemplate.JOYFUL_COLORS[2];
            case "PressioneMin":
                return ColorTemplate.LIBERTY_COLORS[2];
            case "Saturazione":
                return ColorTemplate.JOYFUL_COLORS[4];
            case "Glucosio":
                return ColorTemplate.LIBERTY_COLORS[0];
            default:
                return Color.GRAY;
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static class DateAxisValueFormatter extends ValueFormatter {
        private final List<String> dateValues;

        public DateAxisValueFormatter(List<String> dateValues) {
            this.dateValues = dateValues;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            int index = (int) value;
            if (index >= 0 && index < dateValues.size()) {
                return dateValues.get(index);
            }
            return "";
        }
    }

    private interface FirestoreCallback {
        void onDataFetched(String text);
    }

    private void fetchLastDocumentFromFirestore(String uid, FirestoreCallback callback) {
        firestore.collection("PARAMETRI_UTENTI")
                .whereEqualTo("ID_RichiedenteAsilo", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> documentStrings = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dateValue = document.getString("DataVisita");

                            // Convert dateValue to Date object
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date date = null;
                            try {
                                date = dateFormat.parse(dateValue);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (date != null) {
                                documentStrings.add(documentDataToString(document.getData(), date));
                            }
                        }

                        // Sort the array based on the parsed date
                        Collections.sort(documentStrings, (s1, s2) -> {
                            try {
                                Date date1 = dateFormat.parse(getDateValueFromString(s1));
                                Date date2 = dateFormat.parse(getDateValueFromString(s2));
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        });

                        if (!documentStrings.isEmpty()) {
                            String shareText = documentStrings.get(0); // Get the most recent document
                            callback.onDataFetched(shareText);
                        } else {
                            callback.onDataFetched("");
                        }
                    } else {
                        callback.onDataFetched("");
                    }
                });
    }

    private String documentDataToString(Map<String, Object> data, Date date) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Data Ultima Visita: ").append(dateFormat.format(date)).append("\n");

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (!fieldName.equals("Utente") && !fieldName.equals("ID_RichiedenteAsilo") && !fieldName.equals("DataVisita")) {
                float value = ((Number) fieldValue).floatValue();

                if (value != 0) {
                    stringBuilder.append("Parametro: ").append(getDisplayName(fieldName)).append("\n")
                            .append("Valore: ").append(value).append(" ").append(getMeasurementUnit(fieldName)).append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }

    private String getDateValueFromString(String documentString) {
        // Extract and return the date value from the document string
        String[] lines = documentString.split("\n");
        for (String line : lines) {
            if (line.startsWith("Data Ultima Visita:")) {
                return line.substring("Data Ultima Visita: ".length()).trim();
            }
        }
        return "";
    }

    private String getMeasurementUnit(String fieldName) {
        // Return the measurement unit based on the field name
        switch (fieldName) {
            case "TemperaturaCorporea":
                return "°C";
            case "FrequenzaCardiaca":
                return "BPM";
            case "PressioneMax":
            case "PressioneMin":
                return "mmHg";
            case "Saturazione":
                return "%";
            case "Glucosio":
                return "mg/dL";
            default:
                return "";
        }
    }
}