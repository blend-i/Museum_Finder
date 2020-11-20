package no.hiof.museum_finder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Objects;

import no.hiof.museum_finder.adapter.MuseumRecyclerAdapter;
import no.hiof.museum_finder.adapter2.BucketListRecyclerAdapter;
import no.hiof.museum_finder.model.Museum;

public class HomeFragment extends Fragment implements CardViewClickManager {

    private List<Museum> museumList;
    private List<String> museumUidList;
    private RecyclerView recyclerView;
    private MuseumRecyclerAdapter museumAdapter;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    //private MuseumRecyclerAdapter.MuseumViewHolder museumViewHolder;

    private FirebaseFirestore firestoreDb;
    private CollectionReference museumCollectionReference;
    private ListenerRegistration fireStoreListenerRegistration;

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
        museumList = new ArrayList<>();
        museumUidList = new ArrayList<>();
        firestoreDb = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        museumCollectionReference = firestoreDb.collection("museum");
        /*
        Bundle arguments = getArguments();
        TextView textView = view.findViewById(R.id.homeTextView);
        assert arguments != null;
        HomeFragmentArgs args = HomeFragmentArgs.fromBundle(arguments);
        textView.setText(args.getUsername());
         */
        //generateTestData();
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
                            museumAdapter.notifyItemInserted(museumList.size() -1);
                            break;
                        case REMOVED:
                            museumList.remove(pos);
                            museumUidList.remove(pos);
                            museumAdapter.notifyItemRemoved(pos);
                            break;
                        case MODIFIED:
                            museumList.set(pos, museum);
                            museumAdapter.notifyItemChanged(pos);
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
        recyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumAdapter = new MuseumRecyclerAdapter(getContext(), museumList, this);
        recyclerView.setAdapter(museumAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }


    private void generateTestData() {
        ArrayList<Museum> museums = new ArrayList<>();
            museums.add(new Museum("Bymuseet", "Bymuseet, tidligere Oslo Bymuseum, er siden 2006 en avdeling ved Oslo Museum. Museet er det kulturhistoriske museet for Oslo by med det tidligere Aker herred. Museet ble stiftet 22. desember 1905 som foreningen «Det gamle Christiania». Initiativtager og leder inntil 1912 var arkitekt Fritz Holland.", "11-16","Frognerveien 67, 0266 Oslo"));
            museums.add(new Museum("Frammuseet", "Frammuseet er et museum på Bygdøynes i Oslo med polarskipet «Fram» fra 1892 som hovedattraksjon. Museet skildrer de historiske norske polferdene og de tre polfarerne Fridtjof Nansen, Otto Sverdrup og Roald Amundsen", "10-17", "Bygdøynesveien 39, 0286 Oslo"));
            museums.add(new Museum("Kon-Tiki Museet", "Kon-Tiki Museet er et museum på Bygdøynes i Oslo som huser fartøy og kulturgjenstander fra Thor Heyerdahls ekspedisjoner. Museet er en privat stiftelse.","Ukjent" ,"Bygdøynesveien 36, 0286 Oslo"));
            museums.add(new Museum("Kulturhistorisk museum", "Kulturhistorisk museum er en museumsorganisasjon ved Universitetet i Oslo. Organisasjonen ble opprettet i 1999 under navnet Universitetets kulturhistoriske museer da Universitetets Oldsaksamling, Myntkabinettet og Etnografisk museum ble slått sammen til én organisasjon.", "11-16", "Frederiks gate 2, 0164 Oslo"));
            museums.add(new Museum("MUNCH", "Munchmuseet, fra 2020 markedsført som MUNCH, er et kunstmuseum i Oslo som inneholder Edvard Munchs etterlatte arbeider som han testamenterte til Oslo kommune i 1940. Museet åpnet dørene for publikum i 1963, hundre år etter maleren og grafikerens fødsel.", "10-16", "Tøyengata 53, 0578 Oslo"));
            museums.add(new Museum("Nasjonalgalleriet", "Nasjonalmuseet for kunst er et norsk statlig museum etablert i 2003 gjennom sammenslåingen av Arkitekturmuseet, Kunstindustrimuseet, Museet for samtidskunst, Nasjonalgalleriet og Riksutstillinger.", "Ukjent", "Universitetsgata 13, 0164 Oslo"));
            museums.add(new Museum("Norges Resistance Museum", "Norges Hjemmefrontmuseum, forkortet NHM og ofte omtalt som Hjemmefrontmuseet, er et museum på Akershus festning i Oslo. Museet tar for seg den tyske okkupasjonen av Norge under andre verdenskrig 1940–1945 med spesielt fokus på motstandskampen i landet. NHM er en del av Forsvarets museer. Det åpnet 7. mai 1970.", "10-17", "Bygning 21, 0150 Oslo"));
            museums.add(new Museum("Norsk Folkemuseum", "Norsk Folkemuseum er et kulturhistorisk museum på Bygdøy i Oslo som viser hvordan folk har levd i Norge fra 1500-tallet og frem til i dag. Her presenteres dagliglivet i byene og på landet i et av Europas største friluftsmuseer. Museet har samlinger fra hele landet, i hovedsak fra tiden etter reformasjonen.", "11-16", "Museumsveien 10, 0287 Oslo"));
            museums.add(new Museum("Norsk Teknisk Museum","Norsk Teknisk Museum er et norsk museum for industri, vitenskap, teknologi og medisin. Museet er i 2011 kåret til årets museum. Museet har i de senere årene mottatt en rekke internasjonale priser for innovative utstillinger.","9-16","Kjelsåsveien 143, 0491 Oslo"));
            museums.add(new Museum("Vigelandmuseet","Vigelandmuseet ble oppført i 1920-årene av Oslo kommune som atelier, bolig og fremtidig museum for den norske billedhuggeren Gustav Vigeland. Her arbeidet og levde han frem til sin død.","12-16","Nobels gate 32, 0268 Oslo"));
            museums.add(new Museum("Vikingskipshuset", "Vikingskipshuset, ofte kalt Vikingskipsmuseet, er et museum på Bygdøy i Oslo. Det er en del av Kulturhistorisk museum, som er underlagt Universitetet i Oslo, og rommer gravfunnene fra Tune, Gokstadhaugen i Sandefjord, Oseberghaugen i Tønsberg og Borrehaugene i Vestfold.","11-16","Huk Aveny 35, 0287 Oslo"));
            museums.add(new Museum("Varanger Museum IKS avd Sør-Varanger","Sør-Varanger museum ble etablert i 1964 som en privat institusjon og ble noen år senere overtatt av Sør-Varanger kommune. Foreningen som til da hadde eid museet, Sør-Varanger historie- og museumslag ble til Sør-Varanger Historielag. Historielaget og museet har fortsatt et nært samarbeid.","9-15","Gnr 26 Bnr 77, 9900 Kirkenes"));

            for(Museum m : museums) {
                museumCollectionReference.add(m);
            }
    }

    @Override
    public void onCardViewClick(int position) {
        System.out.println(museumList.get(position).getTitle());
        HomeFragmentDirections.ActionHomeFragmentToMuseumDetailFragment  navigateToDetailFragment = HomeFragmentDirections.actionHomeFragmentToMuseumDetailFragment();
        navigateToDetailFragment.setId(museumList.get(position).getUid());
        Navigation.findNavController(requireView()).navigate(navigateToDetailFragment);
    }

    @Override
    public void onCardViewToggleButtonCheckedChanged(int position, ToggleButton favourite,boolean isChecked) {
        if(isChecked) {
            HeartToggleButtonHandler.setCheckedBucketList(museumList.get(position).getUid(),true, currentUser, firestoreDb);
            System.out.println("CHECKED");
        } else {
            HeartToggleButtonHandler.setCheckedBucketList(museumList.get(position).getUid(),false, currentUser, firestoreDb);
            System.out.println("UNCHECKED");
        }
    }
}