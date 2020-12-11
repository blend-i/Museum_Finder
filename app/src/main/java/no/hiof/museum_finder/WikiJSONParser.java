package no.hiof.museum_finder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;


public class WikiJSONParser {

    String currentCountry;

    /**
     * Methoad that replaces white space in a string with underscores
     * @param title - The museum title to be parsed
     * @return - The parsed title
     */
    private String parseTitle(String title) {
        String name;
        String name2;

        if(title.contains("&")) {
            name = title.replace("&", "and");
            name = name.replace(" ", "_");
            System.out.println(name);
        } else {
            name = title.replace(" ", "_");
        }

        return name;
    }

    /**
     * A method that utilizes Volley to parse data from a WikiMedia response based on a Museum title.
     * Firstly is parses the title to fit the WikiMedia request URL (Munch musem = Munch_museum), sets up
     * a JsonObjectRequest (Volley) and sets request method to GET. OnResponse (if the request has response)
     * parse the data to retrieve the intro section for the requested WikiPedia page.
     * @param title - Museum title to be parsed
     * @param requestQueue - The RequestQueue where the request should be performed
     * @param description - The TextView to display the parsed data (Museum description)
     */
    public void parseWikiData(String title, RequestQueue requestQueue, TextView description, Context context) {
        String urlTitle = parseTitle(title);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        @SuppressLint("MissingPermission")
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                double currentLat = location.getLatitude();
                double currentLng = location.getLongitude();

                currentCountry = reverseGeoCode(currentLat, currentLng, context);

                System.out.println("LANDSKODE: " + currentCountry);

                if(currentCountry.equals("NO")) {
                    currentCountry = "no";
                } else {
                    currentCountry = "en";
                }


                System.out.println("ANNA" + currentCountry);

                String downloadUrl = "https://" + currentCountry + ".wikipedia.org/w/api.php?action=query&format=json&titles=" + urlTitle + "&prop=extracts&exintro&explaintext";

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, downloadUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject wikidata = response.getJSONObject("query").getJSONObject("pages");
                            Iterator<String> keys = wikidata.keys();
                            String key = "";

                            while(keys.hasNext()) {
                                key = keys.next();
                            }

                            String intro = wikidata.getJSONObject(key).getString("extract");

                            description.setText(!intro.isEmpty() ? intro : "The retrieved description is empty");

                        } catch (JSONException e) {
                            e.printStackTrace();
                            description.setText(R.string.no_description);
                            System.out.println("ERROR I ADAPTER");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                requestQueue.add(request);

            }
        });
    }

    public String reverseGeoCode(double lat, double lng, Context context) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        //double latitude = Double.parseDouble(lat);
        //double longitude = Double.parseDouble(lng);
        //using latitude and longitude from last location to pinpoint address
        try {
            addresses = geocoder.getFromLocation(lat, lng, 5); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addresses.get(0).getCountryCode();
    }




}
