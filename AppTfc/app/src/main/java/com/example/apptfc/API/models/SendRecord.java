package com.example.apptfc.API.models;

import android.util.Base64;
import com.google.gson.annotations.SerializedName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SendRecord {
    private String title;
    private String description;

    @SerializedName("date")
    private String dateString;

    private String fileBase64;
    private String fileName;
    private int neighborhoodId;

    private static final SimpleDateFormat apiFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());

    public SendRecord(String title, String description, Date date,
                      String fileBase64, String fileName, int neighborhoodId) {
        this.title = title;
        this.description = description;
        this.dateString = apiFormat.format(date);
        this.fileBase64 = fileBase64;
        this.fileName = fileName;
        this.neighborhoodId = neighborhoodId;
    }

    public static String convertFileToBase64(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        byte[] fileBytes = outputStream.toByteArray();
        return Base64.encodeToString(fileBytes, Base64.DEFAULT);
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDateString() { return dateString; }
    public String getFileBase64() { return fileBase64; }
    public String getFileName() { return fileName; }
    public int getNeighborhoodId() { return neighborhoodId; }
}