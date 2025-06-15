package com.example.apptfc.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.models.CommonArea;
import com.example.apptfc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.List;

public class CommonAreaAdapter extends RecyclerView.Adapter<CommonAreaAdapter.ViewHolder> {
    private List<CommonArea> commonAreas;
    private final OnAreaSelectedListener listener;
    private CommonArea selectedArea = null;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private boolean showDeleteButton = false;

    public interface OnAreaSelectedListener {
        void onAreaSelected(CommonArea area);
        void onDeleteArea(CommonArea area);
    }

    public CommonAreaAdapter(List<CommonArea> commonAreas, OnAreaSelectedListener listener, boolean showDeleteButton) {
        this.commonAreas = commonAreas;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
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
                showDeleteButton = false;
            }
        }
        notifyDataSetChanged();
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

        boolean isSelected = (selectedArea != null && selectedArea.getId() == area.getId());

        int backgroundColor = isSelected ?
                ContextCompat.getColor(holder.itemView.getContext(), R.color.statusResolved) :
                ContextCompat.getColor(holder.itemView.getContext(), R.color.background_light);

        holder.card.setCardBackgroundColor(backgroundColor);

        holder.btnDelete.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            selectedArea = area;
            notifyDataSetChanged();

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
        public final MaterialButton btnDelete;

        public ViewHolder(View view) {
            super(view);
            tvCommonAreaName = view.findViewById(R.id.tvCommonAreaName);
            card = view.findViewById(R.id.cardCommonArea);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}