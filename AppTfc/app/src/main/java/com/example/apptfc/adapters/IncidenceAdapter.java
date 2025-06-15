package com.example.apptfc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.models.Incidence;
import com.example.apptfc.R;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidenceAdapter extends RecyclerView.Adapter<IncidenceAdapter.IncidenceViewHolder> {

    public interface OnIncidenceClickListener {
        void onIncidenceClick(Incidence incidence);
        void onResolveClick(Incidence incidence);
    }

    private List<Incidence> incidences;
    private OnIncidenceClickListener listener;
    private int selectedPosition = -1;
    private boolean showResolveButton; // Controla si se muestra el botón

    public IncidenceAdapter(List<Incidence> incidences, OnIncidenceClickListener listener, boolean showResolveButton) {
        this.incidences = incidences;
        this.listener = listener;
        this.showResolveButton = showResolveButton;
    }

    @NonNull
    @Override
    public IncidenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidences, parent, false);
        return new IncidenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenceViewHolder holder, int position) {
        Incidence incidence = incidences.get(position);
        boolean isSelected = position == selectedPosition;

        holder.title.setText(incidence.getTitle());
        holder.description.setText(incidence.getContent());

        if (incidence.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            holder.date.setText(sdf.format(incidence.getDate()));
        } else {
            holder.date.setText("Fecha no disponible");
        }

        // Mostrar botón solo si está activado y el item está seleccionado
        boolean shouldShowButton = showResolveButton && isSelected;
        holder.btnResolve.setVisibility(shouldShowButton ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected == selectedPosition) {
                selectedPosition = -1; // Deseleccionar si es el mismo item
            }

            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onIncidenceClick(incidence);
            }
        });

        holder.btnResolve.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResolveClick(incidence);
            }
        });
    }

    @Override
    public int getItemCount() {
        return incidences != null ? incidences.size() : 0;
    }

    public void updateData(List<Incidence> newIncidences) {
        this.incidences = newIncidences;
        notifyDataSetChanged();
    }

    public void setShowResolveButton(boolean show) {
        this.showResolveButton = show;
        notifyDataSetChanged();
    }

    static class IncidenceViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        MaterialButton btnResolve;

        public IncidenceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            date = itemView.findViewById(R.id.date);
            btnResolve = itemView.findViewById(R.id.btnResolve);
        }
    }
}