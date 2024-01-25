package it.uniba.dib.sms23248;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

public class SaluteS extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salute_s);

        ViewPager viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        MergedPagerAdapter adapter = new MergedPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new AnagraficaFragment(), "SCHEDA UTENTE");
        adapter.addFragment(new CartellaClinicaFragment(), "CARTELLA CLINICA");
        adapter.addFragment(new ParametriMediciFragment(), "CONTENITORE BIOMEDICALE");
        viewPager.setAdapter(adapter);
    }
}

