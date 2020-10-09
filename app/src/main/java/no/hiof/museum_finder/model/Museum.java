package no.hiof.museum_finder.model;

import java.lang.reflect.Array;
import java.util.ArrayList;

import no.hiof.museum_finder.R;

public class Museum {

    private int uid;
    private String title;
    private String description;
    //private int posterUrl;

    public Museum(int uid, String title, String description) {
        this.uid = uid;
        this.title = title;
        this.description = description;
        //this.posterUrl = posterUrl;
    }


    public int getUid() {
        return uid;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static ArrayList<Museum> getData() {
        ArrayList<Museum> dataList = new ArrayList<>();

        int[] images = getImages();
        String[] titles = getTitles();

        for (int i = 0; i <images.length ; i++) {
            Museum aMuseum = new Museum(images[i], titles[i], "Description of museum");
            dataList.add(aMuseum);
        }
        return dataList;
    }


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
