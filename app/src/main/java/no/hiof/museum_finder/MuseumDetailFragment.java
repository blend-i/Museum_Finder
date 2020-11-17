package no.hiof.museum_finder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.museum_finder.model.Museum;

import static android.content.ContentValues.TAG;


public class MuseumDetailFragment extends Fragment {
    public static final String MUSEUM_UID = "museum_uid";
    private TextView museumTitle;
    private TextView museumDescription;
    private ImageView museumImage;
    private TextView museumLocation;
    private TextView museumOpeningHours;


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

        Bundle arguments = getArguments();
        assert arguments != null;
        MuseumDetailFragmentArgs args = MuseumDetailFragmentArgs.fromBundle(arguments);
        //museumTitle.setText(args.getTitle());
        //museumDescription.setText(args.getDescription());

        final String museumUid = args.getId();

        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
        final DocumentReference museumCollectionReference = firestoreDb.collection("museum").document(museumUid);

        museumCollectionReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Museum museum = documentSnapshot.toObject(Museum.class);
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
    }
}