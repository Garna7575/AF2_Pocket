package com.example.apptfc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.apptfc.API.Incidence;
import com.example.apptfc.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidenceTabAdapter extends ArrayAdapter<Incidence> {

    public IncidenceTabAdapter(Context context, List<Incidence> incidences) {
        super(context, R.layout.incidences_item, incidences);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Incidence incidence = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.incidences_item, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.title);
        TextView tvDescription = convertView.findViewById(R.id.description);
        TextView tvDate = convertView.findViewById(R.id.date);
        TextView tvAuthor = convertView.findViewById(R.id.author);

        tvTitle.setText(incidence.getTitle());
        tvDescription.setText(incidence.getContent());
        tvAuthor.setText(incidence.getAuthor());

        if (incidence.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            tvDate.setText(sdf.format(incidence.getDate()));
        } else {
            tvDate.setText("Fecha no disponible");
        }

        return convertView;
    }
}