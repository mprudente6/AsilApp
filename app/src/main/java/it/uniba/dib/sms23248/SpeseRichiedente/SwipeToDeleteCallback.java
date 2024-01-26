package it.uniba.dib.sms23248.SpeseRichiedente;




import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private ItemSpeseAdapter adapter;
    private SpeseModel viewModel;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();
    String uid=currentUser.getUid();

    public SwipeToDeleteCallback(ItemSpeseAdapter adapter, SpeseModel viewModel) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT); //va bene si detsra che sinistra, no drag and drop
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
        String idProdotto = String.valueOf(adapter.getItemId(position));
        adapter.deleteItem(position);
        viewModel.deleteItem(idProdotto);
    }

}
