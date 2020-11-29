package no.hiof.museum_finder.model;
import com.google.firebase.database.Exclude;

import java.lang.annotation.Annotation;

import no.hiof.museum_finder.R;

public class Museum {

    private String title;
    private String description;
    private String location;
    private boolean isFavorite;
    private double lat;
    private double lng;
    private String open;
    private String photo;
    private String placeId;
    private String rating;

    public Museum () { }

    public Museum(String title, String open, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.open = open;
    }

    public Museum(String title, String open, String photo, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.open = open;
        this.photo = photo;
    }

    public Museum(String title, String open, String photo, String placeId, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.open = open;
        this.photo = photo;
        this.placeId = placeId;
    }

    public Museum(String title, String open, String photo, String rating, String placeId, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
        this.open = open;
        this.photo = photo;
        this.placeId = placeId;
        this.rating = rating;
    }

    public Museum(String title, String description, String location, String open, String photo, String placeId, String rating) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.open = open;
        this.photo = photo;
        this.placeId = placeId;
        this.rating = rating;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Exclude
    public double getLat() {
        return lat;
    }

    @Exclude
    public void setLat(double lat) {
        this.lat = lat;
    }

    @Exclude
    public double getLng() {
        return lng;
    }

    @Exclude
    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getOpen() {
        return open;
    }

    @Exclude
    public String getPlaceId() {
        return placeId;
    }

    public String getPhoto() {
        return photo;
    }

    public String getRating() {
        return rating;
    }

    @Exclude
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

}
