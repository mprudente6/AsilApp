package it.uniba.dib.sms23248;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class UserSpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public UserSpinnerAdapter(Context context, int textViewResourceId, String[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = row.findViewById(android.R.id.text1);
        textView.setText(values[position]);
        textView.setTextColor(Color.BLACK); // Set dropdown text color to black

        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.spinner_item_user, parent, false);
        }

        Spinner spinner = (Spinner) parent;
        spinner.setBackgroundResource(R.drawable.rounded_spinner_background); // Set the rounded background to the Spinner
        TextView textView = row.findViewById(android.R.id.text1);
        textView.setText(values[position]);
        textView.setTextColor(Color.WHITE);

        textView.setPadding(0, 16, 0, 16);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(Color.parseColor("#076AEC")); // Set your background color
        shape.setCornerRadius(15); // Adjust the corner radius as needed
        textView.setBackground(shape);

        return row;
    }
}
