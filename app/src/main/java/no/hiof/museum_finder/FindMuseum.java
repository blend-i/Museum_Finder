package no.hiof.museum_finder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class FindMuseum extends Fragment {

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

    public Context context;

    public TextView titleCardView;
    public TextView openingHoursCardView;
    public ImageView imageCardView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_museum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        materialSearchBar = view.findViewById(R.id.searchBar);
        findButton = view.findViewById(R.id.findButton);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        titleCardView = view.findViewById(R.id.titleCardView);
        openingHoursCardView = view.findViewById(R.id.openingHoursCardView);
        imageCardView = view.findViewById(R.id.imageCardView);
        context = getContext();


        Places.initialize(view.getContext(), "AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc");
        placesClient = Places.createClient(getContext());
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //dont exacly know what this does, but i know i need it
                //startSearch(text.toString(), true, null, true);
            }



            @Override
            public void onButtonClicked(int buttonCode) {
                //this function is called whne you click the button on the search bar. this may be the "back" button or the hamburger menu like button
                if(buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //for example open or close navigation drawer
                } else if(buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        //Filter out what you want to search for. In this case i use ESTABLISHMENT because we want to find museums.
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                // You can use this to restrict search if app is only gonna be used in one country -->  .setCountry("no")

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if(predictionsResponse!=null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                // TODO: 22/10/2020  Here you should use an adapter to display the predictions! i will try with arraylist as for now
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i <predictionList.size() ; i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if(!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }else {
                            Log.i("enTag", "prediction unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                //at this point we dont have the latitude and longitude. we only have a place id reference which the user has clicked on the search result
                //this needs to be sent to google places api and request it to return the latitude and longitude so we can find the actual address
                //and information regarding the address.

                if(position >= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                //Seperate thread so that the suggestion will be delayed before it gets clearaed from search list suggestion after you click it.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 200);

                //closes keyboard after user clicks suggestion
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();
                //Here we write what we are interested in. You can chose opening hours etc.
                List<Place.Field> placeFields  = Arrays.asList(
                        Place.Field.LAT_LNG,
                        Place.Field.OPENING_HOURS,
                        Place.Field.ADDRESS,
                        Place.Field.PHONE_NUMBER,
                        Place.Field.RATING,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.BUSINESS_STATUS,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.PRICE_LEVEL,
                        Place.Field.TYPES,
                        Place.Field.USER_RATINGS_TOTAL,
                        Place.Field.WEBSITE_URI
                );

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        final Place place = fetchPlaceResponse.getPlace();

                        Log.i("Tag", "place found: " + place.getName());
                        Log.i("Tag", "place opening hours: " + place.getOpeningHours());

                        LatLng latLng = place.getLatLng();
                        if(latLng != null){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //Place place1 = fetchPlaceResponse.getPlace();
                                    List<PhotoMetadata> metadata = place.getPhotoMetadatas();
                                    PhotoMetadata photoMetadata = metadata.get(0);

                                    // Create a FetchPhotoRequest.
                                    final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                            .setMaxWidth(500) // Optional.
                                            .setMaxHeight(300) // Optional.
                                            .build();
                                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                                        @Override
                                        public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                                            Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                            imageCardView.setImageBitmap(bitmap);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            if (exception instanceof ApiException) {
                                                final ApiException apiException = (ApiException) exception;
                                                Log.e("TAG", "Place not found: " + exception.getMessage());
                                                final int statusCode = apiException.getStatusCode();
                                                // TODO: Handle error with given status code.
                                            }
                                        }
                                    });
                                }
                            }).start();
                            openingHoursCardView.setText(Objects.requireNonNull(Objects.requireNonNull(place.getOpeningHours()).getWeekdayText()).toString());
                            /*System.out.println("Gjør det du skal her.");
                            System.out.println("Address: " + place.getAddress());
                            System.out.println("Lat Lng : " + place.getLatLng());
                            System.out.println("Opening hours: " + place.getOpeningHours());
                            System.out.println("Phone number: " + place.getPhoneNumber());
                            System.out.println("Rating: " + place.getRating());
                             */
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("randomTag", "place not found: " + e.getMessage());
                            Log.i("randomTag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        //check if gps is enabeled or not aand then request user to enable it

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this.getContext());

        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        //This line will ask the user if they want to accept enable location or not. The code is something i made up as successcode. Do not use elsewhere.
                        resolvableApiException.startResolutionForResult((Activity) getContext(), 27);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
        //Here we ask the fusedLocationProviderClient to give us the last location
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    //task is successful does not gurantee that it is so we check if its null
                    lastKnownLocation = task.getResult();

                    //If it is not null, we get the location
                    if (lastKnownLocation != null) {
                        //putting this in a thread beacuse i read somewhere that Geocoder can use alot of time.
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Geocoder geocoder;
                                List<Address> addresses = null;
                                geocoder = new Geocoder(getContext(), Locale.getDefault());

                                //using latitude and longitude from last location to pinpoint address
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
                        }).start();

                    }
                    // if location is null, then we need to update the information
                    else {
                        final LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        //this function will be executed when an updated location is recieved
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

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Geocoder geocoder;
                                        List<Address> addresses = null;
                                        geocoder = new Geocoder(getContext(), Locale.getDefault());

                                        //using latitude and longitude from last location to pinpoint address
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

                                        System.out.println("Address after location turn on: " + address);
                                        System.out.println("City: " + city);
                                        System.out.println("Country: " + country);
                                    }
                                }).start();
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(getContext(), "unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}