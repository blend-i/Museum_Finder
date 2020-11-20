package no.hiof.museum_finder.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
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

public class MuseumRecyclerAdapter extends RecyclerView.Adapter<MuseumRecyclerAdapter.MuseumViewHolder> {
    private static final String TAG = no.hiof.museum_finder.adapter.MuseumRecyclerAdapter.class.getSimpleName();

    private List<Museum> museumList;
    private LayoutInflater inflater;
    private CardViewClickManager cardViewClickManager;
    private ToggleButton favouriteButton;
    private View.OnClickListener clickListener;

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public MuseumRecyclerAdapter (Context context, List<Museum> museumList, CardViewClickManager cardViewClickManager) {
        //Lager en inflater basert på den konteksten man er i
        this.inflater = LayoutInflater.from(context);
        this.museumList = museumList;
        this.cardViewClickManager = cardViewClickManager;
    }

    @NonNull
    @Override
    public MuseumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        Log.d(TAG, "onCreateViewHolder");
        View itemView = inflater.inflate(R.layout.museum_list_item, parent, false);

        return new MuseumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MuseumViewHolder viewHolder, int position) {

        Museum museumToDisplay = museumList.get(position);
        Log.d(TAG, "onBindViewHolder" + museumToDisplay.getTitle() + " - " + position);

        viewHolder.setMuseum(museumToDisplay);


        /*if(clickListener != null) {
            viewHolder.itemView.setOnClickListener(clickListener);
        }*/
    }

    @Override
    public int getItemCount() {
        return museumList.size();
    }

     class MuseumViewHolder extends RecyclerView.ViewHolder {

        private TextView thumbnailTextView;
        private ImageView thumbnailimageView;
        private TextView descriptionTextView;
        //private Button thumbnailButton;

        public MuseumViewHolder(@NonNull final View itemView) {
            super(itemView);
            thumbnailTextView = itemView.findViewById(R.id.thumbnailTextView);
            thumbnailimageView = itemView.findViewById(R.id.thumbnailimageView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            favouriteButton = itemView.findViewById(R.id.button_favoriteHome);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewClick(getAdapterPosition());
                }
            });

            /*favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cardViewClickManager.onCardViewButtonClick(getAdapterPosition());
                }

            });*/

            final ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
            scaleAnimation.setDuration(500);
            BounceInterpolator bounceInterpolator = new BounceInterpolator();
            scaleAnimation.setInterpolator(bounceInterpolator);

            favouriteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    cardViewClickManager.onCardViewToggleButtonCheckedChanged(getAdapterPosition(), favouriteButton, isChecked);
                    //animation
                    compoundButton.startAnimation(scaleAnimation);
                }
            });
            /*thumbnailButton = itemView.findViewById(R.id.thumbnailButton);
            thumbnailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeFragmentDirections.ActionHomeFragmentToMuseumDetailFragment action = HomeFragmentDirections.actionHomeFragmentToMuseumDetailFragment();
                    action.setTitle(thumbnailTextView.getText().toString());
                    action.setDescription(descriptionTextView.getText().toString());

                    Navigation.findNavController(itemView).navigate(action);
                }
            });

            ImageView imageView = itemView.findViewById(R.id.thumbnailimageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeFragmentDirections.ActionHomeFragmentToMuseumDetailFragment action = HomeFragmentDirections.actionHomeFragmentToMuseumDetailFragment();
                    action.setTitle(thumbnailTextView.getText().toString());
                    action.setDescription(descriptionTextView.getText().toString());
                    Navigation.findNavController(itemView).navigate(action);
                }
            });*/

        }

        public void setMuseum(final Museum museumToDisplay) {
            thumbnailTextView.setText(museumToDisplay.getTitle());
            //thumbnailimageView.setImageResource(museumToDisplay.getUid());
            descriptionTextView.setText(museumToDisplay.getDescription());

            String posterUrl = museumToDisplay.getPosterUrl();

            if(posterUrl != null && !posterUrl.equals("")) {
                Glide.with(thumbnailimageView.getContext())
                        .load(posterUrl)
                        .into(thumbnailimageView);
            } else {
                thumbnailimageView.setImageResource(R.drawable.kon_tiki_museet);
            }
        }
    }
}
