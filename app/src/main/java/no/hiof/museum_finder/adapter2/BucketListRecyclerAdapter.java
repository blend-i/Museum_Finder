package no.hiof.museum_finder.adapter2;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        private final TextView descriptionTextView;
        private final RatingBar ratingBar;


        public BucketListViewHolder(@NonNull final View itemView) {
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
            try {
                String rating = museumToDisplay.getRating();
                ratingBar.setRating(Float.parseFloat(rating));
            } catch (NullPointerException exception) {
                exception.printStackTrace();
            }

            if(thumbnailTextView != null && descriptionTextView != null) {
                thumbnailTextView.setText(museumToDisplay.getTitle());
                descriptionTextView.setText(museumToDisplay.getDescription());
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
        }
    }
}
