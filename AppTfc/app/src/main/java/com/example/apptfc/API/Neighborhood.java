package com.example.apptfc.API;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Neighborhood {
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
}

