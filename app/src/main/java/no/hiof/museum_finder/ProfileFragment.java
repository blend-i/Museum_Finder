package no.hiof.museum_finder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import no.hiof.museum_finder.model.Account;
import no.hiof.museum_finder.model.Museum;

import static android.content.ContentValues.TAG;

/**
 * The ProfileFragment class represents the user profile. We use the firebase auth
 * to get information about the user and display their name, email and profile picture.
 * The seekbar in profile changes the radius value in the api request for nearby museums
 * which lets the user decide the max distance for museums. We also have a logout button which
 * signs the user out of the logged inn session and navigates back to the login screen.
 */

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private ImageView profilePictureImageView;
    private FirebaseAuth auth;
    private SeekBar seekBar;
    private TextView radiusValue;
    public static int radius = 50000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public static int getRadius() {
        return radius;
    }

    public static void setRadius(int radius) {
        ProfileFragment.radius = radius;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.eMailTextView);
        profilePictureImageView = view.findViewById(R.id.profileImageView);
        //getAccountInformationFromDb("account", view);
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        radiusValue = view.findViewById(R.id.radiusValue);

        radiusValue.setText(String.valueOf("Max distance: " + (radius) + " km"));

        seekBar = view.findViewById(R.id.seekBar);
        seekBar.setMax(50000);
        seekBar.setProgress(getRadius());
        radiusValue.setText(String.valueOf((getRadius() / 1000) + " km"));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setRadius(progress);
                radiusValue.setText(String.valueOf((progress / 1000) + " km"));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        nameTextView.setText(firebaseUser.getDisplayName());
        emailTextView.setText(firebaseUser.getEmail());

        if (firebaseUser.getPhotoUrl() != null && !firebaseUser.getPhotoUrl().equals("")) {
            Glide.with(profilePictureImageView.getContext())
                    .load(firebaseUser.getPhotoUrl())
                    .into(profilePictureImageView);
        } else {
            profilePictureImageView.setImageResource(R.drawable.kon_tiki_museet);
        }

        Button button = getView().findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance().signOut(getContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Navigation.findNavController(requireView()).navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment());
                        Toast.makeText(getContext(), "Signed signed out", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
