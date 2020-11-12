package no.hiof.museum_finder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.museum_finder.model.Account;
import no.hiof.museum_finder.model.Museum;

import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private ImageView profilePictureImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAccountInformationFromDb("account", view);
        /*nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.eMailTextView);
        profilePictureImageView = view.findViewById(R.id.profileImageView);

        String firstName = ProfileFragmentArgs.fromBundle(getArguments()).getFirstname();
        String lastName = ProfileFragmentArgs.fromBundle(getArguments()).getLastname();
        String imageUrl = ProfileFragmentArgs.fromBundle(getArguments()).getProfileimage();

        StringBuilder fullName = new StringBuilder();
        fullName.append(firstName);
        fullName.append(" ");
        fullName.append(lastName);


        nameTextView.setText(fullName.toString());
        emailTextView.setText(ProfileFragmentArgs.fromBundle(getArguments()).getEmail());

        if(imageUrl != null && !imageUrl.equals("")) {
            Glide.with(profilePictureImageView.getContext())
                    .load(imageUrl)
                    .into(profilePictureImageView);
        } else {
            profilePictureImageView.setImageResource(R.drawable.kon_tiki_museet);
        }*/
    }

    private void getAccountInformationFromDb(String documentReference, View view) {
        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
        final DocumentReference accountDocumentReference = firestoreDb.collection(documentReference).document("ecKnqpM3RS85wIYIuJAK");

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.eMailTextView);
        profilePictureImageView = view.findViewById(R.id.profileImageView);

        accountDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    Account account = documentSnapshot.toObject(Account.class);
                    //account.setUid(documentSnapshot.getId());

                    StringBuilder fullName = new StringBuilder();
                    fullName.append(account.getFirstName());
                    fullName.append(" ");
                    fullName.append(account.getLastName());

                    nameTextView.setText(fullName.toString());
                    emailTextView.setText(account.geteMail());

                    if(account.getProfilePictureUrl() != null && !account.getProfilePictureUrl().equals("")) {
                        Glide.with(profilePictureImageView.getContext())
                                .load(account.getProfilePictureUrl())
                                .into(profilePictureImageView);
                    } else {
                        profilePictureImageView.setImageResource(R.drawable.kon_tiki_museet);
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
