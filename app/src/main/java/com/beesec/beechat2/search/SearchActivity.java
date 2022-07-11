package com.beesec.beechat2.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterGroups;
import com.beesec.beechat2.adapter.AdapterPost;
import com.beesec.beechat2.adapter.AdapterProduct;
import com.beesec.beechat2.adapter.AdapterUsers;
import com.beesec.beechat2.model.ModelGroups;
import com.beesec.beechat2.model.ModelPost;
import com.beesec.beechat2.model.ModelProduct;
import com.beesec.beechat2.model.ModelUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class SearchActivity extends AppCompatActivity {

    //User
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    RecyclerView users_rv;

    //Group
    AdapterGroups adapterGroups;
    List<ModelGroups> modelGroups;
    RecyclerView groups;

    //Market
    RecyclerView productList;
    AdapterProduct adapterProduct;
    List<ModelProduct> modelProducts;

    //Post
    AdapterPost adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;

    //Other
    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button more,moreUsers;
    long initial;
    String type = "user";

    NightMode sharedPref;

    long initialUsers;
    private static final int TOTAL_ITEM_EACH_LOAD_users = 10;
    private int currentPageUsers = 1;

    long startTime;
    long endTime;

    @Override
    protected void onStart() {
        super.onStart();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        endTime = System.currentTimeMillis();
        long timeSpend = endTime - startTime;
        if (timeSpend > 200000){
            addMoney();
        }
        super.onBackPressed();
    }

    private void addMoney() {

        FirebaseDatabase.getInstance().getReference("Balance").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double add = Double.parseDouble(Objects.requireNonNull(snapshot.child("balance").getValue()).toString()) + 0.01;
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("balance", String.valueOf(add));
                FirebaseDatabase.getInstance().getReference("Balance").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

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
        setContentView(R.layout.activity_search);

        //Back
        findViewById(R.id.back).setOnClickListener(v -> {
            endTime = System.currentTimeMillis();
            long timeSpend = endTime - startTime;
            if (timeSpend > 200000){
                addMoney();
                onBackPressed();
            }else {
                onBackPressed();
            }
        });

        more = findViewById(R.id.more);
        moreUsers = findViewById(R.id.moreUsers);

        //User
        users_rv = findViewById(R.id.users);
        users_rv.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        userList = new ArrayList<>();
        getAllUsers();

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        modelPosts = new ArrayList<>();

        //Groups
        groups = findViewById(R.id.groups);
        groups.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        modelGroups = new ArrayList<>();

        //Market
        productList = findViewById(R.id.products);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        productList.setLayoutManager(gridLayoutManager);
        modelProducts = new ArrayList<>();

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                switch (type) {
                    case "user":
                        filterUser(editText.getText().toString());
                        break;
                    case "post":
                        filterPost(editText.getText().toString());
                        break;
                    case "group":
                        filterGroup(editText.getText().toString());
                        break;
                    case "product":
                        filterProduct(editText.getText().toString());
                        break;
                }
                return true;
            }
            return false;
        });

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    getAllUsers();
                    type = "user";
                    users_rv.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    groups.setVisibility(View.GONE);
                    productList.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    moreUsers.setVisibility(View.VISIBLE);

                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    users_rv.setVisibility(View.GONE);
                    groups.setVisibility(View.GONE);
                    post.setVisibility(View.VISIBLE);
                    productList.setVisibility(View.GONE);
                    type = "post";
                    moreUsers.setVisibility(View.GONE);
                    more.setVisibility(View.VISIBLE);
                    getAllPost();
                } else if (tabLayout.getSelectedTabPosition() == 2) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                    users_rv.setVisibility(View.GONE);
                    moreUsers.setVisibility(View.GONE);
                    groups.setVisibility(View.VISIBLE);
                    productList.setVisibility(View.GONE);
                    post.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    type = "group";
                    getAllGroup();

                } else if (tabLayout.getSelectedTabPosition() == 3) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    users_rv.setVisibility(View.GONE);
                    groups.setVisibility(View.GONE);
                    productList.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    more.setVisibility(View.GONE);
                    moreUsers.setVisibility(View.GONE);
                    type = "product";
                    getAllProducts();
                } else if (tabLayout.getSelectedTabPosition() == 4) {
                    startActivity(new Intent(SearchActivity.this, LocationActivity.class));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users");
        reference2.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    i++;
                }
                initialUsers = i;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });

        findViewById(R.id.moreUsers).setOnClickListener(v -> loadMoreUsers());

        //Tag
        if (getIntent().hasExtra("hashtag")){
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            users_rv.setVisibility(View.GONE);
            groups.setVisibility(View.GONE);
            post.setVisibility(View.VISIBLE);
            productList.setVisibility(View.GONE);
            type = "post";
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
            more.setVisibility(View.GONE);
            moreUsers.setVisibility(View.GONE);
            filterPost(getIntent().getStringExtra("hashtag"));
            editText.setText(getIntent().getStringExtra("hashtag"));
        }

    }

    private void filterPost(String query) {
        FirebaseDatabase.getInstance().getReference("Posts").limitToLast(currentPage*TOTAL_ITEM_EACH_LOAD)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPosts.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelPost = ds.getValue(ModelPost.class);
                            if (Objects.requireNonNull(modelPost).getText().toLowerCase().contains(query.toLowerCase()) ||
                                    modelPost.getType().toLowerCase().contains(query.toLowerCase())){
                                modelPosts.add(modelPost);
                            }
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(SearchActivity.this, modelPosts);
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
                                more.setVisibility(View.GONE);
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

    private void filterProduct(String query) {
        FirebaseDatabase.getInstance().getReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelProducts.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelProduct modelUser = ds.getValue(ModelProduct.class);
                    if (Objects.requireNonNull(modelUser).getTitle().toLowerCase().contains(query.toLowerCase()) || modelUser.getCat().toLowerCase().contains(query.toLowerCase())
                            || modelUser.getType().toLowerCase().contains(query.toLowerCase()) ||
                            modelUser.getLocation().toLowerCase().contains(query.toLowerCase()) ||
                            modelUser.getPrice().toLowerCase().contains(query.toLowerCase()) ||
                            modelUser.getDes().toLowerCase().contains(query.toLowerCase())){
                        modelProducts.add(modelUser);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                    Collections.reverse(modelProducts);
                    adapterProduct = new AdapterProduct(SearchActivity.this, modelProducts);
                    productList.setAdapter(adapterProduct);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterProduct.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.products).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.products).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.products).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filterGroup(String query) {
        FirebaseDatabase.getInstance().getReference("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroups.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelGroups modelUser = ds.getValue(ModelGroups.class);
                    if (Objects.requireNonNull(modelUser).getgName().toLowerCase().contains(query.toLowerCase()) ||
                            modelUser.getgUsername().toLowerCase().contains(query.toLowerCase())){
                        modelGroups.add(modelUser);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                    Collections.reverse(modelGroups);
                    adapterGroups = new AdapterGroups(SearchActivity.this, modelGroups);
                    groups.setAdapter(adapterGroups);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.groups).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filterUser(String query) {
        FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.hasChild("name")){
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                            if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                    modelUser.getUsername().toLowerCase().contains(query.toLowerCase())){
                                userList.add(modelUser);
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }
                        Collections.reverse(userList);
                        adapterUsers = new AdapterUsers(SearchActivity.this, userList);
                        users_rv.setAdapter(adapterUsers);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        if (adapterUsers.getItemCount() == 0){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            findViewById(R.id.users).setVisibility(View.GONE);
                            findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                            moreUsers.setVisibility(View.GONE);
                            findViewById(R.id.moreUsers).setVisibility(View.GONE);
                        }else {
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            findViewById(R.id.moreUsers).setVisibility(View.VISIBLE);
                            findViewById(R.id.users).setVisibility(View.VISIBLE);
                            findViewById(R.id.nothing).setVisibility(View.GONE);
                            moreUsers.setVisibility(View.GONE);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void loadMoreUsers() {
        currentPageUsers++;
        getAllUsers();
    }

    private void getAllPost() {
        FirebaseDatabase.getInstance().getReference("Posts").limitToLast(currentPage*TOTAL_ITEM_EACH_LOAD)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPosts.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelPost = ds.getValue(ModelPost.class);
                            modelPosts.add(modelPost);
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(SearchActivity.this, modelPosts);
                            post.setAdapter(adapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                post.setVisibility(View.GONE);
                                findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                                findViewById(R.id.more).setVisibility(View.GONE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                findViewById(R.id.more).setVisibility(View.VISIBLE);
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
                            findViewById(R.id.more).setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getAllProducts() {
        FirebaseDatabase.getInstance().getReference("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelProducts.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelProduct modelUser = ds.getValue(ModelProduct.class);
                    modelProducts.add(modelUser);
                    Collections.reverse(modelProducts);
                    adapterProduct = new AdapterProduct(SearchActivity.this, modelProducts);
                    productList.setAdapter(adapterProduct);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterProduct.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.products).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.products).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.products).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllGroup() {
        FirebaseDatabase.getInstance().getReference("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroups.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelGroups modelUser = ds.getValue(ModelGroups.class);
                    modelGroups.add(modelUser);
                    Collections.reverse(modelGroups);
                    adapterGroups = new AdapterGroups(SearchActivity.this, modelGroups);
                    groups.setAdapter(adapterGroups);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.groups).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.groups).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUsers() {
        FirebaseDatabase.getInstance().getReference("Users").limitToLast(currentPageUsers*TOTAL_ITEM_EACH_LOAD_users).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.hasChild("name")){
                        ModelUser modelUser = ds.getValue(ModelUser.class);
                        if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals(Objects.requireNonNull(modelUser).getId())){
                            userList.add(modelUser);
                        }
                    }
                    Collections.reverse(userList);
                    adapterUsers = new AdapterUsers(SearchActivity.this, userList);
                    users_rv.setAdapter(adapterUsers);
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    if (adapterUsers.getItemCount() == 0){
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.GONE);
                        findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        findViewById(R.id.users).setVisibility(View.VISIBLE);
                        findViewById(R.id.nothing).setVisibility(View.GONE);
                        if(adapterUsers.getItemCount() == initial){
                            moreUsers.setVisibility(View.GONE);
                            currentPageUsers--;
                        }else {
                            moreUsers.setVisibility(View.VISIBLE);
                        }

                    }
                }


                if (!dataSnapshot.exists()){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.users).setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
