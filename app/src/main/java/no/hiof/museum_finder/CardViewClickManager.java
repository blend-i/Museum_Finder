package no.hiof.museum_finder;

import android.view.View;
import android.widget.ToggleButton;

/**
 * Interface used as a clicklistener for cardviews in recycleadapter. The information
 * for these parameters is passed from the onclick in MuseumRecyclerAdapterApi class into the HomeFragmentApi class.
 * The same logic applies from the BucketListRecyclerAdapter to the BucketlistFragment
 */
public interface CardViewClickManager {
    void onCardViewClick(int position, View v, String distance);
}
