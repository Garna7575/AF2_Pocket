package com.example.apptfc.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.apptfc.API.Neighbor;
import com.example.apptfc.R;

import java.util.List;

public class NeighborAdapter extends ArrayAdapter<Neighbor> {

    public NeighborAdapter(Context context, List<Neighbor> neighbors) {
        super(context, R.layout.item_neighbor, neighbors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Neighbor neighbor = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_neighbor, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.tvNeighborName);
        TextView tvHouse = convertView.findViewById(R.id.tvNeighborHouse);
        TextView tvPhone = convertView.findViewById(R.id.tvNeighborPhone);
        TextView tvEmail = convertView.findViewById(R.id.tvNeighborEmail);

        tvHouse.setText("Vivienda: " + neighbor.getHouse());

        if (neighbor.getUser() != null) {
            tvName.setText(String.format("%s %s",
                    neighbor.getUser().getName(),
                    neighbor.getUser().getSurname()));
            tvPhone.setText("Tel: " + neighbor.getUser().getTlphNumber());
            tvEmail.setText(neighbor.getUser().getEmail());
        } else {
            tvName.setText("Vecino #" + neighbor.getId());
            tvPhone.setText("Tel: No disponible");
            tvEmail.setText("Email: No disponible");
        }

        return convertView;
    }
}