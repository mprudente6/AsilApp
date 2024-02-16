package it.uniba.dib.sms23248.Amministrazione;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;


public class VideoFragment extends Fragment implements VideoAdapter.OnDeleteClickListener, VideoAdapter.OnDownloadClickListener{

  private NetworkChangeReceiver networkChangeReceiver;
    private static final int PICK_VIDEO_REQUEST_GEN = 1;
    private static final int PICK_VIDEO_REQUEST_DONNA = 2;
    private View view;
    private Uri selectedVideoUri;
    private StorageReference storageReference;
    private VideoAdapter videoAdapterGen;
    private VideoAdapter videoAdapterDonna;
    private List<VideoModel> videoListGen;
    private List<VideoModel> videoListDonna;
    private RecyclerView recyclerViewGen;
    private RecyclerView recyclerViewDonna;
    private ProgressDialog progressDialog;

    private String targetFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);
        String connessione = getString(R.string.connessione);



        Button uploadButtonGenerico = view.findViewById(R.id.uploadButton);
        Button uploadButtonDonna = view.findViewById(R.id.uploadButton2);
        recyclerViewGen = view.findViewById(R.id.recyclerViewGen);
        recyclerViewDonna = view.findViewById(R.id.recyclerViewDonna);

        storageReference = FirebaseStorage.getInstance().getReference();

        videoListGen = new ArrayList<>();
        videoAdapterGen = new VideoAdapter(videoListGen, this, this, "videos");

        videoListDonna = new ArrayList<>();
        videoAdapterDonna = new VideoAdapter(videoListDonna, this, this, "videosDonna");

        videoAdapterGen.setOnDeleteClickListener(this);
        videoAdapterGen.setOnDownloadClickListener(this);

        videoAdapterDonna.setOnDeleteClickListener(this);
        videoAdapterDonna.setOnDownloadClickListener(this);

        recyclerViewGen.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGen.setAdapter(videoAdapterGen);

        recyclerViewDonna.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDonna.setAdapter(videoAdapterDonna);

        storageReference = FirebaseStorage.getInstance().getReference();

        fetchVideoGenerico();
        fetchVideoDonna();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Upload...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);


            uploadButtonGenerico.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openVideoChooser(PICK_VIDEO_REQUEST_GEN);
                }
            });


        uploadButtonDonna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openVideoChooser(PICK_VIDEO_REQUEST_DONNA);
            }
        });



        return view;
    }

    @Override
    public void onDeleteClick(VideoModel videoModel, String targetFolder) {
        String connessione = getString(R.string.connessione);
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            showDeleteConfirmationDialog(videoModel, targetFolder);
        } else {
            Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onDownloadClick(VideoModel videoModel) {
        String videoUrl = videoModel.getVideoUrl();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
        request.setTitle("Downloading Video");
        request.setDescription(videoModel.getName());
        request.allowScanningByMediaScanner();


        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, videoModel.getName());

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

        downloadManager.enqueue(request);

    }


    private void openVideoChooser(int requestCode) {
        String connessione = getString(R.string.connessione);
        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            Intent intent = new Intent();
            intent.setType("image/gif");;  //carica video e gif. Abbiamo caricato gif per ragioni estetiche e per non intasare lo storage
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), requestCode);
        } else {
            Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();
            if (selectedVideoUri != null) {
                String videoName = getFileName(selectedVideoUri);
                if (requestCode == PICK_VIDEO_REQUEST_GEN) {
                    targetFolder = "videos";
                    uploadVideo(selectedVideoUri, videoName, "videos");
                } else if (requestCode == PICK_VIDEO_REQUEST_DONNA) {
                    targetFolder = "videosDonna";
                    uploadVideo(selectedVideoUri, videoName, "videosDonna");
                }
            } else {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
         }
    }

    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = null;

        try {
            cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (displayNameIndex != -1) {
                    result = cursor.getString(displayNameIndex);
                } else {
                    result = uri.getLastPathSegment();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private void uploadVideo(Uri videoUri, String videoName, String targetFolder) {
        String uploadedVid = getString(R.string.video_uploaded);
        String failedUpload = getString(R.string.failed_upload);
        if (videoUri != null) {


            progressDialog.show();



            storageReference.child(targetFolder + "/" + videoName).putFile(videoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(getContext(), uploadedVid, Toast.LENGTH_SHORT).show();

                        if ("videosDonna".equals(targetFolder)) {
                            fetchVideoDonna();
                        } else {
                            fetchVideoGenerico();
                        }

                        progressDialog.dismiss();
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(getContext(), failedUpload, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setProgress((int) progress);
                    });
        }
    }
    private void fetchVideoGenerico() {
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

                        });
                    }
                })
                .addOnFailureListener(exception -> {

                });
    }

    private void fetchVideoDonna() {
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

                        });
                    }
                })
                .addOnFailureListener(exception -> {

                });
    }

    private void showDeleteConfirmationDialog(final VideoModel videoModel, final String targetFolder) {
        String DeleteFile = getString(R.string.EliminaFile);
        String sicuro = getString(R.string.VuoiEliminare);
        String delete = getString(R.string.Delete);
        String Annulla = getString(R.string.Cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(DeleteFile);
        builder.setMessage(sicuro);
        builder.setPositiveButton(delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                deleteVideo(videoModel, targetFolder);
            }
        });
        builder.setNegativeButton(Annulla, null);
        builder.show();
    }

    private void deleteVideo(VideoModel videoModel, String targetFolder) {
        String deletedVideo = getString(R.string.deleted_video);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String videoUrl = videoModel.getVideoUrl();
        Uri uri = Uri.parse(videoUrl);
        String fileName = uri.getLastPathSegment();

        if (targetFolder != null && !targetFolder.isEmpty() && fileName.startsWith(targetFolder + "/")) {
            fileName = fileName.substring(targetFolder.length() + 1);
        }

        StorageReference videoReference = storageReference.child(targetFolder).child(fileName);

        videoReference.getMetadata().addOnSuccessListener(metadata -> {
            videoReference.delete().addOnSuccessListener(aVoid -> {
                if ("videosDonna".equals(targetFolder)) {
                    fetchVideoDonna();


                    videoAdapterDonna.notifyDataSetChanged();
                } else {
                    fetchVideoGenerico();
                    videoAdapterGen.notifyDataSetChanged();
                    }

                Toast.makeText(getContext(), deletedVideo, Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
            });
        }).addOnFailureListener(e -> {
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
