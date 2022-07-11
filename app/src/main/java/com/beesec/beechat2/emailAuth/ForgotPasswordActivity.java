package com.beesec.beechat2.emailAuth;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.beesec.beechat2.R;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //EditText
        EditText email = findViewById(R.id.email);

        //Button
        findViewById(R.id.send).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mEmail = email.getText().toString().trim();

            if (mEmail.isEmpty()){
                Snackbar.make(v, "Enter your email", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                mAuth.sendPasswordResetEmail(mEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Snackbar.make(v, "Password reset link sent on your email", Snackbar.LENGTH_LONG).show();
                    }else {
                        Snackbar.make(v, Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), Snackbar.LENGTH_LONG).show();
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                });
            }


        });

    }
}