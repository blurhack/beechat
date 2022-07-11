package com.beesec.beechat2.menu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterWithdraw;
import com.beesec.beechat2.model.ModelWithdraw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class WithdrawActivity extends AppCompatActivity {

    double number = 0;
    //Post
    AdapterWithdraw adapterWithdraw;
    List<ModelWithdraw> modelWithdraws;
    RecyclerView withdraws;

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
        setContentView(R.layout.activity_withdraw);

        EditText name = findViewById(R.id.name);
        EditText amount = findViewById(R.id.amt);
        EditText id = findViewById(R.id.id);

        TextView balance = findViewById(R.id.balance);

        FirebaseDatabase.getInstance().getReference("Balance").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (Objects.requireNonNull(snapshot.child("balance").getValue()).toString().isEmpty()) {
                  number = 0;
                }else {
                    number = Double.parseDouble(Objects.requireNonNull(snapshot.child("balance").getValue()).toString());
                }

                balance.setText("Balance : " + Objects.requireNonNull(snapshot.child("balance").getValue()).toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.createMeet).setOnClickListener(v -> {


            if (name.getText().toString().isEmpty()){
                Snackbar.make(v,"Enter name", Snackbar.LENGTH_LONG ).show();
            }else if (amount.getText().toString().isEmpty()){
                Snackbar.make(v,"Enter amount", Snackbar.LENGTH_LONG ).show();
            }else if (id.getText().toString().isEmpty()){
                Snackbar.make(v,"Enter ID", Snackbar.LENGTH_LONG ).show();
            }else{
                if (Double.parseDouble(amount.getText().toString()) <= number){

                    String time = ""+System.currentTimeMillis();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", time);
                    hashMap.put("user", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("name", name.getText().toString());
                    hashMap.put("amount", amount.getText().toString());
                    hashMap.put("way", id.getText().toString());
                    hashMap.put("type", "sent");
                    FirebaseDatabase.getInstance().getReference("Request").child(time).setValue(hashMap);

                    Toast.makeText(this, "Request sent", Toast.LENGTH_SHORT).show();

                    Snackbar.make(v,"Request sent", Snackbar.LENGTH_LONG ).show();

                    FirebaseDatabase.getInstance().getReference("Balance").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            double add = Integer.parseInt(Objects.requireNonNull(snapshot.child("balance").getValue()).toString()) - Double.parseDouble(amount.getText().toString());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("balance", String.valueOf(add));
                            FirebaseDatabase.getInstance().getReference("Balance").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else {
                    Snackbar.make(v,"You don't have balance", Snackbar.LENGTH_LONG ).show();
                }

            }

        });

        //Post
        withdraws = findViewById(R.id.history);
        withdraws.setLayoutManager(new LinearLayoutManager(WithdrawActivity.this));
        modelWithdraws = new ArrayList<>();
        getAllHistory();

    }

    private void getAllHistory() {
        FirebaseDatabase.getInstance().getReference("Request")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelWithdraws.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelWithdraw modelPost = ds.getValue(ModelWithdraw.class);
                            if (Objects.requireNonNull(ds.child("user").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                modelWithdraws.add(modelPost);
                            }
                            adapterWithdraw = new AdapterWithdraw(WithdrawActivity.this, modelWithdraws);
                            withdraws.setAdapter(adapterWithdraw);
                            if (adapterWithdraw.getItemCount() == 0){
                                withdraws.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            }else {
                                withdraws.setVisibility(View.VISIBLE);
                                findViewById(R.id.nothing).setVisibility(View.GONE);
                            }
                        }

                        if (!snapshot.exists()){
                            withdraws.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}