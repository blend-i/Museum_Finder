package no.hiof.museum_finder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

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

        nameTextView = view.findViewById(R.id.nameTextView);
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
        }
    }
}
