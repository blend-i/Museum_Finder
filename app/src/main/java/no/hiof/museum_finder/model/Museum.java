package no.hiof.museum_finder.model;
import com.google.firebase.database.Exclude;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import no.hiof.museum_finder.R;

public class Museum {
    @Exclude
    private String uid;
    private String title;
    private String description;
    private String openingHours;
    private String location;
    private String posterUrl;

    /*public Museum(int uid, String title, String description) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        //this.posterUrl = posterUrl;
    }

     */
    public Museum () {

    }
    public Museum(String title, String description, String openingHours, String location) {
        this.uid = "";
        this.title = title;
        this.description = description;
        this.openingHours = openingHours;
        this.location = location;
    }

    public Museum(String title, String description, String openingHours, String location, String posterUrl) {
        this.uid = "";
        this.title = title;
        this.description = description;
        this.openingHours = openingHours;
        this.location = location;
        this.posterUrl = posterUrl;
    }
    @Exclude
    public String getUid() {
        return uid;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() { return posterUrl; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /*public static ArrayList<Museum> getData() {
        ArrayList<Museum> dataList = new ArrayList<>();

        int[] images = getImages();
        String[] titles = getTitles();

        for (int i = 0; i <images.length ; i++) {
            Museum aMuseum = new Museum(images[i], titles[i], "Description of museum22");
            dataList.add(aMuseum);
        }
        return dataList;
    }

     */


    private static int[] getImages() {
        return new int[] {
                R.drawable.norsk_folkemuseum, R.drawable.norsk_teknisk_museum, R.drawable.vikingskipshuset,
                R.drawable.kon_tiki_museet, R.drawable.bymuseet, R.drawable.frammuseet, R.drawable.kulturgistorisk_museum,
                R.drawable.munch, R.drawable.nasjonalgalleriet, R.drawable.norges_resistance_museum,
                R.drawable.vigelandmuseet
        };
    }

    public static String[] getTitles() {
        return new String[]  {
                "Norsk_Folkemuseum",
                "Norsk_Teknisk_Museum",
                "Vikingskiphuset",
                "Kon Tiki Museet",
                "Bymuseet",
                "Frammuseet",
                "Kulturhistorisk museum",
                "Munch",
                "Nasjonalgalleriet",
                "Norges Resistance Museum",
                "Vigelandmuseet"
        };
    }
}
