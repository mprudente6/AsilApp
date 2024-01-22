package it.uniba.dib.sms23248;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapterRichiedenti extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoModel> videoList;
    private String targetFolder;
    private VideoAdapter.OnDownloadClickListener onDownloadClickListener;
    // Constructor
    public VideoAdapterRichiedenti(List<VideoModel> videoList, VideoAdapter.OnDownloadClickListener onDownloadClickListener, String targetFolder) {
        this.videoList = videoList;
        this.onDownloadClickListener = onDownloadClickListener;
        this.targetFolder = targetFolder;
    }

    @NonNull
    @Override
    public VideoAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_richiedenti, parent, false);
        return new VideoAdapter.VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapter.VideoViewHolder holder, int position) {
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

    public void setOnDownloadClickListener(VideoAdapter.OnDownloadClickListener onDownloadClickListener) {
        this.onDownloadClickListener = onDownloadClickListener;
    }
}
