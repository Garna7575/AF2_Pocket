package com.example.apptfc.Activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Receipt;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptDetailActivity extends AppCompatActivity {

    private Receipt receipt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);

        receipt = getIntent().getParcelableExtra("receipt");

        setupViews();
    }

    private void setupViews() {
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvAmount = findViewById(R.id.tvDetailAmount);
        TextView tvDate = findViewById(R.id.tvDetailDate);
        TextView tvStatus = findViewById(R.id.tvDetailStatus);
        Button btnPay = findViewById(R.id.btnPay);

        tvTitle.setText(receipt.getTitle());
        tvDescription.setText(receipt.getDescription());
        tvAmount.setText(String.format(Locale.getDefault(), "%.2f", receipt.getValue()));
        tvDate.setText("Fecha: " + receipt.getFormattedDate());

        if (receipt.isPaid()) {
            tvStatus.setText("Estado: Pagado");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnPay.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Estado: Pendiente");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            btnPay.setVisibility(View.VISIBLE);
            btnPay.setOnClickListener(v -> showPaymentDialog());
        }
    }

    private void showPaymentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar pago")
                .setMessage("¿Desea pagar " + String.format(Locale.getDefault(), "%.2f", receipt.getValue()) + " por " + receipt.getTitle() + "?")
                .setPositiveButton("Pagar", (dialog, which) -> simulatePayment())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void simulatePayment() {
        AlertDialog processingDialog = new AlertDialog.Builder(this)
                .setTitle("Procesando pago")
                .setMessage("Espere mientras procesamos su pago...")
                .setCancelable(false)
                .show();

        payment(receipt, processingDialog);
    }

    private void payment(Receipt record, AlertDialog processingDialog) {
        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Void> call = apiService.payment(record.getId());

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processingDialog.dismiss();
                receipt.setPaid(true);
                showPaymentSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                processingDialog.dismiss();
                showPaymentError();
                Log.e("payment", "Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showPaymentSuccess() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(ReceiptDetailActivity.this)
                    .setTitle("Pago completado")
                    .setMessage("El pago de " + String.format(Locale.getDefault(), "%.2f", receipt.getValue()) + " se ha realizado con éxito.")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        setResult(RESULT_OK);
                        finish();
                    })
                    .show();
        });
    }

    private void showPaymentError() {
        runOnUiThread(() -> {
            new AlertDialog.Builder(ReceiptDetailActivity.this)
                    .setTitle("Error en el pago")
                    .setMessage("No se pudo completar el pago. Por favor, inténtelo nuevamente.")
                    .setPositiveButton("Aceptar", null)
                    .show();
        });
    }
}
