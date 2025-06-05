package com.example.apptfc.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.R;

import java.util.List;

public class AdminNeighborhoodAdapter extends ArrayAdapter<Neighborhood> {

    public AdminNeighborhoodAdapter(@NonNull Context context, List<Neighborhood> neighborhoods) {
        super(context, 0, neighborhoods);
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView recordsView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_neighborhood, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.neighborhoodImage);
            holder.nameView = convertView.findViewById(R.id.neighborhoodName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Neighborhood neighborhood = getItem(position);

        if (neighborhood != null) {
            holder.nameView.setText(neighborhood.getName());

            int recordsCount = neighborhood.getRecords() != null ? neighborhood.getRecords().size() : 0;

            // Carga condicional de imagen
            if (neighborhood.getImage() != null) {
                loadImage(neighborhood.getImage(), holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_neighborhood);
            }
        }

        return convertView;
    }

    private void loadImage(String base64Image, ImageView imageView) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... strings) {
                try {
                    String imageData = strings[0];
                    // Elimina el prefijo si existe (ej: "data:image/png;base64,")
                    if (imageData.contains(",")) {
                        imageData = imageData.split(",")[1];
                    }

                    byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                } catch (Exception e) {
                    Log.e("ImageLoad", "Error decoding image: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.placeholder_neighborhood);
                }
            }
        }.execute(base64Image);
    }
}