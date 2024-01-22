package it.uniba.dib.sms23248;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class VideoFragmentRichiedenti extends Fragment implements VideoAdapter.OnDownloadClickListener{


    private static final int PICK_VIDEO_REQUEST_GEN = 1;
    private static final int PICK_VIDEO_REQUEST_DONNA = 2;
    private View view;
    private Uri selectedVideoUri;
    private StorageReference storageReference;
    private VideoAdapterRichiedenti videoAdapterGen;
    private VideoAdapterRichiedenti videoAdapterDonna;
    private List<VideoModel> videoListGen;
    private List<VideoModel> videoListDonna;
    private RecyclerView recyclerViewGen;
    private RecyclerView recyclerViewDonna;
    private ProgressDialog progressDialog;
    private TextView textVideoName;
    private String targetFolder;

    String genere;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference RichiedenteAsilo = db.collection("RICHIEDENTI_ASILO").document(uid);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RichiedenteAsilo.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    genere = documentSnapshot.getString("Genere");

                    if(genere.equals("M"))
                    {
                        RecyclerView recyclerViewDonna =  getActivity().findViewById(R.id.recyclerViewDonna);
                        recyclerViewDonna.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        view = inflater.inflate(R.layout.fragment_video_richiedenti, container, false);
        View item_view=inflater.inflate(R.layout.item_video_richiedenti, container, false);

        recyclerViewGen = view.findViewById(R.id.recyclerViewGen);
        recyclerViewDonna = view.findViewById(R.id.recyclerViewDonna);
        textVideoName=item_view.findViewById(R.id.NomeVideo);
        storageReference = FirebaseStorage.getInstance().getReference();

        videoListGen = new ArrayList<>();
        videoAdapterGen = new VideoAdapterRichiedenti(videoListGen, this, "videos");

        videoListDonna = new ArrayList<>();
        videoAdapterDonna = new VideoAdapterRichiedenti(videoListDonna, this,"videosDonna");

        videoAdapterGen.setOnDownloadClickListener(this);

        videoAdapterDonna.setOnDownloadClickListener(this);

        recyclerViewGen.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGen.setAdapter(videoAdapterGen);

        recyclerViewDonna.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDonna.setAdapter(videoAdapterDonna);

        storageReference = FirebaseStorage.getInstance().getReference();

        fetchVideoUrlsGen();
        fetchVideoUrlsDonna();

        return view;
    }

    @Override
    public void onDownloadClick(VideoModel videoModel) {

        String videoUrl = videoModel.getVideoUrl();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle("Downloading Video");
        request.setDescription(videoModel.getName());

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoModel.getName());

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId = downloadManager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    Toast.makeText(getContext(), "Download completed", Toast.LENGTH_SHORT).show();
                }
            }
        };

        requireContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void fetchVideoUrlsGen() {
        videoListGen.clear();

        StorageReference videosRef = storageReference.child("videos");

        videosRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            String fileName = item.getName();
                            VideoModel videoModel = new VideoModel(downloadUrl.toString(),fileName );
                            videoListGen.add(videoModel);
                            videoAdapterGen.notifyDataSetChanged();

                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Failed to list videos", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchVideoUrlsDonna() {
        videoListDonna.clear();

        StorageReference videosRef = storageReference.child("videosDonna");

        videosRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                            String fileName = item.getName();
                            VideoModel videoModel = new VideoModel(downloadUrl.toString(),fileName);
                            videoListDonna.add(videoModel);
                            videoAdapterDonna.notifyDataSetChanged();
                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getContext(), "Failed to list Donna videos", Toast.LENGTH_SHORT).show();
                });
    }
}