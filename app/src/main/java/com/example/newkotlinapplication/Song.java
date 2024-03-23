package com.example.newkotlinapplication;

import android.net.Uri;

public class Song {
    private long id;
    private String name;
    private String duration;
    private String artist;
    private Uri cover;

    public Song(long id, String name, String duration, String artist, Uri cover) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.artist = artist;
        this.cover = cover;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Uri getCover() {
        return cover;
    }

    public void setCover(Uri cover) {
        this.cover = cover;
    }
}