package no.hiof.museum_finder.model;

import java.util.List;

public class Account {
    private String eMail;
    private BucketList bucketList;

    public Account(String eMail, BucketList bucketList) {
        this.eMail = eMail;
        this.bucketList = bucketList;
    }

    public String geteMail() {
        return eMail;
    }

    public BucketList getBucketList() {
        return bucketList;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public void setBucketList(BucketList bucketList) {
        this.bucketList = bucketList;
    }
}
