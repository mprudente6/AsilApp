package it.uniba.dib.sms23248.RichiedenteAsilo;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

public class MappaCentro extends AppCompatActivity {
    private MapView map = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    String uid = mAuth.getUid();


    Double latitude;
    Double longitude;
    Double zoomlevel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = this.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_mappa_centro);


            retrieveCentroFromRichiedente(uid);

    }

    private void onSuccessRenderMap() {

        if (isFinishing()) {
            return;
        }


        map = findViewById(R.id.mapView);


        if (map == null) {
            return;
        }


        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(zoomlevel);

        GeoPoint startPoint = new GeoPoint(latitude, longitude);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);


        map.getOverlays().add(startMarker);


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

                        latitude = centroAccoglienzaDoc.getDouble("latitude");
                        longitude = centroAccoglienzaDoc.getDouble("longitude");
                        zoomlevel = centroAccoglienzaDoc.getDouble("zoomlevel");

                        onSuccessRenderMap();
                    }
                });
    }
}