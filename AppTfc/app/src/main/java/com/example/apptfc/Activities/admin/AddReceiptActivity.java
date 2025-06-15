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
import com.example.apptfc.API.models.Neighbor;
import com.example.apptfc.API.models.Receipt;
import com.example.apptfc.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddReceiptActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etAmount;
    private Button btnSubmit;
    private ProgressDialog progressDialog;
    private SharedPreferences prefs;
    private int neighborhoodId;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_receipt);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        neighborhoodId = prefs.getInt("neighborhoodId", -1);
        apiService = RetrofitClient.get().create(ApiService.class);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etReceiptTitle);
        etDescription = findViewById(R.id.etReceiptDescription);
        etAmount = findViewById(R.id.etReceiptAmount);
        btnSubmit = findViewById(R.id.btnSubmitReceipt);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                createReceiptsForAllNeighbors();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Título requerido");
            isValid = false;
        }

        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Descripción requerida");
            isValid = false;
        }

        if (etAmount.getText().toString().trim().isEmpty()) {
            etAmount.setError("Monto requerido");
            isValid = false;
        } else {
            try {
                Double.parseDouble(etAmount.getText().toString());
            } catch (NumberFormatException e) {
                etAmount.setError("Monto inválido");
                isValid = false;
            }
        }

        return isValid;
    }

    private void createReceiptsForAllNeighbors() {
        showLoading("Creando recibos...");
        apiService.getNeighborsByNeighborhood(neighborhoodId).enqueue(new Callback<List<Neighbor>>() {
            @Override
            public void onResponse(Call<List<Neighbor>> call, Response<List<Neighbor>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    createReceiptsForNeighbors(response.body());
                } else {
                    hideLoading();
                    Toast.makeText(AddReceiptActivity.this,
                            "No se encontraron vecinos en esta comunidad",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Neighbor>> call, Throwable t) {
                hideLoading();
                Toast.makeText(AddReceiptActivity.this,
                        "Error al obtener vecinos: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReceiptsForNeighbors(List<Neighbor> neighbors) {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        Date currentDate = new Date();

        int totalNeighbors = neighbors.size();
        final int[] completedRequests = {0};
        final boolean[] hasError = {false};

        for (Neighbor neighbor : neighbors) {
            Receipt receipt = new Receipt();
            receipt.setTitle(title);
            receipt.setDescription(description);
            receipt.setValue(amount);
            receipt.setPaid(false);
            receipt.setDate(currentDate);
            receipt.setNeighborId(neighbor.getId());

            apiService.createReceipt(receipt).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    completedRequests[0]++;
                    if (!response.isSuccessful()) {
                        hasError[0] = true;
                    }

                    checkCompletion(totalNeighbors, completedRequests[0], hasError[0]);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    completedRequests[0]++;
                    hasError[0] = true;
                    checkCompletion(totalNeighbors, completedRequests[0], hasError[0]);
                }
            });
        }
    }

    private void checkCompletion(int total, int completed, boolean hasError) {
        if (completed == total) {
            hideLoading();
            if (hasError) {
                Toast.makeText(this,
                        "Algunos recibos no se crearon correctamente",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Todos los recibos se crearon exitosamente",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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