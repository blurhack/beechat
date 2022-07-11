package com.beesec.beechat2.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterPost;
import com.beesec.beechat2.adapter.AdapterReelView;
import com.beesec.beechat2.model.ModelPost;
import com.beesec.beechat2.model.ModelReel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SavedActivity extends AppCompatActivity {

    //Reel
    RecyclerView reelView;
    AdapterReelView adapterReelView;
    List<ModelReel> modelReel;
    List<String> myReelSaves;

    //Post
    AdapterPost adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;
    List<String> mySaves;

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
        setContentView(R.layout.activity_saved);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Reels
        reelView = findViewById(R.id.reel);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        reelView.setLayoutManager(gridLayoutManager);
        modelReel = new ArrayList<>();

        //Post
        post = findViewById(R.id.posts);
        post.setLayoutManager(new LinearLayoutManager(SavedActivity.this));
        modelPosts = new ArrayList<>();
        mySaved();

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    myReelSaved();
                    reelView.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    post.setVisibility(View.VISIBLE);
                    reelView.setVisibility(View.GONE);
                    mySaved();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void myReelSaved() {
        myReelSaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("SavesReel")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    myReelSaves.add(snapshot1.getKey());
                }
                getReel();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mySaved() {
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    mySaves.add(snapshot1.getKey());
                }
                getAllPost();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void getReel() {
        FirebaseDatabase.getInstance().getReference("Reels")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelReel.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelReel modelPost = ds.getValue(ModelReel.class);
                            for (String id: myReelSaves){
                                if (Objects.requireNonNull(modelPost).getpId().equals(id)){
                                    modelReel.add(modelPost);
                                }
                            }
                            adapterReelView = new AdapterReelView(modelReel);
                            reelView.setAdapter(adapterReelView);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterReelView.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.VISIBLE);
                                findViewById(R.id.nothing).setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getAllPost() {

        FirebaseDatabase.getInstance().getReference("Posts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPosts.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelPost = ds.getValue(ModelPost.class);
                            for (String id: mySaves){
                                if (Objects.requireNonNull(modelPost).getpId().equals(id)) {
                                    modelPosts.add(modelPost);
                                }
                            }
                            adapterPost = new AdapterPost(SavedActivity.this, modelPosts);
                            post.setAdapter(adapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                post.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                post.setVisibility(View.VISIBLE);
                                findViewById(R.id.nothing).setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}