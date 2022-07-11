package com.beesec.beechat2.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterPostReport;
import com.beesec.beechat2.model.ModelPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportedPostActivity extends AppCompatActivity {

    private RecyclerView users_rv;
    private List<ModelPost> userList;
    private AdapterPostReport adapterUsers;
    List<String> followingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(ReportedPostActivity.this));
        userList = new ArrayList<>();
        followingList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("ReportPost").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    followingList.add(ds.getKey());
                    getAllUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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


    private void getAllUsers() {
        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelUser = ds.getValue(ModelPost.class);
                            for (String id : followingList){
                                if (Objects.requireNonNull(modelUser).getId().equals(id)){
                                    userList.add(modelUser);
                                }
                            }
                        }
                        adapterUsers = new AdapterPostReport(ReportedPostActivity.this, userList);
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void filter(String query) {
        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelUser = ds.getValue(ModelPost.class);
                            for (String id : followingList){
                                if (Objects.requireNonNull(modelUser).getId().equals(id)){
                                    if (Objects.requireNonNull(modelUser).getText().toLowerCase().contains(query.toLowerCase()) ||
                                            modelUser.getType().toLowerCase().contains(query.toLowerCase())){
                                        userList.add(modelUser);
                                    }
                                }
                            }
                        }
                        adapterUsers = new AdapterPostReport(ReportedPostActivity.this, userList);
                        users_rv.setAdapter(adapterUsers);
                        adapterUsers.notifyDataSetChanged();
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}