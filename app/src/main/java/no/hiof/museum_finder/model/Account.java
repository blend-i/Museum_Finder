package no.hiof.museum_finder.model;

import java.util.List;

/**
 * Model for an account created in the Firebase database
 */
public class Account {
    private String eMail;

    public Account(String eMail) {
        this.eMail = eMail;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }
}
