package com.example.apptfc.API;

public class creationNeighborhoods {
    private String name;
    private byte[] image;
    private Admin admin;

    // Getters y setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}

