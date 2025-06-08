package com.example.apptfc.API;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {
    private int id;
    private String name, username, surname, tlphNumber, password, email, role;
    private Date birthDate;

    public User() {
    }

    public User(String name, String surname, String email, String username, String tlphNumber, String password, Date birthDate, String role) {
        this.name = name;
        this.username = username;
        this.surname = surname;
        this.tlphNumber = tlphNumber;
        this.password = password;
        this.birthDate = birthDate;
        this.email = email;
        this.role = role;
    }

    public User(int id, String name, String username, String surname, String tlph_number, String password, String email, String role, Date birthDate) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.surname = surname;
        this.tlphNumber = tlph_number;
        this.password = password;
        this.email = email;
        this.role = role;
        this.birthDate = birthDate;
    }

    protected User(Parcel in) {
        id = in.readInt();
        name = in.readString();
        username = in.readString();
        surname = in.readString();
        tlphNumber = in.readString();
        email = in.readString();
        role = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getSurname() {
        return surname;
    }

    public String getTlphNumber() {
        return tlphNumber;
    }

    public String getPassword() {
        return password;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setTlphNumber(String tlphNumber) {
        this.tlphNumber = tlphNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", surname='" + surname + '\'' +
                ", tlphNumber='" + tlphNumber + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(surname);
        dest.writeString(tlphNumber);
        dest.writeString(email);
    }
}