package no.hiof.museum_finder;

import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import no.hiof.museum_finder.adapter2.BucketListRecyclerAdapter;
import no.hiof.museum_finder.model.Museum;
import static android.content.ContentValues.TAG;

public class BucketlistFragment extends Fragment implements CardViewClickManager{
    private List<Museum> museumList;
    private List<String> museumUidList;
    private RecyclerView recyclerView;
    private BucketListRecyclerAdapter bucketlistAdapter;
    private FirebaseFirestore firestoreDb;
    private CollectionReference museumCollectionReference;
    private ListenerRegistration fireStoreListenerRegistration;
    private FirebaseAuth auth;

    public BucketlistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucketlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        museumList = new ArrayList<>();
        museumUidList = new ArrayList<>();
        firestoreDb = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        FirebaseUser signedInUser = auth.getCurrentUser();
        museumCollectionReference = firestoreDb.collection("account").document(signedInUser.getUid()).collection("bucketList");

        setUpRecyclerView();
    }

    private void createFireStoreReadListener() {
        if(museumCollectionReference == null) {
            return;
        }
        try {
            fireStoreListenerRegistration = museumCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(e != null) {
                        Log.w(TAG, "Listen failed", e);
                        return;
                    }

                    assert queryDocumentSnapshots != null;
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();
                        Museum museum = documentSnapshot.toObject(Museum.class);
                        museum.setPlaceId(documentSnapshot.getId());
                        int pos = museumList.indexOf(museum.getPlaceId());

                        switch (documentChange.getType()) {
                            case ADDED:
                                museumList.add(museum);
                                museumUidList.add(museum.getPlaceId());
                                bucketlistAdapter.notifyItemInserted(museumList.size() -1);
                                break;
                            case REMOVED:
                                if(museumList.size()!=0){
                                    museumList.remove(pos);
                                    museumUidList.remove(pos);
                                    bucketlistAdapter.notifyItemRemoved(pos);
                                }
                                break;
                            case MODIFIED:
                                museumList.set(pos, museum);
                                bucketlistAdapter.notifyItemChanged(pos);
                                break;
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.d("BucketlistFragment", "Could not load bucketlist");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        createFireStoreReadListener();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(fireStoreListenerRegistration != null) {
            fireStoreListenerRegistration.remove();
        }
    }

    private void setUpRecyclerView() {
        recyclerView = getView().findViewById(R.id.bucketListRecyclerView);
        bucketlistAdapter = new BucketListRecyclerAdapter(getContext(), museumList, this);
        recyclerView.setAdapter(bucketlistAdapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Do some stuff
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            //Do some stuff
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

    @Override
    public void onCardViewClick(int position, View view, String distance) {

        MaterialElevationScale exitTransition = new MaterialElevationScale(false);
        exitTransition.setDuration(300);

        MaterialElevationScale reenterTransition = new MaterialElevationScale(true);
        reenterTransition.setDuration(300);

        String museumCardDetailTransitionName = getString(R.string.museum_card_detail_transition_name);
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder().addSharedElement(view, museumCardDetailTransitionName).build();
        System.out.println(museumList.get(position).getTitle());
        BucketlistFragmentDirections.ActionBucketlistFragmentToMuseumDetail  navigateToDetailFragment = BucketlistFragmentDirections.actionBucketlistFragmentToMuseumDetail();
        navigateToDetailFragment.setPlaceId(museumList.get(position).getPlaceId());
        navigateToDetailFragment.setOpeningHours(museumList.get(position).getOpen());
        navigateToDetailFragment.setPhotoUrl(museumList.get(position).getPhoto());
        navigateToDetailFragment.setRating(museumList.get(position).getRating());
        navigateToDetailFragment.setDistance(distance);
        navigateToDetailFragment.setTitle(museumList.get(position).getTitle());
        navigateToDetailFragment.setLocation(museumList.get(position).getLocation());
        Navigation.findNavController(requireView()).navigate(navigateToDetailFragment, extras);
    }
}