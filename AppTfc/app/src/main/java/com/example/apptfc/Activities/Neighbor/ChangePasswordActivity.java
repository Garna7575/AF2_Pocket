package com.example.apptfc.Activities.Neighbor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.PasswordChangeRequest;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword, btnCancel;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        initViews();

        setupListeners();

        apiService = RetrofitClient.get().create(ApiService.class);
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupListeners() {
        btnChangePassword.setOnClickListener(v -> attemptPasswordChange());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void attemptPasswordChange() {
        if (validateFields()) {
            changePassword();
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Ingrese su contraseña actual");
            isValid = false;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Ingrese una nueva contraseña");
            isValid = false;
        } else if (newPassword.length() < 8) {
            etNewPassword.setError("Mínimo 8 caracteres");
            isValid = false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            isValid = false;
        }

        return isValid;
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();

        PasswordChangeRequest request = new PasswordChangeRequest(currentPassword, newPassword);

        Call<Void> call = apiService.changePassword(request, prefs.getInt("id", -1));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Contraseña cambiada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(int statusCode) {
        switch (statusCode) {
            case 401:
                etCurrentPassword.setError("Contraseña actual incorrecta");
                break;
            case 400:
                Toast.makeText(this, "La nueva contraseña no cumple los requisitos",
                        Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Error al cambiar la contraseña",
                        Toast.LENGTH_SHORT).show();
        }
    }
}