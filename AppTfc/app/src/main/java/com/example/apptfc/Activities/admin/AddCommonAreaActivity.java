package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.CommonArea;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCommonAreaActivity extends AppCompatActivity {

    private EditText etAreaName;
    private Button btnSubmit;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_common_area);

        setupViews();

        btnSubmit.setOnClickListener(v -> addCommonArea());
    }

    private void setupViews() {
        etAreaName = findViewById(R.id.etAreaName);
        btnSubmit = findViewById(R.id.btnSubmitArea);
    }

    private void addCommonArea() {
        String name = etAreaName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del área", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Agregando área...");

        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int neighborhoodId = prefs.getInt("neighborhoodId", -1);

        CommonArea newArea = new CommonArea();
        newArea.setName(name);
        newArea.setNeighborhoodId(neighborhoodId);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.createCommonArea(newArea).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                hideLoading();
                if (response.isSuccessful()) {
                    Toast.makeText(AddCommonAreaActivity.this, "Área agregada", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddCommonAreaActivity.this, "Error al agregar área", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                hideLoading();
                Toast.makeText(AddCommonAreaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
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