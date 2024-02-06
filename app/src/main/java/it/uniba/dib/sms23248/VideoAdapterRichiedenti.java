package it.uniba.dib.sms23248;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.VideoModel;

public class VideoAdapterRichiedenti extends RecyclerView.Adapter<VideoAdapterRichiedenti.VideoViewHolder> {

    private List<VideoModel> videoList;
    private OnDeleteClickListener onDeleteClickListener;
    private OnDownloadClickListener onDownloadClickListener;


    public VideoAdapterRichiedenti(List<VideoModel> videoList, OnDownloadClickListener onDownloadClickListener) {
        this.videoList = videoList;
        this.onDownloadClickListener = onDownloadClickListener;
    }

    public interface OnDownloadClickListener {
        void onDownloadClick(VideoModel videoModel);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_richiedenti, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel videoModel = videoList.get(position);


        String nomeVideoSenzaEstensione = removeFileExtension(videoModel.getName());

        holder.textNomeVideo.setText(nomeVideoSenzaEstensione);

        Glide.with(holder.itemView.getContext())
                .load(videoModel.getVideoUrl())
                .placeholder(R.drawable.video_placeholder)
                .into(holder.videoThumbnailImageView);

        holder.videoThumbnailImageView.setAdjustViewBounds(true);

        holder.textNomeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDownloadClickListener != null) {
                    onDownloadClickListener.onDownloadClick(videoModel);
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
        TextView textNomeVideo;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnailImageView = itemView.findViewById(R.id.videoThumbnailImageView);
            textNomeVideo = itemView.findViewById(R.id.NomeVideo);
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

    private String removeFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        } else {
            return fileName;
        }
    }
}
