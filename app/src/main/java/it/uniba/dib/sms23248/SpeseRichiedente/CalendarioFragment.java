package it.uniba.dib.sms23248.SpeseRichiedente;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
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
    private RecyclerView recyclerViewItemsSpesa;
    private ItemSpeseAdapter itemSpeseAdapter;
    private Map<String, List<ItemSpese>> itemSpesaMap; //  Map per inserire gli itemSpesa per ogni data
    private String selectedDate;
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
        String connessione = getString(R.string.connessione);

        calendarView = view.findViewById(R.id.calendarView);
        editTextName = view.findViewById(R.id.editTextName);
        spinnerType = view.findViewById(R.id.spinnerType);
        editTextPrice = view.findViewById(R.id.editTextPrice);
        btnAddItem = view.findViewById(R.id.btnAddItem);
        recyclerViewItemsSpesa = view.findViewById(R.id.recyclerViewEvents);

        //Spinner dei tipi
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.tipi, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        // inizializzazione di del Map per inserire i dati di ogni ItemSpesa
        itemSpesaMap = new HashMap<>();

        //RecyclerView
        itemSpeseAdapter = new ItemSpeseAdapter(requireContext(),new ArrayList<>(), viewModel, recyclerViewItemsSpesa);
        recyclerViewItemsSpesa.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewItemsSpesa.setAdapter(itemSpeseAdapter);
        // in modo da cancellare gli ItemSpesa con lo SwipeUp
        itemSpeseAdapter.enableSwipeToDelete();


        // la view è inizializzata alla data corrente
        selectedDate = getCurrentDate();
        showItemsSpesaForDate(selectedDate);

        //il listener aggiorna selectedDate alla data selezionata dall'user nel calendario
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {

            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            showItemsSpesaForDate(selectedDate);
        });

        //Inseirto listener al bottone per aggiungere un nuovo ItemSpesa
        btnAddItem.setOnClickListener(v -> addItemAction());


            fetchItems();




        return view;
    }

 //metodo che garantisce che la data sia inizializzata
    private String getCurrentDate() {

        return ItemSpese.getCurrentDate();
    }

    // metodo che mostra gli itemSpesa per la rispettiva data selezionata
    private void showItemsSpesaForDate(String date) {
        //ottiene liste di ItemsSpesa associata alla data se ce ne sono
        List<ItemSpese> itemSpese = itemSpesaMap.get(date);

        //se sono presenti ItemSpesa vengono impostati nell'adattore così li mostra nel RecyclerViwer
        if (itemSpese != null) {
            itemSpeseAdapter.setItemSpese(itemSpese);
        } else {
            //se invece è nulla viene impostato un ArrayList vuoto
            itemSpeseAdapter.setItemSpese(new ArrayList<>());
        }
        itemSpeseAdapter.notifyDataSetChanged();
    }


    private void addItemAction() {
        String riempicampi = getString(R.string.Riempicampi);
        String prezzoalto = getString(R.string.PrezzoAlto);

        //Se Internet non è disponibile non è possibile aggiungere nuovi ItemSpesa
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        //Viene creato un UID random che sarà l'IdProdotto
        UUID uuid = UUID.randomUUID();
        String itemId = uuid.toString();
        String itemName = editTextName.getText().toString().trim();
        String itemType = spinnerType.getSelectedItem().toString().trim();
        String itemPriceText = editTextPrice.getText().toString().trim();

        //controlla che i campi siano riempiti
        if (!itemName.isEmpty() && !itemPriceText.isEmpty()) {
                //se itemPrice è vuoto e viene convertito prima del controllo !itemPriceText.isEmpty()
                // viene lanciata l'Eccezione
                Double itemPrice = Double.valueOf(itemPriceText);

                //viene impostato l'ItemSpesa con i dati frontiti dall'EditText e la data selezionata dall'utente
                ItemSpese newItem = new ItemSpese(selectedDate, itemId, itemName, itemPrice, itemType);
                Log.e("DATE ADD", "data: " + selectedDate);

                // L'itemSpesa è aggiunto nel Map per la data selezionata
                List<ItemSpese> ItemSpesaForDate = itemSpesaMap.get(selectedDate);
                if (ItemSpesaForDate == null) {
                    ItemSpesaForDate = new ArrayList<>();
                    itemSpesaMap.put(selectedDate, ItemSpesaForDate);
                }

                // Eseguiamo il Fetch del Budget dalla Collection RICHIEDENTI_ASILO dell'user corrente

                //dichiariamo finalItemsSpesaForDate poiché andrà usata in una lamda
                //e quindi non deve più essere modificata (effectively final)
                List<ItemSpese> finalItemsSpesaForDate = ItemSpesaForDate;
                documentRefBudget.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Double currentBudget = documentSnapshot.getDouble("Budget");

                                //se il budget è minore del prezzo, si informa l'utente che il prezzo è troppo alto
                                if (currentBudget != null && currentBudget - itemPrice >= 0) {
                                    // ItemSpesa è aggiunto alla lista
                                    finalItemsSpesaForDate.add(newItem);
                                    showItemsSpesaForDate(selectedDate);
                                    viewModel.addItem(itemId,  editTextName.getText().toString(), itemType, itemPrice, selectedDate);

                                    // I campi di input ritornano vuoti
                                    editTextName.getText().clear();
                                    spinnerType.setSelection(0);
                                    editTextPrice.getText().clear();
                                } else {
                                    Toast.makeText(getContext(), prezzoalto, Toast.LENGTH_SHORT).show();
                                    editTextName.getText().clear();
                                    spinnerType.setSelection(0);
                                    editTextPrice.getText().clear();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {

                        });

        } else {
            Toast.makeText(getContext(), riempicampi, Toast.LENGTH_SHORT).show();

        }
    }



   // Fetch ItemSpesa da Firebase
    private void fetchItems() {
       //è stato necessario creare una subcollection, poiché altrimenti ogni item aggiunto sovrascrive il precedente
        CollectionReference subspeseCollection = documentRefSpese.collection("Subspese");

        subspeseCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                         //converte il documento in un oggetto ItemSpese
                        ItemSpese subspeseItem = document.toObject(ItemSpese.class);

                        Log.d("fetchItems", "Retrieved Event: " + subspeseItem.getNome() + ", " + subspeseItem.getTipo() + ", " + subspeseItem.getPrezzo() + ", " + subspeseItem.getData());

                        //Ottiene la data
                        String subspeseItemDate = subspeseItem.getData();
                        //Ottiene gli itemSpesa presenti per la data associata e li mette nella lista
                        List<ItemSpese> itemSpeseForDate = itemSpesaMap.get(subspeseItemDate);

                        //se esiste, riempie il Map con l'ItemSpesa per quella data
                        if (itemSpeseForDate == null) {
                            itemSpeseForDate = new ArrayList<>();
                            itemSpesaMap.put(subspeseItemDate, itemSpeseForDate);
                        }
                      //aggiunge ItemSpese alla lista
                        itemSpeseForDate.add(subspeseItem);
                    }

                    // aggiorna l'UI
                    showItemsSpesaForDate(selectedDate);
                })
                .addOnFailureListener(e -> {

                });
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("selectedDate", selectedDate);
    }
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            selectedDate = savedInstanceState.getString("selectedDate");
            showItemsSpesaForDate(selectedDate);
        }
    }
    @Override
    public void onDestroyView() {
        //deregistra il networkChangeReceiver per Internet non disponibile
        super.onDestroyView();
        if (networkChangeReceiver != null) {
            getActivity().unregisterReceiver(networkChangeReceiver);
        }
    }

}