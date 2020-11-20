package no.hiof.museum_finder;

import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import no.hiof.museum_finder.model.Museum;

public class HeartToggleButtonHandler {

    static CollectionReference bucketCollectionReference = null;
    static Museum museum;

    public static void setCheckedBucketList(final String museumId, final boolean bool, FirebaseUser user, FirebaseFirestore fireStoreDb)  {
        bucketCollectionReference = fireStoreDb.collection("account").document(user.getUid()).collection("bucketList");
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    assert documentSnapshot != null;
                    museum = documentSnapshot.toObject(Museum.class);
                    assert museum != null;
                    museum.setFavorite(bool);

                    if (documentSnapshot.exists())  {

                        if(!museum.isFavorite()){
                            bucketCollectionReference.document(museumId).delete();
                        }
                    } else {
                        bucketCollectionReference.document(museumId).set(museum);

                        //bucketListSpecificMuseumReference.set(museum);
                        System.out.println("ADDED TO BUCKETLIST");
                    }
                }
            }
        });
    }

    public static void checkIfMusuemExistsInBucketList(final String museumId, final ToggleButton favourite) {
        final DocumentReference bucketListSpecificMuseumReference = bucketCollectionReference.document(museumId);
        bucketListSpecificMuseumReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(documentSnapshot.exists()) {
                        favourite.setChecked(true);
                        System.out.println("DOCUMENT EXISTS IN BUCKETLIST");
                    } else {
                        System.out.println("Document doesn't exist");
                    }
                }
            }
        });
    }
}
