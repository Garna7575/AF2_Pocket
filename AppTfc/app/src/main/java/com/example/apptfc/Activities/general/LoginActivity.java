package com.example.apptfc.Activities.general;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.ForgotPassword;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.models.User;
import com.example.apptfc.Activities.Neighbor.MainNeighborActivity;
import com.example.apptfc.Activities.admin.MainAdminActivity;
import com.example.apptfc.R;
import com.example.apptfc.dialogs.ForgotPasswordDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements ForgotPasswordDialog.ForgotPasswordListener {
    private ApiService apiService;
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvForgotPassword;
    private ProgressDialog progressDialog;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
    }

    private void setupViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Iniciando sesión...");
        progressDialog.setCancelable(false);

        setupListeners();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    searchUser(username, password);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void searchUser(String username, String password){
        apiService = RetrofitClient.get().create(ApiService.class);

        apiService.getUser(username, password).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    storeInfo(response.body());
                    if (response.body().getRole().equals("ADMIN")){
                        startActivity(new Intent(LoginActivity.this, MainAdminActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainNeighborActivity.class));
                        finish();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Nombre de usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
            }
        });
    }

    private void searchUser(String email){
        apiService = RetrofitClient.get().create(ApiService.class);

        apiService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sendPasswordResetEmail(email, response.body().getId());
                } else {
                    Toast.makeText(LoginActivity.this, "El correo no está registrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
            }
        });
    }

    private void showForgotPasswordDialog() {
        ForgotPasswordDialog dialog = new ForgotPasswordDialog();
        dialog.show(getSupportFragmentManager(), "ForgotPasswordDialog");
    }

    private void sendPasswordResetEmail(String email, int id) {
        apiService = RetrofitClient.get().create(ApiService.class);

        //Show progress dialog
        progressDialog.setMessage("Enviando email de recuperación...");
        progressDialog.show();

        //Create ForgotPassword object and set its values
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setEmail(email);
        forgotPassword.setId(id);

        Call<Void> call = apiService.forgotPassword(forgotPassword);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                //End progress dialog when response is received
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Email enviado con instrucciones", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error al enviar el email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "API_ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEmailEntered(String email) {
        searchUser(email);
    }

    private void storeInfo(User user){
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("name", user.getName());
        editor.putString("surname", user.getSurname());
        editor.putString("email", user.getEmail());
        editor.putString("username", user.getUsername());
        editor.putString("birthDate", user.getBirthDate().toString());
        editor.putString("tlphNumber", user.getTlphNumber());
        editor.putString("role", user.getRole());
        editor.apply();
    }
}