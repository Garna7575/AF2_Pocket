package com.example.apptfc.Activities.admin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button; // O com.google.android.material.button.MaterialButton
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService; // Asegúrate que esta importación sea correcta
import com.example.apptfc.API.models.Neighbor;
import com.example.apptfc.API.models.PaymentEmailDTO;
import com.example.apptfc.API.models.Receipt;
import com.example.apptfc.API.RetrofitClient; // Asegúrate que esta importación sea correcta
import com.example.apptfc.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NeighborDetailActivity extends AppCompatActivity {
    private static final String TAG = "NeighborDetailActivity";
    private TextView tvName, tvHouse, tvPhone, tvEmail, tvAddress;
    private Button btnNotifyPendingReceipts, btnDeleteNeighbor;
    private Neighbor currentNeighbor;
    private ApiService apiService;
    private ProgressDialog progressDialog;
    private List<Receipt> pendingReceiptsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neighbor_detail);

        currentNeighbor = getIntent().getParcelableExtra("NEIGHBOR");

        apiService = RetrofitClient.get().create(ApiService.class);

        initializeViews();
        populateNeighborDetails();
        fetchPendingReceiptsAndSetupButtons();
        setupActionListeners();
    }

    private void initializeViews() {
        tvName = findViewById(R.id.tvName);
        tvHouse = findViewById(R.id.tvHouse);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.tvAddress);
        btnNotifyPendingReceipts = findViewById(R.id.btnNotifyPendingReceipts);
        btnDeleteNeighbor = findViewById(R.id.btnDeleteNeighbor);
    }

    private void populateNeighborDetails() {
        tvName.setText(String.format("%s %s",
                currentNeighbor.getUser().getName() != null ? currentNeighbor.getUser().getName() : "",
                currentNeighbor.getUser().getSurname() != null ? currentNeighbor.getUser().getSurname() : "").trim());

        String phone = currentNeighbor.getUser().getTlphNumber();
        tvPhone.setText((phone != null && !phone.isEmpty()) ? phone : "No disponible");

        String email = currentNeighbor.getUser().getEmail();
        tvEmail.setText((email != null && !email.isEmpty()) ? email : "No disponible");

        String house = currentNeighbor.getHouse();
        tvHouse.setText((house != null && !house.isEmpty()) ? house : "No disponible");
        tvAddress.setText((house != null && !house.isEmpty()) ? house : "No disponible");
    }

    private void fetchPendingReceiptsAndSetupButtons() {
        showLoadingDialog("Cargando información...");
        Call<List<Receipt>> call = apiService.getPendingReceipts(currentNeighbor.getId());
        call.enqueue(new Callback<List<Receipt>>() {
            @Override
            public void onResponse(Call<List<Receipt>> call, Response<List<Receipt>> response) {
                dismissLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    pendingReceiptsList = response.body();
                    Log.d(TAG, "Recibos pendientes obtenidos: " + pendingReceiptsList.size());
                } else {
                    pendingReceiptsList = null;
                    Log.e(TAG, "Error al obtener recibos pendientes: " + response.code());
                    Toast.makeText(NeighborDetailActivity.this, "No se pudieron cargar los recibos pendientes.", Toast.LENGTH_SHORT).show();
                }
                setupActionButtonsVisibility();
            }

            @Override
            public void onFailure(Call<List<Receipt>> call, Throwable t) {
                dismissLoadingDialog();
                pendingReceiptsList = null;
                Log.e(TAG, "Fallo al obtener recibos pendientes", t);
                Toast.makeText(NeighborDetailActivity.this, "Fallo de conexión al cargar recibos.", Toast.LENGTH_SHORT).show();
                setupActionButtonsVisibility();
            }
        });
    }

    private void setupActionButtonsVisibility() {
        if (pendingReceiptsList != null && !pendingReceiptsList.isEmpty() &&
                currentNeighbor.getUser().getEmail() != null && !currentNeighbor.getUser().getEmail().isEmpty()) {
            btnNotifyPendingReceipts.setVisibility(View.VISIBLE);
        } else {
            btnNotifyPendingReceipts.setVisibility(View.GONE);
        }

        btnDeleteNeighbor.setVisibility(View.VISIBLE);
    }


    private void setupActionListeners() {
        btnNotifyPendingReceipts.setOnClickListener(v -> {
            if (pendingReceiptsList != null && !pendingReceiptsList.isEmpty() &&
                    currentNeighbor.getUser().getEmail() != null && !currentNeighbor.getUser().getEmail().isEmpty()) {
                sendPaymentReminderEmail();
            } else {
                Toast.makeText(NeighborDetailActivity.this, "No hay recibos pendientes o el email del vecino no está configurado.", Toast.LENGTH_LONG).show();
            }
        });

        btnDeleteNeighbor.setOnClickListener(v -> confirmDeleteNeighbor());
    }

    private void sendPaymentReminderEmail() {
        String neighborName = (currentNeighbor.getUser().getName() != null ? currentNeighbor.getUser().getName() : "") +
                " " +
                (currentNeighbor.getUser().getSurname() != null ? currentNeighbor.getUser().getSurname() : "");

        PaymentEmailDTO paymentEmailDTO = new PaymentEmailDTO(
                currentNeighbor.getUser().getEmail(),
                neighborName.trim(),
                pendingReceiptsList
        );

        showLoadingDialog("Enviando recordatorio...");
        Call<Void> call = apiService.sendPaymentReminderEmail(paymentEmailDTO);
        Log.d(TAG, paymentEmailDTO.getReceipts().get(0).getDate().toString());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dismissLoadingDialog();
                if (response.isSuccessful()) {
                    Toast.makeText(NeighborDetailActivity.this, "Recordatorio enviado correctamente.", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Error al enviar recordatorio: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body for sendUnpaidReceiptsEmail", e);
                    }
                    Toast.makeText(NeighborDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "SendReminder Error: " + errorMsg);
                }
            }

            //Revisar por qué falla.

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                dismissLoadingDialog();
                Toast.makeText(NeighborDetailActivity.this, "Fallo de conexión al enviar recordatorio: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "SendReminder Failure", t);
            }
        });
    }

    private void confirmDeleteNeighbor() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Baja")
                .setMessage("¿Estás seguro de que quieres dar de baja a este vecino? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Dar de Baja", (dialog, which) -> deleteNeighbor())
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteNeighbor() {
        if (apiService == null) {
            Toast.makeText(this, "Servicio API no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoadingDialog("Dando de baja al vecino...");
        Call<Void> call = apiService.deleteNeighbor(currentNeighbor.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                dismissLoadingDialog();
                if (response.isSuccessful()) {
                    Toast.makeText(NeighborDetailActivity.this, "Vecino dado de baja correctamente.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    String errorMsg = "Error al dar de baja al vecino: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyStr = response.errorBody().string();
                            errorMsg += " - " + errorBodyStr;
                            Log.e("API_ERROR_BODY", "DeleteNeighbor Error Body: " + errorBodyStr);
                        }
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error parsing error body for deleteNeighbor", e);
                    }
                    Toast.makeText(NeighborDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "DeleteNeighbor Error: " + errorMsg + " (Full response: " + response.toString() + ")");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                dismissLoadingDialog();
                Toast.makeText(NeighborDetailActivity.this, "Fallo de conexión al dar de baja: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_FAILURE", "DeleteNeighbor Failure", t);
            }
        });
    }

    private void showLoadingDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}