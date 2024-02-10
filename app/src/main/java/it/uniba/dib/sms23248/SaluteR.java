package it.uniba.dib.sms23248;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SaluteR extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MergedPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salute_r);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        // tab layout nella schermata Cartella Clinica di richiedente asilo
        // contenente 2 pagine da visualizzare: Cartella Clinica, Parametri Medici
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getFragmentTitle(position))
        ).attach();
    }

    private void setupViewPager(ViewPager2 viewPager) {
        adapter = new MergedPagerAdapter(this);
        adapter.addFragment(new CartellaClinicaR(), "CARTELLA CLINICA");
        adapter.addFragment(new ParametriMediciR(), "PARAMETRI MEDICI");
        viewPager.setAdapter(adapter);
    }
}