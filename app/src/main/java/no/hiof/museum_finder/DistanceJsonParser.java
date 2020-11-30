package no.hiof.museum_finder;

import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DistanceJsonParser {
    public void jsonParseAndDisplayDistanceInKm(double origin_lat, double origin_lng, double destination_lat, double destination_lng, TextView distance, RequestQueue requestQueue, String apiKey) {
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+origin_lat+","+origin_lng+"&destinations="+ destination_lat + "," + destination_lng+"&key="+apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("rows");
                    JSONObject elements = jsonArray.getJSONObject(0);
                    JSONArray elementsArray = elements.getJSONArray("elements");
                    JSONObject distanceAndDuration = elementsArray.getJSONObject(0);
                    String meters = distanceAndDuration.getJSONObject("distance").getString("value");


                    distance.setText((Integer.parseInt(meters) / 1000) + " km away");

                } catch (JSONException e) {
                    e.printStackTrace();
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
}