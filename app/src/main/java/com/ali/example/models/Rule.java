package com.ali.example.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.ali.example.R;

import java.io.Serializable;

public enum Rule implements Parcelable ,Serializable {
    OVER_21(R.string.change_rules_to_over_21),
    EQUALS_21(R.string.change_rules_to_equals_21);
    public final int ruleChangeTextResource;

    Rule(int ruleChangeTextResource){
        this.ruleChangeTextResource = ruleChangeTextResource;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Rule> CREATOR = new Creator<Rule>() {
        @Override
        public Rule createFromParcel(Parcel in) {
            return Rule.values()[in.readInt()];
        }

        @Override
        public Rule[] newArray(int size) {
            return new Rule[size];
        }
    };


}
