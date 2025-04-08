package com.example.apptfc.API;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Record {
    private int id;
    private String name;
    private String description;

    @SerializedName("date")
    private String dateString;

    private static final SimpleDateFormat apiFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
    private static final SimpleDateFormat displayFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public String getFormattedDate() {
        if (dateString != null) {
            try {
                Date date = apiFormat.parse(dateString);
                return displayFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return "Fecha inválida";
            }
        }
        return "No disponible";
    }
}