package com.example.apptfc.Activities.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.models.VoteResult;
import com.example.apptfc.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteDetailActivity extends AppCompatActivity {

    private TextView txtFavor, txtContra, txtTotal, txtFavorPercent, txtContraPercent;
    private LinearLayout progressFavor, progressContra;
    private Button btnEndVote;
    private int voteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);

        setupViews();

        voteId = getIntent().getIntExtra("voteId", -1);

        if (voteId != -1) {
            fetchVoteDetails(voteId);
        }

        btnEndVote.setOnClickListener(v -> confirmEndVote());
    }

    private void setupViews() {
        txtFavor = findViewById(R.id.txtFavor);
        txtContra = findViewById(R.id.txtContra);
        txtTotal = findViewById(R.id.txtTotal);
        txtFavorPercent = findViewById(R.id.txtFavorPercent);
        txtContraPercent = findViewById(R.id.txtContraPercent);
        progressFavor = findViewById(R.id.progressFavor);
        progressContra = findViewById(R.id.progressContra);
        btnEndVote = findViewById(R.id.btnEndVote);
    }

    private void fetchVoteDetails(int id) {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.getVotesResult(id).enqueue(new Callback<VoteResult>() {
            @Override
            public void onResponse(Call<VoteResult> call, Response<VoteResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    VoteResult result = response.body();
                    updateUI(result);
                } else {
                    Toast.makeText(VoteDetailActivity.this, "Error al obtener detalles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VoteResult> call, Throwable t) {
                Log.e("VoteDetailsActivity", "Error al cargar resultados", t);
                Toast.makeText(VoteDetailActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(VoteResult result) {
        // Valores numéricos
        txtFavor.setText(String.valueOf(result.getInFavor()));
        txtContra.setText(String.valueOf(result.getAgainst()));
        txtTotal.setText(String.valueOf(result.getTotal()));

        // Calcular porcentajes
        int total = result.getTotal();
        if (total > 0) {
            int favorPercent = (int) (((float) result.getInFavor() / total) * 100);
            int contraPercent = 100 - favorPercent;

            txtFavorPercent.setText(favorPercent + "%");
            txtContraPercent.setText(contraPercent + "%");

            // Ajustar barras de progreso
            progressFavor.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    result.getInFavor()));

            progressContra.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    result.getAgainst()));
        } else {
            txtFavorPercent.setText("0%");
            txtContraPercent.setText("0%");
        }
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
                if (response.isSuccessful()) {
                    Toast.makeText(VoteDetailActivity.this, "Votación finalizada", Toast.LENGTH_SHORT).show();
                    btnEndVote.setEnabled(false);
                    btnEndVote.setAlpha(0.5f);
                } else {
                    Toast.makeText(VoteDetailActivity.this, "Error al finalizar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(VoteDetailActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
