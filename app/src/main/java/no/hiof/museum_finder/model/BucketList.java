package no.hiof.museum_finder.model;

import java.util.List;

public class BucketList {
    private List<Museum> bucketlist;

    public BucketList(List<Museum> bucketlist) {
        this.bucketlist = bucketlist;
    }

    public List<Museum> getBucketlist() {
        return bucketlist;
    }

    public void setBucketlist(List<Museum> bucketlist) {
        this.bucketlist = bucketlist;
    }
}
