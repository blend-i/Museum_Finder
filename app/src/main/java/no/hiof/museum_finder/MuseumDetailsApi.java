package no.hiof.museum_finder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MuseumDetailsApi extends AppCompatActivity {


    //This class tells you your current location
    public FusedLocationProviderClient fusedLocationProviderClient;

    //this class is responsible for loading the suggestions as you see the user type in the search string.
    public PlacesClient placesClient;

    //as the suggestions are recieved, we need a list to save those.
    public List<AutocompletePrediction> predictionList;

    public Location lastKnownLocation;

    //update userrequest if last location is null
    public LocationCallback locationCallback;

    //Search bar
    public MaterialSearchBar materialSearchBar;

    //Finds the result the user is searching for
    public Button findButton;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum_details_api);

        materialSearchBar = findViewById(R.id.searchBar);
        findButton = findViewById(R.id.findButton);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MuseumDetailsApi.this);
        Places.initialize(MuseumDetailsApi.this, "AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc");
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //check if gps is enabeled or not aand then request user to enable it

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(MuseumDetailsApi.this);

        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(MuseumDetailsApi.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MuseumDetailsApi.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        //This line will ask the user if they want to accept enable location or not. The code is something i made up as successcode. Do not use elsewhere.
                        resolvableApiException.startResolutionForResult(MuseumDetailsApi.this, 27);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 27) {
            //RESULT_OK is if the user has enabeled the gps. here we get access to location
            if (resultCode == RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    lastKnownLocation = task.getResult();
                    if (lastKnownLocation != null) {

                        Geocoder geocoder;
                        List<Address> addresses = null;
                        geocoder = new Geocoder(MuseumDetailsApi.this, Locale.getDefault());

                        try {
                            addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                        System.out.println("Address: " + address);
                        System.out.println("City: " + city);
                        System.out.println("Country: " + country);
                    }
                    // if location is null, then we need to update the information
                    else {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                //if its still null then just return and stop
                                if(locationResult == null){
                                    return;
                                }
                                //update location
                                lastKnownLocation = locationResult.getLastLocation();


                                Geocoder geocoder;
                                List<Address> addresses = null;
                                geocoder = new Geocoder(MuseumDetailsApi.this, Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(MuseumDetailsApi.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}