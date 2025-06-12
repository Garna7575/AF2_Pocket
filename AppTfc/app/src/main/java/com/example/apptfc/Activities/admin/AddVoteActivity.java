package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.Vote;
import com.example.apptfc.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVoteActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button btnSubmit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vote);

        etTitle = findViewById(R.id.etVoteTitle);
        etDescription = findViewById(R.id.etVoteDescription);
        btnSubmit = findViewById(R.id.btnSubmitVote);

        btnSubmit.setOnClickListener(v -> submitVote());
    }

    private void submitVote() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Creando votación...");

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);

        Vote newVote = new Vote();
        newVote.setName(title);
        newVote.setDescription(description);
        newVote.setNeighborhoodId(neighborhoodId);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Void> call = apiService.createVote(newVote);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    Toast.makeText(AddVoteActivity.this, "Votación creada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddVoteActivity.this, "Error al crear votación", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                hideLoading();
                Toast.makeText(AddVoteActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}