package com.example.apptfc.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Reservation;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    private List<Reservation> reservations;
    private Context context;
    private OnReservationActionListener listener;
    private int expandedPosition = -1;

    public interface OnReservationActionListener {
        void onReservationDeleted();
    }

    public ReservationAdapter(List<Reservation> reservations, Context context, OnReservationActionListener listener) {
        this.reservations = reservations;
        this.context = context;
        this.listener = listener;
    }

    public void updateReservations(List<Reservation> newReservations) {
        this.reservations = newReservations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.bind(reservation);

        final boolean isExpanded = position == expandedPosition;
        holder.buttonsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);

        holder.itemView.setOnClickListener(v -> {
            expandedPosition = isExpanded ? -1 : position;
            notifyDataSetChanged();
        });

        holder.btnDelete.setOnClickListener(v -> {
            deleteReservation(reservation);
        });
    }

    private void deleteReservation(Reservation reservation) {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Void> call = apiService.deleteReservation(reservation.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Reserva eliminada", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onReservationDeleted();
                    }
                } else {
                    Toast.makeText(context, "Error al eliminar la reserva", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }
    public void collapseAll() {
        expandedPosition = -1;
        notifyDataSetChanged();
    }



    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvZoneName, tvTime;
        private LinearLayout buttonsLayout;
        private Button btnDelete;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvZoneName = itemView.findViewById(R.id.tvZoneName);
            tvTime = itemView.findViewById(R.id.tvTime);
            buttonsLayout = itemView.findViewById(R.id.buttonsLayout);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Reservation reservation) {
            tvZoneName.setText(reservation.getCommonArea().getName());
            String timeRange = formatTime(reservation.getStartTime()) + " - " +
                    formatTime(reservation.getEndTime());
            tvTime.setText(timeRange);
        }

        private String formatTime(String isoTime) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(isoTime);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return isoTime;
            }
        }
    }
}