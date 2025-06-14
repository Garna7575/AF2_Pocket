package com.example.apptfc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.models.Incidence;
import com.example.apptfc.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidenceAdapter extends RecyclerView.Adapter<IncidenceAdapter.IncidenceViewHolder> {

    private List<Incidence> incidences;

    public IncidenceAdapter(List<Incidence> incidences) {
        this.incidences = incidences;
    }

    @NonNull
    @Override
    public IncidenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.incidences_item, parent, false);
        return new IncidenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenceViewHolder holder, int position) {
        Incidence incidence = incidences.get(position);

        holder.title.setText(incidence.getTitle());
        holder.description.setText(incidence.getContent());

        if (incidence.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            holder.date.setText(sdf.format(incidence.getDate()));
        } else {
            holder.date.setText("Fecha no disponible");
        }

    }

    @Override
    public int getItemCount() {
        return incidences != null ? incidences.size() : 0;
    }

    public void updateData(List<Incidence> newIncidences) {
        this.incidences = newIncidences;
        notifyDataSetChanged();
    }

    static class IncidenceViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date, author;

        public IncidenceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            date = itemView.findViewById(R.id.date);
            author = itemView.findViewById(R.id.author);
        }
    }
}
