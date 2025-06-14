package com.example.apptfc.API.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Neighborhood implements Parcelable {
    private int id;
    private String name;
    @SerializedName("base64Image")
    private String image;
    List<Record> records = new ArrayList<>();
    public Neighborhood(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public List<Record> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        return name;
    }

    protected Neighborhood(Parcel in) {
        id = in.readInt();
        name = in.readString();
        image = in.readString();
        records = in.createTypedArrayList(Record.CREATOR);
    }

    public static final Creator<Neighborhood> CREATOR = new Creator<Neighborhood>() {
        @Override
        public Neighborhood createFromParcel(Parcel in) {
            return new Neighborhood(in);
        }

        @Override
        public Neighborhood[] newArray(int size) {
            return new Neighborhood[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(image);
        dest.writeTypedList(records);
    }
}

