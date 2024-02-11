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
import java.util.HashMap;
import java.util.List;
import java.util.Map;



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
    private RecyclerView recyclerView2;
    private List<UploadedFile> fileList;
    private FileAdapter fileAdapter;
    private FileAdapter fileAdapter2;
    private List<UploadedFile> fileListUploads;
    private List<UploadedFile> fileListDocumentiUtili;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_documenti, container, false);

        selectFile = view.findViewById(R.id.selectFile);
        upload = view.findViewById(R.id.upload);
        Button upload2=view.findViewById(R.id.upload2);
        selectNotification = view.findViewById(R.id.selectNotification);


        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView2= view.findViewById(R.id.recyclerView2);

        fileListUploads = new ArrayList<>();
        fileListDocumentiUtili = new ArrayList<>();
        fileAdapter = new FileAdapter(fileListUploads, this);
        fileAdapter2 = new FileAdapter(fileListDocumentiUtili, this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileAdapter);



        LinearLayoutManager layoutManager2 = new LinearLayoutManager(requireContext());
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setAdapter(fileAdapter2);





        fetchUploads();
        fetchDocumentiUtili();

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
                String connessione = getString(R.string.connessione);
                String select = getString(R.string.select_pdf);

                if (pdfUri != null) {
                    if (NetworkUtils.isNetworkAvailable(requireContext())) {

                        String destinationPath = "Uploads/" + getFileNameFromUri(pdfUri);
                        startsUploadAndCheckExistingFile(pdfUri, destinationPath);
                    } else {
                        Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(), select, Toast.LENGTH_SHORT).show();
                }
            }
        });

        upload2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String connessione = getString(R.string.connessione);
                String select = getString(R.string.select_pdf);

                if (pdfUri != null) {
                    if (NetworkUtils.isNetworkAvailable(requireContext())) {

                        String destinationPath = "DocumentiUtili/" + getFileNameFromUri(pdfUri);
                        startsUploadAndCheckExistingFile(pdfUri, destinationPath);
                    } else {
                        Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(), select, Toast.LENGTH_SHORT).show();
                }
            }
        });


        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onDownloadClick(UploadedFile uploadedFile) {

                downloadFile(uploadedFile.getFileUrl());
            }

            @Override
            public void onDeleteClick(UploadedFile uploadedFile) {
                String connessione = getString(R.string.connessione);
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    showDeleteConfirmationDialog(uploadedFile);
                } else {
                    Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
                }
            }
        });
        fileAdapter2.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onDownloadClick(UploadedFile uploadedFile) {
                downloadFile(uploadedFile.getFileUrl());
            }

            @Override
            public void onDeleteClick(UploadedFile uploadedFile) {
                String connessione = getString(R.string.connessione);
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    showDeleteConfirmationDialog(uploadedFile);
                } else {
                    Toast.makeText(requireContext(), connessione, Toast.LENGTH_LONG).show();
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

            }
        }
    }

    private void fetchUploads() {

        DatabaseReference documentiUploads = database.getReference("Uploads");


        documentiUploads.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                fileListUploads.clear();
               //struttura realtime database: -folder
                //                              -nome file
                //                                 -nome file: url
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot fileSnapshot : categorySnapshot.getChildren()) {
                        Log.d("Firebase", "File Snapshot Key: " + fileSnapshot.getKey());
                        Log.d("Firebase", "File Snapshot Value: " + fileSnapshot.getValue());

                        String fileName = fileSnapshot.getKey();  //nome pdf

                        if (fileSnapshot.hasChild("url")) {
                            String fileUrl = fileSnapshot.child("url").getValue().toString(); //url pdf
                            Log.d("Firebase", "File URL: " + fileUrl);

                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileListUploads.add(uploadedFile);

                                Log.d("Firebase", "File Name: " + fileName + ", File URL: " + fileUrl);
                            }
                        } else {

                            String fileUrl = fileSnapshot.getValue(String.class);
                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileListUploads.add(uploadedFile);


                            }
                          }
                    }
                }

                fileAdapter.notifyDataSetChanged();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void fetchDocumentiUtili(){
        DatabaseReference documentiUtiliReference = database.getReference("DocumentiUtili");
        documentiUtiliReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileListDocumentiUtili.clear();


                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot fileSnapshot : categorySnapshot.getChildren()) {
                        Log.d("Firebase", "File Snapshot Key: " + fileSnapshot.getKey());
                        Log.d("Firebase", "File Snapshot Value: " + fileSnapshot.getValue());

                        String fileName = fileSnapshot.getKey();


                        if (fileSnapshot.hasChild("url")) {
                            String fileUrl = fileSnapshot.child("url").getValue().toString();
                            Log.d("Firebase", "File URL: " + fileUrl);

                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileListDocumentiUtili.add(uploadedFile);

                                Log.d("Firebase", "File Name: " + fileName + ", File URL: " + fileUrl);
                            }
                        } else {

                            String fileUrl = fileSnapshot.getValue(String.class);
                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileListDocumentiUtili.add(uploadedFile);

                                Log.d("Firebase", "File Name: " + fileName + ", File URL: " + fileUrl);
                            }
                        }
                    }
                }

                fileAdapter2.notifyDataSetChanged();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error: " + databaseError.getMessage());

            }
        });
    }


    private void startsUploadAndCheckExistingFile(Uri pdfUri, String destinationPath) {
        String caricamento = getString(R.string.Caricamento);
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(caricamento);
        progressDialog.setProgress(0);
        progressDialog.show();
        String err=getString(R.string.errore);

        String fileName = getFileNameFromUri(pdfUri);

        StorageReference storageReference = storage.getReference();
        StorageReference fileReference = storageReference.child(destinationPath).child(fileName);

        fileReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            progressDialog.dismiss();
            showFileExistsDialog(fileName);
        }).addOnFailureListener(e -> {
            if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                uploadNewFile(fileReference, pdfUri, fileName, destinationPath);
            } else {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), err + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadNewFile(StorageReference fileReference, Uri pdfUri, String fileName, String destinationPath) {
        String uploaded_file = getString(R.string.upload_pdf);
        UploadTask uploadTask = fileReference.putFile(pdfUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), uploaded_file, Toast.LENGTH_SHORT).show();

            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                DatabaseReference reference = database.getReference(destinationPath);

                int index = destinationPath.indexOf('/');

                String folderName= destinationPath.substring(0, index);

                reference.child(fileName).setValue(url).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (folderName.equals("Uploads")) {  //docuementi che vanno visualizzati in il mio Centro

                            UploadedFile uploadedFile = new UploadedFile(fileName, url);
                            fileListUploads.add(uploadedFile);

                            fileAdapter.notifyDataSetChanged();
                            fetchUploads();

                        } else if (folderName.equals("DocumentiUtili")) { //documenti da far visualizzare nella sezione DocumentiUtili di media
                            UploadedFile uploadedFile = new UploadedFile(fileName, url);
                            fileListDocumentiUtili.add(uploadedFile);
                            fileAdapter2.notifyDataSetChanged();
                            fetchDocumentiUtili();
                        }
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




    private void showFileExistsDialog(String fileName) {
        String attention = getString(R.string.Attenzione);
        String fialeGiaCaricato = getString(R.string.FilegiaCaricato);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(attention);
        builder.setMessage(fialeGiaCaricato);
        builder.setPositiveButton("OK", null);
        builder.show();
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
        String err=getString(R.string.errore);
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
                Toast.makeText(requireContext(), err + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        String downMan=getString(R.string.download_manager);
        String downloadM = getString(R.string.FilegiaCaricato);
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
            Toast.makeText(requireContext(),downMan, Toast.LENGTH_SHORT).show();
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
        String destinationPath = uploadedFile.getFileUrl().contains("Uploads") ? "Uploads" : "DocumentiUtili";

        DatabaseReference parentNodeReference = databaseReference.child(destinationPath);

        parentNodeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String deleteSuccess=getString(R.string.DeleteSuccess);
                String ErrorRemoveDB=getString(R.string.erroreDB);
                String ErrorRemoveStorage=getString(R.string.erroreStorage);
                String fileNotFound=getString(R.string.fileNotFound);





                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot fileSnapshot : categorySnapshot.getChildren()) {

                        String fileKey = fileSnapshot.getKey();
                        String fileUrl = fileSnapshot.child("url").getValue(String.class);

                        if (fileName.equals(fileKey)) {
                            DatabaseReference fileDatabaseReference = parentNodeReference.child(categorySnapshot.getKey()).child(fileKey);
                            StorageReference fileReference = storageReference.child(destinationPath).child(categorySnapshot.getKey()).child(fileKey);


                            fileReference.delete().addOnSuccessListener(aVoid -> {

                                fileDatabaseReference.removeValue().addOnSuccessListener(aVoid1 -> {
                                    if (destinationPath.equals("Uploads")) {
                                        fileListUploads.clear();
                                        fetchUploads();
                                        fileAdapter.notifyDataSetChanged();
                                    } else if (destinationPath.equals("DocumentiUtili")) {
                                        fileListDocumentiUtili.clear();
                                        fetchDocumentiUtili();
                                        fileAdapter2.notifyDataSetChanged();
                                    }
                                    Toast.makeText(requireContext(),deleteSuccess, Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), ErrorRemoveDB + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }).addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), ErrorRemoveStorage + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                    }
                }


                Toast.makeText(requireContext(), fileNotFound, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String errorSearch=getString(R.string.erroreRicerca);
                Toast.makeText(requireContext(), errorSearch + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

