package no.hiof.museum_finder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hiof.museum_finder.model.Museum;

import static android.content.ContentValues.TAG;

public class MuseumDetailFragment extends Fragment {
    public static final String MUSEUM_UID = "museum_uid";
    private TextView museumTitle;
    private TextView museumDescription;
    private ImageView museumImage;
    private TextView museumLocation;
    private TextView museumOpeningHours;
    private Button bucketListButton;
    private FirebaseAuth firebaseAuth;
    private Museum museum;
    private FirebaseFirestore fireStoreDb;
    private ToggleButton favourite;

    private CollectionReference bucketCollectionReference;

    public MuseumDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_museum_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        museumTitle = view.findViewById(R.id.museumTitleTextView);
        museumDescription = view.findViewById(R.id.museumDescriptionTextView);
        museumImage = view.findViewById(R.id.imageView);
        museumLocation = view.findViewById(R.id.locationTextView);
        museumOpeningHours = view.findViewById(R.id.openingHoursTextView);

        bucketListButton = view.findViewById(R.id.addToBucketListButton);

        fireStoreDb = FirebaseFirestore.getInstance();

        museumTitle.setVisibility(View.INVISIBLE);
        museumDescription.setVisibility(View.INVISIBLE);
        museumImage.setVisibility(View.INVISIBLE);
        museumLocation.setVisibility(View.INVISIBLE);
        museumOpeningHours.setVisibility(View.INVISIBLE);

        Bundle arguments = getArguments();
        assert arguments != null;
        MuseumDetailFragmentArgs args = MuseumDetailFragmentArgs.fromBundle(arguments);
        //museumTitle.setText(args.getTitle());
        //museumDescription.setText(args.getDescription());

        final String museumUid = args.getId();

       firebaseAuth = FirebaseAuth.getInstance();

       FirebaseUser user = firebaseAuth.getCurrentUser();

        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
        final DocumentReference museumCollectionReference = firestoreDb.collection("museum").document(museumUid);

        museumCollectionReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    museumTitle.setVisibility(View.VISIBLE);
                    museumDescription.setVisibility(View.VISIBLE);
                    museumImage.setVisibility(View.VISIBLE);
                    museumLocation.setVisibility(View.VISIBLE);
                    museumOpeningHours.setVisibility(View.VISIBLE);

                    DocumentSnapshot documentSnapshot = task.getResult();
                    museum = documentSnapshot.toObject(Museum.class);
                    museum.setUid(documentSnapshot.getId());

                    StringBuilder openingHours = new StringBuilder();
                    openingHours.append("Opening hours: ");
                    openingHours.append(museum.getOpeningHours());

                    StringBuilder location = new StringBuilder();
                    location.append("Location: ");
                    location.append(museum.getLocation());

                    museumTitle.setText(museum.getTitle());
                    museumDescription.setText(museum.getDescription());
                    museumLocation.setText(location.toString());
                    museumOpeningHours.setText(openingHours.toString());

                    if (museum.getPosterUrl() != null && !museum.getPosterUrl().isEmpty()) {
                        Glide.with(museumImage.getContext())
                                .load(museum.getPosterUrl())
                                .into(museumImage);
                    }
                }
                else
                {
                    Log.d(TAG, "Get failed with", task.getException());
                }
            }
        });

        final DocumentReference userDocumentReference = fireStoreDb.collection("account").document(user.getUid());

        bucketCollectionReference = fireStoreDb.collection("account").document(user.getUid()).collection("bucketList");

        bucketListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                //bucketCollectionReference.add(new Museum(museum.getTitle(), museum.getDescription(), museum.getOpeningHours(), museum.getLocation(), museum.getPosterUrl()));
                                bucketCollectionReference.document(museumUid).set(new Museum(museum.getTitle(), museum.getDescription(), museum.getOpeningHours(), museum.getLocation(), museum.getPosterUrl(), true));
                            }   else {
                                Log.d("TAG", "Could not add bucketlist");
                            }
                        } else {
                            Log.d("TAG", "Task unseccesful");
                        }
                    }
                });
            }
        });

        favourite = getView().findViewById(R.id.button_favorite);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);



        favourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //animation

                if (isChecked) {
                    setCheckedBucketList(museumUid, true);
                    System.out.println("UNCHECKED");
                } else {
                    setCheckedBucketList(museumUid, false);
                }
                compoundButton.startAnimation(scaleAnimation);
            }
        });
    }

    private void setCheckedBucketList(final String museumId, final boolean bool) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(!documentSnapshot.exists()) {
                        /*museum = documentSnapshot.toObject(Museum.class);
                        museum.setFavorite(bool);*/
                        bucketCollectionReference.document(museumId).set(new Museum(museum.getTitle(), museum.getDescription(), museum.getOpeningHours(), museum.getLocation(), museum.getPosterUrl(), true));

                        //bucketListSpecificMuseumReference.set(museum);
                        System.out.println("ADDED TO BUCKETLIST");
                    } else {
                        bucketCollectionReference.document(museumId).delete();
                    }
                }
            }
        });
    }
}