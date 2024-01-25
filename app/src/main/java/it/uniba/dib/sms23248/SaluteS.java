package it.uniba.dib.sms23248;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SaluteS extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MergedPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salute_s);

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getFragmentTitle(position))
        ).attach();
    }

    private void setupViewPager(ViewPager2 viewPager) {
        adapter = new MergedPagerAdapter(this);
        adapter.addFragment(new AnagraficaFragment(), "SCHEDA UTENTE");
        adapter.addFragment(new CartellaClinicaFragment(), "CARTELLA CLINICA");
        adapter.addFragment(new ParametriMediciFragment(), "CONTENITORE BIOMEDICALE");
        viewPager.setAdapter(adapter);
    }
}