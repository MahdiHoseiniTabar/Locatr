package com.example.locatr;

public class GalleryItem {
    private String mId;
    private String mTitle;
    private String mUrl;
    private double lat;
    private double lon;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public GalleryItem(String id, String title, String url) {
        mId = id;
        mTitle = title;
        mUrl = url;
    }

    @Override
    public String toString() {
        return "GalleryItem{" +
                "mTitle='" + mTitle + '\'' +
                '}';
    }
}
