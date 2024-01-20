package it.uniba.dib.sms23248;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Spinner choice;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        choice=findViewById(R.id.spinner_scelta_Utente) ;
        Spinner languageSpinner = findViewById(R.id.spinner_lingua);


        List<String> languages = Arrays.asList("ITA", "ENG");

        FlagSpinnerAdapter adapter = new FlagSpinnerAdapter(this, R.layout.flag_spinner, languages);
        languageSpinner.setAdapter(adapter);
        List<String> views_user=new ArrayList<>();
        views_user.add(0,"Scegli Utente");
        views_user.add("Richiedente Asilo");
        views_user.add("Staff");




        UserSpinnerAdapter adapterChoice = new UserSpinnerAdapter(this, android.R.layout.simple_spinner_item, views_user.toArray(new String[0]));
        adapterChoice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choice.setAdapter(adapterChoice);

        choice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("Scegli Utente")) {
                    // Clear the selection
                    choice.setSelection(0);
                } else {
                    String item = parent.getItemAtPosition(position).toString();

                    // Clear the selection
                    choice.setSelection(0);

                    if (item.equals("Richiedente Asilo")) {
                        Intent intent = new Intent(MainActivity.this, AccessoRichiedenteAsilo.class);
                        startActivity(intent);
                    } else if (item.equals("Staff")) {
                        Intent intent = new Intent(MainActivity.this, AccessoStaff.class);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }


}


