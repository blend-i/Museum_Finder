package no.hiof.museum_finder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    public static boolean gpsEnabled;
    public GPSBroadcastReceiver gpsBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Used this page for inspiration on creating the broadcastreciever:
         * https://stackoverflow.com/questions/57306302/how-to-detect-if-user-turned-off-location-in-settings
         */
        registerReceiver(gpsBroadcastReceiver = new GPSBroadcastReceiver(new LocationCallBack() {
            @Override
            public void onLocationTriggered() {
                unregisterReceiver(gpsBroadcastReceiver);
            }
        }), new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));


        if (gpsEnabled) {
            unregisterReceiver(gpsBroadcastReceiver);
        }

        /**
         * Checks when you start the application if the internet on device is on. If its not then we show an
         * AlertDialog which ask the user to turn on the internett and try again.
         * If the user has internet on device we load up the app by setContentView and checking if user has gps location
         */
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.no_internet_dialog);

            dialog.setCanceledOnTouchOutside(false);

            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            Button tryAgainButton = dialog.findViewById(R.id.tryAgainButton);
            tryAgainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
            try {
                dialog.show();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else {

            setContentView(R.layout.activity_main);
            NavController controller = Navigation.findNavController(this, R.id.fragment);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_NavBar);
            NavigationUI.setupWithNavController(bottomNavigationView, controller);

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}