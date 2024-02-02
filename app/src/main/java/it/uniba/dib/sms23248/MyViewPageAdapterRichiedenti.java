package it.uniba.dib.sms23248;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyViewPageAdapterRichiedenti extends FragmentStateAdapter {
        private final Context context;
    public MyViewPageAdapterRichiedenti(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        //context serve per fare l'inflate di activity_amministrazione per usare tab_lyout
        this.context = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 1:
                return new DocumentiFragmentRichiedenti();
            case 0:
                return new VideoFragmentRichiedenti();
            default:
                return new DocumentiFragmentRichiedenti();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }


}
