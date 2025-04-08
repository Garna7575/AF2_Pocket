package com.example.apptfc.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.Neighborhood;
import com.example.apptfc.API.PostRequest;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.User;
import com.example.apptfc.R;
import com.example.apptfc.adapters.NeighborhoodAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText etName, etSurname, etUsername, etEmail, etPassword, etConfirmPassword, etAge, etTlphNumber;
    private AutoCompleteTextView etCommunity;
    private Button btnRegister;
    private List<Neighborhood> neighborhoods = new ArrayList<>();
    private NeighborhoodAdapter adapter;
    private Map<String, Integer> neighborhoodMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAge = findViewById(R.id.etAge);
        btnRegister = findViewById(R.id.btnRegister);
        etCommunity = findViewById(R.id.etComunity);
        etTlphNumber = findViewById(R.id.etTlphNumber);

        adapter = new NeighborhoodAdapter(this, neighborhoods);
        etCommunity.setAdapter(adapter);

        getAllNeighborhoods();

        etCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!neighborhoods.isEmpty()) {
                    etCommunity.showDropDown();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String username = etUsername.getText().toString();
                    String selectedNeighborhood = etCommunity.getText().toString();

                    int neighborhoodId = neighborhoodMap.get(selectedNeighborhood);

                    alreadyInDB(username, new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (response.body() != null && response.body()) {
                                Toast.makeText(RegisterActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                            } else {
                                PostRequest user = new PostRequest();
                                user.setName(etName.getText().toString());
                                user.setSurname(etSurname.getText().toString());
                                user.setTlphNumber(etTlphNumber.getText().toString());
                                user.setEmail(etEmail.getText().toString());
                                user.setUsername(username);
                                user.setPassword(etPassword.getText().toString());
                                user.setAge(Integer.parseInt(etAge.getText().toString()));
                                user.setHouse("12");
                                user.setNeighborhoodId(neighborhoodId);

                                registerUser(user);
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Toast.makeText(RegisterActivity.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void getAllNeighborhoods() {
        apiService = RetrofitClient.get().create(ApiService.class);

        Call<List<Neighborhood>> call = apiService.getAllNeighborhoods();
        call.enqueue(new Callback<List<Neighborhood>>() {
            @Override
            public void onResponse(Call<List<Neighborhood>> call, Response<List<Neighborhood>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    neighborhoods.clear();
                    neighborhoodMap.clear();

                    for (Neighborhood neighborhood : response.body()) {
                        neighborhoods.add(neighborhood);
                        neighborhoodMap.put(neighborhood.getName(), neighborhood.getId());
                    }

                    adapter.notifyDataSetChanged();
                    Log.d("RegisterActivity", "Vecindarios cargados: " + neighborhoods.size());
                } else {
                    Log.e("RegisterActivity", "Error en la respuesta: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Error al cargar las comunidades", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Neighborhood>> call, Throwable t) {
                Log.e("RegisterActivity", "Error de conexión: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void registerUser(PostRequest user) {
        apiService = RetrofitClient.get().create(ApiService.class);

        Call<Void> call = apiService.createNeighbor(user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registro realizado con éxito", Toast.LENGTH_SHORT).show();
                    resetFields();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    Log.e("RegisterActivity", "Error en la respuesta: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Error en el registro: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("RegisterActivity", "Error de conexión: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (etName.getText().toString().trim().isEmpty()) {
            showError(etName, "Campo obligatorio");
            isValid = false;
        }
        if (etAge.getText().toString().trim().isEmpty()) {
            showError(etAge, "Campo obligatorio");
            isValid = false;
        } else if (Integer.parseInt(etAge.getText().toString()) < 18) {
            showError(etAge, "Debe ser mayor de 18");
            isValid = false;
        }
        if (etPassword.getText().toString().trim().isEmpty()) {
            showError(etPassword, "Campo obligatorio");
            isValid = false;
        }
        if (etConfirmPassword.getText().toString().trim().isEmpty()) {
            showError(etConfirmPassword, "Campo obligatorio");
            isValid = false;
        }
        if (etTlphNumber.getText().toString().trim().isEmpty()){
            showError(etTlphNumber, "Campo obligatorio");
            isValid = false;
        }
        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            showError(etConfirmPassword, "Las contraseñas no coinciden");
            isValid = false;
        }
        String selectedNeighborhood = etCommunity.getText().toString().trim();
        boolean neighborhoodExists = neighborhoods.stream()
                .anyMatch(neighborhood -> neighborhood.getName().equalsIgnoreCase(selectedNeighborhood));

        if (!neighborhoodExists) {
            showError(etCommunity, "Vecindario no válido");
            isValid = false;
        }

        return isValid;
    }

    private void showError(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
        shakeView(editText);
    }

    private void shakeView(View view) {
        Animation shake = new TranslateAnimation(-10, 10, 0, 0);
        shake.setDuration(300);
        shake.setRepeatMode(Animation.REVERSE);
        shake.setRepeatCount(3);
        view.startAnimation(shake);
    }

    private void alreadyInDB(String username, Callback<Boolean> callback) {
        apiService = RetrofitClient.get().create(ApiService.class);

        apiService.getUserByUsername(username).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(null, Response.success(true));
                } else {
                    callback.onResponse(null, Response.success(false));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                callback.onResponse(null, Response.success(false));
            }
        });
    }

    private void resetFields() {
        etName.setText("");
        etSurname.setText("");
        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        etAge.setText("");
        etTlphNumber.setText("");
        etCommunity.setText("");
    }
}
