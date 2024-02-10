package it.uniba.dib.sms23248.RichiedenteAsilo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyViewPageAdapterRichiedenti extends FragmentStateAdapter {
        private final Context context;
    public MyViewPageAdapterRichiedenti(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
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
