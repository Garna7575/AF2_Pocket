package com.example.apptfc.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.Record;
import com.example.apptfc.R;

import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> records;
    private Context context;

    public RecordAdapter(List<Record> records, Context context) {
        this.records = records;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = records.get(position);

        holder.recordTitle.setText(record.getName() != null ?
                record.getName() : "Sin título");

        holder.recordDate.setText("Publicado: " + record.getFormattedDate());

        holder.btnDownload.setOnClickListener(v -> {
            String url = "http://10.0.2.2:8080/pocket/records/download/" + record.getId();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    public void updateRecords(List<Record> newRecords) {
        records = newRecords;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordTitle, recordDate;
        Button btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordTitle = itemView.findViewById(R.id.recordTitle);
            recordDate = itemView.findViewById(R.id.recordDate);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}