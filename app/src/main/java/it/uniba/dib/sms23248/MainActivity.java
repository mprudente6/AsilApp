package it.uniba.dib.sms23248;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
    Spinner demo;
    private FirebaseAuth mAuth;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
        mAuth = FirebaseAuth.getInstance();

        choice=findViewById(R.id.spinner_scelta_Utente) ;
        demo=findViewById(R.id.spinner_utenteDemo);
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

        List<String> views_demo = new ArrayList<>();
        views_demo.add(0, "Utente demo");
        views_demo.add("Richiedente Asilo");
        views_demo.add("Staff");

        UserSpinnerAdapter demoAdapter = new UserSpinnerAdapter(this, android.R.layout.simple_spinner_item, views_demo.toArray(new String[0]));
        demoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        demo.setAdapter(demoAdapter);

        demo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).equals("Utente demo")) {
                    // Clear the selection
                    demo.setSelection(0);
                } else {
                    String item = parent.getItemAtPosition(position).toString();

                    // Clear the selection
                    demo.setSelection(0);

                    if (item.equals("Richiedente Asilo")) {
                        if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                            signInDemoUserAsRichiedente();
                        } else {
                            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                        }
                    } else if (item.equals("Staff")) {
                        if (NetworkUtils.isNetworkAvailable(MainActivity.this)) {
                            signInDemoUserAsStaff();
                        } else {
                            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private void signInDemoUserAsRichiedente() {

        // Simulate login process for demo user using FirebaseAuth
        mAuth.signInWithEmailAndPassword("utentedemo@gmail.com", "password")
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI or navigate to the appropriate activity
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // The user is signed in, navigate to the appropriate activity
                            Intent intent = new Intent(MainActivity.this, HomeR.class);
                            startActivity(intent);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInDemoUserAsStaff() {
        // Simulate login process for demo user using FirebaseAuth
        mAuth.signInWithEmailAndPassword("utentedemo@gmail.com", "password")
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI or navigate to the appropriate activity
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // The user is signed in, navigate to the appropriate activity
                            Intent intent = new Intent(MainActivity.this, HomeS.class);
                            startActivity(intent);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}


