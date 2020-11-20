package no.hiof.museum_finder.adapter2;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import no.hiof.museum_finder.HomeFragmentDirections;
import no.hiof.museum_finder.R;
import no.hiof.museum_finder.model.Museum;

/*
Det som skjer her:

Vi har en konstruktør som tar imot context og en liste med dyr.
Fra context så lager vi en inflater. Denne inflateren bruker vi for å få et view basert på vår xml.
Det viewet bruker vi til å opprette new Viewholder slik at hver gang recyclerView finner ut at den trenger ny viewholder så sier den til adapteren "lag en ny viewholder til meg"
Da får viewholder en instant av lista i form av view og da kan hente ut de enkle viewene.


 */

public class BucketListRecyclerAdapter extends RecyclerView.Adapter<BucketListRecyclerAdapter.BucketListViewHolder> {

    private static final String TAG = BucketListRecyclerAdapter.class.getSimpleName();

    private List<Museum> museumList;
    private LayoutInflater inflater;
    public View.OnClickListener clickListener;

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public BucketListRecyclerAdapter(Context context, List<Museum> museumList) {
        //Lager en inflater basert på den konteksten man er i
        this.inflater = LayoutInflater.from(context);
        this.museumList = museumList;
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
        viewHolder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return museumList.size();
    }

    public class BucketListViewHolder extends RecyclerView.ViewHolder {

        private TextView thumbnailTextView;
        private ImageView thumbnailimageView;
        private TextView descriptionTextView;
        private Button thumbnailButton;


        public BucketListViewHolder(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);

        }

        public void setMuseum(final Museum museumToDisplay) {
            if(thumbnailTextView != null && descriptionTextView != null) {
                thumbnailTextView.setText(museumToDisplay.getTitle());
                descriptionTextView.setText(museumToDisplay.getDescription());
            }

            String posterUrl = museumToDisplay.getPosterUrl();

            if(posterUrl != null && !posterUrl.equals("")) {
                Glide.with(thumbnailimageView.getContext())
                        .load(posterUrl)
                        .into(thumbnailimageView);
            }
        }
    }
}
