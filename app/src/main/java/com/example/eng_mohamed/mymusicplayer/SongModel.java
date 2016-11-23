package com.example.eng_mohamed.mymusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eng_Mohamed on 11/8/2016.
 */

public class SongModel implements Parcelable {
    private long id;
    private String title;
    private String artist;

    public SongModel(long ID, String Title, String Artist) {
        id=ID;
        title=Title;
        artist=Artist;
    }

    protected SongModel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
    }

    public static final Creator<SongModel> CREATOR = new Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel in) {
            return new SongModel(in);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
    }
}
