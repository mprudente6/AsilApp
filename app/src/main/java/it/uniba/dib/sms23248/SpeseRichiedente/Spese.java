package it.uniba.dib.sms23248.SpeseRichiedente;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import it.uniba.dib.sms23248.R;

public class Spese extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    SpeseViewAdapter speseAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spese);

        tabLayout= findViewById(R.id.tab_layout);


        viewPager2=findViewById(R.id.view_pager);
        speseAdapter= new SpeseViewAdapter(this);


        viewPager2.setAdapter(new SpeseViewAdapter(this));
        viewPager2.setOffscreenPageLimit(1);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

         //garantisce che quando un utente scorre tra le pagine del ViewPager2, il tab corrispondente nel TabLayout viene selezionato
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });




    }




}