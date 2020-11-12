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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import no.hiof.museum_finder.model.Account;

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
    }

    private void getAccountInformationFromDb(String documentReference, View view) {
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.eMailTextView);
        profilePictureImageView = view.findViewById(R.id.profileImageView);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        System.out.println("SKILLE" + account.getEmail());
        FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
        final CollectionReference accountCollectionReference = firestoreDb.collection("account");

        accountCollectionReference.whereEqualTo("eMail", account.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();

                            for(QueryDocumentSnapshot document : querySnapshot) {
                                Account account = document.toObject(Account.class);
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
                        }
                        else
                        {
                            Log.d(TAG, "Get failed with", task.getException());
                        }
                    }
                });

    }
}
