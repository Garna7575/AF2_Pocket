package com.example.apptfc.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Receipt;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;
import com.example.apptfc.adapters.ReceiptAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptListActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private RecyclerView recyclerView;
    private ReceiptAdapter adapter;
    private List<Receipt> receiptList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_list);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        setupRecyclerView(receiptList);
        loadReceipts();
    }

    private void setupRecyclerView(List<Receipt> initialReceipts) {
        recyclerView = findViewById(R.id.receiptsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReceiptAdapter(initialReceipts, new ReceiptAdapter.OnReceiptClickListener() {
            @Override
            public void onReceiptClick(Receipt receipt) {
                Intent intent = new Intent(ReceiptListActivity.this, ReceiptDetailActivity.class);
                intent.putExtra("receipt", (Parcelable) receipt);
                startActivityForResult(intent, 1);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadReceipts() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando recibos...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        int neighborId = prefs.getInt("neighborId", -1);
        if (neighborId == -1) {
            progressDialog.dismiss();
            showError("No se pudo identificar al usuario");
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<List<Receipt>> call = apiService.getReceiptsByNeighbor(neighborId);

        call.enqueue(new Callback<List<Receipt>>() {
            @Override
            public void onResponse(Call<List<Receipt>> call, Response<List<Receipt>> response) {
                progressDialog.dismiss();

                try {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            processReceipts(response.body());
                        } else {
                            showError("No se encontraron recibos");
                        }
                    } else {
                        handleApiError(response.code());
                    }
                } catch (Exception e) {
                    Log.e("ProcessError", "Error procesando respuesta", e);
                    showError("Error procesando datos");
                }
            }

            @Override
            public void onFailure(Call<List<Receipt>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e("NetworkFailure", "Error real: ", t);
                showError("Error de conexión. Intente nuevamente");
            }
        });
    }

    private void processReceipts(List<Receipt> receipts) {
        try {
            // Validar y parsear fechas
            for (Receipt receipt : receipts) {
                if (receipt.getDate() == null) {
                    throw new IllegalArgumentException("Fecha inválida en recibo: " + receipt.getId());
                }
            }

            // Filtrar últimos 3 meses
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -3);
            Date threeMonthsAgo = calendar.getTime();

            List<Receipt> filteredReceipts = new ArrayList<>();
            for (Receipt receipt : receipts) {
                if (receipt.getDate().after(threeMonthsAgo)) {
                    filteredReceipts.add(receipt);
                }
            }

            // Ordenar por fecha (más reciente primero)
            Collections.sort(filteredReceipts, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));

            // Actualizar UI
            runOnUiThread(() -> {
                if (adapter == null) {
                    setupRecyclerView(filteredReceipts);
                } else {
                    adapter.updateReceipts(filteredReceipts);
                }
            });

        } catch (Exception e) {
            Log.e("ProcessError", "Error procesando recibos", e);
            showError("Error en los datos recibidos");
        }
    }

    private void handleApiError(int errorCode) {
        String errorMsg = "Error al cargar recibos";
        switch (errorCode) {
            case 404:
                errorMsg = "No se encontraron recibos";
                break;
            case 500:
                errorMsg = "Error del servidor";
                break;
        }
        showError(errorMsg);
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(ReceiptListActivity.this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            loadReceipts();
        }
    }
}