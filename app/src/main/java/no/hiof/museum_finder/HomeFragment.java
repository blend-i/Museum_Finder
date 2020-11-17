package no.hiof.museum_finder;

import android.content.Intent;
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
import android.widget.Button;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import no.hiof.museum_finder.model.Museum;

public class HomeFragment extends Fragment {

    private List<Museum> museumList;
    private List<String> museumUidList;
    private RecyclerView recyclerView;
    private MuseumRecyclerAdapter museumAdapter;

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
        /*RecyclerView museumRecyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumRecyclerView.setAdapter(new MuseumRecyclerAdapter(this.getContext(), ));
        museumRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));


         */


            recyclerView = getView().findViewById(R.id.museumRecyclerView);
            museumAdapter = new MuseumRecyclerAdapter(this.getActivity(), museumList);



        //museumAdapter.setButtonClickListener(new Button.OnClickListener());
        museumAdapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);

                /*Museum museum = museumList.get(position);
                Intent intent = new Intent(HomeFragment.this.getContext(), MuseumDetailFragment.class);
                intent.putExtra(MuseumDetailFragment.MUSEUM_UID, museum.getUid());

                startActivity(intent);*/
                System.out.println("DET BLE KLIKKA JO");
            }
        });

        recyclerView.setAdapter(museumAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    /*private void setupFavoritesRecyclerView() {
        RecyclerView museumRecyclerView = getView().findViewById(R.id.museumRecyclerView);
        museumRecyclerView.setAdapter(new MuseumRecyclerAdapter(this.getContext(), Museum.getData()));
        museumRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

     */

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
}