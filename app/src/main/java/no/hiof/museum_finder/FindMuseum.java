package no.hiof.museum_finder;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
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
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import no.hiof.museum_finder.model.Museum;

import static android.app.Activity.RESULT_OK;

/**
 * Some methods of places search and materialsearchbar are
 * inspired by this video: https://www.youtube.com/watch?v=ifoVBdtXsv0&t=0s&ab_channel=AbbasHassan
 */
public class FindMuseum extends Fragment {
    public FusedLocationProviderClient fusedLocationProviderClient;
    public PlacesClient placesClient;
    public List<AutocompletePrediction> predictionList;
    public Location lastKnownLocation;
    public LocationCallback locationCallback;
    public MaterialSearchBar materialSearchBar;

    public TextView titleCardView;
    public TextView openingHoursCardView;
    public ImageView imageCardView;
    public TextView locationTextView;
    public TextView ratingTextView;
    private RatingBar ratingBar;
    private RequestQueue requestQueue;
    private TextView description;
    private ToggleButton favourite;
    private Bitmap bitmap;
    private Museum foundMuseum;
    private CollectionReference bucketCollectionReference;
    private FirebaseFirestore fireStoreDb;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_museum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        materialSearchBar = view.findViewById(R.id.searchBar);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        titleCardView = view.findViewById(R.id.titleCardView);
        openingHoursCardView = view.findViewById(R.id.openingHoursCardView);
        imageCardView = view.findViewById(R.id.imageCardView);
        locationTextView = view.findViewById(R.id.location);
        ratingTextView = view.findViewById(R.id.ratingTextView2);
        ratingBar = view.findViewById(R.id.ratingBarDetail);
        requestQueue = Volley.newRequestQueue(requireContext());
        description = view.findViewById(R.id.descriptionFindMuseum);
        favourite = view.findViewById(R.id.button_favorite_find);

        fireStoreDb = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        Places.initialize(view.getContext(), getResources().getString(R.string.maps_api_key));
        placesClient = Places.createClient(getContext());
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

            }

            @Override
            public void onButtonClicked(int buttonCode) {
                //Disable search when u click backbutton
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {

                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            // Finds autocompletepredictions of type ESTABLISHMENT based on each letter written
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        //Filter out what you want to search for. In this case i use ESTABLISHMENT because we want to find museums.
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();


                /**
                 * Send a findAutocompletePredictions request and listens to the response. If the task is successful then our
                 * List<AutocompletePrediction> predictionList is set to that response.
                 */
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                CharacterStyle s = new CharacterStyle() {
                                    @Override
                                    public void updateDrawState(TextPaint tp) {

                                    }
                                };

                                /**
                                 * Here we create a list which is to be filtered out even more. We use a for loop to go through
                                 * the first list with autocomplete predictions of type ESTABLISHMENT, then loop each response
                                 * with anohter for loop to see if the response has the placeType of "MUSEUM" before we add it
                                 * to the list. THerefore the user can only search for museums in the museum app.
                                 */
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);

                                    for (int j = 0; j < prediction.getPlaceTypes().size(); j++) {
                                        if (prediction.getPlaceTypes().get(j).name().equals("MUSEUM")) {
                                            suggestionsList.add(prediction.getFullText(null).toString());
                                        }
                                    }
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("enTag", "prediction unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**
         *  At this point we dont have the latitude and longitude. we only have a place id reference which the user has clicked on the search result
         * this needs to be sent to google places api and request it to return the latitude and longitude so we can find the actual address
         * and information regarding the address.
         */
        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                favourite.setChecked(false);

                if (position >= predictionList.size()) {
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
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeId = selectedPrediction.getPlaceId();

                //Here we write what information we are interested in.
                List<Place.Field> placeFields = Arrays.asList(
                        Place.Field.LAT_LNG,
                        Place.Field.OPENING_HOURS,
                        Place.Field.ADDRESS,
                        Place.Field.PHONE_NUMBER,
                        Place.Field.RATING,
                        Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.BUSINESS_STATUS,
                        Place.Field.PHOTO_METADATAS,
                        Place.Field.PRICE_LEVEL,
                        Place.Field.NAME,
                        Place.Field.USER_RATINGS_TOTAL,
                        Place.Field.WEBSITE_URI,
                        Place.Field.ID,
                        Place.Field.TYPES
                );

                //Builds a FetchPlaceRequest based on information about placeId and the placefield types of information specified in placeFields
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                /**
                 * Tries to fetchPlace with the fetchPleaceRequest information. If successful, then we try to get
                 * photo of the place. This method uses photometadata in a Fetchphotorequest so that we can get the
                 * bitmap value which can be interpreted by the imageView built in method "setImageBitmap(bitmap)
                 * Link to this doc: https://developers.google.com/places/android-sdk/photos
                 */
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        final Place place = fetchPlaceResponse.getPlace();
                        LatLng latLng = place.getLatLng();
                        if (latLng != null) {

                            try {
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
                                        bitmap = fetchPhotoResponse.getBitmap();
                                        imageCardView.setImageBitmap(bitmap);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        if (exception instanceof ApiException) {
                                            final ApiException apiException = (ApiException) exception;
                                            Log.e("TAG", "Place not found: " + exception.getMessage());
                                            final int statusCode = apiException.getStatusCode();
                                        }
                                    }
                                });

                            } catch (Exception e) {
                                Log.d("Photometadata", "Cant find photometadata");
                                imageCardView.setBackgroundResource(R.drawable.nophoto);
                            }

                            /**
                             * In the lines below we populate the different elements with information about the search result
                             * which the user sees when he has clicked on an autocompleted line in his search bar.
                             */

                            //Makes the favorite button visible when the user has a search result
                            favourite.setAlpha(1);

                            LocalDate date = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                date = LocalDate.now().minusDays(1);
                                date.getDayOfWeek();
                            }

                            try {
                                openingHoursCardView.setText(place.getOpeningHours().getWeekdayText().get(date.getDayOfWeek().getValue()));
                            } catch (Exception e) {
                                Log.d("Tag", "Could not find opening hours");
                                openingHoursCardView.setText("Openinghours not available");
                            }

                            try {
                                locationTextView.setText(place.getAddress());
                                titleCardView.setText(place.getName());

                                ratingBar.setNumStars(5);
                                ratingBar.setRating(place.getRating().floatValue());
                                ratingBar.setAlpha(1);
                                ratingTextView.setText(" / " + place.getRating().toString());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            try {
                                WikiJSONParser wikiJSONParser = new WikiJSONParser();
                                wikiJSONParser.parseWikiData(place.getName(), requestQueue, description, getContext());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            foundMuseum = new Museum(place.getName(), "no description", place.getAddress(), latLng.latitude, latLng.longitude,"Openingshours not available", "no_photo","0");
                            System.out.println("OPENINGHOURS FIND" + foundMuseum.getPlaceId());
                            foundMuseum.setPlaceId(place.getId());
                            foundMuseum.setLat(latLng.latitude);
                            foundMuseum.setLng(latLng.longitude);

                            if(place.getOpeningHours() != null) {
                                foundMuseum.setOpen(String.valueOf(place.getOpeningHours()));
                            }

                            if(place.getPhotoMetadatas() != null) {
                                foundMuseum.setPhoto(place.getPhotoMetadatas().get(0).zza());
                            }

                            if(place.getRating() != null) {
                                foundMuseum.setRating(String.valueOf(place.getRating()));
                            }

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            final String museumPlaceId = foundMuseum.getPlaceId();

                            bucketCollectionReference = fireStoreDb.collection("account").document(user.getUid()).collection("bucketList");

                            //Animation for the favorite button
                            final ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
                            scaleAnimation.setDuration(500);
                            BounceInterpolator bounceInterpolator = new BounceInterpolator();
                            scaleAnimation.setInterpolator(bounceInterpolator);

                            /**
                             * Listens to the favorite button. If the user checks the button then its added to the database with
                             * setCheckedBucketList method and passes inn true. If its unchecked it sends a false boolean, and if the
                             * document exists in the database, it will remove the document from the bucketlist in the database.
                             */
                            favourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                    //animation

                                    if (isChecked) {
                                        setCheckedBucketList(museumPlaceId, true);
                                        System.out.println("UNCHECKED");
                                    } else {
                                        setCheckedBucketList(museumPlaceId, false);
                                    }
                                    compoundButton.startAnimation(scaleAnimation);
                                }
                            });

                            checkIfMusuemExistsInBucketList(foundMuseum.getPlaceId());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
            }
        });
    }

    /**
     * Adds museum to our database or deletes it based on the boolean value passed.
     * @param museumId - the museum user has searched for
     * @param bool - checked or unchecked / true or false
     */
    private void setCheckedBucketList(final String museumId, final boolean bool) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()) {
                        foundMuseum = documentSnapshot.toObject(Museum.class);
                        foundMuseum.setFavorite(bool);
                        if (!foundMuseum.isFavorite()) {
                            bucketCollectionReference.document(museumId).delete();
                        }
                    } else {
                        bucketCollectionReference.document(museumId).set(foundMuseum);
                    }
                }
            }
        });
    }

    /**
     * This method is used to recognize if the museum you are searching for has been checked before by
     * doing a search in our database to see if its there or not.
     * @param museumId - the museum searched for
     */
    private void checkIfMusuemExistsInBucketList(final String museumId) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()) {
                        favourite.setChecked(true);
                        System.out.println("DOCUMENT EXISTS IN BUCKETLIST");
                    } else {
                        System.out.println("Document doesn't exist");
                    }
                }
            }
        });
    }
}
