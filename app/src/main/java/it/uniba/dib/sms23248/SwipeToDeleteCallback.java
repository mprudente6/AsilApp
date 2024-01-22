package it.uniba.dib.sms23248;




import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private EventAdapter adapter;
    private SpeseModel viewModel;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();
    String uid=currentUser.getUid();

    public SwipeToDeleteCallback(EventAdapter adapter, SpeseModel viewModel) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.viewModel = viewModel;

    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        String idProdotto = String.valueOf(adapter.getItemId(position)); // Assuming you have a method to get the item ID
        adapter.deleteItem(position);
        viewModel.deleteItem(idProdotto); // Call the deleteItem method in your ViewModel
    }

}
