package no.hiof.museum_finder.model;

import java.util.List;

public class Account {
    private String eMail;
    private List<Museum> bucketList;

    public Account(String eMail, List<Museum> bucketList) {
        this.eMail = eMail;
        this.bucketList = bucketList;
    }

    public Account(String eMail) {
        this.eMail = eMail;

    }


    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public List<Museum> getBucketList() {
        return bucketList;
    }

    public void setBucketList(List<Museum> bucketList) {
        this.bucketList = bucketList;
    }
}
