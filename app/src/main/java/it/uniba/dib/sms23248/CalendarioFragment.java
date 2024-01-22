package it.uniba.dib.sms23248;

import static androidx.viewpager.widget.PagerAdapter.POSITION_NONE;

import android.app.DatePickerDialog;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import it.uniba.dib.sms23248.Event;
import it.uniba.dib.sms23248.EventAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import it.uniba.dib.sms23248.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;


public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private EditText editTextName;

    private EditText editTextPrice;
    private Spinner spinnerType;
    private Button btnAddItem;
    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private Map<String, List<Event>> eventMap; // Map to store events for each date
    private String selectedDate; // Currently selected date
    private NetworkChangeReceiver networkChangeReceiver;
FirebaseAuth mAuth=FirebaseAuth.getInstance();
FirebaseUser currentUser=mAuth.getCurrentUser();
String uid=currentUser.getUid();
    private SpeseModel viewModel;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentRefBudget = db.collection("RICHIEDENTI_ASILO").document(uid);
    DocumentReference documentRefSpese = db.collection("SPESE").document(uid);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(SpeseModel.class);


        calendarView = view.findViewById(R.id.calendarView);
        editTextName = view.findViewById(R.id.editTextName);
        spinnerType = view.findViewById(R.id.spinnerType);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        btnAddItem = view.findViewById(R.id.btnAddItem);
        recyclerViewEvents = view.findViewById(R.id.recyclerViewEvents);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.tipi, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        // Initialize event map
        eventMap = new HashMap<>();

        // Set up RecyclerView
        eventAdapter = new EventAdapter(new ArrayList<>(), viewModel, recyclerViewEvents);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewEvents.setAdapter(eventAdapter);

        eventAdapter.enableSwipeToDelete();

        // Set up CalendarView listener
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Update events for the selected date
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            updateEventsForDate(selectedDate);
        });

        // Set initial date
        selectedDate = getCurrentDate();
        updateEventsForDate(selectedDate);

        // Set up Add Item button click listener
        btnAddItem.setOnClickListener(v -> addItemAction());

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            fetchItems();
        } else {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
        }



        return view;
    }

    // Get the current date in "yyyy-MM-dd" format
    private String getCurrentDate() {
        // Get the current date from the CalendarView
        return Event.getCurrentDate();
    }

    // Update events for the selected date
    private void updateEventsForDate(String date) {
        List<Event> events = eventMap.get(date);
        if (events != null) {
            eventAdapter.setEvents(events);
        } else {
            eventAdapter.setEvents(new ArrayList<>());
        }
        eventAdapter.notifyDataSetChanged();
    }

    // Add Item button click handler
    private void addItemAction() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        UUID uuid = UUID.randomUUID();
        String itemId = uuid.toString();
        String itemName = editTextName.getText().toString().trim();
        String itemType = spinnerType.getSelectedItem().toString().trim();
        Double itemPriceString = Double.valueOf(editTextPrice.getText().toString().trim());

        if (!itemName.isEmpty() && itemPriceString >= 0.0) {
            // Create a new event with the entered item details
            Event newItem = new Event(selectedDate, itemId, itemName, itemPriceString, itemType);
            Log.e("DATE ADD","data: "+selectedDate);
            // Add the item to the map for the selected date
            List<Event> eventsForDate = eventMap.get(selectedDate);
            if (eventsForDate == null) {
                eventsForDate = new ArrayList<>();
                eventMap.put(selectedDate, eventsForDate);
            }

            String nome = editTextName.getText().toString();
            String tipo = spinnerType.getSelectedItem().toString();
            double prezzo = Double.parseDouble(editTextPrice.getText().toString());
            List<Event> finalEventsForDate = eventsForDate;
            documentRefBudget.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double currentBudget = documentSnapshot.getDouble("Budget");

                            if (currentBudget != null && currentBudget - prezzo >= 0) {
                                // Update the events for the selected date
                                finalEventsForDate.add(newItem);
                                updateEventsForDate(selectedDate);

                                // Add the item to the ViewModel
                                viewModel.addItem(itemId, nome, tipo, prezzo, selectedDate);

                                // Clear the input fields
                                editTextName.getText().clear();
                                spinnerType.setSelection(0);
                                editTextPrice.getText().clear();

                                Toast.makeText(getContext(), "Spesa aggiunta!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Il prezzo è troppo alto!", Toast.LENGTH_SHORT).show();
                                editTextName.getText().clear();
                                spinnerType.setSelection(0);
                                editTextPrice.getText().clear();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure to retrieve the current budget
                        // You might want to consider rolling back the item addition
                    });
        } else {
            Toast.makeText(getContext(), "Riempire tutti i campi!", Toast.LENGTH_SHORT).show();
        }
    }



    private void fetchItems() {

        CollectionReference subspeseCollection = documentRefSpese.collection("Subspese");

        subspeseCollection.get() // Use get() instead of addSnapshotListener
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Clear existing data in eventMap


                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Parse the data and update your UI or data structure accordingly
                        Event subspeseItem = document.toObject(Event.class); // Assuming Event is the model class

                        // Add log statements to debug
                        Log.d("fetchItems", "Retrieved Event: " + subspeseItem.getNome() + ", " + subspeseItem.getTipo() + ", " + subspeseItem.getPrezzo() + ", " + subspeseItem.getData());

                        // Extract date from subspeseItem and add to eventMap
                        String subspeseItemDate = subspeseItem.getData();
                        List<Event> eventsForDate = eventMap.get(subspeseItemDate);

                        if (eventsForDate == null) {
                            eventsForDate = new ArrayList<>();
                            eventMap.put(subspeseItemDate, eventsForDate);
                        }

                        eventsForDate.add(subspeseItem);
                    }

                    // Update UI for the selected date
                    updateEventsForDate(selectedDate);
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to fetch items
                    Log.e("fetchItems", "Error fetching items: " + e.getMessage());
                });
    }

    @Override
    public void onResume() {
        super.onResume();


    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save your fragment's state if needed
        outState.putString("selectedDate", selectedDate);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore your fragment's state if needed
        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("selectedDate");
            updateEventsForDate(selectedDate);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkChangeReceiver != null) {
            getActivity().unregisterReceiver(networkChangeReceiver);
        }
    }

}