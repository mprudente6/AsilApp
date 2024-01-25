package it.uniba.dib.sms23248;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MappaCentro extends AppCompatActivity {
    private MapView map = null;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private ItemizedIconOverlay<OverlayItem> itemizedOverlay;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    String uid = mAuth.getUid();


    Double latitude;
    Double longitude;
    Double zoomlevel;

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = this.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_mappa_centro);

        if (NetworkUtils.isNetworkAvailable(MappaCentro.this)) {
            retrieveCentroFromRichiedente(uid); // Replace with actual user UID
        } else {
            Toast.makeText(MappaCentro.this, "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void onSuccessRenderMap() {
        // Check if the activity is finishing or has been destroyed
        if (isFinishing()) {
            Log.e(TAG, "Aggiornamento in corso. Riprovare più tardi");
            return;
        }

        // Find the MapView by its ID
        map = findViewById(R.id.mapView);

        // Check if the MapView is null (e.g., if the layout is not correctly inflated)
        if (map == null) {
            Log.e(TAG, "Mappa inesistente");
            return;
        }

        // Set up the MapView and render the map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(zoomlevel);

        GeoPoint startPoint = new GeoPoint(latitude, longitude);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        // Add the marker to the map overlays
        map.getOverlays().add(startMarker);

        // Set the center of the map
        mapController.setCenter(startPoint);
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
                    Log.e(TAG, "Error retrieving Centro from STAFF collection", e);
                });
    }


    private void retrieveCentroAccoglienzaDocument(String centro) {
        db.collection("CENTRI_ACCOGLIENZA")
                .whereEqualTo("Nome", centro)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for updates in Centro Accoglienza document", e);
                        return;
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot centroAccoglienzaDoc = queryDocumentSnapshots.getDocuments().get(0);

                        // Retrieve fields from Centro Accoglienza document
                        String nome = centroAccoglienzaDoc.getString("Nome");
                        String descrizione = centroAccoglienzaDoc.getString("Descrizione");
                        String sitoWeb = centroAccoglienzaDoc.getString("Sito web");
                        String indirizzo = centroAccoglienzaDoc.getString("Indirizzo");
                        String telefono = centroAccoglienzaDoc.getString("Telefono");
                        String email = centroAccoglienzaDoc.getString("Email");
                        latitude = centroAccoglienzaDoc.getDouble("latitude");
                        longitude = centroAccoglienzaDoc.getDouble("longitude");
                        zoomlevel = centroAccoglienzaDoc.getDouble("zoomlevel");

                        onSuccessRenderMap(); // Call the map rendering method
                    }
                });
    }
}
