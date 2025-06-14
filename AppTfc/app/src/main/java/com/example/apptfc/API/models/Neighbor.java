package com.example.apptfc.API.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Neighbor implements Parcelable {
    private int id;
    private String house;
    private User user;

    public Neighbor() {
    }

    public Neighbor(int id, String house, User user) {
        this.id = id;
        this.house = house;
        this.user = user;
    }

    // Constructor para Parcelable
    protected Neighbor(Parcel in) {
        id = in.readInt();
        house = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Neighbor> CREATOR = new Creator<Neighbor>() {
        @Override
        public Neighbor createFromParcel(Parcel in) {
            return new Neighbor(in);
        }

        @Override
        public Neighbor[] newArray(int size) {
            return new Neighbor[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Neighbor{" +
                "id=" + id +
                ", house='" + house + '\'' +
                ", user=" + (user != null ? user.toString() : "null") + // Evitar NullPointerException si user es null
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(house);
        dest.writeParcelable(user, flags);
    }
}