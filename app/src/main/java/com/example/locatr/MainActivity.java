package com.example.locatr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private GoogleApiClient client;

    private static final int REQUEST_CODE =0 ;
    private ImageView imageView_flicker;
    public static final String TAG = "MainActivity";
    private static final String[] LOCATION_PERMISSION_GROUP = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView_flicker = findViewById(R.id.image);

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }


    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int error = apiAvailability.isGooglePlayServicesAvailable(this);
        if (error != ConnectionResult.SUCCESS){
            Dialog dialog = apiAvailability.getErrorDialog(this,
                    error,
                    REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            dialog.show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem item = menu.findItem(R.id.action_location);
        item.setEnabled(client.isConnected());
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
    protected void onStart() {
        super.onStart();
        client.connect();
        invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
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

                new SearchTask(imageView_flicker).execute(location);

            }
        });
    }

    private boolean hasLocationPermission(){
        int result = ContextCompat.checkSelfPermission(this
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
}
