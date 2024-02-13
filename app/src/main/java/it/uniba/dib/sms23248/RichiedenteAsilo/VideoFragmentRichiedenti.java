package it.uniba.dib.sms23248.RichiedenteAsilo;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.VideoModel;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.R;

public class VideoFragmentRichiedenti extends Fragment implements VideoAdapterRichiedenti.OnDownloadClickListener {
    private View view;
    private StorageReference storageReference;
    private VideoAdapterRichiedenti videoAdapterRichiedenti;
    private List<VideoModel> videoList;
    private RecyclerView recyclerView;
    private TextView textVideoName;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();




        view = inflater.inflate(R.layout.fragment_video_richiedenti, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        textVideoName = view.findViewById(R.id.NomeVideo);
        storageReference = FirebaseStorage.getInstance().getReference();

        videoList = new ArrayList<>();
        videoAdapterRichiedenti = new VideoAdapterRichiedenti(videoList, this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(videoAdapterRichiedenti);
        fetchUserSexAndVideos();

        return view;
    }

    @Override
    public void onDownloadClick(VideoModel videoModel) {
        String videoUrl = videoModel.getVideoUrl();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle("Downloading Video");
        request.setDescription(videoModel.getName());

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoModel.getName());

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void fetchUserSexAndVideos() {
        String noFetch_userData= getString(R.string.no_fetchData);


        db.collection("RICHIEDENTI_ASILO").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String userSex = document.getString("Genere");
                    if ("M".equals(userSex)) {
                        fetchVideosFromFolder("videos");
                    } else if ("F".equals(userSex)) {
                        fetchVideosFromFolder("videos");
                        fetchVideosFromFolder("videosDonna");
                    }
                }
            } else {
                Toast.makeText(getContext(), noFetch_userData, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVideosFromFolder(String folder) {
        StorageReference videosRef = storageReference.child(folder);
        String NoURLdownload=getString(R.string.failURL);
        String noListvideo=getString(R.string.noListVideo);

        videosRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            String fileName = item.getName();
                            VideoModel videoModel = new VideoModel(downloadUrl.toString(), fileName);
                            videoList.add(videoModel);
                            videoAdapterRichiedenti.notifyDataSetChanged();
                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), NoURLdownload, Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(),noListvideo, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        if (networkChangeReceiver != null) {
            requireContext().unregisterReceiver(networkChangeReceiver);
        }
        super.onDestroyView();
    }
}
