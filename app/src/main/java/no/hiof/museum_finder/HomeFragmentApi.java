package no.hiof.museum_finder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeFragmentApi extends Fragment {

    private final int PERMISSION_LOCATION_ID = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLong;
    private MuseumRecyclerAdapterApi museumAdapter;
    private RecyclerView recyclerView;
    private PlacesClient placesClient;
    private List<Museum> museumArrayList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_api, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
        placesClient = Places.createClient(getContext());

        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurrentLocation();
            Log.d("HAS PERMISSION", "HAR PERMISSION");
        } else {
            Log.d("HAS NOT PERMISSION", "HAR PERMISSION");
            getCurrentLocation();
            EasyPermissions.requestPermissions(this, "Access fine location needed to get my location", PERMISSION_LOCATION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return view;
    }

    /**
     * Method called if the app has the ACCESS_FINE_LOCATION.
     * Creates a Location task that tries to retrieve last location from the FusedLocationProviderClient,
     * which has a onSuccessListener, if the task is successful get latitude and longitude of the devices
     * last location, create a new instance of NearbyMuseumTask and pass a nearbysearch URL on execution of
     * the task (this to download museum data, parse the data and set markers on the map for every museum
     * within the specified radius of the device in use). Lastly retrieve the supportMapFragment, when the
     * map is ready set the maps ui settings (gestures and controls) and animate to the location based on
     * current latitude and current longitude (current location of the device).
     */
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(PERMISSION_LOCATION_ID)
    private void getCurrentLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();

                    String placeType = "museum";
                    int radius = 50000;
                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?location=" + currentLat + "," + currentLong +
                            "&radius=" + radius +
                            "&type=" + placeType +
                            "&key=AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc"; //+ getResources().getString(R.string.maps_api_key);

                    new NearbyMuseumTask().execute(url);
                }
            }
        });
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


    private class MuseumDataParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> implements CardViewClickManager {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            NearbySearchJSONParser2 nearbySearchJSONParser = new NearbySearchJSONParser2();

            List<HashMap<String, String>> mapList = null;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                mapList = nearbySearchJSONParser.parseResult(jsonObject);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }

            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //map.clear();

            museumArrayList = new ArrayList<>();

            System.out.println("HASHMAPS: " + hashMaps);

            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMapList = hashMaps.get(i);
                //String description = hashMapList.get("openHours");

                String open = hashMapList.get("open");
                String name = hashMapList.get("name");
                String photo = hashMapList.get("photo");
                String placeId = hashMapList.get("placeId");
                String rating = hashMapList.get("rating");

                try {
                    double lat = Double.parseDouble(hashMapList.get("lat"));
                    double lng = Double.parseDouble(hashMapList.get("lng"));
                    museumArrayList.add(new Museum(name, open, photo, rating,  placeId, lat, lng));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            recyclerView = getView().findViewById(R.id.museumRecyclerViewApi);
            museumAdapter = new MuseumRecyclerAdapterApi(getContext(), museumArrayList, this);
            recyclerView.setAdapter(museumAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }



        @Override
        public void onCardViewClick(int position, View v) {
            String location = reverseGeoCode(museumArrayList.get(position).getLat(), museumArrayList.get(position).getLng());

            MaterialElevationScale exitTransition = new MaterialElevationScale(false);
            exitTransition.setDuration(300);

            MaterialElevationScale reenterTransition = new MaterialElevationScale(true);
            reenterTransition.setDuration(300);

            String museumCardDetailTransitionName = getString(R.string.museum_card_detail_transition_name);
            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder().addSharedElement(v, museumCardDetailTransitionName).build();
            //System.out.println(museumList.get(position).getTitle());
            HomeFragmentApiDirections.ActionHomeFragmentApiToMuseumDetail navigateToDetailFragment = HomeFragmentApiDirections.actionHomeFragmentApiToMuseumDetail();
            navigateToDetailFragment.setPlaceId(museumArrayList.get(position).getPlaceId());
            navigateToDetailFragment.setOpeningHours(museumArrayList.get(position).getOpen());
            navigateToDetailFragment.setPhotoUrl(museumArrayList.get(position).getPhoto());
            navigateToDetailFragment.setRating(museumArrayList.get(position).getRating());
            navigateToDetailFragment.setTitle(museumArrayList.get(position).getTitle());
            navigateToDetailFragment.setLocation(location);
            Navigation.findNavController(requireView()).navigate(navigateToDetailFragment, extras);
            setExitTransition(exitTransition);
            setReenterTransition(reenterTransition);
        }

        public String reverseGeoCode(double lat, double lng) {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(getContext(), Locale.getDefault());

            //double latitude = Double.parseDouble(lat);
            //double longitude = Double.parseDouble(lng);
            //using latitude and longitude from last location to pinpoint address
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses.get(0).getAddressLine(0);
        }

        @Override
        public void onCardViewToggleButtonCheckedChanged(int position, ToggleButton favourite, boolean isChecked) {

        }
    }
}
