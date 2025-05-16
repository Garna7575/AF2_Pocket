package com.example.apptfc.adapters;

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

import java.util.Collections;
import java.util.List;

public class CommonAreaAdapter extends RecyclerView.Adapter<CommonAreaAdapter.ViewHolder> {
    private List<CommonArea> commonAreas;
    private final OnAreaSelectedListener listener;
    private CommonArea selectedArea = null;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnAreaSelectedListener {
        void onAreaSelected(CommonArea area);
    }

    public CommonAreaAdapter(List<CommonArea> commonAreas, OnAreaSelectedListener listener) {
        this.commonAreas = commonAreas;
        this.listener = listener;
    }

    public void updateData(List<CommonArea> newData) {
        if (newData != null) {
            Collections.sort(newData, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        }

        this.commonAreas = newData;
        if (selectedArea != null) {
            selectedPosition = RecyclerView.NO_POSITION;
            for (int i = 0; i < newData.size(); i++) {
                if (newData.get(i).getId() == selectedArea.getId()) {
                    selectedPosition = i;
                    selectedArea = newData.get(i);
                    break;
                }
            }
            if (selectedPosition == RecyclerView.NO_POSITION) {
                selectedArea = null;
            }
        }
        notifyDataSetChanged();
    }


    public void setSelectedArea(CommonArea area) {
        this.selectedArea = area;
        if (commonAreas != null) {
            for (int i = 0; i < commonAreas.size(); i++) {
                if (commonAreas.get(i).getId() == area.getId()) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < commonAreas.size()) {
            selectedArea = commonAreas.get(position);
            selectedPosition = position;
            notifyDataSetChanged();
        }
    }

    public CommonArea getSelectedArea() {
        return selectedArea;
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

        boolean isSelected = position == selectedPosition;

        holder.card.setStrokeWidth(isSelected ? 4 : 0);
        holder.card.setStrokeColor(isSelected ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent) :
                0);

        holder.itemView.setOnClickListener(v -> {
            selectedArea = area;
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onAreaSelected(area);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commonAreas != null ? commonAreas.size() : 0;
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

