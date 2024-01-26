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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private Context context;


    private List<Event> events;
    private RecyclerView recyclerView;
    private SpeseModel viewModel;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser currentUser=mAuth.getCurrentUser();
    String uid=currentUser.getUid();

    public EventAdapter(Context context, List<Event> events, SpeseModel viewModel, RecyclerView recyclerView) {
        this.context = context;
        this.events = events;
        this.recyclerView = recyclerView;
        this.viewModel=viewModel;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }
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

        Event event = events.get(position);
        holder.textName.setText(prodotto + " "+event.getNome());
        holder.textType.setText(tipo +" "+ event.getTipo());
        holder.textPrice.setText(prezzo + " "+event.getPrezzo()+"€");
    }

    @Override
    public int getItemCount() {
        return events.size();
    }



    public void deleteItem(int position) {
        if (position >= 0 && position < events.size()) {
            Event deletedItem = events.remove(position);
            notifyItemRemoved(position);
            viewModel.deleteItem(deletedItem.getIdProdotto());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textType;
        private TextView textPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName=itemView.findViewById(R.id.tvNome);
            textType=itemView.findViewById(R.id.tvTipo);
            textPrice=itemView.findViewById(R.id.tvPrezzo);
        }



    }
}