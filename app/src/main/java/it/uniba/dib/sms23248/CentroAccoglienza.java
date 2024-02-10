package it.uniba.dib.sms23248;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import it.uniba.dib.sms23248.Amministrazione.FileAdapter;
import it.uniba.dib.sms23248.Amministrazione.UploadedFile;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;

public class CentroAccoglienza extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String uid = currentUser.getUid();


    private NetworkChangeReceiver networkChangeReceiver;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri pdfUri;


    private RecyclerView recyclerView;
    private List<UploadedFile> fileList;
    private FileAdapterCentro fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_accoglienza);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        recyclerView = findViewById(R.id.recyclerViewC);
        fileList = new ArrayList<>();
        fileAdapter = new FileAdapterCentro(fileList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(CentroAccoglienza.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(fileAdapter);

        fetchDataFromDatabase();
        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UploadedFile uploadedFile) {

                startDownload(uploadedFile.getFileUrl(), uploadedFile.getFileName());
            }

            @Override
            public void onDeleteClick(UploadedFile uploadedFile) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

            retrieveCentroFromRichiedente(uid);

    }

    private void retrieveCentroFromRichiedente(String currentUserUid) {
        db.collection("RICHIEDENTI_ASILO")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String centro = documentSnapshot.getString("Centro");
                        if (centro != null) {
                            retrieveCentroAccoglienzaDocument(centro);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    private void retrieveCentroAccoglienzaDocument(String centro) {
        db.collection("CENTRI_ACCOGLIENZA")
                .whereEqualTo("Nome", centro)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot centroAccoglienzaDoc = queryDocumentSnapshots.getDocuments().get(0);


                        String nome = centroAccoglienzaDoc.getString("Nome");
                        String descrizione = centroAccoglienzaDoc.getString("Descrizione");
                        String sitoWeb = centroAccoglienzaDoc.getString("Sito web");
                        String indirizzo = centroAccoglienzaDoc.getString("Indirizzo");
                        String telefono = centroAccoglienzaDoc.getString("Telefono");
                        String email = centroAccoglienzaDoc.getString("Email");


                        updateUI(nome, descrizione, sitoWeb, indirizzo, telefono, email);
                    }
                });
    }


    private void updateUI(String nome, String descrizione, String sitoWeb, String indirizzo, String telefono, String email) {

        TextView Nome = findViewById(R.id.Nome);
        Nome.setText(nome);

        TextView TestoDescrizione = findViewById(R.id.Description);
        TestoDescrizione.setText(descrizione);

        TextView TestoSitoWeb = findViewById(R.id.Link);
        TestoSitoWeb.setText(sitoWeb);

        TextView TestoIndirizzo = findViewById(R.id.Indirizzo);
        TestoIndirizzo.setText(indirizzo);

        TextView TestoTelefono = findViewById(R.id.Tel);
        TestoTelefono.setText(telefono);

        TextView TestoEmail = findViewById(R.id.Email);
        TestoEmail.setText(email);
    }

    private void fetchDataFromDatabase() {
        DatabaseReference documentiUtiliReference = database.getReference("Uploads");
        documentiUtiliReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fileList.clear();


                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot fileSnapshot : categorySnapshot.getChildren()) {


                        String fileName = fileSnapshot.getKey();


                        if (fileSnapshot.hasChild("url")) {
                            String fileUrl = fileSnapshot.child("url").getValue().toString();

                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileList.add(uploadedFile);

                            }
                        } else {

                            String fileUrl = fileSnapshot.getValue(String.class);
                            if (fileName != null && fileUrl != null) {
                                UploadedFile uploadedFile = new UploadedFile(fileName, fileUrl);
                                fileList.add(uploadedFile);

                            }
                        }
                    }
                }

                fileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String fallitoRecupero=getString(R.string.failFetch);
                Toast.makeText(CentroAccoglienza.this, fallitoRecupero + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = CentroAccoglienza.this.getContentResolver().query(uri, null, null, null, null)) {
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

    public void startDownload(String fileUrl, String fileName) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String downloadUrl = uri.toString();
                String filename=uri.getPath();
                downloadPdf(downloadUrl, filename);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String fallitoDownload=getString(R.string.failURL);
                Toast.makeText(CentroAccoglienza.this, fallitoDownload + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void downloadPdf(String url, String fileName) {
        String DownloadManage=getString(R.string.download_manager);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String fileNameOnly = getFileNameFromPath(fileName);
        request.setTitle(fileNameOnly);
        request.setDescription("Downloading");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) CentroAccoglienza.this.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        } else {
            Toast.makeText(CentroAccoglienza.this, DownloadManage, Toast.LENGTH_SHORT).show();
        }
    }




    private String sanitizeFileName(String originalFileName) {
        return originalFileName.replaceAll("[.#$\\[\\]]", "_");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}
