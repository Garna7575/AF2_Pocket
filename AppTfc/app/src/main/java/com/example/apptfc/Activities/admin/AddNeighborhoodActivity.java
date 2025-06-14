package com.example.apptfc.Activities.admin;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.models.Admin;
import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.models.creationNeighborhoods;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNeighborhoodActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private EditText etName;
    private ImageView imagePreview;
    private byte[] imageBytes = null;
    Button btnSelectImage, btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_neighborhood);

        setupViews();

        setupListeners();
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());

        btnSubmit.setOnClickListener(v -> {
            if (validateFields()) {
                createNeighborhood();
            }
        });
    }

    private void setupViews() {
        etName = findViewById(R.id.etNeighborhoodName);
        imagePreview = findViewById(R.id.imagePreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private boolean validateFields() {
        if (etName.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre de la comunidad", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageBytes == null) {
            Toast.makeText(this, "Selecciona una imagen", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            handleSelectedImage(data.getData());
        }
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            imagePreview.setImageBitmap(bitmap);
            imageBytes = convertBitmapToBytes(bitmap);
        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] convertBitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

    private void createNeighborhood() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        int adminId = prefs.getInt("adminId", -1);

        creationNeighborhoods newNeighborhood = new creationNeighborhoods();
        newNeighborhood.setName(etName.getText().toString().trim());
        newNeighborhood.setImage(imageBytes);
        newNeighborhood.setAdmin(new Admin());
        newNeighborhood.getAdmin().setId(adminId);

        ApiService apiService = RetrofitClient.get().create(ApiService.class);
        apiService.sendNeighborhoodCreationRequest(newNeighborhood).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddNeighborhoodActivity.this, "Error al crear comunidad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddNeighborhoodActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}