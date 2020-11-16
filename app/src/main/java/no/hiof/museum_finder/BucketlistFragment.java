package no.hiof.museum_finder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import no.hiof.museum_finder.adapter.MuseumRecyclerAdapter;
import no.hiof.museum_finder.adapter2.BucketListRecyclerAdapter;
import no.hiof.museum_finder.model.Museum;
import static android.content.ContentValues.TAG;

public class BucketlistFragment extends Fragment {

    private List<Museum> museumList;
    private List<String> museumUidList;
    private RecyclerView recyclerView;
    private FirebaseFirestore firestoreDb;
    private BucketListRecyclerAdapter bucketlistAdapter;
    private CollectionReference museumCollectionReference;
    private ListenerRegistration fireStoreListenerRegistration;

    public BucketlistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bucketlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        museumList = new ArrayList<>();
        museumUidList = new ArrayList<>();
        firestoreDb = FirebaseFirestore.getInstance();
        museumCollectionReference = firestoreDb.collection("account").document("7AMUXAVCiNNllKTIBxty").collection("bucketlist");
        setUpRecyclerView();
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
                            bucketlistAdapter.notifyItemInserted(museumList.size() -1);
                            break;
                        case REMOVED:
                            museumList.remove(pos);
                            museumUidList.remove(pos);
                            bucketlistAdapter.notifyItemRemoved(pos);
                            break;
                        case MODIFIED:
                            museumList.set(pos, museum);
                            bucketlistAdapter.notifyItemChanged(pos);
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

    private void setUpRecyclerView() {
        recyclerView = getView().findViewById(R.id.bucketListRecyclerView);
        bucketlistAdapter = new BucketListRecyclerAdapter(this.getContext(), museumList);

        recyclerView.setAdapter(bucketlistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}