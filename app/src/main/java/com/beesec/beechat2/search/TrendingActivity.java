package com.beesec.beechat2.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterPost;
import com.beesec.beechat2.model.ModelPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TrendingActivity extends AppCompatActivity {

    String type = "";

    //Post
    AdapterPost adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;

    //Post
    AdapterPost getAdapterPost;
    List<ModelPost> modelPostList;
    RecyclerView postView;

    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button more;
    long initial;

    NightMode sharedPref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //Search
        findViewById(R.id.search).setOnClickListener(v1 -> startActivity(new Intent(TrendingActivity.this, SearchActivity.class)));

        more = findViewById(R.id.more);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    i++;
                }
                initial = i;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(TrendingActivity.this));
        modelPosts = new ArrayList<>();
        trending();
        findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });


        //Post
        postView = findViewById(R.id.postView);
        postView.setLayoutManager(new LinearLayoutManager(TrendingActivity.this));
        modelPostList = new ArrayList<>();

        //Type
        findViewById(R.id.music).setOnClickListener(v -> {
            type = "music";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.sports).setOnClickListener(v -> {
            type = "sports";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.memes).setOnClickListener(v -> {
            type = "memes";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.vines).setOnClickListener(v -> {
            type = "vines";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.tv).setOnClickListener(v -> {
            type = "movie";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.animals).setOnClickListener(v -> {
            type = "animals";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.diy).setOnClickListener(v -> {
            type = "diy";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.beauty).setOnClickListener(v -> {
            type = "beauty";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.art).setOnClickListener(v -> {
            type = "art";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.food).setOnClickListener(v -> {
            type = "food";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.style).setOnClickListener(v -> {
            type = "style";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });

        findViewById(R.id.decor).setOnClickListener(v -> {
            type = "decor";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.politics).setOnClickListener(v -> {
            type = "politics";
            post.setVisibility(View.GONE);
            postView.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getPostsType();
        });
        findViewById(R.id.trending).setOnClickListener(v -> {
            type = "";
            post.setVisibility(View.VISIBLE);
            postView.setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        });


    }

    private void loadMoreData() {
        currentPage++;
        trending();
    }

    private void trending() {

        FirebaseDatabase.getInstance().getReference("Posts").limitToLast(currentPage*TOTAL_ITEM_EACH_LOAD)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPosts.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelPost = ds.getValue(ModelPost.class);
                            modelPosts.add(modelPost);
                            Collections.shuffle(modelPosts);
                            adapterPost = new AdapterPost(TrendingActivity.this, modelPosts);
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
                                if(adapterPost.getItemCount() >= initial){
                                    more.setVisibility(View.GONE);
                                    currentPage--;
                                    more.setText("Load more");
                                }else {
                                    more.setVisibility(View.VISIBLE);
                                    more.setText("Load more");
                                }
                            }
                        }

                        if (!snapshot.exists()){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            post.setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void getPostsType(){
        FirebaseDatabase.getInstance().getReference("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelPostList.clear();
               for (DataSnapshot ds : snapshot.getChildren()){
                   if (Objects.requireNonNull(ds.child("text").getValue()).toString().toLowerCase().contains(type)){
                       ModelPost modelPost = ds.getValue(ModelPost.class);
                       modelPostList.add(modelPost);
                       Collections.shuffle(modelPostList);
                       getAdapterPost = new AdapterPost(TrendingActivity.this, modelPostList);
                       postView.setAdapter(getAdapterPost);
                       findViewById(R.id.progressBar).setVisibility(View.GONE);
                       if (getAdapterPost.getItemCount() == 0){
                           findViewById(R.id.progressBar).setVisibility(View.GONE);
                           postView.setVisibility(View.GONE);
                           findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                       }else {
                           findViewById(R.id.progressBar).setVisibility(View.GONE);
                           postView.setVisibility(View.VISIBLE);
                           findViewById(R.id.nothing).setVisibility(View.GONE);
                       }
                   }else {
                       findViewById(R.id.progressBar).setVisibility(View.GONE);
                       postView.setVisibility(View.GONE);
                       findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                   }
               }

                if (!snapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    postView.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}