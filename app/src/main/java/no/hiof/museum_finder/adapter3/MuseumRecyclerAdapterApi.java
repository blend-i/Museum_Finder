package no.hiof.museum_finder.adapter3;

import android.content.Context;
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

import com.bumptech.glide.Glide;

import java.util.List;

import no.hiof.museum_finder.CardViewClickManager;
import no.hiof.museum_finder.R;
import no.hiof.museum_finder.model.Museum;

/*
Det som skjer her:

Vi har en konstruktør som tar imot context og en liste med dyr.
Fra context så lager vi en inflater. Denne inflateren bruker vi for å få et view basert på vår xml.
Det viewet bruker vi til å opprette new Viewholder slik at hver gang recyclerView finner ut at den trenger ny viewholder så sier den til adapteren "lag en ny viewholder til meg"
Da får viewholder en instant av lista i form av view og da kan hente ut de enkle viewene.

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
        System.out.println("MUSEUMID" + museumToDisplay.getUid());
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

        public MuseumViewHolderApi(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            ratingBar = itemView.findViewById(R.id.ratingBarApi);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewClick(getAdapterPosition(), v);
                }
            });
        }

        public void setMuseum(final Museum museumToDisplay) {
           /*
            if(thumbnailTextView != null && descriptionTextView != null) {
                thumbnailTextView.setText(museumToDisplay.getTitle());
                descriptionTextView.setText(museumToDisplay.getDescription());
            }
            */
            String posterUrl = museumToDisplay.getPhoto();

            String rating = museumToDisplay.getRating();

            String url = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=" + 400 +
                    "&photoreference=" + posterUrl +
                    "&key=AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc";

            System.out.println("POSTERURL FRA ADAPTER: " + posterUrl);

            //sets the title
            thumbnailTextView.setText(museumToDisplay.getTitle());

            //sets the photo of museum in cardview
            if(posterUrl != null && !posterUrl.equals("")) {
                Glide.with(thumbnailimageView.getContext())
                        .load(url)
                        .into(thumbnailimageView);
            }

            //sets if museum open or closed
            if(museumToDisplay.getOpen().equals("true")) {
                descriptionTextView.setText("Open");
            } else {
                descriptionTextView.setText("Closed");
            }

            ratingBar.setRating(Float.parseFloat(rating));

        }
    }
}
