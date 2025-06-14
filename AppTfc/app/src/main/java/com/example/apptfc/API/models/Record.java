package com.example.apptfc.API.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Record implements Parcelable {
    private int id;
    private String title;
    private String description;

    @SerializedName("date")
    private String dateString;

    private static final SimpleDateFormat apiFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
    private static final SimpleDateFormat displayFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public int getId() { return id; }
    public String getTitle() { return title; }
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

    public Record() {}

    protected Record(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        dateString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(dateString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Record> CREATOR = new Creator<Record>() {
        @Override
        public Record createFromParcel(Parcel in) {
            return new Record(in);
        }

        @Override
        public Record[] newArray(int size) {
            return new Record[size];
        }
    };
}
