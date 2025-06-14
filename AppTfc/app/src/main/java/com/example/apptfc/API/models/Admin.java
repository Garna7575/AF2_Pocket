package com.example.apptfc.API.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Admin {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("registrationNumber")
    @Expose
    private String registrationNumber;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
}
