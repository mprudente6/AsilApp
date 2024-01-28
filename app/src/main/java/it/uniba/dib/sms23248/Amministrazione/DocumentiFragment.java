package it.uniba.dib.sms23248.Amministrazione;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class DocumentiFragment extends Fragment {



    private NetworkChangeReceiver networkChangeReceiver;

    View view;
    Button selectFile, upload;
    TextView selectNotification;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri pdfUri;
    int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE=4;

    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private List<UploadedFile> fileList;
    private FileAdapter fileAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_documenti, container, false);
        selectFile = view.findViewById(R.id.selectFile);
        upload = view.findViewById(R.id.upload);
        selectNotification = view.findViewById(R.id.selectNotification);
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_LONG).show();

            return view;
        }

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        fileList = new ArrayList<>();
        fileAdapter = new FileAdapter(fileList,this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileAdapter);



        fetchDataFromDatabase();

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("PDF", "On click");
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {


                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                }
            }
        });



        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfUri!=null){
                    if (NetworkUtils.isNetworkAvailable(requireContext())) {
                        startsUploadAndCheckExistingFile(pdfUri);
                    } else {
                        Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
                    }

                }
                else
                    Toast.makeText(requireContext(),"Seleziona un file",Toast.LENGTH_SHORT).show();
            }
        });

        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UploadedFile uploadedFile) {

                downloadFile(uploadedFile.getFileUrl());
            }

            @Override
            public void onDeleteClick(UploadedFile uploadedFile) {
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    showDeleteConfirmationDialog(uploadedFile);
                } else {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
                }

            }
        });


        return view;

    }

    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, 86);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 86) {
            if (resultCode == RESULT_OK && data != null) {

                pdfUri = data.getData();
                String fileName = getFileNameFromUri(pdfUri);
                selectNotification.setText(fileName);

            } else {
                Toast.makeText(requireContext(), "File selection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchDataFromDatabase() {
        DatabaseReference reference = database.getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String fileName = snapshot.getKey();
                    String fileUrl = snapshot.getValue(String.class);
                    UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                    fileList.add(uploadedFile);
                }
                fileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startsUploadAndCheckExistingFile(Uri pdfUri) {
        String caricamento = getString(R.string.Caricamento);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(caricamento);
        progressDialog.setProgress(0);
        progressDialog.show();

        String fileName = getFileNameFromUri(pdfUri);

        StorageReference storageReference = storage.getReference();
        StorageReference fileReference = storageReference.child("Uploads").child(fileName);


        fileReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            progressDialog.dismiss();
            showFileExistsDialog(fileName);
        }).addOnFailureListener(e -> {

            if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                uploadNewFile(fileReference, pdfUri, fileName);
            } else {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Failed to check file existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showFileExistsDialog(String fileName) {
        String attention = getString(R.string.Attenzione);
        String fialeGiaCaricato = getString(R.string.FilegiaCaricato);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(attention);
        builder.setMessage(fialeGiaCaricato);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void uploadNewFile(StorageReference fileReference, Uri pdfUri, String fileName) {
        UploadTask uploadTask = fileReference.putFile(pdfUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "File uploaded!", Toast.LENGTH_SHORT).show();

            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                DatabaseReference reference = database.getReference();
                reference.child(fileName).setValue(url).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UploadedFile uploadedFile = new UploadedFile(fileName, url);
                        fileList.add(uploadedFile);
                        fileAdapter.notifyDataSetChanged();


                    } else {


                    }
                }).addOnFailureListener(e -> {

                });
            });
        }).addOnProgressListener(snapshot -> {
            int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setProgress(currentProgress);
        }).addOnFailureListener(e -> {
      
            progressDialog.dismiss();

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





    public void downloadFile(String fileUrl) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String downloadUrl = uri.toString();
                String filename=uri.getPath(); //restituisce /Uploads/nome_file.pdf
                downloadingOnDevice(downloadUrl, filename);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void downloadingOnDevice(String url, String fileName) {
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
            Toast.makeText(requireContext(), "DownloadManager not available", Toast.LENGTH_SHORT).show();
        }
    }




    private String sanitizeFileName(String originalFileName) {
        return originalFileName.replaceAll("[.#$\\[\\]]", "_");
    }

    private void showDeleteConfirmationDialog(final UploadedFile uploadedFile) {
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

                deleteFile(uploadedFile);
            }
        });
        builder.setNegativeButton(Annulla, null);
        builder.show();
    }

    private void deleteFile(UploadedFile uploadedFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String fileName = uploadedFile.getFileName();

        StorageReference fileReference = storageReference.child("Uploads").child(fileName);
        DatabaseReference fileDatabaseReference = databaseReference.child(fileName);

        fileReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                fileDatabaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fileList.remove(uploadedFile);
                        fileAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "File deleted", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(requireContext(), "Failed to delete file from Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(requireContext(), "Failed to delete file from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
