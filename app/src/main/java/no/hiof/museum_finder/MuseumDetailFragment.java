package no.hiof.museum_finder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.museum_finder.model.Museum;


public class MuseumDetailFragment extends Fragment {
    private TextView museumTitle;
    private TextView museumDescription;
    private ImageView museumImage;
    private TextView museumLocation;
    private TextView museumOpeningHours;
    private RatingBar museumRating;
    private FirebaseAuth firebaseAuth;
    private Museum museum;
    private FirebaseFirestore fireStoreDb;
    private ToggleButton favourite;
    private CollectionReference bucketCollectionReference;

    public MuseumDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDrawingViewId(R.id.fragment);
        materialContainerTransform.setDuration(300);
        materialContainerTransform.setScrimColor(Color.TRANSPARENT);
        materialContainerTransform.setAllContainerColors(Color.TRANSPARENT);
        setSharedElementEnterTransition(materialContainerTransform);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle arguments = getArguments();
        assert arguments != null;
        MuseumDetailFragmentArgs args = MuseumDetailFragmentArgs.fromBundle(arguments);

        museum = new Museum(args.getTitle(), "Museum description", args.getLocation(), args.getOpeningHours(), args.getPhotoUrl(), args.getPlaceId(), args.getRating());
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
        museumRating = view.findViewById(R.id.ratingBarDetail);

        fireStoreDb = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        museumTitle.setText(museum.getTitle());
        //museumDescription.setText(args.getDescription());
        museumOpeningHours.setText(museum.getOpen().equals("true") ? "Open" : "Closed");
        museumLocation.setText(museum.getLocation());
        museumRating.setRating(Float.parseFloat(museum.getRating()));

        if (!museum.getPhoto().isEmpty()) {

            String url = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=" + 400 +
                    "&photoreference=" + museum.getPhoto() +
                    "&key=AIzaSyCis2iHvAD0nBpKigxJAHA0CVGo_vq88nc";

            Glide.with(museumImage.getContext())
                    .load(url)
                    .into(museumImage);
        }

        final String museumPlaceId = museum.getPlaceId();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        bucketCollectionReference = fireStoreDb.collection("account").document(user.getUid()).collection("bucketList");

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
                    setCheckedBucketList(museumPlaceId, true);
                    System.out.println("UNCHECKED");
                } else {
                    setCheckedBucketList(museumPlaceId, false);
                }
                compoundButton.startAnimation(scaleAnimation);
            }
        });
        checkIfMusuemExistsInBucketList(museum.getPlaceId());
    }


    private void setCheckedBucketList(final String museumId, final boolean bool) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()) {
                        museum = documentSnapshot.toObject(Museum.class);
                        museum.setFavorite(bool);
                        if (!museum.isFavorite()) {
                            bucketCollectionReference.document(museumId).delete();
                        }
                    } else {
                        bucketCollectionReference.document(museumId).set(museum);
                    }
                }
            }
        });
    }

    private void checkIfMusuemExistsInBucketList(final String museumId) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if (documentSnapshot.exists()) {
                        favourite.setChecked(true);
                        System.out.println("DOCUMENT EXISTS IN BUCKETLIST");
                    } else {
                        System.out.println("Document doesn't exist");
                    }
                }
            }
        });
    }
}