package it.uniba.dib.sms23248;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParametriMediciR extends Fragment {
    private FirebaseFirestore firestore;
    private String userId;
    private List<BarChart> barCharts;

    boolean allChartsEmpty = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametri_medici_r, container, false);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            // Handle the case where the user is not logged in
            showToast("User not logged in");
        }

        // Initialize the list of BarCharts
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
                barChart.setVisibility(View.GONE); // Hide the empty chart
            }
        }

        fetchDataFromFirestore(userId);

        return view;
    }

    private String getFieldName(int index) {
        // Return the field name based on the index
        String[] fieldNames = {"Temperatura Corporea", "Frequenza Cardiaca", "Pressione Massima", "Pressione Minima", "Saturazione", "Glicemia"};
        return fieldNames[index];
    }

    private void fetchDataFromFirestore(String uid) {
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
                                    // Handle the field name and value
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

                        // Populate and adjust the BarCharts
                        setupBarCharts(fieldEntriesMap, dateValues);
                    } else {
                        // Handle errors
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
            dataSet.setValueFormatter(new CustomValueFormatter(fieldName)); // Set custom value formatter

            BarData barData = new BarData(dataSet);

            BarChart barChart = barCharts.get(index++);
            customizeBarChart(barChart, dateValues);

            barChart.setData(barData);

            // Show the chart
            barChart.setVisibility(View.VISIBLE);

            barChart.invalidate();
        }

        // Show toast only if all charts are empty
        if (allChartsEmpty) {
            displayMessage();
        }

        allChartsEmpty = true; // Reset the flag for the next iteration
    }

    // Custom ValueFormatter for formatting chart values
    private static class CustomValueFormatter extends ValueFormatter {
        private final String fieldName;

        public CustomValueFormatter(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getBarLabel(BarEntry barEntry) {
            // Append measurement unit based on field name
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

    private void displayMessage() {
        // Get the RelativeLayout
        RelativeLayout relativeLayout = requireView().findViewById(R.id.chartContainer);

        // Remove all BarChart views
        for (BarChart barChart : barCharts) {
            if (barChart != null) {
                relativeLayout.removeView(barChart);
            }
        }

        // Display your message below the title
        TextView messageTextView = requireView().findViewById(R.id.messageTextView);
        messageTextView.setText(getString(R.string.noParametri));
        messageTextView.setVisibility(View.VISIBLE);
    }

    private void customizeBarChart(BarChart barChart, List<String> dateValues) {
        barChart.getDescription().setEnabled(false); // Disable description

        barChart.getXAxis().setValueFormatter(new DateAxisValueFormatter(dateValues));

        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Display X-axis at the bottom

        // Disable X-axis labels
        barChart.getXAxis().setEnabled(false);

        // Customize legend
        Legend legend = barChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(16f); // Set legend text size
        legend.setTypeface(Typeface.DEFAULT_BOLD);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);

        // Add margin to the legend
        legend.setYOffset(12f); // Set the vertical offset
        legend.setXOffset(10f); // Set the horizontal offset
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
        // Assign colors based on field name using ColorTemplate
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

    // Custom ValueFormatter for X-axis to display date values
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
}