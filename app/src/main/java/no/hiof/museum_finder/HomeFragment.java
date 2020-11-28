package no.hiof.museum_finder;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.hiof.museum_finder.adapter.MuseumRecyclerAdapter;
import no.hiof.museum_finder.adapter2.BucketListRecyclerAdapter;
import no.hiof.museum_finder.model.Museum;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment implements CardViewClickManager {
    private List<Museum> museumList;
    private List<String> museumUidList;
    private RecyclerView recyclerView;
    private MuseumRecyclerAdapter museumAdapter;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private View homeView;


    //private MuseumRecyclerAdapter.MuseumViewHolder museumViewHolder;

    private FirebaseFirestore firestoreDb;
    private CollectionReference museumCollectionReference;
    private ListenerRegistration fireStoreListenerRegistration;

    private static final String TAG = HomeFragment.class.getSimpleName();


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //postponeEnterTransition();
        ViewTreeObserver vto = requireView().getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                startPostponedEnterTransition();
                return true;
            }
        });

        museumList = new ArrayList<>();
        museumUidList = new ArrayList<>();
        firestoreDb = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        homeView = view;

        museumCollectionReference = firestoreDb.collection("museum");
        /*
        Bundle arguments = getArguments();
        TextView textView = view.findViewById(R.id.homeTextView);
        assert arguments != null;
        HomeFragmentArgs args = HomeFragmentArgs.fromBundle(arguments);
        textView.setText(args.getUsername());
         */
        //generateTestData();

        setUpRecyclerView(view);

    }

    private void createFireStoreReadListener() {

        fireStoreListenerRegistration = museumCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null) {
                    Log.w(TAG, "Listen failed", e);
                    return;
                }

                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                    QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();
                    Museum museum = documentSnapshot.toObject(Museum.class);
                    museum.setUid(documentSnapshot.getId());
                    int pos = museumList.indexOf(museum.getUid());

                    switch (documentChange.getType()) {
                        case ADDED:
                            museumList.add(museum);
                            museumUidList.add(museum.getUid());
                            museumAdapter.notifyItemInserted(museumList.size() -1);
                            break;
                        case REMOVED:
                            museumList.remove(pos);
                            museumUidList.remove(pos);
                            museumAdapter.notifyItemRemoved(pos);
                            break;
                        case MODIFIED:
                            museumList.set(pos, museum);
                            museumAdapter.notifyItemChanged(pos);
                            break;
                    }
                }
            }
        });
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

    private void setUpRecyclerView(View view) {
        recyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumAdapter = new MuseumRecyclerAdapter(getContext(), museumList, this);
        recyclerView.setAdapter(museumAdapter);


        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            //Do some stuff
            recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            //Do some stuff
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }

    }



    @Override
    public void onCardViewClick(int position, View view) {
        MaterialElevationScale exitTransition = new MaterialElevationScale(false);
        exitTransition.setDuration(300);

        MaterialElevationScale reenterTransition = new MaterialElevationScale(true);
        reenterTransition.setDuration(300);

        String museumCardDetailTransitionName = getString(R.string.museum_card_detail_transition_name);
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder().addSharedElement(view, museumCardDetailTransitionName).build();
        System.out.println(museumList.get(position).getTitle());
        HomeFragmentDirections.ActionHomeFragmentToMuseumDetailFragment  navigateToDetailFragment = HomeFragmentDirections.actionHomeFragmentToMuseumDetailFragment();
        navigateToDetailFragment.setPlaceId(museumList.get(position).getUid());
        Navigation.findNavController(requireView()).navigate(navigateToDetailFragment, extras);
        setExitTransition(exitTransition);
        setReenterTransition(reenterTransition);
    }

    @Override
    public void onCardViewToggleButtonCheckedChanged(int position, ToggleButton favourite,boolean isChecked) {
        if(isChecked) {
            HeartToggleButtonHandler.setCheckedBucketList(museumList.get(position).getUid(),true, currentUser, firestoreDb);
            System.out.println("CHECKED");
        } else {
            HeartToggleButtonHandler.setCheckedBucketList(museumList.get(position).getUid(),false, currentUser, firestoreDb);
            System.out.println("UNCHECKED");
        }
    }


}

