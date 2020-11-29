package no.hiof.museum_finder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavController controller = Navigation.findNavController(this, R.id.fragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_NavBar);
        NavigationUI.setupWithNavController(bottomNavigationView, controller);

        Dexter.withContext(MainActivity.this).withPermission(ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MainActivity.this, "permission GRANTED", Toast.LENGTH_SHORT).show();
                // Initialize the SDK
                Places.initialize(getApplicationContext(), getResources().getString(R.string.maps_api_key));

                // Create a new PlacesClient instance
                PlacesClient placesClient = Places.createClient(MainActivity.this);

                // Use fields to define the data types to return.
                List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);

                // Use the builder to create a FindCurrentPlaceRequest.
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                // Call findCurrentPlace and handle the response (first check that the user has granted permission).
                if (ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);

                    placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                            if (task.isSuccessful()) {
                                FindCurrentPlaceResponse response = task.getResult();
                                //Doesent have to be included as long as task is succesful we got permission and can go to next activity.
                                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                    Log.i("TAG", String.format("Place '%s' has likelihood: %f",
                                            placeLikelihood.getPlace().getName(),
                                            placeLikelihood.getLikelihood()));
                                    placeLikelihood.getPlace().getAddress();
                                }
                                //startActivity(new Intent(MainActivity.this, MuseumDetailsApi.class));

                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    Log.e("TAG", "Place not found: " + apiException.getStatusCode());
                                }
                            }
                        }
                    });
                } else {
                    // A local method to request required permissions;
                    // See https://developer.android.com/training/permissions/requesting
                    Log.d("MainActivity", "permission denied, need permission");
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                if (permissionDeniedResponse.isPermanentlyDenied()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder
                            .setTitle("Permission Denied")
                            .setMessage("Permission to access device location is denied. Go to settings to allow the permission")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                }
                            }).show();
                } else {
                    Toast.makeText(MainActivity.this, "onPermissionDenied", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    private void getLocationPermission() {
        System.out.println("getLocationPermission: need permission");
    }

    public void toMuseumDetail(View view) {
        //startActivity(new Intent(MainActivity.this, MuseumDetailsApi.class));
    }

}