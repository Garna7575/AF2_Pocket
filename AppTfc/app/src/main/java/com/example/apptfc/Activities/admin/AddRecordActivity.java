package com.example.apptfc.Activities.admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apptfc.API.ApiService;
import com.example.apptfc.API.RetrofitClient;
import com.example.apptfc.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecordActivity extends AppCompatActivity {
    private EditText etName, etDescription;
    private Button btnSelectFile, btnSubmit;
    private TextView tvFileName;
    private Uri fileUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        setupViews();
        setupListeners();
    }

    private void setupListeners() {
        btnSelectFile.setOnClickListener(v -> selectFile());
        btnSubmit.setOnClickListener(v -> uploadRecord());
    }

    private void setupViews() {
        etName = findViewById(R.id.etRecordName);
        etDescription = findViewById(R.id.etRecordDescription);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSubmit = findViewById(R.id.btnSubmitRecord);
        tvFileName = findViewById(R.id.tvFileName);
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            tvFileName.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (var cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadRecord() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fileUri == null) {
            Toast.makeText(this, "Selecciona un archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Subiendo acta...");

        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            File file = new File(getCacheDir(), getFileName(fileUri));
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
            RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
            RequestBody datePart = RequestBody.create(MediaType.parse("text/plain"),
                    String.valueOf(System.currentTimeMillis()));

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(fileUri)),
                    file
            );
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.getName(),
                    requestFile
            );

            SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
            int neighborhoodId = prefs.getInt("neighborhoodId", -1);
            RequestBody neighborhoodIdPart = RequestBody.create(
                    MediaType.parse("text/plain"),
                    String.valueOf(neighborhoodId)
            );
            
            ApiService apiService = RetrofitClient.get().create(ApiService.class);
            Call<Void> call = apiService.uploadRecord(
                    namePart,
                    descriptionPart,
                    datePart,
                    filePart,
                    neighborhoodIdPart
            );

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    hideLoading();
                    if (response.isSuccessful()) {
                        Toast.makeText(AddRecordActivity.this, "Acta subida con éxito", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddRecordActivity.this, "Error al subir el acta", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    hideLoading();
                    Toast.makeText(AddRecordActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            hideLoading();
            Toast.makeText(this, "Error al procesar el archivo", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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