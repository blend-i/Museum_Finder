package no.hiof.museum_finder;

/**
 * Method used as the constructor for the GPSBroadCastReceiver method to register
 * and unregister receiver. This method is called in MainActivity class
 */
public interface LocationCallBack {
    void onLocationTriggered();
}
