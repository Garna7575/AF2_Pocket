package com.example.apptfc.Activities.general;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.API.models.User;
import com.example.apptfc.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalInfoActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private EditText etName, etSurname, etUsername, etEmail, etPhone, etBirthdate;
    private ImageView profileImage;
    private Button btnSave, btnChangePassword, btnCancel, btnChangePhoto;
    private ApiService apiService;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        prefs = getSharedPreferences("UserData", MODE_PRIVATE);

        initViews();

        setupListeners();

        loadData();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etSurname = findViewById(R.id.et_lastname);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etBirthdate = findViewById(R.id.et_birthdate);
        profileImage = findViewById(R.id.profile_image);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnChangePhoto = findViewById(R.id.btn_change_photo);

        apiService = RetrofitClient.get().create(ApiService.class);
    }

    private void setupListeners() {
        etBirthdate.setOnClickListener(v -> showDatePickerDialog());

        btnSave.setOnClickListener(v -> saveProfileChanges());

        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadData(){
        etName.setText(prefs.getString("name", ""));
        etSurname.setText(prefs.getString("surname", ""));
        etUsername.setText(prefs.getString("username", ""));
        etEmail.setText(prefs.getString("email", ""));
        etPhone.setText(prefs.getString("tlphNumber", ""));
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
        etBirthdate.setText(sdf.format(date));
    }

    private void saveProfileChanges() {
        if (validateFields()) {
            User updatedUser = new User();
            updatedUser.setId(prefs.getInt("id", -1));
            updatedUser.setName(etName.getText().toString());
            updatedUser.setUsername(etUsername.getText().toString());
            updatedUser.setSurname(etSurname.getText().toString());
            updatedUser.setEmail(etEmail.getText().toString());
            updatedUser.setTlphNumber(etPhone.getText().toString());

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthDate = sdf.parse(etBirthdate.getText().toString());
                updatedUser.setBirthDate(birthDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Call<Void> call = apiService.updateUser(updatedUser, updatedUser.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PersonalInfoActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PersonalInfoActivity.this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(PersonalInfoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Nombre requerido");
            isValid = false;
        }
        if (etSurname.getText().toString().trim().isEmpty()) {
            etSurname.setError("apellidos requerido");
            isValid = false;
        }
        if (etUsername.getText().toString().trim().isEmpty()) {
            etUsername.setError("nombre de usuario requerido");
            isValid = false;
        }
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Email requerido");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Email no válido");
            isValid = false;
        }
        if (etPhone.getText().toString().trim().isEmpty()) {
            etPhone.setError("Teléfono requerido");
            isValid = false;
        }
        if (etBirthdate.getText().toString().trim().isEmpty()) {
            etBirthdate.setError("Fecha de nacimiento requerida");
            isValid = false;
        }
        return isValid;
    }
}