package com.example.apptfc.Activities.Neighbor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.Incidence;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostIncidenceActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private static final String LAST_INCIDENCE_TIME_KEY = "LAST_INCIDENCE_TIME_KEY";
    private TextInputEditText etTitle, etContent;
    private TextInputLayout tilTitle, tilContent;
    private MaterialButton btnSubmit;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_incidence);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        setupViews();
        setupListeners();
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                createIncidence();
            }
        });
    }

    private void setupViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tilTitle = findViewById(R.id.tilTitle);
        tilContent = findViewById(R.id.tilContent);
        btnSubmit = findViewById(R.id.btnSubmit);

        toolbar = findViewById(R.id.toolbar);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (etTitle.getText().toString().trim().isEmpty()) {
            tilTitle.setError("El título es obligatorio");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }

        if (etContent.getText().toString().trim().isEmpty()) {
            tilContent.setError("La descripción es obligatoria");
            isValid = false;
        } else if (etContent.getText().toString().trim().length() < 20) {
            tilContent.setError("La descripción debe tener al menos 20 caracteres");
            isValid = false;
        } else {
            tilContent.setError(null);
        }

        return isValid;
    }

    private void createIncidence() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        Incidence newIncidence = new Incidence(title, content, prefs.getString("username", "unknown"), new Date());

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        Call<Void> call = apiService.postIncidence(prefs.getInt("neighborId", -1), newIncidence);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Enviar Incidencia");

                if (response.isSuccessful()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(LAST_INCIDENCE_TIME_KEY, System.currentTimeMillis());
                    editor.apply();

                    Toast.makeText(PostIncidenceActivity.this, "Incidencia enviada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(PostIncidenceActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", "Código: " + response.code() + " - Error: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("Enviar Incidencia");

                Toast.makeText(PostIncidenceActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API_FAILURE", "Error: " + t.getMessage(), t);
            }
        });
    }
}