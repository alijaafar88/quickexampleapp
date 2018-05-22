package com.ali.example.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;

// Poor man's typealias
public class Scores extends EnumMap<Team, Integer> implements Parcelable, Serializable {
    Scores() {
        super(Team.class);
    }


    private Scores(Parcel in) {
        this();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            this.put(in.readParcelable(Team.class.getClassLoader()), in.readInt());
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Scores createFromParcel(Parcel in) {
            return new Scores(in);
        }

        public Scores[] newArray(int size) {
            return new Scores[size];
        }
    };

    protected Scores(HashMap<Team,Integer> scores) {
        super(scores);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.size());
        for (Team team: this.keySet()) {
            dest.writeParcelable(team, flags);
            dest.writeInt(this.get(team));
        }
    }

}