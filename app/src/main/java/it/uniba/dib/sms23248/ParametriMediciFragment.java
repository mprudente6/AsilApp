package it.uniba.dib.sms23248;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParametriMediciFragment extends Fragment implements SensorEventListener {
    private LinearLayout temperatureLayout, heartRateLayout, bloodPressureLayout, pulseOxLayout, glucoseLayout;
    private FirebaseFirestore firestore;
    private String userId; // Assuming you have the user ID stored somewhere in your app

    Random random = new Random();
    private double simulatedTemperature = 0;
    private int simulatedHeartRate = 0;
    private int[] simulatedBloodPressure = {0, 0};
    private int simulatedPulseOx = 0;
    private int simulatedGlucose = 0;

    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private static final int REQUEST_BODY_SENSORS = 1;
    private boolean isFirstHeartRateValueDisplayed = false;

    private boolean isFragmentVisible = false; // Added to track fragment visibility

    private float heartRate = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametri_medici, container, false);

        firestore = FirebaseFirestore.getInstance(); // Initialize Firebase

        temperatureLayout = view.findViewById(R.id.temperatureLayout);
        heartRateLayout = view.findViewById(R.id.heartRateLayout);
        bloodPressureLayout = view.findViewById(R.id.bloodPressureLayout);
        pulseOxLayout = view.findViewById(R.id.pulseOxLayout);
        glucoseLayout = view.findViewById(R.id.glucoseLayout);

        userId = "1qRWhwM51WP3VjEfMnc4NejOzBh2"; // Replace with your actual user ID

        Button temperatureButton = view.findViewById(R.id.temperatureButton);
        Button heartRateButton = view.findViewById(R.id.heartRateButton);
        Button bloodPressureButton = view.findViewById(R.id.bloodPressureButton);
        Button pulseOxButton = view.findViewById(R.id.pulseOxButton);
        Button glucoseButton = view.findViewById(R.id.glucoseButton);

        temperatureButton.setOnClickListener(v -> {
            showLoadingLayout(temperatureLayout, R.id.temperatureProgressBar, R.id.temperatureResultTextView);
            simulateTemperatureMeasurement();
        });

        heartRateButton.setOnClickListener(v -> {
            checkHeartRateSensor();
        });

        bloodPressureButton.setOnClickListener(v -> {
            showLoadingLayout(bloodPressureLayout, R.id.bloodPressureProgressBar, R.id.bloodPressureResultTextView);
            simulateBloodPressureMeasurement();
        });

        pulseOxButton.setOnClickListener(v -> {
            showLoadingLayout(pulseOxLayout, R.id.pulseOxProgressBar, R.id.pulseOxResultTextView);
            simulatePulseOxMeasurement();
        });

        glucoseButton.setOnClickListener(v -> {
            showLoadingLayout(glucoseLayout, R.id.glucoseProgressBar, R.id.glucoseResultTextView);
            simulateGlucoseMeasurement();
        });

        // Find the save button by its ID
        Button saveButton = view.findViewById(R.id.saveButton);

        // Set a click listener for the save button
        saveButton.setOnClickListener(v -> saveButtonClicked());

        return view;
    }

    private void checkHeartRateSensor() {
        // Initialize SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        // Heart Rate Sensor
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            showToast("Sensore Heart Rate disponibile sul tuo dispositivo!");
            showMeasurementInstructions();
        } else {
            showToast("Sensore Heart Rate non disponibile sul dispositivo...");
            showLoadingLayout(heartRateLayout, R.id.heartRateProgressBar, R.id.heartRateResultTextView);
            simulateHeartRateMeasurement();
        }

        // Request runtime permissions if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_BODY_SENSORS);
            }
        }
    }

    private void showMeasurementInstructions() {
        // Display instructions on how to measure heart rate through available hardware
        Toast.makeText(requireContext(), "Poggia il dito sul sensore per misurare la frequenza cardiaca", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle only heart rate sensor data changes
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            // Heart Rate data
            float heartRateTmp = event.values[0];

            // Display heart rate measurement if it is different from 0
            if (heartRateTmp != 0 && !isFirstHeartRateValueDisplayed) {
                heartRate = heartRateTmp;

                Log.d("HeartRate", "Frequenza cardiaca: " + formatDouble(heartRate) + " bpm");
                showToast("Frequenza cardiaca: " + formatDouble(heartRate) + " bpm");
                isFirstHeartRateValueDisplayed = true;

                onDestroy();
            }

            // Reset the flag when the heart rate becomes 0
            if (heartRateTmp == 0) {
                isFirstHeartRateValueDisplayed = false;
                showMeasurementInstructions();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_BODY_SENSORS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // Permission granted, you can now use the sensors
//        } else {
//            // Permission denied, handle accordingly
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister sensor listeners when the fragment is destroyed
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, heartRateSensor);
        }
    }

    private void saveButtonClicked() {
        // Check if the document exists in the database
        firestore.collection("PARAMETRI_UTENTI")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Document exists, update the measurement fields
                            updateExistingDocument(userId);
                        } else {
                            // Document does not exist, create a new one
                            createNewDocument();
                        }
                    } else {
                        // Handle error
                        showToast("Errore durante la verifica del documento");
                    }
                });
    }

    private void updateExistingDocument(String documentId) {
        Map<String, Object> data = new HashMap<>();
        data.put("ID_RichiedenteAsilo", userId);
        data.put("DataVisita", new java.util.Date());

        // Check and update each field if it has a non-zero value
        if (simulatedTemperature != 0) {
            data.put("TemperaturaCorporea", formatDouble(simulatedTemperature));
        }
        if (heartRate != 0) {
            data.put("FrequenzaCardiaca", formatDouble(heartRate));
        } else if (simulatedHeartRate != 0) {
            data.put("FrequenzaCardiaca", simulatedHeartRate);
        }
        if (simulatedBloodPressure[0] != 0) {
            data.put("PressioneMax", simulatedBloodPressure[0]);
        }
        if (simulatedBloodPressure[1] != 0) {
            data.put("PressioneMin", simulatedBloodPressure[1]);
        }
        if (simulatedPulseOx != 0) {
            data.put("Saturazione", simulatedPulseOx);
        }
        if (simulatedGlucose != 0) {
            data.put("Glucosio", simulatedGlucose);
        }

        // Update the document in the database
        firestore.collection("PARAMETRI_UTENTI").document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    // Document successfully updated
                    showToast("Parametri medici aggiornati con successo!");
                })
                .addOnFailureListener(e -> {
                    // Handle update failures
                    showToast("Errore nell'aggiornamento dei parametri medici");
                });
    }

    private double formatDouble(double value) {
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void createNewDocument() {
        Map<String, Object> data = new HashMap<>();
        data.put("ID_RichiedenteAsilo", userId);
        data.put("DataVisita", new java.util.Date());

        data.put("TemperaturaCorporea", formatDouble(simulatedTemperature));

        if (heartRate != 0) {
            data.put("FrequenzaCardiaca", heartRate);
        } else {
            data.put("FrequenzaCardiaca", simulatedHeartRate);
        }

        data.put("PressioneMin", simulatedBloodPressure[1]);
        data.put("PressioneMax", simulatedBloodPressure[0]);

        data.put("Saturazione", simulatedPulseOx);

        data.put("Glucosio", simulatedGlucose);

        // Use the userId as the document ID
        String documentId = userId;

        // Create a new document in the database
        firestore.collection("PARAMETRI_UTENTI")
                .document(documentId)  // Set the document ID
                .set(data)             // Use set() instead of add()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Handle success if needed
                        showToast("Nuovi parametri medici creati con successo!");
                    } else {
                        // Handle error
                        showToast("Errore nella creazione dei parametri medici");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showLoadingLayout(LinearLayout layout, int progressBarId, int resultTextViewId) {
        ProgressBar progressBar = layout.findViewById(progressBarId);
        TextView resultTextView = layout.findViewById(resultTextViewId);

        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setVisibility(View.GONE);
    }

    private void simulateTemperatureMeasurement() {
        simulatedTemperature = 36.5 + random.nextDouble() * 2; // Replace this with actual measurement logic

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = temperatureLayout.findViewById(R.id.temperatureProgressBar);
                TextView resultTextView = temperatureLayout.findViewById(R.id.temperatureResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%.2f °C", simulatedTemperature));
            }
        }, 2000); // Simulating loading for 2 seconds
    }

    private void simulateHeartRateMeasurement() {
        simulatedHeartRate = 60 + random.nextInt(100); // Replace this with actual measurement logic

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = heartRateLayout.findViewById(R.id.heartRateProgressBar);
                TextView resultTextView = heartRateLayout.findViewById(R.id.heartRateResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d bpm", simulatedHeartRate));
            }
        }, 2000); // Simulating loading for 2 seconds
    }

    private void simulateBloodPressureMeasurement() {
        // Simulate blood pressure measurement
        simulatedBloodPressure = getBloodPressure(); // Simulated blood pressure

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = bloodPressureLayout.findViewById(R.id.bloodPressureProgressBar);
                TextView resultTextView = bloodPressureLayout.findViewById(R.id.bloodPressureResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d mmHg\n%d mmHg", simulatedBloodPressure[0], simulatedBloodPressure[1]));
            }
        }, 2000); // Simulating loading for 2 seconds
    }

    private int[] getBloodPressure() {
        int systolic = 110 + random.nextInt(50); // Simulated range: 110-160 mmHg
        int diastolic = 70 + random.nextInt(30); // Simulated range: 70-100 mmHg
        return new int[]{systolic, diastolic};
    }

    private void simulatePulseOxMeasurement() {
        simulatedPulseOx = 80 + random.nextInt(20); // Simulated range: 80-100%

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = pulseOxLayout.findViewById(R.id.pulseOxProgressBar);
                TextView resultTextView = pulseOxLayout.findViewById(R.id.pulseOxResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d%%", simulatedPulseOx));
            }
        }, 2000); // Simulating loading for 2 seconds
    }

    private void simulateGlucoseMeasurement() {
        simulatedGlucose = 70 + random.nextInt(200); // Simulated range: 70-270 mg/dL

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = glucoseLayout.findViewById(R.id.glucoseProgressBar);
                TextView resultTextView = glucoseLayout.findViewById(R.id.glucoseResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d mg/dL", simulatedGlucose));
            }
        }, 2000); // Simulating loading for 2 seconds
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize views and set listeners here if needed
    }
}