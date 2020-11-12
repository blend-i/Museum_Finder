package no.hiof.museum_finder.model;

public class Account {
    private String firstName;
    private String lastName;
    private String eMail;
    private String profilePictureUrl;

    public Account() {
    }

    public Account(String firstName, String lastName, String eMail, String profilePictureUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.eMail = eMail;
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String geteMail() {
        return eMail;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}
