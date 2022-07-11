package com.beesec.beechat2.phoneAuth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hbb20.CountryCodePicker;
import com.beesec.beechat2.R;
import com.beesec.beechat2.emailAuth.LoginActivity;

public class GenerateOTPActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_o_t_p);

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Text
        findViewById(R.id.forgot).setOnClickListener(v -> startActivity(new Intent(GenerateOTPActivity.this, LoginActivity.class)));

        //EditText
        EditText phone = findViewById(R.id.phone);

        //CCP
        CountryCodePicker ccp = findViewById(R.id.code);
        String code = ccp.getSelectedCountryCode();
        phone.setText("+"+code);

        //Button
        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mPhone = phone.getText().toString().trim();
            if (mPhone.isEmpty()){
                Snackbar.make(v,"Enter your phone number", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                Intent intent = new Intent(GenerateOTPActivity.this, VerifyOTPActivity.class);
                intent.putExtra("phonenumber", mPhone);
                startActivity(intent);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }
}