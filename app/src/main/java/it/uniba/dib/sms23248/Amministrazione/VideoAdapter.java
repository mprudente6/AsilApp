package it.uniba.dib.sms23248.Amministrazione;




import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import it.uniba.dib.sms23248.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<VideoModel> videoList;
    private OnDeleteClickListener onDeleteClickListener;
    private final String targetFolder;
    private OnDownloadClickListener onDownloadClickListener;
    // Constructor
    public VideoAdapter(List<VideoModel> videoList, OnDeleteClickListener onDeleteClickListener, OnDownloadClickListener onDownloadClickListener, String targetFolder) {
        this.videoList = videoList;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onDownloadClickListener = onDownloadClickListener;
        this.targetFolder = targetFolder;
    }

    public interface OnDownloadClickListener {

        void onDownloadClick(VideoModel videoModel);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel videoModel = videoList.get(position);
        holder.textNomeVideo.setText(videoModel.getName());

        Glide.with(holder.itemView.getContext())
                .load(videoModel.getVideoUrl())
                .placeholder(R.drawable.video_placeholder)
                .into(holder.videoThumbnailImageView);

        holder.videoThumbnailImageView.setAdjustViewBounds(true);

        holder.textNomeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDownloadClick(videoModel);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Invoke the method to show the delete confirmation dialog
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(videoModel, targetFolder);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnailImageView;
        public ImageButton deleteButton;

        TextView textNomeVideo;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnailImageView = itemView.findViewById(R.id.videoThumbnailImageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            textNomeVideo=itemView.findViewById(R.id.NomeVideo);
        }


    }

    public interface OnDeleteClickListener {
        void onDeleteClick(VideoModel videoModel, String targetFolder);

        void onDownloadClick(VideoModel videoModel);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setOnDownloadClickListener(OnDownloadClickListener onDownloadClickListener) {
        this.onDownloadClickListener = onDownloadClickListener;
    }
}
