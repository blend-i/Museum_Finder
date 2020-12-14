package no.hiof.museum_finder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ResourceBundle;

public class GPSBroadcastReceiver extends BroadcastReceiver {

    private final LocationCallBack locationCallBack;

    public GPSBroadcastReceiver(LocationCallBack iLocationCallBack){
        this.locationCallBack = iLocationCallBack;
    }

    /**
     * Listens if the user turns location on or if the user turn location off on the device.
     * Since our main method checks if the gps is on or off at the beginning, we set it to the opposite
     * of that when user turns location on or off.
     * If gpsEnabled is false, then we show an ALertDialog which ask the user to turn on location on phone settings
     * and offer a button that sends you there.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                MainActivity.gpsEnabled = !MainActivity.gpsEnabled;

                if(MainActivity.gpsEnabled) {
                    Toast.makeText(context, "Location on", Toast.LENGTH_SHORT).show();

                } else if(!MainActivity.gpsEnabled) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setMessage("Failed to find location, please enable your location settings");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(myIntent);
                            paramDialogInterface.dismiss();

                            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                            try {
                                MainActivity.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            cancelMessageDialog(context);
                        }
                    });
                    dialog.show();
                }

            } else {
                MainActivity.gpsEnabled = false;
                System.out.println("FALSE");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * If the user clicks cancel we show an additional AlertDialog which informs the user that
     * the app is not usable without turning on location settings and ask again if the user wants
     * to turn location settings on.
     * @param context - contect of the onReceive method
     */
    public void cancelMessageDialog(Context context) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("We are sorry, you need to enable location settings to use this application");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
                paramDialogInterface.dismiss();

                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                try {
                    MainActivity.gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialog.show();
    }
}
