package it.uniba.dib.sms23248.Amministrazione;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class AmministrazionePageAdapter extends FragmentStateAdapter {




    public AmministrazionePageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

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


}
