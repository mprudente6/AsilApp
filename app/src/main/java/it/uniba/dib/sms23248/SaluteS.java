package it.uniba.dib.sms23248;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SaluteS extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MergedPagerAdapter adapter;

    public static String UID = HomeS.UID;

    boolean contenitoreAperto = pwContenitore.contenitoreAperto;

    private LinearLayout temperatureLayout, heartRateLayout, bloodPressureLayout, pulseOxLayout, glucoseLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salute_s);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        // se il contenitore è aperto mostra la terza scheda: Contenitore Biomedicale
        // con visualizzazione dei pulsanti di misurazione anziché del solo pulsante 'Apri Contenitore'
        // (poiché già aperto)
        if (contenitoreAperto) {
            viewPager.setCurrentItem(2, false);
        }

        // tab layout nella schermata Salute lato Staff
        // (accessibile dopo autenticazione tramite scansione del Qr code di un richiedente asilo)
        // contenente 3 schede visualizzabili: Scheda Utente, Cartella Clinica e Contenitore Biomedicale
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getFragmentTitle(position))
        ).attach();

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                // premendo il tasto indietro da questa scheda l'utente visualizzerà:
                // la home (se si trova nella scheda anagrafica o in cartella clinica)
                if (viewPager.getCurrentItem() == 0 || viewPager.getCurrentItem() == 1) {
                    // naviga in HomeS
                    openHomeS();
                } else if (viewPager.getCurrentItem() == 2) { // o il pulsante 'Apri Contenitore (altrimenti)
                    if (contenitoreAperto) {
                        pwContenitore.contenitoreAperto = false;
                        contenitoreAperto = pwContenitore.contenitoreAperto;

                        temperatureLayout = findViewById(R.id.temperatureLayout);
                        heartRateLayout = findViewById(R.id.heartRateLayout);
                        bloodPressureLayout = findViewById(R.id.bloodPressureLayout);
                        pulseOxLayout = findViewById(R.id.pulseOxLayout);
                        glucoseLayout = findViewById(R.id.glucoseLayout);
                        Button saveButton = findViewById(R.id.saveButton);
                        Button apriContenitoreButton = findViewById(R.id.apriContenitore);

                        temperatureLayout.setVisibility(View.GONE);
                        heartRateLayout.setVisibility(View.GONE);
                        bloodPressureLayout.setVisibility(View.GONE);
                        pulseOxLayout.setVisibility(View.GONE);
                        glucoseLayout.setVisibility(View.GONE);
                        saveButton.setVisibility(View.GONE);

                        apriContenitoreButton.setVisibility(View.VISIBLE);
                    }
                    else { // naviga in HomeS
                        openHomeS();
                    }

                }

                // evita che dopo la prima scansione di QR code utente della sessione
                // alla riapertura della sezione salute lato staff il contenitore risulti già aperto
                pwContenitore.contenitoreAperto = false;
                contenitoreAperto = pwContenitore.contenitoreAperto;
            }
        };

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void setupViewPager(ViewPager2 viewPager) {
        adapter = new MergedPagerAdapter(this);
        adapter.addFragment(new AnagraficaFragment(), "SCHEDA UTENTE");
        adapter.addFragment(new CartellaClinicaFragment(), "CARTELLA CLINICA");
        adapter.addFragment(new ParametriMediciFragment(), "CONTENITORE BIOMEDICALE");
        viewPager.setAdapter(adapter);
    }

    private void openHomeS() {
        Intent intent = new Intent(SaluteS.this, HomeS.class);
        startActivity(intent);
    }
}