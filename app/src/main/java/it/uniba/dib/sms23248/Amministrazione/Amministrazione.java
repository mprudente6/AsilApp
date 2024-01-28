package it.uniba.dib.sms23248.Amministrazione;





import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import it.uniba.dib.sms23248.HomeS;
import it.uniba.dib.sms23248.R;

public class Amministrazione extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    AmministrazionePageAdapter amministrazionePageAdapter;
    ImageView homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amministrazione);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        homeIcon=findViewById(R.id.imageHome);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Amministrazione.this, HomeS.class);


                startActivity(intent);
            }

        });

        amministrazionePageAdapter = new AmministrazionePageAdapter(this);
        viewPager2.setAdapter(amministrazionePageAdapter);

        viewPager2.setOffscreenPageLimit(4);
        viewPager2.setUserInputEnabled(false);
        String mappa = getString(R.string.mappa_string);
        String dati = getString(R.string.Dati);
        String video = getString(R.string.Video);

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(dati);
                            break;
                        case 1:
                            tab.setText(mappa);
                            break;
                        case 2:
                            tab.setText("Pdf");
                            break;
                        case 3:
                            tab.setText(video);
                            break;

                    }
                }).attach();
    }


}
