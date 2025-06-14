package com.example.apptfc.Activities.general;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.Neighborhood;
import com.example.apptfc.API.models.PostRequest;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.models.User;
import com.example.apptfc.R;
import com.example.apptfc.adapters.NeighborhoodAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText etName, etSurname, etUsername, etEmail, etPassword, etConfirmPassword, etBirthDate, etTlphNumber, etHouse;
    private AutoCompleteTextView etCommunity;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLoginLink;
    private List<Neighborhood> neighborhoods = new ArrayList<>();
    private NeighborhoodAdapter adapter;
    private Map<String, Integer> neighborhoodMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupViews();
        setupAdapters();
        getAllNeighborhoods();
        setupListeners();
    }

    private void setupViews() {
        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnRegister = findViewById(R.id.btnRegister);
        etCommunity = findViewById(R.id.etComunity);
        etTlphNumber = findViewById(R.id.etTlphNumber);
        etHouse = findViewById(R.id.etHouse);
        cbTerms = findViewById(R.id.cbTerms);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupAdapters() {
        adapter = new NeighborhoodAdapter(this, neighborhoods);
        etCommunity.setAdapter(adapter);
    }

    private void setupListeners() {
        etCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!neighborhoods.isEmpty()) {
                    etCommunity.showDropDown();
                }
            }
        });

        etBirthDate.setOnClickListener(v -> showDatePickerDialog());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIfUserIsAlreadyInDB();
            }
        });

        tvLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void checkIfUserIsAlreadyInDB() {
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
                        PostRequest user = generatePostRequest(neighborhoodId);

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

    private PostRequest generatePostRequest(int neighborhoodId) {
        PostRequest user = new PostRequest();
        user.setName(etName.getText().toString());
        user.setSurname(etSurname.getText().toString());
        user.setTlphNumber(etTlphNumber.getText().toString());
        user.setEmail(etEmail.getText().toString());
        user.setUsername(etUsername.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.setBirthDate(new Date(etBirthDate.getText().toString()));
        user.setHouse(etHouse.getText().toString());
        user.setNeighborhoodId(neighborhoodId);
        return user;
    }

    private void getAllNeighborhoods() {
        apiService = RetrofitClient.get().create(ApiService.class);

        Call<List<Neighborhood>> call = apiService.getAllNeighborhoods();
        call.enqueue(new Callback<List<Neighborhood>>() {
            @Override
            public void onResponse(Call<List<Neighborhood>> call, Response<List<Neighborhood>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Clear the existing neighborhoods and neighborhoodMap
                    neighborhoods.clear();
                    neighborhoodMap.clear();

                    //Fill up the neighborhoods and neighborhoodMap
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
                    finish();
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

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    updateBirthDate(selectedDate.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateBirthDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etBirthDate.setText(sdf.format(date));
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (!validateName()) isValid = false;
        if (!validateEmail()) isValid = false;
        if (!validatePhone()) isValid = false;
        if (!validatePassword()) isValid = false;
        if (!validateConfirmPass()) isValid = false;
        if (!validateNeighborhood()) isValid = false;
        if (!validateBirthdate()) isValid = false;
        if (!validateTerms()) isValid = false;

        return isValid;
    }

    private boolean validateName() {
        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {
            showError(etName, "Campo obligatorio.");
            return false;
        }
        return true;
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email requerido.");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido.");
            return false;
        }
        return true;
    }

    private boolean validatePhone() {
        String phone = etTlphNumber.getText().toString().trim();

        if (phone.isEmpty()) {
            showError(etTlphNumber, "Campo obligatorio.");
            return false;
        }
        if (phone.length() != 9) {
            showError(etTlphNumber, "El campo debe tener 9 dígitos.");
            return false;
        }
        return true;
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();

        if (password.isEmpty()) {
            showError(etPassword, "Campo obligatorio.");
            return false;
        }
        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[0-9].*") ||
                !password.matches(".*[!@#$%^&*].*")) {
            showError(etPassword, "Mínimo 8 caracteres, una mayúscula, un número y un carácter especial.");
            return false;
        }
        return true;
    }

    private boolean validateConfirmPass() {
        String password = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (confirmPass.isEmpty()) {
            showError(etConfirmPassword, "Campo obligatorio.");
            return false;
        }
        if (!confirmPass.equals(password)) {
            showError(etConfirmPassword, "Las contraseñas no coinciden.");
            return false;
        }
        return true;
    }

    private boolean validateNeighborhood() {
        String neighborhood = etCommunity.getText().toString().trim();

        if (neighborhood.isEmpty()) {
            showError(etCommunity, "Campo obligatorio.");
            return false;
        }
        boolean neighborhoodExists = neighborhoods.stream()
                .anyMatch(n -> n.getName().equalsIgnoreCase(neighborhood));

        if (!neighborhoodExists) {
            showError(etCommunity, "Vecindario no válido.");
            return false;
        }
        return true;
    }

    private boolean validateBirthdate() {
        String birthDate = etBirthDate.getText().toString().trim();

        if (birthDate.isEmpty()) {
            showError(etBirthDate, "Campo obligatorio.");
            return false;
        }
        if (!isAdult()) {
            showError(etBirthDate, "Debes ser mayor de 18 años.");
            return false;
        }
        return true;
    }

    private boolean isAdult() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date birthDate = sdf.parse(etBirthDate.getText().toString());

            Calendar today = Calendar.getInstance();
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthDay.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 18;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean validateTerms() {
        if (!cbTerms.isChecked()) {
            showError(etBirthDate, "Debes aceptar las normas.");
            return false;
        }
        return true;
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

}
