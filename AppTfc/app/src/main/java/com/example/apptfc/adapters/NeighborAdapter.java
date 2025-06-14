package com.example.apptfc.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.models.Neighbor;
import com.example.apptfc.Activities.admin.NeighborDetailActivity;
import com.example.apptfc.R;

import java.util.List;

public class NeighborAdapter extends RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder> {
    private List<Neighbor> neighbors;

    public NeighborAdapter(List<Neighbor> neighbors, Context context) {
        this.neighbors = neighbors;
    }

    @NonNull
    @Override
    public NeighborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_neighbor, parent, false);
        return new NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NeighborViewHolder holder, int position) {
        Neighbor neighbor = neighbors.get(position);
        holder.tvHouse.setText("Vivienda: " + neighbor.getHouse());

        if (neighbor.getUser() != null) {
            holder.tvName.setText(String.format("%s %s",
                    neighbor.getUser().getName(),
                    neighbor.getUser().getSurname()));
            holder.tvPhone.setText("Tel: " + neighbor.getUser().getTlphNumber());
            holder.tvEmail.setText(neighbor.getUser().getEmail());
        } else {
            holder.tvName.setText("Vecino #" + neighbor.getId());
            holder.tvPhone.setText("Tel: No disponible");
            holder.tvEmail.setText("Email: No disponible");
        }
    }

    @Override
    public int getItemCount() {
        return neighbors != null ? neighbors.size() : 0;
    }

    public void updateData(List<Neighbor> newNeighbors) {
        this.neighbors = newNeighbors;
        notifyDataSetChanged();
    }

    class NeighborViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvHouse, tvPhone, tvEmail;

        public NeighborViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNeighborName);
            tvHouse = itemView.findViewById(R.id.tvNeighborHouse);
            tvPhone = itemView.findViewById(R.id.tvNeighborPhone);
            tvEmail = itemView.findViewById(R.id.tvNeighborEmail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Neighbor neighbor = neighbors.get(position);
                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, NeighborDetailActivity.class);
                        intent.putExtra("NEIGHBOR", neighbor);

                        if (context instanceof Activity) {
                            ((Activity) context).startActivityForResult(intent, 1);
                        } else {
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }
    }
}