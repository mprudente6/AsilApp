package it.uniba.dib.sms23248;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import java.util.List;

public class FlagSpinnerAdapter extends ArrayAdapter<String> {

    private final List<String> languages; // Your list of languages
    private final LayoutInflater inflater;

    public FlagSpinnerAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.languages = objects;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.flag_spinner, parent, false);
        ImageView flagImageView = view.findViewById(R.id.flagImageView);

        // Set the flag image based on the language
        if (languages.get(position).equals("ITA")) {
            flagImageView.setImageResource(R.drawable.flag_of_italy_svg);
        } else if (languages.get(position).equals("ENG")) {
            flagImageView.setImageResource(R.drawable.engflag);
        }

        // You can set additional information if needed

        return view;
    }
}