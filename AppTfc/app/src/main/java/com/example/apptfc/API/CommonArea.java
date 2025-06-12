package com.example.apptfc.API;

public class CommonArea {
    private int id;
    private String name;
    private int neighborhoodId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNeighborhoodId() {
        return neighborhoodId;
    }

    public void setNeighborhoodId(int neighborhoodId) {
        this.neighborhoodId = neighborhoodId;
    }

    @Override
    public String toString() {
        return "CommonArea{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", neighborhoodId=" + neighborhoodId +
                '}';
    }
}
