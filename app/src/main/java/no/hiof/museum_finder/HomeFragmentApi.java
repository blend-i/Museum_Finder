package no.hiof.museum_finder;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.transition.MaterialElevationScale;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import no.hiof.museum_finder.adapter3.MuseumRecyclerAdapterApi;
import no.hiof.museum_finder.model.Museum;

/**
 * This class represents the Home screen in the application. It works as the head which connects
 * the adapter and parser classes together to host the recyclerview with information about the museums.
 */
public class HomeFragmentApi extends Fragment {

    private final int PERMISSION_LOCATION_ID = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private MuseumRecyclerAdapterApi museumAdapter;
    private RecyclerView recyclerView;
    private PlacesClient placesClient;
    private List<Museum> museumArrayList;
    private TextView distanceTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_api, container, false);
        return view;
    }

    /**
     * When view is created we check to see if GPS is enabled on the phone. If thats true, we check if the phone has internet. If the user
     * has internet and location settings on, we will try to get user current location.
     * If the user turns off location on phone, an AlertDialog will pop up asking the user to enable their location. If the user
     * clicks "Go to settnings" button we open new intent which takes user to location settings. If internet is not enabled
     * we also open up an AlertDialog which ask the user to turn their internet on to use the application.
     *
     * @param view               - HomeFragment xml file
     * @param savedInstanceState - Savedinstance
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.gpsEnabled) {

            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            /**
             * Checks status of network and pops AlertDialog if user is not connected to the internet.
             * If network is on then we initialize fusedLocationProviderClient and places api and try to
             * get user current location
             */
            if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.no_internet_dialog);

                dialog.setCanceledOnTouchOutside(false);

                dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

                Button tryAgainButton = dialog.findViewById(R.id.tryAgainButton);
                tryAgainButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.findNavController(requireView()).navigate(HomeFragmentApiDirections.actionHomeFragmentApiSelf());
                        dialog.cancel();
                    }
                });
                dialog.show();
            } else {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
                Places.initialize(view.getContext(), getResources().getString(R.string.maps_api_key));
                placesClient = Places.createClient(requireContext());
                getCurrentLocation();
            }

            /**
             * gpsEnabled is a static variable in MainActivity which listens to BroadcastReciever in the
             * GPSBroadcastReciever class. The GPSBroadcastReciever class listens to if the user turns their location
             * on or off and the variable in MainActivity is decided by this. If !gpsEnabled then we create an intent
             * and send user to location settings in phone if the user clicks "Go to settings" button.
             */
        } else if (!MainActivity.gpsEnabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(getResources().getString(R.string.location_off));
            dialog.setPositiveButton(getResources().getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    paramDialogInterface.dismiss();

                    LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    try {
                        MainActivity.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.cancel();
                }
            });
            dialog.show();
        }

        distanceTextView = view.findViewById(R.id.distanceTextView);
    }

    /**
     * Checks if the user has given our application permission to use location with checking the state of ACCESS_FINE_LOCATION
     * If permission is granted we do a LocationRequest with max 1 per 5 seconds location calls and update users location.
     * With this location and the radius which is default 50000 or decided by the user we create an URL with users current
     * location, radius, museum placetype and send our API key for the request to the inner class NearbyMuseumTask.
     */
    private void getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest currentLocation = new LocationRequest();
            currentLocation.setInterval(5000);
            currentLocation.setFastestInterval(2000);
            currentLocation.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationProviderClient.requestLocationUpdates(currentLocation, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    fusedLocationProviderClient.removeLocationUpdates(this);

                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                        int latestCurrentLocation = locationResult.getLocations().size() - 1;

                        double currentLat = locationResult.getLocations().get(latestCurrentLocation).getLatitude();
                        double currentLong = locationResult.getLocations().get(latestCurrentLocation).getLongitude();

                        String placeType = "museum";
                        int radius = ProfileFragment.getRadius();
                        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                                "?location=" + currentLat + "," + currentLong +
                                "&radius=" + radius +
                                "&type=" + placeType +
                                "&key=" + getResources().getString(R.string.maps_api_key);

                        new NearbyMuseumTask().execute(url);
                    }
                }
            }, Looper.getMainLooper());
            Log.d("HAS PERMISSION", "HAR PERMISSION");

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(getResources().getString(R.string.location_off));
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.dismiss();
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_ID);

                }
            });
            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    paramDialogInterface.cancel();
                }
            });
            dialog.show();

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_ID);
        }
    }

    /**
     * Checks if the requestcode is the same as PERMISSION_LOCATION_ID then we get the user location if the user
     * clicks allow
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(getContext(), requestCode + " " + PERMISSION_LOCATION_ID, Toast.LENGTH_SHORT).show();
        if (requestCode == PERMISSION_LOCATION_ID) {
            getCurrentLocation();
        }
    }

    /**
     * Local class NearbyMuseumTask that inherits from AsyncTask to handle the download
     * of the museum data that will be retrieved through the downloadUrl method, this happens
     * in the background and passes the first string from AsyncTasks params to the downloadUrl
     * method, and returns the museumData. On post execution create a new instance of
     * MuseumDataParserTask to execute the parsing operation on the museumData retrieved.
     */
    private class NearbyMuseumTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String museumData = null;

            try {
                museumData = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return museumData;
        }

        @Override
        protected void onPostExecute(String museumData) {
            new MuseumDataParserTask().execute(museumData);

        }
    }

    /**
     * Method that handles the download of the data based on the URL passed in. In the process
     * it initializes a HttpURLConnection on the URL passed in to the method, gets the input stream
     * and passes it to a buffered reader and appends data to a string builder while the buffered
     * reader has lines to read.
     *
     * @param downloadUrl - nearby places url to be downloaded
     * @return - data retrieved from the download
     * @throws IOException - if download fails
     */
    private String downloadUrl(String downloadUrl) throws IOException {
        URL url = new URL(downloadUrl);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.connect();

        InputStream urlInputStream = httpURLConnection.getInputStream();
        BufferedReader museumDataReader = new BufferedReader(new InputStreamReader(urlInputStream));

        StringBuilder museumDataBuilder = new StringBuilder();
        String line = "";

        while ((line = museumDataReader.readLine()) != null) {
            museumDataBuilder.append(line);
        }

        String museumData = museumDataBuilder.toString();
        museumDataReader.close();
        return museumData;
    }

    /**
     * Local class MuseumDataParserTask inherits from AsyncTask to handle JSON parsing
     * (utilizes the JsonParser class) of the museum data in the background (The data retrieved in method donwloadUrl)
     */
    private class MuseumDataParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> implements CardViewClickManager {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            NearbySearchJSONParserHome nearbySearchJSONParserHome = new NearbySearchJSONParserHome();
            List<HashMap<String, String>> mapList = null;
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(strings[0]);
                mapList = nearbySearchJSONParserHome.parseResult(jsonObject);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

            return mapList;
        }

        /**
         * initialize an ArrayList
         * loop over museumdata and get the different variables of the museum
         * to create a museum object which then is added to museumArrayList.
         * After the loop, we initiaslize recyclerView and museumAdapter with museumArrayList and set
         * this adapter on the recyclerView
         *
         * @param hashMaps
         */
        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            museumArrayList = new ArrayList<>();

            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMapList = hashMaps.get(i);
                //WikiJSONParser wikiJSONParser = new WikiJSONParser();
                //String description = hashMapList.get("openHours");

                try {
                    String open = hashMapList.get("open");
                    String name = hashMapList.get("name");
                    String photo = hashMapList.get("photo");
                    String placeId = hashMapList.get("placeId");
                    String rating = hashMapList.get("rating");
                    double lat = Double.parseDouble(hashMapList.get("lat"));
                    double lng = Double.parseDouble(hashMapList.get("lng"));
                    museumArrayList.add(new Museum(name, open, photo, rating, placeId, lat, lng));

                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            recyclerView = getView().findViewById(R.id.museumRecyclerViewApi);
            museumAdapter = new MuseumRecyclerAdapterApi(getContext(), museumArrayList, this);
            recyclerView.setAdapter(museumAdapter);

            /**
             * If user device is horizontal we show a gridview with spancount of 2.
             */
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            }

            /**
             * If user device is vertical we show a list with a linear layout.
             */
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        }


        /**
         * Interface method which is implemented by MuseumDataParserTask class. Here we override the
         * onCardViewCLick method which is also implemented in MuseumRecyclerAdapter where it passes location of museum,
         * view and distance. This method activates when user clicks a cardview. It has MaterialElevationScale which creates an
         * animation while switching to MuseumDetailFragment. When navigating to this fragment we use safeargs arguments from navgraph
         * and specify their value. Then we pass this information to MuseumDetailFragment and navigate there.
         *
         * @param position - location of the museum (lat lng)
         * @param v        - the spesific cardview of the museum in recyclerview
         * @param distance - distance between userlocation and museum
         */
        @Override
        public void onCardViewClick(int position, View v, String distance) {

            String location = reverseGeoCode(museumArrayList.get(position).getLat(), museumArrayList.get(position).getLng());

            MaterialElevationScale exitTransition = new MaterialElevationScale(false);
            exitTransition.setDuration(300);

            MaterialElevationScale reenterTransition = new MaterialElevationScale(true);
            reenterTransition.setDuration(300);

            String museumCardDetailTransitionName = getString(R.string.museum_card_detail_transition_name);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder().addSharedElement(v, museumCardDetailTransitionName).build();
            HomeFragmentApiDirections.ActionHomeFragmentApiToMuseumDetail navigateToDetailFragment = HomeFragmentApiDirections.actionHomeFragmentApiToMuseumDetail();
            navigateToDetailFragment.setPlaceId(museumArrayList.get(position).getPlaceId());
            navigateToDetailFragment.setOpeningHours(museumArrayList.get(position).getOpen());
            navigateToDetailFragment.setPhotoUrl(museumArrayList.get(position).getPhoto());
            navigateToDetailFragment.setRating(museumArrayList.get(position).getRating());
            navigateToDetailFragment.setTitle(museumArrayList.get(position).getTitle());
            navigateToDetailFragment.setLat(String.valueOf(museumArrayList.get(position).getLat()));
            navigateToDetailFragment.setLng(String.valueOf(museumArrayList.get(position).getLng()));
            navigateToDetailFragment.setDistance(distance);
            navigateToDetailFragment.setLocation(location);
            Navigation.findNavController(requireView()).navigate(navigateToDetailFragment, extras);
            setExitTransition(exitTransition);
            setReenterTransition(reenterTransition);
        }

        /**
         * This method uses lat and lng to find a specific address which is used by onCardViewClick method
         * to find an address and pass it to MuseumDetailFragment. Geocoder generated address(es) based on
         * lat and lng. We use the List of type address to equal this and return the first index.
         *
         * @param lat - latitude of the museum
         * @param lng - longitude of the museum
         * @return - adress of the museum
         */
        public String reverseGeoCode(double lat, double lng) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(lat, lng, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses.get(0).getAddressLine(0);
        }
    }
}
