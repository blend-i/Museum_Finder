package no.hiof.museum_finder.adapter3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MuseumRecyclerAdapterApi extends RecyclerView.Adapter<MuseumRecyclerAdapterApi.MuseumViewHolderApi> {
    private static final String TAG = MuseumRecyclerAdapterApi.class.getSimpleName();

    private List<Museum> museumList;
    private LayoutInflater inflater;
    public View.OnClickListener clickListener;
    private CardViewClickManager cardViewClickManager;

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public MuseumRecyclerAdapterApi(Context context, List<Museum> museumList, CardViewClickManager cardViewClickManager) {
        //Lager en inflater basert på den konteksten man er i
        this.inflater = LayoutInflater.from(context);
        this.museumList = museumList;
        this.cardViewClickManager = cardViewClickManager;
    }

    @NonNull
    @Override
    public MuseumViewHolderApi onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        Log.d(TAG, "onCreateViewHolder");
        View itemView = inflater.inflate(R.layout.museum_list_item, parent, false);
        return new MuseumViewHolderApi(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull MuseumViewHolderApi viewHolder, int position) {

        Museum museumToDisplay = museumList.get(position);
        Log.d(TAG, "onBindViewHolder" + museumToDisplay.getTitle() + " - " + position);

        viewHolder.setMuseum(museumToDisplay);
        viewHolder.itemView.setTransitionName(museumToDisplay.getPlaceId());
    }

    @Override
    public int getItemCount() {
        return museumList.size();
    }

    public class MuseumViewHolderApi extends RecyclerView.ViewHolder {
        private TextView thumbnailTextView;
        private ImageView thumbnailimageView;
        private TextView descriptionTextView;
        private Button thumbnailButton;
        private RatingBar ratingBar;
        private TextView distance;
        private RequestQueue requestQueue;
        private FusedLocationProviderClient fusedLocationProviderClient;
        private double originLat;
        private double originLng;

        public MuseumViewHolderApi(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            ratingBar = itemView.findViewById(R.id.ratingBarApi);
            distance = itemView.findViewById(R.id.distanceTextView);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(itemView.getContext());
            originLat = 0;
            originLng = 0;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewClick(getAdapterPosition(), v);
                }
            });
        }

        public void setMuseum(final Museum museumToDisplay) {
            requestQueue = Volley.newRequestQueue(itemView.getContext());

            String posterUrl = museumToDisplay.getPhoto();
            String rating = museumToDisplay.getRating();

            double lat = museumToDisplay.getLat();
            double lng = museumToDisplay.getLng();

            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=" + 400 +
                    "&photoreference=" + posterUrl +
                    "&key=" + itemView.getResources().getString(R.string.maps_api_key);

            //Suppressing here
            @SuppressLint("MissingPermission") Task<Location> task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    originLat = location.getLatitude();
                    originLng = location.getLongitude();

                    jsonParseAndDisplayDistanceInKm(originLat, originLng, lat, lng);
                }
            });

            //sets the title
            thumbnailTextView.setText(museumToDisplay.getTitle());

            //sets the photo of museum in cardview
            if (posterUrl != null && !posterUrl.equals("")) {
                Glide.with(thumbnailimageView.getContext())
                        .load(photoUrl)
                        .into(thumbnailimageView);
            }

            //sets if museum open or closed
            if (museumToDisplay.getOpen().equals("true")) {
                descriptionTextView.setText("Open");
            } else {
                descriptionTextView.setText("Closed");
            }

            ratingBar.setRating(Float.parseFloat(rating));
        }

        private void jsonParseAndDisplayDistanceInKm(double origin_lat, double origin_lng, double destination_lat, double destination_lng) {
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
