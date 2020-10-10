package no.hiof.museum_finder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MuseumDetailFragment extends Fragment {
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

        Bundle arguments = getArguments();
        TextView museumTitle = view.findViewById(R.id.museumTitleTextView);
        TextView museumDescription = view.findViewById(R.id.museumDescriptionTextView);
        assert arguments != null;
        MuseumDetailFragmentArgs args = MuseumDetailFragmentArgs.fromBundle(arguments);
        museumTitle.setText(args.getTitle());
        museumDescription.setText(args.getDescription());
    }
}