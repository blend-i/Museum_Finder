package no.hiof.museum_finder.model;

import java.util.Map;

public class BucketList {
    private Map<String,Museum> bucketlist;

    public BucketList(Map<String, Museum> bucketlist) {
        this.bucketlist = bucketlist;
    }

    public Map<String, Museum> getBucketlist() {
        return bucketlist;
    }

    public void setBucketlist(Map<String, Museum> bucketlist) {
        this.bucketlist = bucketlist;
    }
}
