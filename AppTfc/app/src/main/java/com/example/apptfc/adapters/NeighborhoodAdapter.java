package com.example.apptfc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.apptfc.API.models.Neighborhood;

import java.util.List;

public class NeighborhoodAdapter extends ArrayAdapter<Neighborhood> {

    public NeighborhoodAdapter(@NonNull Context context, List<Neighborhood> neighborhoods) {
        super(context, android.R.layout.simple_dropdown_item_1line, neighborhoods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Neighborhood neighborhood = getItem(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        if (neighborhood != null) {
            textView.setText(neighborhood.getName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}

