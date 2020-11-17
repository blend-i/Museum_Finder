package no.hiof.museum_finder.model;

import java.util.List;

public class Account {
    private String UUID;
    private String eMail;
    private BucketList bucketList;

    public Account(String UUID, String eMail, BucketList bucketList) {
        this.UUID = UUID;
        this.eMail = eMail;
        this.bucketList = bucketList;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
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
