package it.uniba.dib.sms23248.Amministrazione;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import it.uniba.dib.sms23248.NetworkAvailability.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkAvailability.NetworkUtils;
import it.uniba.dib.sms23248.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PosizioneFragment extends Fragment {

    View view;

    NetworkChangeReceiver networkChangeReceiver;
    MapView map = null;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView coordinates;
    private double savedZoomLevel = 3;

    private ItemizedIconOverlay<OverlayItem> itemizedOverlay;
    private GeoPoint savedCenter;
    DocumentReference documentRef ;
    FirebaseAuth mauth;
    FirebaseUser currentUser;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String connesso=getString(R.string.connessione);

        view = inflater.inflate(R.layout.fragment_posizione, container, false);

        coordinates = view.findViewById(R.id.textCoordinate);

        map = view.findViewById(R.id.mapView);


        mauth = FirebaseAuth.getInstance();
        currentUser = mauth.getCurrentUser();
        String  uid=currentUser.getUid();
        retrieveCentroFromStaff(uid);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                if (NetworkUtils.isNetworkAvailable(requireContext())) {

                    savedZoomLevel = map.getZoomLevelDouble();
                    saveChosenPositionToFirestore(p);
                    updateCoordinatesTextView( p);
                } else {
                    Toast.makeText(requireContext(),connesso, Toast.LENGTH_LONG).show();
                }



                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {

                return false;
            }
        });

        map.getOverlays().add(0, mapEventsOverlay);




        //controlla la connessione internet
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkChangeReceiver, intentFilter);



        return view;
    }

    private void saveChosenPositionToFirestore(GeoPoint chosenPosition) {
        String positionsaved = getString(R.string.posizione_salvata);
       String errore=getString(R.string.errore);

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", chosenPosition.getLatitude());
        data.put("longitude", chosenPosition.getLongitude());
        data.put("zoomlevel", savedZoomLevel);

        // Document Reference del Centro di Accoglienza di appertenenza dello Staff loggatto
        documentRef.update(data)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(getContext(), positionsaved, Toast.LENGTH_SHORT).show();

                    itemizedOverlay.removeAllItems();

                    OverlayItem overlayItem = new OverlayItem("Posizione", "posizione scelta", chosenPosition);
                    itemizedOverlay.addItem(overlayItem);

                    map.invalidate();
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(getContext(), errore +e, Toast.LENGTH_SHORT).show();
                });
    }

    private void retrievePositionFromFirestore(DocumentReference documentRef) {
        documentRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double latitude = documentSnapshot.getDouble("latitude");
                        Double longitude = documentSnapshot.getDouble("longitude");
                        Double storedZoomLevel = documentSnapshot.getDouble("zoomlevel");

                        if (latitude != null && longitude != null) {
                            GeoPoint storedPosition = new GeoPoint(latitude, longitude);

                            if (storedZoomLevel != null) {
                                initializeMap(storedPosition, storedZoomLevel);
                            } else {
                                initializeMap(storedPosition, 3);
                            }

                            updateCoordinatesTextView(storedPosition);
                            OverlayItem overlayItem = new OverlayItem("Chosen Position", "Description", storedPosition);
                            itemizedOverlay.addItem(overlayItem);


                            map.invalidate();

                        } else {

                            initializeMap(null, 3);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
    }


    private void initializeMap(GeoPoint centerPoint, double zoomLevel) {
        map = view.findViewById(R.id.mapView);

        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setBuiltInZoomControls(true);
            map.setMultiTouchControls(true);

            itemizedOverlay = new ItemizedIconOverlay<>(new ArrayList<>(), getResources().getDrawable(org.osmdroid.library.R.drawable.marker_default), null, getContext());

            map.getOverlays().add(itemizedOverlay);

            IMapController mapController = map.getController();

            if (zoomLevel != 3) {
                mapController.setZoom(zoomLevel);
            }

            if (centerPoint != null) {
                mapController.setCenter(centerPoint);
            }
        }
    }

    private void updateCoordinatesTextView(GeoPoint geoPoint) {
        if (view != null) {
            TextView coordinatesTextView = view.findViewById(R.id.textCoordinate);
            if (coordinatesTextView != null) {
                String lat = view.getResources().getString(R.string.LatiGeo);
                String longi = view.getResources().getString(R.string.LongiGeo);

                String coordinatesText = lat + ": " + geoPoint.getLatitude() + "\n" + longi + ": " + geoPoint.getLongitude();
                coordinatesTextView.setText(coordinatesText);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (map != null) {
            map.onResume();

            if (savedCenter != null && savedZoomLevel != 3) {
                map.getController().setCenter(savedCenter);
                map.getController().setZoom(savedZoomLevel);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (map != null) {
            map.onPause();

            savedCenter = (GeoPoint) map.getMapCenter();
            savedZoomLevel = map.getZoomLevelDouble();
        }
    }

    private void retrieveCentroFromStaff(String currentUserUid) {

        db.collection("STAFF")
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
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        documentRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        retrievePositionFromFirestore(documentRef);
                    }
                })
                .addOnFailureListener(e -> {
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
