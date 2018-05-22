package com.ali.example.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import com.ali.example.R;

/**
 * Created by pdv on 2/28/17.
 */

public enum Team implements Parcelable{
    RED,
    BLUE;

    @StringRes
    public int getDisplayNameRes() {
        switch (this) {
            case RED:
                return R.string.red;
            case BLUE:
                return R.string.blue;
        }
        return -1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Team> CREATOR = new Parcelable.Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return Team.values()[in.readInt()];
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };
}
