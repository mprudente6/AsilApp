package it.uniba.dib.sms23248;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.FileAdapter;
import it.uniba.dib.sms23248.Amministrazione.UploadedFile;

public class FileAdapterCentro extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private final List<UploadedFile> fileList;


    public FileAdapterCentro(List<UploadedFile> fileList) {
        this.fileList = fileList;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_centro, parent, false);
        return new FileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
        UploadedFile uploadedFile = fileList.get(position);
        holder.fileNameTextView.setText(uploadedFile.getFileName());

        holder.fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    listener.onDownloadClick(uploadedFile);
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

