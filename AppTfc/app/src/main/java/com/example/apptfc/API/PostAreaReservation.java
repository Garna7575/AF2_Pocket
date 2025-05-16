package com.example.apptfc.API;

public class PostAreaReservation {
    private int id;
    private int commonAreaId;

    private int neighborId;
    private String startTime;
    private String endTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommonAreaId() {
        return commonAreaId;
    }

    public void setCommonAreaId(int commonAreaId) {
        this.commonAreaId = commonAreaId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getNeighborId() {
        return neighborId;
    }

    public void setNeighborId(int neighborId) {
        this.neighborId = neighborId;
    }
}
