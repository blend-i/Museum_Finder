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
    /*private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private FirebaseFirestore firestoreDb;
    private CollectionReference collectionReference;*/


   /* public LoginFragment() {
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        signInButton = view.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this.requireContext());
        //updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account) {

        if (account != null) {
            LoginFragmentDirections.ActionLoginFragmentToHomeFragment action =  LoginFragmentDirections.actionLoginFragmentToHomeFragment();
            //Navigation.findNavController(requireView()).navigate(action);

            LoginFragmentDirections.ActionLoginFragmentToProfileFragment profileAction = LoginFragmentDirections.actionLoginFragmentToProfileFragment();
            profileAction.setFirstname(account.getGivenName());
            profileAction.setLastname(account.getFamilyName());
            profileAction.setEmail(account.getEmail());
            profileAction.setProfileimage(account.getPhotoUrl().toString());*/

            /*Navigation.findNavController(requireView()).navigate(profileAction);

            Context context = this.getContext();
            CharSequence text = "Signed in as: " + account.getEmail();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        if (account == null) {
            return;

        }
        LoginFragmentDirections.ActionLoginFragmentToHomeFragment action =  LoginFragmentDirections.actionLoginFragmentToHomeFragment();
        //Navigation.findNavController(requireView()).navigate(action);

        //LoginFragmentDirections.ActionLoginFragmentToProfileFragment profileAction = LoginFragmentDirections.actionLoginFragmentToProfileFragment();
        /*profileAction.setFirstname(account.getGivenName());
        profileAction.setLastname(account.getFamilyName());
        profileAction.setEmail(account.getEmail());
        profileAction.setProfileimage(account.getPhotoUrl().toString());*/

        /*Navigation.findNavController(requireView()).navigate(action);
        Context context = this.getContext();
        CharSequence text = "Signed in as: " + account.getEmail();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 1) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this.requireContext());
            System.out.println(account.getAccount());
            System.out.println(lastSignedInAccount.getAccount());

            String lastSignedInEmail = lastSignedInAccount.getEmail();
            String accountToBeSignedIn = account.getEmail();


            if(!lastSignedInEmail.equals(accountToBeSignedIn)) {
                if (account.getPhotoUrl().equals(null)) {
                    Account accountWithDefaultPicture = new Account(account.getGivenName(), account.getFamilyName(), account.getEmail(), "https://st.depositphotos.com/2101611/3925/v/600/depositphotos_39258143-stock-illustration-businessman-avatar-profile-picture.jpg");
                    addAccountToDb(accountWithDefaultPicture);
                } else {
                    Account accountWithProfilePicture = new Account(account.getGivenName(), account.getFamilyName(), account.getEmail(), account.getPhotoUrl().toString());
                    addAccountToDb(accountWithProfilePicture);
                }
            }

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        }
        catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void addAccountToDb(Account account) {
        firestoreDb = FirebaseFirestore.getInstance();
        collectionReference = firestoreDb.collection("account");
        collectionReference.add(account);
    }

    //button.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_homeFragment));*/


}
