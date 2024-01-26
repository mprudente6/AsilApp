package it.uniba.dib.sms23248.SpeseRichiedente;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.uniba.dib.sms23248.SpeseRichiedente.CalendarioFragment;
import it.uniba.dib.sms23248.SpeseRichiedente.SpeseFragment;

public class SpeseViewAdapter extends FragmentStateAdapter {
    public SpeseViewAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new CalendarioFragment();
            case 1:
                return new BilancioFragment();
            case 2:
                return new SpeseFragment();
            default:
                return new BilancioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
