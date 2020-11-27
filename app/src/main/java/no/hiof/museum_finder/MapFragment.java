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
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat, currentLong;
    private String URL1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        //

        //mapFragment = SupportMapFragment.newInstance();
        //getChildFragmentManager().beginTransaction().setReorderingAllowed(true).add(R.id.map, mapFragment, "tag").commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        Log.d("onCreateView", "I onCreateView");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());


        if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            getCurrentLocation();
            Log.d("HAS PERMISSION", "HAR PERMISSION");
        } else {
            Log.d("HAS NOT PERMISSION", "HAR PERMISSION");
            getCurrentLocation();
            EasyPermissions.requestPermissions(this, "Access fine location needed to get my location", PERMISSION_LOCATION_ID, Manifest.permission.ACCESS_FINE_LOCATION);
        }
        Log.d("onCreate", "I onCreate");

        String placeType = "museum";

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + "?location=" +
                currentLat + "," + currentLong + "&radius=5000" + "&types=museum" + "AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc";

        System.out.println("CURRENT LOCATION" + currentLat + "," +currentLong);

        return view;
    }


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

                    new NearbyMuseumTask().execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLat + "," + currentLong + "&radius=5000&type=museum&key=" + getResources().getString(R.string.maps_api_key));

                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;

                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    //new LatLng(currentLat, currentLong), 10
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
            new MuseumParserTask().execute(museumData);
        }
    }

    private String downloadUrl(String downloadUrl) throws IOException {
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

    private class MuseumParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();

            List<HashMap<String, String>> mapList = null;
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(jsonObject);
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
                double lat = Double.parseDouble(hashMapList.get("lat"));
                double lng = Double.parseDouble(hashMapList.get("lng"));
                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat,lng);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);
            }
        }
    }
}
