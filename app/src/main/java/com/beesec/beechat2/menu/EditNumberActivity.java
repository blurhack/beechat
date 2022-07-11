package com.beesec.beechat2.menu;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.annotations.NotNull;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class EditNumberActivity extends AppCompatActivity {

    String verificationId;
    EditText nNo;

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
        setContentView(R.layout.activity_edit_number);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

         nNo = findViewById(R.id.nNo);

        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
             if (nNo.getText().toString().isEmpty()){  findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(v, "Enter new phone number", Snackbar.LENGTH_SHORT).show();
            }else {
                sendVerificationCode(nNo.getText().toString());
            }
        });

    }

    private void sendVerificationCode(String phonenumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                EditNumberActivity.this,
                mCallbacks);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                verifyCode(code);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(EditNumberActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(credential);
        Snackbar.make(nNo, "Phone number Changed", Snackbar.LENGTH_SHORT).show();
        nNo.setText("");
    }


}