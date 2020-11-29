package no.hiof.museum_finder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MapFragment extends Fragment {

    private final int PERMISSION_LOCATION_ID = 1;
    private SupportMapFragment mapFragment;
    private static GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLong;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());


        if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
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
                if(location != null) {
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();

                    String placeType = "museum";
                    int radius = 50000;
                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                            "?location=" + currentLat + "," + currentLong +
                            "&radius=" + radius +
                            "&type=" + placeType +
                            "&key=" + getResources().getString(R.string.maps_api_key);

                    new NearbyMuseumTask().execute(url);

                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            setMapUISettings();

                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(currentLat,currentLong), 12
                            ));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    /**
     * Local class NearbyMuseumTask that inherits from AsyncTask to handle the download
     * of the museum data that will be retrieved through the downloadUrl method, this happens
     * in the background and passes the first string from AsyncTasks params to the downloadUrl
     * method, and returns the museumData. On post execution create a new instance of
     * MuseumDataParserTask to execute the parsing operation on the museumData retrieved.
     */
    private static class NearbyMuseumTask extends AsyncTask<String, Integer, String> {
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
     * @param downloadUrl - nearby places url to be downloaded
     * @return - data retrieved from the download
     * @throws IOException - if download fails
     */
    private static String downloadUrl(String downloadUrl) throws IOException {
        URL url = new URL(downloadUrl);

        HttpURLConnection httpURLConnection= (HttpURLConnection) url.openConnection();
        httpURLConnection.connect();

        InputStream urlInputStream =  httpURLConnection.getInputStream();
        BufferedReader museumDataReader = new BufferedReader(new InputStreamReader(urlInputStream));

        StringBuilder museumDataBuilder = new StringBuilder();
        String line = "";
        
        while( (line = museumDataReader.readLine()) != null ) {
            museumDataBuilder.append(line);
        }

        String museumData = museumDataBuilder.toString();
        museumDataReader.close();

        return  museumData;
    }

    /**
     * Local class MuseumDataParserTask inherits from AsyncTask to handle JSON parsing
     * (utilizes the JsonParser class) of the museum data in the background
     * (The data retrieved in method donwloadUrl) on post execution: clear the map, loop
     * over the museum data, for every museum add a marker to the map based on the location (LatLng),
     * and set the title of the the marker = name of the museum, a snippet that shows if the museum
     * is currently open / closed and set the marker icon like
     * (ic_museum_marker from drawable resources).
     */
    private static class MuseumDataParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            NearbySearchJSONParser nearbySearchJSONParser = new NearbySearchJSONParser();

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
            map.clear();

            for(int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMapList = hashMaps.get(i);

                try {
                    double lat = Double.parseDouble(hashMapList.get("lat"));
                    double lng = Double.parseDouble(hashMapList.get("lng"));
                    String openNow = hashMapList.get("openNow");
                    String name = hashMapList.get("name");
                    LatLng latLng = new LatLng(lat,lng);

                    if(openNow == "true") {
                        openNow = "open";
                    } else {
                        openNow = "closed";
                    }

                    MarkerOptions options = new MarkerOptions();
                    options.position(latLng);
                    options.title(name);
                    options.snippet("currently: " + openNow);
                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_museum_marker3));
                    map.addMarker(options);

                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                }


            }
        }
    }

    /**
     * Method to enable gestures and controls in Google Maps
     * Gestures: enabled all gestures enabled (zoom, scroll, tilt, rotate)
     * Controls: enabled Zoom, Compass and MyLocation controls
     */
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(PERMISSION_LOCATION_ID)
    private void setMapUISettings() {
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);

        map.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {

            }
        });

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return false;
            }
        });

        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION))
            map.setMyLocationEnabled(true);
        else
            EasyPermissions.requestPermissions(this, "Access fine location needed to get my location", PERMISSION_LOCATION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
    }


}
