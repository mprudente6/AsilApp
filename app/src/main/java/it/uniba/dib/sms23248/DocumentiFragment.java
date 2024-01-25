package it.uniba.dib.sms23248;

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
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import it.uniba.dib.sms23248.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class DocumentiFragment extends Fragment implements UploadCallback{



    private NetworkChangeReceiver networkChangeReceiver;

    View view;
    Button selectFile, upload;
    TextView notification;
    FirebaseDatabase database;
    FirebaseStorage storage;
    int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;
    int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE=4;
    Uri pdfUri;

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
        notification = view.findViewById(R.id.notification);
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
                Log.e("PDF","On click");
                if (ContextCompat.checkSelfPermission(requireContext(), "android.permission.READ_EXTERNAL_STORAGE")
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.e("PDF","Permission granted");

                    selectPdf();
                } else {
                    Log.e("PDF","Requesting permission");

                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);

                }
            }
        });



        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfUri!=null){
                    if (NetworkUtils.isNetworkAvailable(requireContext())) {
                        uploadFile(pdfUri);
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

                downloadFile(uploadedFile.getFileUrl(), uploadedFile.getFileName());
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

    @Override
    public void onUploadSuccess(String fileName, String downloadUrl) {
        // Handle successful upload, e.g., update UI or proceed with additional tasks
        Log.d("UploadCallback", "Upload successful for file: " + fileName);
        // You can trigger the download or any other actions here
    }

    @Override
    public void onUploadFailure(String fileName, Exception exception) {
        // Handle upload failure, e.g., show an error message
        Log.e("UploadCallback", "Upload failed for file: " + fileName, exception);
        Toast.makeText(requireContext(), "Failed to upload file: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void fetchDataFromDatabase() {
        DatabaseReference reference = database.getReference();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileList.clear(); // Clear the existing list
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

    private void uploadFile(Uri pdfUri) {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Caricamento in corso");
        progressDialog.setProgress(0);
        progressDialog.show();

        String fileName = getFileNameFromUri(pdfUri);

        StorageReference storageReference = storage.getReference();
        StorageReference fileReference = storageReference.child("Uploads").child(fileName);

        // Check if the file with the same name exists
        fileReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            progressDialog.dismiss();
            showFileExistsDialog(fileName);
        }).addOnFailureListener(e -> {
            // File with the same name doesn't exist, proceed with the upload
            if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                uploadNewFile(fileReference, pdfUri, fileName);
            } else {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Failed to check file existence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showFileExistsDialog(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Attenzione");
        builder.setMessage("Questo file è stato già caricato! Elimina il vecchio prima di continuare!");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void uploadNewFile(StorageReference fileReference, Uri pdfUri, String fileName) {
        UploadTask uploadTask = fileReference.putFile(pdfUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(requireContext(), "File caricato!", Toast.LENGTH_SHORT).show();

            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();
                DatabaseReference reference = database.getReference();
                reference.child(fileName).setValue(url).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UploadedFile uploadedFile = new UploadedFile(fileName, url);
                        fileList.add(uploadedFile);
                        fileAdapter.notifyDataSetChanged();
                        // Notify the callback about successful upload
                        onUploadSuccess(fileName, url);
                    } else {
                        // Notify the callback about upload failure
                        onUploadFailure(fileName, task.getException());
                    }
                }).addOnFailureListener(e -> {
                    // Notify the callback about upload failure
                    onUploadFailure(fileName, e);
                });
            });
        }).addOnProgressListener(snapshot -> {
            int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setProgress(currentProgress);
        }).addOnFailureListener(e -> {
            // Notify the callback about upload failure
            progressDialog.dismiss();
            onUploadFailure(fileName, e);
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



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("PERMISSION", "Granted");
                    selectPdf();
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Log.e("PERMISSION", "Don't ask again");
                        showPermissionSettingsDialog();
                    } else {
                        // User denied the permission without selecting "Don't ask again."
                        // Handle this situation as needed (e.g., show a message to the user).
                        Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 86) {
            if (resultCode == RESULT_OK && data != null) {
                pdfUri = data.getData();
                String fileName = getFileNameFromUri(pdfUri);
                notification.setText(fileName);
            } else {
                Toast.makeText(requireContext(), "File selection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void selectPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, 86);
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
            Toast.makeText(requireContext(), "DownloadManager not available", Toast.LENGTH_SHORT).show();
        }
    }




    private String sanitizeFileName(String originalFileName) {
        return originalFileName.replaceAll("[.#$\\[\\]]", "_");
    }

    private void showDeleteConfirmationDialog(final UploadedFile uploadedFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Elimina File");
        builder.setMessage("Sei sicuro di voler eliminare questo file?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFile(uploadedFile);
            }
        });
        builder.setNegativeButton("Cancel", null);
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
                        Toast.makeText(requireContext(), "File eliminato", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure to delete from Database
                        Toast.makeText(requireContext(), "Failed to delete file from Database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure to delete from Storage
                Toast.makeText(requireContext(), "Failed to delete file from Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onDestroyView() {
        // Unregister the BroadcastReceiver when the fragment is destroyed
        if (networkChangeReceiver != null) {
            requireContext().unregisterReceiver(networkChangeReceiver);
        }
        super.onDestroyView();
    }

    private void showPermissionSettingsDialog() {
        Log.e("PERMISSION","show permission dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Permission Required")
                .setMessage("This app needs storage permission. You can grant the permission in the app settings.")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", requireActivity().getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the user's choice (e.g., show a message)
                        Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
    public interface UploadCallback {
        void onUploadSuccess(String fileName, String downloadUrl);
        void onUploadFailure(String fileName, Exception exception);
    }

}

