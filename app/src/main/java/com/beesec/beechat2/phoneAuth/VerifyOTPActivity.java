package com.beesec.beechat2.phoneAuth;

import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.R;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class VerifyOTPActivity extends AppCompatActivity {

    private String verificationId;
    EditText otp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_o_t_p);

        mAuth = FirebaseAuth.getInstance();

        final String phonenumber = getIntent().getStringExtra("phonenumber");
        sendVerificationCode(phonenumber);

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Text
        findViewById(R.id.forgot).setOnClickListener(v -> onBackPressed());

        //EditText
        otp = findViewById(R.id.otp);

        //Button
        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String code = otp.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6){
                Snackbar.make(v,"Enter OTP", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else {
                verifyCode(code);
            }

        });

    }

    private  void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyOTPActivity.this, task -> {
                    if (task.isSuccessful()) {
                        final String phone = getIntent().getStringExtra("phonenumber");
                        Query userQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("phone").equalTo(phone);
                        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount()>0){

                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                Intent intent = new Intent(VerifyOTPActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                            }else {
                                                FirebaseAuth.getInstance().getCurrentUser().delete();
                                                Intent intent = new Intent(VerifyOTPActivity.this, RegisterActivity.class);
                                                intent.putExtra("phone", phone);
                                                startActivity(intent);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                                                finish();
                                                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }else {
                                    Intent intent = new Intent(VerifyOTPActivity.this, RegisterActivity.class);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(VerifyOTPActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        String msg = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(VerifyOTPActivity.this, msg, Toast.LENGTH_SHORT).show();
                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void sendVerificationCode(String phonenumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                mCallbacks);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                otp.setText(code);
                verifyCode(code);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        }
    };

}