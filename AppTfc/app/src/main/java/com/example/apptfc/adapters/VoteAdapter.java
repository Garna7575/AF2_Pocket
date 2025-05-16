package com.example.apptfc.adapters;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.Vote;
import com.example.apptfc.Activities.LoginActivity;
import com.example.apptfc.Activities.RegisterActivity;
import com.example.apptfc.R;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteAdapter extends RecyclerView.Adapter<VoteAdapter.VoteViewHolder> {
    private List<Vote> voteList;
    private Context context;

    public VoteAdapter(List<Vote> voteList, Context context) {
        this.voteList = voteList;
        this.context = context;
    }

    @NonNull
    @Override
    public VoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vote_item, parent, false);
        return new VoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoteViewHolder holder, int position) {
        Vote vote = voteList.get(position);
        Log.d("voteBindViewHolder", vote.toString());
        holder.voteTitle.setText(vote.getName());
        holder.voteDescription.setText(vote.getDescription());

        holder.btnVote.setOnClickListener(v -> showVoteOptionsDialog(vote));
    }

    @Override
    public int getItemCount() {
        return voteList.size();
    }

    public static class VoteViewHolder extends RecyclerView.ViewHolder {
        TextView voteTitle, voteDescription;
        Button btnVote;

        public VoteViewHolder(@NonNull View itemView) {
            super(itemView);
            voteTitle = itemView.findViewById(R.id.voteTitle);
            voteDescription = itemView.findViewById(R.id.voteDescription);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }

    private void showVoteOptionsDialog(Vote vote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Selecciona tu voto")
                .setMessage("¿Cómo quieres votar en '" + vote.getName() + "'?")
                .setPositiveButton("A favor", (dialog, which) -> confirmVote(vote, true))
                .setNegativeButton("En contra", (dialog, which) -> confirmVote(vote, false))
                .setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void confirmVote(Vote vote, boolean inFavor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar voto")
                .setMessage("No podrás cambiar tu voto. ¿Estás seguro?")
                .setPositiveButton("Sí, votar", (dialog, which) -> sendVote(vote, inFavor))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendVote(Vote vote, boolean inFavor) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        int neighborId = prefs.getInt("neighborId", -1);
        Log.d("voteAdapter", "neighborId obtenido en Adapter: " + neighborId);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<ResponseBody> call = apiService.vote(neighborId, vote.getId(), inFavor);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("voteAdapter", "URL generada: " + call.request().url().toString());
                Log.d("voteAdapter", "neighborId: " + neighborId + ", voteId: " + vote.getId() + ", inFavor: " + inFavor);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseText = response.body().string();
                        Log.d("voteAdapter", "Respuesta de la API: " + responseText);
                        Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error al leer respuesta", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String responseText = response.body().string();
                        Toast.makeText(context, responseText, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Error en la conexión", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
