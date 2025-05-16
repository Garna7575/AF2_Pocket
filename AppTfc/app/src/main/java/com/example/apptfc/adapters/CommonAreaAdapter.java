package com.example.apptfc.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.CommonArea;
import com.example.apptfc.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CommonAreaAdapter extends RecyclerView.Adapter<CommonAreaAdapter.ViewHolder> {
    private List<CommonArea> commonAreas;
    private final OnAreaSelectedListener listener;
    private int selectedPosition = -1;
    private CommonArea selectedArea = null;

    public interface OnAreaSelectedListener {
        void onAreaSelected(CommonArea area);
    }

    public CommonAreaAdapter(List<CommonArea> commonAreas, OnAreaSelectedListener listener) {
        this.commonAreas = commonAreas;
        this.listener = listener;
    }

    public void updateData(List<CommonArea> newData) {
        this.commonAreas = newData;
        notifyDataSetChanged();
    }

    public void setSelectedArea(CommonArea area) {
        this.selectedArea = area;
        this.selectedPosition = findAreaPosition(area);
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < commonAreas.size()) {
            this.selectedPosition = position;
            this.selectedArea = commonAreas.get(position);
            notifyDataSetChanged();

            // Notificar al listener si es necesario
            if (listener != null) {
                listener.onAreaSelected(selectedArea);
            }
        }
    }

    private int findAreaPosition(CommonArea area) {
        if (area == null || commonAreas == null) return -1;

        for (int i = 0; i < commonAreas.size(); i++) {
            if (commonAreas.get(i).getId() == area.getId()) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_common_area, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommonArea area = commonAreas.get(position);
        holder.tvCommonAreaName.setText(area.getName());

        boolean isSelected = position == selectedPosition ||
                (selectedArea != null && selectedArea.getId() == area.getId());

        holder.card.setStrokeWidth(isSelected ? 4 : 0);
        holder.card.setStrokeColor(isSelected ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent) :
                0);

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            if (listener != null) {
                listener.onAreaSelected(area);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commonAreas != null ? commonAreas.size() : 0;
    }

    public CommonArea getSelectedArea() {
        return selectedArea;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvCommonAreaName;
        public final MaterialCardView card;

        public ViewHolder(View view) {
            super(view);
            tvCommonAreaName = view.findViewById(R.id.tvCommonAreaName);
            card = view.findViewById(R.id.cardCommonArea);
        }
    }
}