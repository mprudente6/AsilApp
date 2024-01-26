package it.uniba.dib.sms23248.Amministrazione;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import it.uniba.dib.sms23248.Amministrazione.GeocodingTask;
import it.uniba.dib.sms23248.NetworkChangeReceiver;
import it.uniba.dib.sms23248.NetworkUtils;
import it.uniba.dib.sms23248.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
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
    private String locationQuery = "";
    SearchView searchView;
    TextView coordinates;
    private double savedZoomLevel = -1.0;

    private ItemizedIconOverlay<OverlayItem> itemizedOverlay;
    private GeoPoint savedCenter;
    DocumentReference documentRef ;
    FirebaseAuth mauth;
    FirebaseUser currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_LONG).show();

            return view;
        }
        view = inflater.inflate(R.layout.posizione_servizi, container, false);
        searchView = view.findViewById(R.id.searchView);
        coordinates = view.findViewById(R.id.textCoordinate);

        Context ctx = getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        map = (MapView) view.findViewById(R.id.mapView);



        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    Log.d(TAG, "Map tapped at: " + p.getLatitude() + ", " + p.getLongitude());

                    savedZoomLevel = map.getZoomLevelDouble();
                    saveChosenPositionToFirestore(p);
                    updateCoordinatesTextView( p);
                } else {
                    Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show();
                }



                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // Handle the long press event here
                return false;
            }
        });
        map.getOverlays().add(0, mapEventsOverlay);
        mauth = FirebaseAuth.getInstance();
         currentUser = mauth.getCurrentUser();
        String  uid=currentUser.getUid();
        retrieveCentroFromStaff(uid);




        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                locationQuery = query;

                new GeocodingTask(map, view).execute(locationQuery);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        requireContext().registerReceiver(networkChangeReceiver, intentFilter);


        return view;
    }

    private void saveChosenPositionToFirestore(GeoPoint chosenPosition) {
        // Assuming you have a document reference already defined
        Map<String, Object> data = new HashMap<>();
        data.put("latitude", chosenPosition.getLatitude());
        data.put("longitude", chosenPosition.getLongitude());
        data.put("zoomlevel", savedZoomLevel);

        documentRef.update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Chosen position saved to Firestore successfully");
                    Toast.makeText(getContext(), "Position has been saved!", Toast.LENGTH_SHORT).show();

                    itemizedOverlay.removeAllItems();

                    OverlayItem overlayItem = new OverlayItem("Chosen Position", "Description", chosenPosition);
                    itemizedOverlay.addItem(overlayItem);

                    map.invalidate();
                })

                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving chosen position to Firestore", e);
                    Toast.makeText(getContext(), "Error saving chosen position", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveStoredPositionFromFirestore(DocumentReference documentRef) {
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
                                initializeMap(storedPosition, -1.0); // Default zoom level if not saved
                            }

                            updateCoordinatesTextView(storedPosition);
                            OverlayItem overlayItem = new OverlayItem("Chosen Position", "Description", storedPosition);
                            itemizedOverlay.addItem(overlayItem);


                            map.invalidate();

                        } else {

                            initializeMap(null, -1.0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving stored position from Firestore", e);
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

            if (zoomLevel != -1.0) {
                mapController.setZoom(zoomLevel);
            } else {
                mapController.setZoom(9.5);
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

            if (savedCenter != null && savedZoomLevel != -1.0) {
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
        // Assuming you have a collection reference for STAFF
        db.collection("STAFF")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String centro = documentSnapshot.getString("Centro");
                        if (centro != null) {
                            // Once you have Centro, query CENTRI_ACCOGLIENZA to find the matching document
                            retrieveCentroAccoglienzaDocument(centro);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving Centro from STAFF collection", e);
                });
    }

    private void retrieveCentroAccoglienzaDocument(String centro) {
        // Assuming you have a collection reference for CENTRI_ACCOGLIENZA
        db.collection("CENTRI_ACCOGLIENZA")
                .whereEqualTo("Nome", centro)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming there is only one matching document, you can retrieve it
                        DocumentReference centroAccoglienzaRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        // Use centroAccoglienzaRef instead of hardcoding "C001"
                        documentRef = centroAccoglienzaRef;

                        // Now you can proceed with the rest of your logic
                        retrieveStoredPositionFromFirestore(documentRef);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving Centro Accoglienza document", e);
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
}
