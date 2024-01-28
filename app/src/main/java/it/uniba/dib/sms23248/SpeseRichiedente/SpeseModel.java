package it.uniba.dib.sms23248.SpeseRichiedente;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;

import java.util.Map;

public class SpeseModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();
    String uid=currentUser.getUid();   //uid dell'utente attualmente loggato, usato come DocReference

    DocumentReference documentRefBudget = db.collection("RICHIEDENTI_ASILO").document(uid);

    DocumentReference documentRef = db.collection("SPESE").document(uid);




    private final MutableLiveData<Double> updatedBudgetLiveData = new MutableLiveData<>();

    public LiveData<Double> getUpdatedBudgetLiveData() {

        return updatedBudgetLiveData;
    }





    public void addItem(String id, String nome, String tipo, double prezzo, String selectedDate){


        Map<String, Object> item = new HashMap<>();
        item.put("idProdotto",id);
        item.put("nome", nome);
        item.put("tipo", tipo);
        item.put("prezzo", prezzo);
        item.put("data",selectedDate);



        documentRef.collection("Subspese").add(item)
                .addOnSuccessListener(documentReference -> {

                    documentRefBudget.get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Double currentBudget = documentSnapshot.getDouble("Budget");


                                    if (currentBudget != null) {

                                        double newBudget = currentBudget - prezzo;


                                        documentRefBudget.update("Budget", newBudget)
                                                .addOnSuccessListener(aVoid -> {
                                                    updatedBudgetLiveData.postValue(newBudget);
                                                })
                                                .addOnFailureListener(e -> {

                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {

                            });

                })
                .addOnFailureListener(e -> {

                });

    }
    public void deleteItem(String idProdotto) {
        DocumentReference parentDocument = db.collection("SPESE").document(uid);


        CollectionReference subcollection = parentDocument.collection("Subspese");


        subcollection.whereEqualTo("idProdotto", idProdotto).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    Double deletedItemPrice = document.getDouble("prezzo");


                    updateBudget(deletedItemPrice);


                    subcollection.document(document.getId()).delete()
                            .addOnSuccessListener(aVoid -> {

                            })
                            .addOnFailureListener(e -> {

                            });
                }
            }
        });
    }

    private void updateBudget(double deletedItemPrice) {

        documentRefBudget.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double currentBudget = documentSnapshot.getDouble("Budget");


                        if (currentBudget != null) {

                            double newBudget = currentBudget + deletedItemPrice;


                            documentRefBudget.update("Budget", newBudget)
                                    .addOnSuccessListener(aVoid -> {
                                        updatedBudgetLiveData.postValue(newBudget);
                                    })
                                    .addOnFailureListener(e -> {

                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {

                });
    }




}

