package it.uniba.dib.sms23248.SpeseRichiedente;


import it.uniba.dib.sms23248.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ItemSpeseAdapter extends RecyclerView.Adapter<ItemSpeseAdapter.ViewHolder> {
    private final Context context;


    private List<ItemSpese> itemSpese;
    private final RecyclerView recyclerView;
    private final SpeseModel viewModel;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();
    String uid=currentUser.getUid();

    //costruttore per inizializzare l'adapter che popolerà il RecyclerViewer con gli ItemSpesa
    public ItemSpeseAdapter(Context context, List<ItemSpese> itemSpese, SpeseModel viewModel, RecyclerView recyclerView) {
        this.context = context;
        this.itemSpese = itemSpese;
        this.recyclerView = recyclerView;
        this.viewModel=viewModel;
    }
//notifica all'adapter che la lista è stata aggiornata
    public void setItemSpese(List<ItemSpese> itemSpese) {
        this.itemSpese = itemSpese;
        notifyDataSetChanged();
    }

    //abilita lo Swipe up come per eliminare un ItemSpesa
    public void enableSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(this, viewModel));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spese_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String prodotto = context.getString(R.string.Prodotto);
        String tipo = context.getString(R.string.Tipo);
        String prezzo = context.getString(R.string.Prezzo);

        ItemSpese itemSpese = this.itemSpese.get(position);
        holder.textName.setText(prodotto + " "+ itemSpese.getNome());
        holder.textType.setText(tipo +" "+ itemSpese.getTipo());
        holder.textPrice.setText(prezzo + " "+ "%.2f"+itemSpese.getPrezzo()+"€");
    }

    @Override
    public int getItemCount() {
        return itemSpese.size();
    }



    public void deleteItem(int position) {
        if (position >= 0 && position < itemSpese.size()) {
            ItemSpese deletedItem = itemSpese.remove(position);
            notifyItemRemoved(position);
            viewModel.deleteItem(deletedItem.getIdProdotto());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textType;
        private final TextView textPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName=itemView.findViewById(R.id.tvNome);
            textType=itemView.findViewById(R.id.tvTipo);
            textPrice=itemView.findViewById(R.id.tvPrezzo);
        }



    }
}