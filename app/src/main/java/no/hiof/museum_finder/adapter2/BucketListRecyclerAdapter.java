package no.hiof.museum_finder.adapter2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import no.hiof.museum_finder.CardViewClickManager;
import no.hiof.museum_finder.R;
import no.hiof.museum_finder.model.Museum;


public class BucketListRecyclerAdapter extends RecyclerView.Adapter<BucketListRecyclerAdapter.BucketListViewHolder> {

    private static final String TAG = BucketListRecyclerAdapter.class.getSimpleName();

    private List<Museum> museumList;
    private LayoutInflater inflater;
    public View.OnClickListener clickListener;
    private CardViewClickManager cardViewClickManager;

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public BucketListRecyclerAdapter(Context context, List<Museum> museumList, CardViewClickManager cardViewClickManager) {
        //Lager en inflater basert på den konteksten man er i
        this.inflater = LayoutInflater.from(context);
        this.museumList = museumList;
        this.cardViewClickManager = cardViewClickManager;
    }

    @NonNull
    @Override
    public BucketListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        Log.d(TAG, "onCreateViewHolder");
        View itemView = inflater.inflate(R.layout.museum_list_item, parent, false);

        return new BucketListViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull BucketListViewHolder viewHolder, int position) {

        Museum museumToDisplay = museumList.get(position);
        Log.d(TAG, "onBindViewHolder" + museumToDisplay.getTitle() + " - " + position);


        viewHolder.setMuseum(museumToDisplay);
        //.itemView.setOnClickListener(clickListener);
        viewHolder.itemView.setTransitionName(museumToDisplay.getPlaceId());
    }

    @Override
    public int getItemCount() {
        return museumList.size();
    }

    public class BucketListViewHolder extends RecyclerView.ViewHolder {

        private final TextView thumbnailTextView;
        private final ImageView thumbnailimageView;
        private final TextView openingHoursTextView;
        private final RatingBar ratingBar;
        private TextView distance;
        private RequestQueue requestQueue;
        private FusedLocationProviderClient fusedLocationProviderClient;
        private double originLat;
        private double originLng;
        private double lat;
        private double lng;

        //Suppressing here
        @SuppressLint("MissingPermission") Task<Location> task;


        @SuppressLint("MissingPermission")
        public BucketListViewHolder(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            openingHoursTextView = itemView.findViewById(R.id.descriptionTextView);
            ratingBar = itemView.findViewById(R.id.ratingBarApi);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(itemView.getContext());
            distance = itemView.findViewById(R.id.distanceTextView);

            originLat = 0;
            originLng = 0;
            requestQueue = Volley.newRequestQueue(itemView.getContext());
            task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    originLat = location.getLatitude();
                    originLng = location.getLongitude();
                    jsonParseAndDisplayDistanceInKm(originLat,originLng,lat,lng);
                }
            });


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewClick(getAdapterPosition(), v, distance.getText().toString());
                }
            });

        }

        public void setMuseum(final Museum museumToDisplay) {

            lat = museumToDisplay.getLat();
            lng = museumToDisplay.getLng();

            System.out.println("LAT I SETMUSEUM: " + lat);
            System.out.println("LNG I SETMUSEUM: " + lng);

            if(thumbnailTextView != null) {
                thumbnailTextView.setText(museumToDisplay.getTitle());
            }

            //sets if museum open or closed
            if (museumToDisplay.getOpen().equals("true")) {
                openingHoursTextView.setText("Open");
            } else {
                openingHoursTextView.setText("Closed");
            }


            String posterUrl = museumToDisplay.getPhoto();

            String url = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=" + 400 +
                    "&photoreference=" + posterUrl +
                    "&key=AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc";


            if(posterUrl != null && !posterUrl.equals("")) {
                Glide.with(thumbnailimageView.getContext())
                        .load(url)
                        .into(thumbnailimageView);
            }
            ratingBar.setRating(Float.parseFloat(museumToDisplay.getRating()));
        }

        private void jsonParseAndDisplayDistanceInKm(double origin_lat, double origin_lng, double destination_lat, double destination_lng) {
            System.out.println("ORIGIN LAT: " + origin_lat + " " + origin_lng);
            System.out.println("DEST LAT: " + destination_lat + " " + destination_lng);
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="+origin_lat+","+origin_lng+"&destinations="+ destination_lat + "," + destination_lng+"&key="+itemView.getResources().getString(R.string.maps_api_key);

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
}
