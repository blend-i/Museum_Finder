package no.hiof.museum_finder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
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
import android.os.Build;
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
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;


import no.hiof.museum_finder.adapter3.MuseumRecyclerAdapterApi;
import no.hiof.museum_finder.model.Museum;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HomeFragmentApi extends Fragment implements ConnectivityManager.OnNetworkActiveListener {

    private final int PERMISSION_LOCATION_ID = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLong;
    private MuseumRecyclerAdapterApi museumAdapter;
    private RecyclerView recyclerView;
    private PlacesClient placesClient;
    private List<Museum> museumArrayList;
    private TextView distanceTextView;
    public Location lastKnownLocation;
    public LocationCallback locationCallback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_api, container, false);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.gpsEnabled) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            //Get active network info
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            //Check network status
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

                //EasyPermissions.requestPermissions(this, "No permission, please enable permission for location", PERMISSION_LOCATION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
                /*if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d("HAS NOT PERMISSION", "HAR PERMISSION");

                    getCurrentLocation();
                    Log.d("HAS PERMISSION", "HAR PERMISSION");
                } else {
                    EasyPermissions.requestPermissions(this, "Location permission needed to get museum listings", PERMISSION_LOCATION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
                    //getCurrentLocation();
                }*/

                getCurrentLocation();


            }
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
     * Method called if the app has the ACCESS_FINE_LOCATION.
     * Creates a Location task that tries to retrieve last location from the FusedLocationProviderClient,
     * which has a onSuccessListener, if the task is successful get latitude and longitude of the devices
     * last location, create a new instance of NearbyMuseumTask and pass a nearbysearch URL on execution of
     * the task (this to download museum data, parse the data and set markers on the map for every museum
     * within the specified radius of the device in use). Lastly retrieve the supportMapFragment, when the
     * map is ready set the maps ui settings (gestures and controls) and animate to the location based on
     * current latitude and current longitude (current location of the device).
     */
    //@SuppressLint("MissingPermission")
    //@SuppressWarnings("MissingPermission")
    //@AfterPermissionGranted(PERMISSION_LOCATION_ID)
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

                    if(locationResult != null && locationResult.getLocations().size() > 0) {
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

        } else if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){

                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setMessage(getResources().getString(R.string.location_off));
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        paramDialogInterface.dismiss();
                        requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_LOCATION_ID);

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
            requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_LOCATION_ID);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                Toast.makeText(getContext(), "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
                getCurrentLocation();

        System.out.println("GRANTRESULTS: " + grantResults);
        System.out.println("REQUESTCODE: " + requestCode);
        System.out.println("PERMISSIONS: " + permissions);
    }

    @Override
    public void onNetworkActive() {
        System.out.println("NETWORK ACTIVE");
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
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(strings[0]);
                mapList = nearbySearchJSONParser.parseResult(jsonObject);
                System.out.println("MAPLIST I MUSEUMDATA: " + mapList);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
                System.out.println("ERROR I MUSEUMDATA: ");
            }

            return mapList;
        }


        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            museumArrayList = new ArrayList<>();
            System.out.println("HASHMAPS: " + hashMaps);

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
            //recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Do some stuff
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            }

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                //Do some stuff
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        }

        @Override
        public void onCardViewClick(int position, View v) {

        }

        @Override
        public void onCardViewClick(int position, View v, String distance) {

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
            navigateToDetailFragment.setLat(String.valueOf(museumArrayList.get(position).getLat()));
            navigateToDetailFragment.setLng(String.valueOf(museumArrayList.get(position).getLng()));
            navigateToDetailFragment.setDistance(distance);
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
                addresses = geocoder.getFromLocation(lat, lng, 5); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
