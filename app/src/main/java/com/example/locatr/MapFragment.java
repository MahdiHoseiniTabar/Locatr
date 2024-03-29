package com.example.locatr;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment {

    private static final int REQUEST_PERMISSION = 1;
    private GoogleApiClient client;
    private static final int REQUEST_CODE =0 ;
    public static final String TAG = "MainActivity";
    private static final String[] LOCATION_PERMISSION_GROUP = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private Bitmap mapImage;
    private GalleryItem mapItem;
    private Location currentLocation;
    private GoogleMap map;

    public static MapFragment newInstance() {

        Bundle args = new Bundle();

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        setHasOptionsMenu(true);

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                updateUI();
            }
        });
    }

    private void updateUI(){
        if(map == null || mapImage== null)
            return;

        LatLng itemPoint = new LatLng(mapItem.getLat()
                ,mapItem.getLon());
        LatLng myPoint = new LatLng(currentLocation.getLatitude()
                ,currentLocation.getLongitude());

        BitmapDescriptor itemBitmap = BitmapDescriptorFactory
                .fromBitmap(mapImage);

        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        map.clear();
        map.addMarker(myMarker);
        map.addMarker(itemMarker);

        int margin = getResources().getDimensionPixelSize(R.dimen.inset_margin);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(myPoint)
                .include(itemPoint)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,
                margin);
        map.animateCamera(cameraUpdate);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem item = menu.findItem(R.id.action_location);
        item.setEnabled(client.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_location){
            if (hasLocationPermission())
                findImage();
            else
                requestPermissions(LOCATION_PERMISSION_GROUP,REQUEST_PERMISSION);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }
    public void findImage(){
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(client,
                request,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.i(TAG, "onLocationChanged: " + location);

                        new SearchTask().execute(location);

                    }
                });
    }
    private boolean hasLocationPermission(){
        int result = ContextCompat.checkSelfPermission(getActivity()
                ,LOCATION_PERMISSION_GROUP[0]);

        return result == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION)
            if (hasLocationPermission())
                findImage();

    }

    public class SearchTask extends AsyncTask<Location,Void,Void> {

        GalleryItem item;
        Bitmap bitmap;
        Location location;



        @Override
        protected Void doInBackground(Location... locations) {
            location=locations[0];
            FlickrFetcher fetcher = new FlickrFetcher();
            List<GalleryItem> itemList = fetcher.searchPhoto(locations[0]);
            if (itemList.size() == 0)
                return null;

            item = itemList.get(0);

            try {
                byte[] bytes = fetcher.getUrlBytes(item.getUrl());
                bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            currentLocation = location;
            mapImage = bitmap;
            mapItem = item;
            updateUI();
        }
    }
}
