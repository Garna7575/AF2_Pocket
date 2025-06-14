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

import com.example.apptfc.API.models.Neighborhood;
import com.example.apptfc.R;

import java.util.List;

public class AdminNeighborhoodAdapter extends ArrayAdapter<Neighborhood> {

    private Context context;

    public AdminNeighborhoodAdapter(@NonNull Context context, List<Neighborhood> neighborhoods) {
        super(context, 0, neighborhoods);
        this.context = context;
    }

    private static class NeighborhoodViewHolder {
        ImageView imageView;
        TextView nameView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        NeighborhoodViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_neighborhood, parent, false);
            holder = new NeighborhoodViewHolder();
            holder.imageView = convertView.findViewById(R.id.neighborhoodImage);
            holder.nameView = convertView.findViewById(R.id.neighborhoodName);
            convertView.setTag(holder);
        } else {
            holder = (NeighborhoodViewHolder) convertView.getTag();
        }

        Neighborhood neighborhood = getItem(position);
        if (neighborhood != null) {
            holder.nameView.setText(neighborhood.getName());
            if (neighborhood.getImage() != null) {
                new ImageLoaderTask(holder.imageView).execute(neighborhood.getImage());
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_neighborhood);
            }
        }
        return convertView;
    }

    private static class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public ImageLoaderTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                String imageData = strings[0];
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
    }
}
