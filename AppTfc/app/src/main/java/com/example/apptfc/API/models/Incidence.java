package com.example.apptfc.API.models;

import java.util.Date;

public class Incidence {
    int id;
    private String title;
    private String content;
    private String author;
    private Date date;

    public Incidence(String title, String description, String author, Date date) {
        this.title = title;
        this.content = description;
        this.author = author;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
