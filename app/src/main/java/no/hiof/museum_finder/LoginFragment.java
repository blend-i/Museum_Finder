package no.hiof.museum_finder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.hiof.museum_finder.model.Account;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class LoginFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private final String TAG = LoginFragment.class.getSimpleName();
    private final int RC_SIGN_IN = 1;
    private final Context context = this.getContext();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                                    new AuthUI.IdpConfig.GoogleBuilder().build());

                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .build(),
                                    RC_SIGN_IN);
                        }
                    }).start();

                }
                else {
                    LoginFragmentDirections.ActionLoginFragmentToHomeFragment action =  LoginFragmentDirections.actionLoginFragmentToHomeFragment();
                    Navigation.findNavController(requireView()).navigate(action);
                    Log.d("signedin", Objects.requireNonNull(currentUser.getEmail()));
                }
            }
        };

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
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        auth.removeAuthStateListener(authStateListener);
    }



}
