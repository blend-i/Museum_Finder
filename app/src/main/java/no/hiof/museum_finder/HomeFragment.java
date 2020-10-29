package no.hiof.museum_finder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import no.hiof.museum_finder.adapter.MuseumRecyclerAdapter;
import no.hiof.museum_finder.model.Museum;

public class HomeFragment extends Fragment {

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
        setUpRecyclerView();

        /*
        Bundle arguments = getArguments();
        TextView textView = view.findViewById(R.id.homeTextView);
        assert arguments != null;
        HomeFragmentArgs args = HomeFragmentArgs.fromBundle(arguments);
        textView.setText(args.getUsername());
         */
    }


    private void setUpRecyclerView() {
        RecyclerView museumRecyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumRecyclerView.setAdapter(new MuseumRecyclerAdapter(this.getContext(), Museum.getData()));
        museumRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    private void setupFavoritesRecyclerView() {
        RecyclerView museumRecyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumRecyclerView.setAdapter(new MuseumRecyclerAdapter(this.getContext(), Museum.getData()));
        museumRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}