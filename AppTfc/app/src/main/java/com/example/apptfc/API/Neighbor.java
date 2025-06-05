package com.example.apptfc.API;

public class Neighbor {
    private int id;
    private String house;
    private User user;

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
                ", user=" + user +
                '}';
    }
}
