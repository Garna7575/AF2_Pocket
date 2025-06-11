package com.example.apptfc.Activities.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.VoteResult;
import com.example.apptfc.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteDetailActivity extends AppCompatActivity {

    private TextView txtFavor, txtContra, txtTotal;
    private Button btnEndVote;
    private int voteId;
    private static final String TAG = "VoteDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);

        txtFavor = findViewById(R.id.txtFavor);
        txtContra = findViewById(R.id.txtContra);
        txtTotal = findViewById(R.id.txtTotal);
        btnEndVote = findViewById(R.id.btnEndVote);

        voteId = getIntent().getIntExtra("voteId", -1);

        if (voteId != -1) {
            fetchVoteDetails(voteId);
        }

        btnEndVote.setOnClickListener(v -> confirmEndVote());
    }

    private void fetchVoteDetails(int id) {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getVotesResult(id).enqueue(new Callback<VoteResult>() {
            @Override
            public void onResponse(Call<VoteResult> call, Response<VoteResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VoteResult result = response.body();
                    txtFavor.setText("A favor: " + result.getInFavor());
                    txtContra.setText("En contra: " + result.getAgainst());
                    txtTotal.setText("Total: " + result.getTotal());
                } else {
                    Toast.makeText(VoteDetailActivity.this, "Error al obtener detalles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoteResult> call, Throwable t) {
                Log.e(TAG, "Error al cargar resultados", t);
                Toast.makeText(VoteDetailActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void confirmEndVote() {
        new AlertDialog.Builder(this)
                .setTitle("Finalizar votación")
                .setMessage("¿Estás seguro de finalizar esta votación? No se podrán realizar más votos.")
                .setPositiveButton("Finalizar", (dialog, which) -> endVote())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void endVote() {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.endVote(voteId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(VoteDetailActivity.this, "Votación finalizada", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(VoteDetailActivity.this, "Error al finalizar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
