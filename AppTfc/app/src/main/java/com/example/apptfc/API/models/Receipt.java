package com.example.apptfc.API.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Receipt implements Parcelable {
    private int id;
    private String title;
    private String description;
    private double value;
    private boolean paid;
    private Date date;
    private int neighborId;

    // Constructor usado por Parcelable
    protected Receipt(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        value = in.readDouble();
        paid = in.readByte() != 0;
        long dateMillis = in.readLong();
        date = new Date(dateMillis);
        neighborId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeDouble(value);
        dest.writeByte((byte) (paid ? 1 : 0));
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeInt(neighborId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Receipt> CREATOR = new Creator<Receipt>() {
        @Override
        public Receipt createFromParcel(Parcel in) {
            return new Receipt(in);
        }

        @Override
        public Receipt[] newArray(int size) {
            return new Receipt[size];
        }
    };

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getNeighborId() { return neighborId; }
    public void setNeighborId(int neighborId) { this.neighborId = neighborId; }

    public String getFormattedDate() {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
