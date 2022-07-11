package com.beesec.beechat2.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;

@SuppressWarnings("ALL")
public class EditPasswordActivity extends AppCompatActivity {

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        EditText cPass = findViewById(R.id.cPass);
        EditText nPass = findViewById(R.id.nPass);

        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            if (cPass.getText().toString().isEmpty()){  findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter your current password", Snackbar.LENGTH_SHORT).show();
            }else if (nPass.getText().toString().isEmpty()){  findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter your new password", Snackbar.LENGTH_SHORT).show();
            }else {
                AuthCredential authCredential = EmailAuthProvider.getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(), cPass.getText().toString());
                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(authCredential).addOnSuccessListener(aVoid -> {
                   FirebaseAuth.getInstance().getCurrentUser().updatePassword(nPass.getText().toString());
                    Snackbar.make(v, "Password Changed", Snackbar.LENGTH_SHORT).show();
                    cPass.setText("");
                    nPass.setText("");
                }).addOnFailureListener(e -> Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT).show());
            }
        });

    }
}