package com.example.apptfc.API;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private CommonArea commonArea;
    private String startTime;
    private String endTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CommonArea getCommonArea() {
        return commonArea;
    }

    public void setCommonArea(CommonArea commonArea) {
        this.commonArea = commonArea;
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

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", commonArea=" + commonArea +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
