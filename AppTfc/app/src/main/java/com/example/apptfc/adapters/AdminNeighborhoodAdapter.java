package com.example.apptfc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class AdminNeighborhoodAdapter extends ArrayAdapter<Neighborhood> {

    private static final int TYPE_NEIGHBORHOOD = 0;
    private static final int TYPE_ADD_ITEM = 1;
    private boolean showAddItem = false;

    public AdminNeighborhoodAdapter(@NonNull Context context, List<Neighborhood> neighborhoods) {
        super(context, 0, neighborhoods);
    }

    public void setShowAddItem(boolean show) {
        this.showAddItem = show;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return super.getCount() + (showAddItem ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return (showAddItem && position == getRegularItemCount()) ? TYPE_ADD_ITEM : TYPE_NEIGHBORHOOD;
    }

    private int getRegularItemCount() {
        return super.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (getItemViewType(position) == TYPE_ADD_ITEM) {
            return createAddItemView(convertView, parent);
        }
        return createNeighborhoodView(position, convertView, parent);
    }

    private View createNeighborhoodView(int position, View convertView, ViewGroup parent) {
        NeighborhoodViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_neighborhood, parent, false);
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
            loadImage(neighborhood.getImage(), holder.imageView);
        }

        return convertView;
    }

    private View createAddItemView(View convertView, ViewGroup parent) {
        AddItemViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_add_neighborhood, parent, false);
            holder = new AddItemViewHolder();
            holder.addIcon = convertView.findViewById(R.id.addIcon);
            holder.addText = convertView.findViewById(R.id.addText);
            convertView.setTag(holder);
        } else {
            holder = (AddItemViewHolder) convertView.getTag();
        }

        holder.addText.setText("Agregar comunidad");
        holder.addIcon.setImageResource(R.drawable.ic_add);
        holder.addIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.teal_700));

        return convertView;
    }

    private void loadImage(String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_neighborhood)
                    .error(R.drawable.placeholder_neighborhood)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_neighborhood);
        }
    }

    private static class NeighborhoodViewHolder {
        ImageView imageView;
        TextView nameView;
    }

    private static class AddItemViewHolder {
        ImageView addIcon;
        TextView addText;
    }
}