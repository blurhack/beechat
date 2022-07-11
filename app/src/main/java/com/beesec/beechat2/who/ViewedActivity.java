package com.beesec.beechat2.who;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterUsers;
import com.beesec.beechat2.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewedActivity extends AppCompatActivity {

    //User
    String id;
    private RecyclerView users_rv;
    private List<ModelUser> userList;
    private AdapterUsers adapterUsers;
    List<String> idList;

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
        setContentView(R.layout.activity_who);

        id = getIntent().getStringExtra("id");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FirebaseDatabase.getInstance().getReference("Ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("on")){
                    mAdView.setVisibility(View.VISIBLE);
                }else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(ViewedActivity.this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        getViews();

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

    }

    private void filter(String query) {
        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            if (ds.hasChild("name")){
                                ModelUser modelUser = ds.getValue(ModelUser.class);
                                for (String id : idList) {
                                    assert modelUser != null;
                                    if (modelUser.getId().equals(id) && !modelUser.getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                                modelUser.getUsername().toLowerCase().contains(query.toLowerCase())){
                                            userList.add(modelUser);
                                        }
                                    }
                            }


                                adapterUsers = new AdapterUsers(ViewedActivity.this, userList);
                                users_rv.setAdapter(adapterUsers);
                                if (adapterUsers.getItemCount() == 0){
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    users_rv.setVisibility(View.GONE);
                                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                }else {
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    users_rv.setVisibility(View.VISIBLE);
                                    findViewById(R.id.nothing).setVisibility(View.GONE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getViews(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(Objects.requireNonNull(getIntent().getStringExtra("storyid"))).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    idList.add(snapshot1.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {

        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){

                            if (ds.hasChild("name")){
                                ModelUser modelUser = ds.getValue(ModelUser.class);
                                for (String id : idList) {
                                    assert modelUser != null;
                                    if (modelUser.getId().equals(id) && !modelUser.getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                        userList.add(modelUser);
                                    }
                            }

                                adapterUsers = new AdapterUsers(ViewedActivity.this, userList);
                                users_rv.setAdapter(adapterUsers);
                                if (adapterUsers.getItemCount() == 0){
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    users_rv.setVisibility(View.GONE);
                                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                }else {
                                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                                    users_rv.setVisibility(View.VISIBLE);
                                    findViewById(R.id.nothing).setVisibility(View.GONE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}