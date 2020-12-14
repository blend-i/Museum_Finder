package no.hiof.museum_finder.adapter3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

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
import no.hiof.museum_finder.DistanceJsonParser;
import no.hiof.museum_finder.R;
import no.hiof.museum_finder.model.Museum;

/**
 * Adapterclass which has the job to dynamically create and bind objects to the recyclerview when the user is scrolling.
 */
public class MuseumRecyclerAdapterApi extends RecyclerView.Adapter<MuseumRecyclerAdapterApi.MuseumViewHolderApi> {
    private static final String TAG = MuseumRecyclerAdapterApi.class.getSimpleName();
    private List<Museum> museumList;
    private LayoutInflater inflater;
    public View.OnClickListener clickListener;
    private CardViewClickManager cardViewClickManager;

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Constructor for the recycler adapter which is set on the recyclerview.
     *
     * @param context              - creates an inflater based on the context of the fragment your on.
     * @param museumList           - list of museums to be shown
     * @param cardViewClickManager - clicklistener interface on a recyclerview list item
     */
    public MuseumRecyclerAdapterApi(Context context, List<Museum> museumList, CardViewClickManager cardViewClickManager) {
        this.inflater = LayoutInflater.from(context);
        this.museumList = museumList;
        this.cardViewClickManager = cardViewClickManager;
    }

    /**
     * Inflates the museum list objects as a card in the recyclerview with the museum_list_item.xml file
     *
     * @param parent   - recyclerview
     * @param position - position of the museum
     * @return - museumobject itemview
     */
    @NonNull
    @Override
    public MuseumViewHolderApi onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View itemView = inflater.inflate(R.layout.museum_list_item, parent, false);
        return new MuseumViewHolderApi(itemView);
    }

    /**
     * Binds the itemview created to the viewholder dynamically and sets their spesific information
     *
     * @param viewHolder - holds the different museums
     * @param position   - keeps track of the position of each museum
     */
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
        private TextView openingHoursTextView;
        private Button thumbnailButton;
        private RatingBar ratingBar;
        private TextView distance;
        private RequestQueue requestQueue;
        private FusedLocationProviderClient fusedLocationProviderClient;
        private double originLat;
        private double originLng;
        private double lat;
        private double lng;

        //Suppressing here
        @SuppressLint("MissingPermission")
        Task<Location> task;


        /**
         * Initialize varius variables and fusedProviderClient task to get current lat and lng of the user
         * and pass it in the DistanceJsonParser().jsonParseAndDisplayDistanceInKm method, together with museum lat, lng, distance textview,
         * requestQueue and api key to use google Distance Matrix Api which calculates the distance.
         *
         * @param itemView - museum list item
         */
        @SuppressLint("MissingPermission")
        public MuseumViewHolderApi(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            openingHoursTextView = itemView.findViewById(R.id.descriptionTextView);
            ratingBar = itemView.findViewById(R.id.ratingBarApi);
            distance = itemView.findViewById(R.id.distanceTextView);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(itemView.getContext());
            originLat = 0;
            originLng = 0;
            requestQueue = Volley.newRequestQueue(itemView.getContext());
            task = fusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        originLat = location.getLatitude();
                        originLng = location.getLongitude();
                        new DistanceJsonParser().jsonParseAndDisplayDistanceInKm(originLat, originLng, lat, lng, distance, requestQueue, itemView.getResources().getString(R.string.maps_api_key));
                    }
                }
            });

            /**
             * Uses CardViewClickManager interface and listens for a click on the itemview card. When the user clicks the method is
             * activated and the paramteres are passed to the onCardViewClick which is used in HomeFragmentApi.
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewClick(getAdapterPosition(), v, distance.getText().toString());
                }
            });
        }

        /**
         * This method sets the different elements in the museum_list_item.xml file with information about
         * title, lat, lng, photo, rating and opening hours.
         * @param museumToDisplay - museumobject with information about the specific museum
         */
        public void setMuseum(final Museum museumToDisplay) {
            String posterUrl = museumToDisplay.getPhoto();
            String rating = museumToDisplay.getRating();
            String photoUrl;
            lat = museumToDisplay.getLat();
            lng = museumToDisplay.getLng();


            if (posterUrl.equals("0")) {
                photoUrl = "https://bloggersbaba.com/wp-content/uploads/2020/04/no-image-available.jpg";
            } else {
                photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                        "?maxwidth=" + 400 +
                        "&photoreference=" + posterUrl +
                        "&key=" + itemView.getResources().getString(R.string.maps_api_key);
            }

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
                openingHoursTextView.setText("Open");
                openingHoursTextView.setTextColor(Color.GREEN);
            } else {
                openingHoursTextView.setText("Closed");
                openingHoursTextView.setTextColor(Color.RED);
            }

            if (rating.equals("0")) {
                ratingBar.setAlpha(0);
            } else {
                ratingBar.setRating(Float.parseFloat(rating));
            }
        }
    }
}
