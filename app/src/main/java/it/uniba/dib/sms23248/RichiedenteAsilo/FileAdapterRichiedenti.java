package it.uniba.dib.sms23248.RichiedenteAsilo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.FileAdapter;
import it.uniba.dib.sms23248.Amministrazione.UploadedFile;
import it.uniba.dib.sms23248.R;
import it.uniba.dib.sms23248.RichiedenteAsilo.DocumentiFragmentRichiedenti;

public class FileAdapterRichiedenti extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private final List<UploadedFile> fileList;
    private final DocumentiFragmentRichiedenti documentiFragment;

    public FileAdapterRichiedenti(List<UploadedFile> fileList, DocumentiFragmentRichiedenti documentiFragment) {
        this.fileList = fileList;
        this.documentiFragment = documentiFragment;
    }



    public interface OnItemClickListener {
        void onItemClick(UploadedFile uploadedFile);
    }

    private FileAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(FileAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_richiedenti, parent, false);
        return new FileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
        UploadedFile uploadedFile = fileList.get(position);
        holder.fileNameTextView.setText(uploadedFile.getFileName());

        holder.fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the clicked view is the TextView
                Log.d("FileAdapter", "Text clicked for item: " + uploadedFile.getFileName());
                if (listener != null) {
                    listener.onItemClick(uploadedFile);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fileNameTextView;

        public ViewHolder(View view) {
            super(view);
            fileNameTextView = view.findViewById(R.id.fileNameTextView);
        }
    }
}

