package com.beesec.beechat2.phoneAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.R;
import com.beesec.beechat2.menu.PrivacyActivity;
import com.beesec.beechat2.menu.TermsActivity;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    String phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        phonenumber = getIntent().getStringExtra("phone");

        TextView terms = findViewById(R.id.terms);
        TextView privacy = findViewById(R.id.privacy);

        privacy.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, PrivacyActivity.class)));

        terms.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, TermsActivity.class)));

        EditText name = findViewById(R.id.name);
        EditText username = findViewById(R.id.username);
        CheckBox checkBox = findViewById(R.id.checkbox);
        EditText code = findViewById(R.id.code);

        //Button
        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mName = name.getText().toString().trim();
            String mUsername = username.getText().toString().trim();
            if(!checkBox.isChecked()){
                Snackbar.make(v,"Agree to privacy policy & Terms & Conditions", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else
            if (mName.isEmpty()){
                Snackbar.make(v,"Enter your Name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else if(mUsername.isEmpty()){
                Snackbar.make(v,"Enter your Username", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else {

                if (code.getText().toString().trim().isEmpty()){
                    Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(mUsername);
                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                Snackbar.make(v,"Username already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                            }else {
                                register(mName,mUsername);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(v,error.getMessage(), Snackbar.LENGTH_LONG).show();
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        }
                    });
                }else {

                    FirebaseDatabase.getInstance().getReference("Code").child(code.getText().toString().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){

                                String hisId = Objects.requireNonNull(dataSnapshot.child("user").getValue()).toString();

                                FirebaseDatabase.getInstance().getReference().child("Balance").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        double add = Double.parseDouble(Objects.requireNonNull(snapshot.child("balance").getValue()).toString()) + 0.01;
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("balance", String.valueOf(add));
                                        FirebaseDatabase.getInstance().getReference().child("Balance").child(hisId).updateChildren(hashMap);
                                        FirebaseDatabase.getInstance().getReference("Code").child(code.getText().toString().trim()).getRef().removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                                Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").equalTo(mUsername);
                                usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.getChildrenCount()>0){
                                            Snackbar.make(v,"Username already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                        }else {
                                            register(mName,mUsername);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Snackbar.make(v,error.getMessage(), Snackbar.LENGTH_LONG).show();
                                        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                                    }
                                });

                            }else {
                                Toast.makeText(RegisterActivity.this, "Your Referral Code has expired or doesn't exist", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

            }
        });
    }


    private void register(String mName, String mUsername) {

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", userId);
        hashMap.put("name", mName);
        hashMap.put("email", "");
        hashMap.put("username", mUsername);
        hashMap.put("bio", "");
        hashMap.put("verified","");
        hashMap.put("location","");
        hashMap.put("phone",phonenumber);
        hashMap.put("status", ""+System.currentTimeMillis());
        hashMap.put("typingTo","noOne");
        hashMap.put("link","");
        hashMap.put("photo", "");
        FirebaseDatabase.getInstance().getReference("Users").child(userId).setValue(hashMap).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()){
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }
        });

    }

}