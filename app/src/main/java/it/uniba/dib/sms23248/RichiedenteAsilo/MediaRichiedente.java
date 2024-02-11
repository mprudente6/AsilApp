package it.uniba.dib.sms23248.RichiedenteAsilo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import it.uniba.dib.sms23248.HomeR;
import it.uniba.dib.sms23248.R;

public class MediaRichiedente extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPageAdapterRichiedenti myViewPageAdapter;
    ImageView homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_richiedente);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        homeIcon=findViewById(R.id.imageHome);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaRichiedente.this, HomeR.class);
                startActivity(i);
            }

        });

        myViewPageAdapter = new MyViewPageAdapterRichiedenti(this);
        viewPager2.setAdapter(myViewPageAdapter);

        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setUserInputEnabled(false);

        String documentiUtili = getString(R.string.documentiUtili);
        String videoPerTe = getString(R.string.videoPerTe);

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 1:
                            tab.setText(documentiUtili);
                            break;
                        case 0:
                            tab.setText(videoPerTe);
                            break;
                    }
                }).attach();
    }
}