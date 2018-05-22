package com.ali.example.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class BagState implements Parcelable , Serializable {

    public Team team;
    public float rotation;
    public float positionX;
    public float positionY;
    
    public BagState(){
    }

    private BagState(Parcel in) {
        team = in.readParcelable(Team.class.getClassLoader());
        rotation = in.readFloat();
        positionX = in.readFloat();
        positionY = in.readFloat();
    }

    public static final Creator<BagState> CREATOR = new Creator<BagState>() {
        @Override
        public BagState createFromParcel(Parcel in) {
            return new BagState(in);
        }

        @Override
        public BagState[] newArray(int size) {
            return new BagState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(team,flags);
        dest.writeFloat(rotation);
        dest.writeFloat(positionX);
        dest.writeFloat(positionY);
    }
}
