package it.uniba.dib.sms23248.RichiedenteAsilo;

import static android.app.Activity.RESULT_OK;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.FileAdapter;
import it.uniba.dib.sms23248.Amministrazione.UploadedFile;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.R;


public class DocumentiFragmentRichiedenti extends Fragment {

    View view;
    Button selectFile, upload;
    TextView notification;
    FirebaseDatabase database;
    FirebaseStorage storage;
    int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;
    Uri pdfUri;


    private RecyclerView recyclerView;
    private List<UploadedFile> fileList;
    private FileAdapterRichiedenti fileAdapter;
    private NetworkChangeReceiver networkChangeReceiver;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_documenti_richiedenti, container, false);



        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        fileList = new ArrayList<>();
        fileAdapter = new FileAdapterRichiedenti(fileList,this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileAdapter);

        fetchDataFromDatabase();

        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UploadedFile uploadedFile) {

                downloadFile(uploadedFile.getFileUrl(), uploadedFile.getFileName());
            }

            @Override
            public void onDeleteClick(UploadedFile uploadedFile) {
            }
        });


        return view;

    }

    private void fetchDataFromDatabase() {
        DatabaseReference documentiUtiliReference = database.getReference("DocumentiUtili");
        documentiUtiliReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileList.clear();


                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot fileSnapshot : categorySnapshot.getChildren()) {
                        Log.d("FirebaseDebug", "File Snapshot Key: " + fileSnapshot.getKey());
                        Log.d("FirebaseDebug", "File Snapshot Value: " + fileSnapshot.getValue());

                        String fileName = fileSnapshot.getKey();


                        if (fileSnapshot.hasChild("url")) {
                            String fileUrl = fileSnapshot.child("url").getValue().toString();
                            Log.d("FirebaseDebug", "File URL: " + fileUrl);

                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileList.add(uploadedFile);

                                Log.d("FirebaseDebug", "File Name: " + fileName + ", File URL: " + fileUrl);
                            }
                        } else {

                            String fileUrl = fileSnapshot.getValue(String.class);
                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileList.add(uploadedFile);

                                Log.d("FirebaseDebug", "File Name: " + fileName + ", File URL: " + fileUrl);
                            }
                        }
                    }
                }

                fileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String failFetch= getString(R.string.failFetch);
                Toast.makeText(requireContext(),failFetch + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        result = sanitizeFileName(result);
        return result;
    }

    public void downloadFile(String fileUrl, String fileName) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String downloadUrl = uri.toString();
                String filename=uri.getPath();
                openBrowser(downloadUrl, filename);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String failURL=getString(R.string.failURL);
                Toast.makeText(requireContext(),failURL  + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileNameFromPath(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < filePath.length() - 1) {
            return filePath.substring(lastSlashIndex + 1);
        } else {
            return filePath;
        }
    }
    private void openBrowser(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String fileNameOnly = getFileNameFromPath(fileName);
        request.setTitle(fileNameOnly);
        request.setDescription("Downloading");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        } else {

            String downManager=getString(R.string.download_manager);
            Toast.makeText(requireContext(),downManager, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        String select_file=getString(R.string.selectFile);
        if(requestCode == 86 && resultCode == RESULT_OK && data != null){
            pdfUri = data.getData();
            String fileName = getFileNameFromUri(pdfUri);
            notification.setText(fileName);
        } else {
            Toast.makeText(requireContext(),select_file, Toast.LENGTH_SHORT).show();
        }
    }

    private String sanitizeFileName(String originalFileName) {
        return originalFileName.replaceAll("[.#$\\[\\]]", "_");
    }

    @Override
    public void onDestroyView() {

        if (networkChangeReceiver != null) {
            requireContext().unregisterReceiver(networkChangeReceiver);
        }
        super.onDestroyView();
    }
}

