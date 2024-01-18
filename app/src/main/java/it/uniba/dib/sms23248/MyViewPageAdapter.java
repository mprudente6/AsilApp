package it.uniba.dib.sms23248;





import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import it.uniba.dib.sms23248.DocumentiFragment;
import it.uniba.dib.sms23248.InformazioniFragment;
import it.uniba.dib.sms23248.PosizioneFragment;
import it.uniba.dib.sms23248.VideoFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

//Adapter che serve a gestire i Fragment( le tab)
public class MyViewPageAdapter extends FragmentStateAdapter {


    private final Context context;

    public MyViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        //context serve per fare l'inflate di activity_amministrazione per usare tab_lyout
        this.context = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new InformazioniFragment();
            case 1:
                return new PosizioneFragment();
            case 2:
                return new DocumentiFragment();
            case 3:
                return new VideoFragment();

            default:
                return new InformazioniFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public void setupTabLayout(TabLayout tabLayout, ViewPager2 viewPager) {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View tabView = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);




            tab.setCustomView(tabView);
        }).attach();
    }
}
