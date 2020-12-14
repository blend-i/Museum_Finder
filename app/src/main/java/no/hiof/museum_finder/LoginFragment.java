package no.hiof.museum_finder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import no.hiof.museum_finder.model.Account;
import no.hiof.museum_finder.model.BucketList;
import no.hiof.museum_finder.model.Museum;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

/**
 * This class handles the login functionality for the application with FirebaseAuth.
 */
public class LoginFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore firebaseDb;
    private final String TAG = LoginFragment.class.getSimpleName();
    private final int RC_SIGN_IN = 1;
    private final Context context = this.getContext();
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * The auth and authstatelistener sees if we have a current user. If there is not user logged in
         * then we use an AuthUI builder with generated loginmethods for creating email and password,
         * login with google account, or get access anonymously
         * Then we add that account to our firebase database and navigate to HomeFragmentApi.
         */
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser == null) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<AuthUI.IdpConfig> providers = Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                    new AuthUI.IdpConfig.AnonymousBuilder().build());

                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .setLogo(R.mipmap.museum_logo)
                                            .build(),
                                    RC_SIGN_IN);
                        }
                    }).start();

                } else {
                    addAccountToDb(new Account(currentUser.getEmail()));
                    Navigation.findNavController(requireView()).navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragmentApi());
                }
            }
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != RC_SIGN_IN)
            return;

        if (resultCode == RESULT_OK) {
            FirebaseUser currentUser = auth.getCurrentUser();
            //Toast.makeText(context, "Signed in as " + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(context, "Signed in cancelled", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            auth.addAuthStateListener(authStateListener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        auth.removeAuthStateListener(authStateListener);
    }

    private void addAccountToDb(final Account account) {
        final FirebaseUser currentUser = auth.getCurrentUser();
        firebaseDb = FirebaseFirestore.getInstance();
        final CollectionReference accountCollection = firebaseDb.collection("account");
        DocumentReference documentReference = accountCollection.document(currentUser.getUid());

        /*if(currentUser != null)
            accountCollection.document(currentUser.getUid()).set(account, SetOptions.merge());
         */

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Log.d("TAG", "Dokument exists");
                    } else {
                        Log.d("TAG", "Dokument does not exist. Adding User.");
                        accountCollection.document(currentUser.getUid()).set(account);
                    }
                } else {
                    Log.d("TAG", "Task unsuccseful");
                }
            }
        });
    }
}
