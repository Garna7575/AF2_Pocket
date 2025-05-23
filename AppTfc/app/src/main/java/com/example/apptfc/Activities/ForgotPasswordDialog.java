package com.example.apptfc.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.apptfc.R;

public class ForgotPasswordDialog extends DialogFragment {

    private ForgotPasswordListener listener;

    public interface ForgotPasswordListener {
        void onEmailEntered(String email);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ForgotPasswordListener) {
            listener = (ForgotPasswordListener) context;
        } else {
            throw new ClassCastException(context + " debe implementar ForgotPasswordListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_forgot_password, null);

        EditText emailInput = view.findViewById(R.id.emailInput);
        Button btnSend = view.findViewById(R.id.btnSend);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();

        btnSend.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                listener.onEmailEntered(email);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Por favor, introduce un correo válido", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        return dialog;
    }
}
