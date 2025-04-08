package com.example.apptfc.API;

public class User {
    private int id;
    private String name, username, surname, tlphNumber, password, email, role;
    private int age;

    public User(String name, String surname, String email, String username, String tlphNumber, String password, int age, String role) {
        this.name = name;
        this.username = username;
        this.surname = surname;
        this.tlphNumber = tlphNumber;
        this.password = password;
        this.age = age;
        this.email = email;
        this.role = role;
    }

    public User(int id, String name, String username, String surname, String tlph_number, String password, String email, String role, int age) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.surname = surname;
        this.tlphNumber = tlph_number;
        this.password = password;
        this.email = email;
        this.role = role;
        this.age = age;
    }

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

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
