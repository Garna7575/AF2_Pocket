package com.example.apptfc.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.Receipt;
import com.example.apptfc.R;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ViewHolder> {
    private final List<Receipt> receipts;
    private final OnReceiptClickListener listener;

    public ReceiptAdapter(List<Receipt> receipts, OnReceiptClickListener listener) {
        this.receipts = receipts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receipt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receipt receipt = receipts.get(position);
        holder.bind(receipt);
        holder.itemView.setOnClickListener(v -> listener.onReceiptClick(receipt));
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public void updateReceipts(List<Receipt> newReceipts) {
        this.receipts.clear();
        this.receipts.addAll(newReceipts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvAmount, tvDate, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReceiptTitle);
            tvAmount = itemView.findViewById(R.id.tvReceiptAmount);
            tvDate = itemView.findViewById(R.id.tvReceiptDate);
            tvStatus = itemView.findViewById(R.id.tvReceiptStatus);
        }

        public void bind(Receipt receipt) {
            tvTitle.setText(receipt.getTitle());
            tvAmount.setText(String.format(Locale.getDefault(), "%.2f €", receipt.getValue()));
            tvDate.setText(formatDate(receipt.getDate()));
            tvStatus.setText(receipt.isPaid() ? "Pagado" : "Pendiente");
            tvStatus.setTextColor(receipt.isPaid() ? Color.GREEN : Color.RED);
        }

        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }

    public interface OnReceiptClickListener {
        void onReceiptClick(Receipt receipt);
    }
}
