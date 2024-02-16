package it.uniba.dib.sms23248;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ParametriMediciFragment extends Fragment implements SensorEventListener {

    private LinearLayout temperatureLayout, heartRateLayout, bloodPressureLayout, pulseOxLayout, glucoseLayout;
    private FirebaseFirestore firestore;
    private String userId;

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

    private float heartRate = 0;

    boolean contenitoreAperto = pwContenitore.contenitoreAperto;


    // conversione da tipo di dato Date a String
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String formattedCurrentDate = dateFormat.format(new Date());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parametri_medici, container, false);

        firestore = FirebaseFirestore.getInstance(); // Inizializza Firebase

        // LinearLayout di ogni parametro medico
        temperatureLayout = view.findViewById(R.id.temperatureLayout);
        heartRateLayout = view.findViewById(R.id.heartRateLayout);
        bloodPressureLayout = view.findViewById(R.id.bloodPressureLayout);
        pulseOxLayout = view.findViewById(R.id.pulseOxLayout);
        glucoseLayout = view.findViewById(R.id.glucoseLayout);

        userId = HomeS.UID; // valore del QR code utente RichiedenteAsilo inquadrato dall'utente Staff loggato (= UID di RichiedenteAsilo)

        // Pulsanti di misurazione
        MaterialButton temperatureButton = view.findViewById(R.id.temperatureButton);
        MaterialButton heartRateButton = view.findViewById(R.id.heartRateButton);
        MaterialButton bloodPressureButton = view.findViewById(R.id.bloodPressureButton);
        MaterialButton pulseOxButton = view.findViewById(R.id.pulseOxButton);
        MaterialButton glucoseButton = view.findViewById(R.id.glucoseButton);

        // unico elemento visualizzabile con la scansione del QR code utente nella pagina 'Contenitore Biomedicale'
        Button apriContenitoreButton = view.findViewById(R.id.apriContenitore);
        apriContenitoreButton.setOnClickListener(v -> apriContenitoreButtonClicked());

        Button saveButton = view.findViewById(R.id.saveButton);

        // Al click del pulsante 'Salva' procedi con salvataggio dei dati di misurazione raccolti
        saveButton.setOnClickListener(v -> saveButtonClicked());

        if (contenitoreAperto) {
            // rendi visibile i pulsanti di misurazione
            temperatureLayout.setVisibility(View.VISIBLE);
            heartRateLayout.setVisibility(View.VISIBLE);
            bloodPressureLayout.setVisibility(View.VISIBLE);
            pulseOxLayout.setVisibility(View.VISIBLE);
            glucoseLayout.setVisibility(View.VISIBLE);

            // nascondi il pulsante 'Apri Contenitore'
            apriContenitoreButton.setVisibility(View.GONE);
        }

        // Al click di ciascun pulsante di misurazione:
        // mostra il pulsante 'Salva' ancora nascosto;
        // mostra progress bar per simulare misurazione con ritardo di caricamento;
        // mostra risultato della misurazione.

        // Tutte le misurazioni dei parametri medici sono simulate.
        // Se il dispositivo usato ha il sensore hardware Heart Rate, la frequenza cardiaca si potrà misurare realmente
        temperatureButton.setOnClickListener(v -> {
            saveButton.setVisibility(View.VISIBLE);
            showLoadingLayout(temperatureLayout, R.id.temperatureProgressBar, R.id.temperatureResultTextView);
            simulateTemperatureMeasurement();
        });

        heartRateButton.setOnClickListener(v -> {
            saveButton.setVisibility(View.VISIBLE);
            checkHeartRateSensor(); // controllo su presenza del sensore
        });

        bloodPressureButton.setOnClickListener(v -> {
            saveButton.setVisibility(View.VISIBLE);
            showLoadingLayout(bloodPressureLayout, R.id.bloodPressureProgressBar, R.id.bloodPressureResultTextView);
            simulateBloodPressureMeasurement();
        });

        pulseOxButton.setOnClickListener(v -> {
            saveButton.setVisibility(View.VISIBLE);
            showLoadingLayout(pulseOxLayout, R.id.pulseOxProgressBar, R.id.pulseOxResultTextView);
            simulatePulseOxMeasurement();
        });

        glucoseButton.setOnClickListener(v -> {
            saveButton.setVisibility(View.VISIBLE);
            showLoadingLayout(glucoseLayout, R.id.glucoseProgressBar, R.id.glucoseResultTextView);
            simulateGlucoseMeasurement();
        });

        return view;
    }

    // al click del pulsante 'Apri Contenitore' procedi con funzionalità di:
    // apertura della fotocamera;
    // scansione del QR code Contenitore;
    // inserimento di una password del Contenitore
    private void apriContenitoreButtonClicked() {
        Intent intent = new Intent(ParametriMediciFragment.this.getActivity(), Contenitore.class);
        startActivity(intent);
    }

    private void checkHeartRateSensor() {
        // Inizializza SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        // Inizializza Heart Rate Sensor
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) { // sensore disponibile: mostra istruzioni per l'uso
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
            showToast(getString(R.string.sensore_disponibile));

            // Mostra istruzioni per misurare la frequenza cardiaca tramite il sensore presente nel dispositivo
            showToast(getString(R.string.istruzioni_sensore));

        } else { // sensore non disponibile: mostra risultato simulato con ritardo di caricamento
            showToast(getString(R.string.sensore_non_disponibile));
            showLoadingLayout(heartRateLayout, R.id.heartRateProgressBar, R.id.heartRateResultTextView);
            simulateHeartRateMeasurement();
        }

        // Richiesta di permessi a runtime se necessario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_BODY_SENSORS);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Al cambiamento dei dati rilevati col sensore Heart Rate
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            // salva dati rilevati
            float heartRateTmp = event.values[0];

            // Mostra il risultato della misurazione solo se:
            // diverso da 0 e non ancora mostrato
            if (heartRateTmp != 0 && !isFirstHeartRateValueDisplayed) {
                heartRate = heartRateTmp;

                showToast(getString(R.string.detected_heart_rate));
                showHeartRate(formatDouble(heartRate));

                // il risultato è stato mostrato
                isFirstHeartRateValueDisplayed = true;

                onDestroy(); // termina l'attività del sensore
            }

            // Fino a che il risultato è uguale a 0:
            // la variabile flag booleana è false (dato non mostrato);
            // mostra istruzioni
            if (heartRateTmp == 0) {
                isFirstHeartRateValueDisplayed = false;

                // Mostra istruzioni per misurare la frequenza cardiaca tramite il sensore presente nel dispositivo
                showToast(getString(R.string.istruzioni_sensore));
            }
        }
    }

    private void showHeartRate(double value) { // con sensore
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView resultTextView = heartRateLayout.findViewById(R.id.heartRateResultTextView);
                resultTextView.setVisibility(View.VISIBLE);
                resultTextView.setText(String.format(Locale.getDefault(), "%.2f bpm", value));
            }
        },0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Gestione della diversa precisione dei dati rilevati
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Annulla la registrazione dei sensor listeners
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, heartRateSensor);
        }
    }

    private void saveButtonClicked() {
        // Controlla l'esistenza nel db del documento per lo specifico utente
        firestore.collection("PARAMETRI_UTENTI")
                .whereEqualTo("ID_RichiedenteAsilo", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean documentExists = !task.getResult().isEmpty();

                        if (documentExists) {
                            // controlla se il documento esistente ha la data di oggi come valore del campo DataVisita
                            checkExistingDocumentWithCurrentDate();
                        } else {
                            // documento inesistente: creane uno
                            showToast(getString(R.string.prima_visita));
                            createNewDocument();
                        }
                    } else { // Errore
                        showToast(getString(R.string.errore_parametri_medici_s));
                    }
                });
    }

    private void checkExistingDocumentWithCurrentDate() {

        firestore.collection("PARAMETRI_UTENTI")
                .whereEqualTo("ID_RichiedenteAsilo", userId)
                .whereEqualTo("DataVisita", formattedCurrentDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean documentExistsWithCurrentDate = !task.getResult().isEmpty();

                        if (documentExistsWithCurrentDate) {
                            // se il documento esistente ha la data odierna, aggiornalo
                            DocumentSnapshot existingDocument = task.getResult().getDocuments().get(0);
                            updateExistingDocument(existingDocument.getId());
                        } else {
                            // altrimenti, creane uno nuovo
                            createNewDocument();
                        }

                    } else { // Errore
                        showToast(getString(R.string.errore_parametri_medici_s));
                    }
                });
    }

    private void updateExistingDocument(String documentId) {
        Map<String, Object> data = new HashMap<>();

        // aggiorna il documento con ogni valore diverso da 0
        if (simulatedTemperature != 0) {
            data.put("TemperaturaCorporea", formatDouble(simulatedTemperature));
        }
        if (heartRate != 0) { // se presente sensore, salva il valore rilevato
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
            //        data.put("Glicemia", simulatedGlucose);
        }

        // aggiorna nel db
        firestore.collection("PARAMETRI_UTENTI").document(documentId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    showToast(getString(R.string.parametri_medici_aggiornati));
                })
                .addOnFailureListener(e -> {
                    showToast(getString(R.string.errore_parametri_medici_aggiornati));
                });
    }

    // arrotonda i valori di tipo double
    private double formatDouble(double value) {
        BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void createNewDocument() {
        Map<String, Object> data = new HashMap<>();
        data.put("ID_RichiedenteAsilo", userId);

        data.put("DataVisita", formattedCurrentDate);

        data.put("TemperaturaCorporea", formatDouble(simulatedTemperature));

        if (heartRate != 0) {
            data.put("FrequenzaCardiaca", formatDouble(heartRate));
        } else {
            data.put("FrequenzaCardiaca", simulatedHeartRate);
        }

        data.put("PressioneMin", simulatedBloodPressure[1]);
        data.put("PressioneMax", simulatedBloodPressure[0]);

        data.put("Saturazione", simulatedPulseOx);

        data.put("Glucosio", simulatedGlucose);
//        data.put("Glicemia", simulatedGlucose);

        // creazione nel db
        firestore.collection("PARAMETRI_UTENTI")
                .add(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast(getString(R.string.parametri_medici_creati));
                    } else {
                        showToast(getString(R.string.errore_parametri_medici_creati));
                    }
                });
    }

    private void showToast(String message) { // mostra messaggio
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showLoadingLayout(LinearLayout layout, int progressBarId, int resultTextViewId) {
        ProgressBar progressBar = layout.findViewById(progressBarId);
        TextView resultTextView = layout.findViewById(resultTextViewId);

        // simula misurazione con attesa nel caricamento del risultato
        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setVisibility(View.GONE);
    }

    private void simulateTemperatureMeasurement() { // simula valore di temperatura corporea
        simulatedTemperature = 36.5 + random.nextDouble() * 2; // 36.5 - 38.5

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // mostra risultato simulazione
                ProgressBar progressBar = temperatureLayout.findViewById(R.id.temperatureProgressBar);
                TextView resultTextView = temperatureLayout.findViewById(R.id.temperatureResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%.2f °C", simulatedTemperature));
            }
        }, 2000); // Simula caricamento per 2 secondi
    }

    private void simulateHeartRateMeasurement() { // simula valore di frequenza cardiaca
        simulatedHeartRate = 60 + random.nextInt(100); // 60 - 159

        new Handler().postDelayed(new Runnable() { // mostra risultato simulazione
            @Override
            public void run() {
                ProgressBar progressBar = heartRateLayout.findViewById(R.id.heartRateProgressBar);
                TextView resultTextView = heartRateLayout.findViewById(R.id.heartRateResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d bpm", simulatedHeartRate));
            }
        }, 2000);  // Simula caricamento per 2 secondi
    }

    private void simulateBloodPressureMeasurement() { // simula valore di pressione
        simulatedBloodPressure = getBloodPressure();

        new Handler().postDelayed(new Runnable() { // mostra risultato simulazione
            @Override
            public void run() {
                ProgressBar progressBar = bloodPressureLayout.findViewById(R.id.bloodPressureProgressBar);
                TextView resultTextView = bloodPressureLayout.findViewById(R.id.bloodPressureResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d mmHg\n%d mmHg", simulatedBloodPressure[0], simulatedBloodPressure[1]));
            }
        }, 2000);  // Simula caricamento per 2 secondi
    }

    private int[] getBloodPressure() {
        int max = 110 + random.nextInt(50); // 110-160
        int min = 70 + random.nextInt(30); // 70-100
        return new int[]{max, min};
    }

    private void simulatePulseOxMeasurement() { // simula valore di saturazione ossigeno
        simulatedPulseOx = 80 + random.nextInt(20); // 80 - 100

        new Handler().postDelayed(new Runnable() { // mostra risultato
            @Override
            public void run() {
                ProgressBar progressBar = pulseOxLayout.findViewById(R.id.pulseOxProgressBar);
                TextView resultTextView = pulseOxLayout.findViewById(R.id.pulseOxResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d%%", simulatedPulseOx));
            }
        }, 2000); // Simula caricamento per 2 secondi
    }

    private void simulateGlucoseMeasurement() { // simula valore di glicemia
        simulatedGlucose = 70 + random.nextInt(200); // 70 - 270

        new Handler().postDelayed(new Runnable() { // mostra risultato
            @Override
            public void run() {
                ProgressBar progressBar = glucoseLayout.findViewById(R.id.glucoseProgressBar);
                TextView resultTextView = glucoseLayout.findViewById(R.id.glucoseResultTextView);

                progressBar.setVisibility(View.GONE);
                resultTextView.setVisibility(View.VISIBLE);

                resultTextView.setText(String.format("%d mg/dL", simulatedGlucose));
            }
        }, 2000); // Simula caricamento per 2 secondi
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}