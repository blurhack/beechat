package com.beesec.beechat2.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.adapter.AdapterLive;
import com.beesec.beechat2.adapter.AdapterPodcast;
import com.beesec.beechat2.adapter.AdapterPost;
import com.beesec.beechat2.adapter.AdapterStory;
import com.beesec.beechat2.faceFilters.FaceFilters;
import com.beesec.beechat2.group.GroupFragment;
import com.beesec.beechat2.model.ModelLive;
import com.beesec.beechat2.model.ModelPost;
import com.beesec.beechat2.model.ModelStory;
import com.beesec.beechat2.notifications.NotificationScreen;
import com.beesec.beechat2.post.CreatePostActivity;
import com.beesec.beechat2.R;
import com.beesec.beechat2.search.TrendingActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    //Post
    AdapterPost adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;

    //Live
    private AdapterLive live;
    private List<ModelLive> modelLives;
    RecyclerView liveView;

    //Pod
    private AdapterPodcast podcast;
    private List<ModelLive> modelLiveList;
    RecyclerView podView;

    //Story
    private AdapterStory adapterStory;
    private List<ModelStory> modelStories;
    RecyclerView storyView;

    //Follow
    List<String> followingList;

    //OtherId;
    ProgressBar progressBar;
    TextView nothing;


    private static final int TOTAL_ITEM_EACH_LOAD = 18;
    private int currentPage = 1;
    Button more;
    long initial;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //Post
        post = v.findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(getContext()));
        modelPosts = new ArrayList<>();
        checkFollowing();

        more = v.findViewById(R.id.more);
        v.findViewById(R.id.more).setOnClickListener(view -> {
            more.setText("Loading...");
            loadMoreData();
        });

        //PostIntent
        v.findViewById(R.id.create_post).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), CreatePostActivity.class)));

        //Search
        v.findViewById(R.id.search).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), TrendingActivity.class)));

        //Groups
        v.findViewById(R.id.add).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), GroupFragment.class)));

        //Camera
        v.findViewById(R.id.camera).setOnClickListener(v1 -> startActivity(new Intent(getActivity(), FaceFilters.class)));

        //Notification
        FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("Count").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    v.findViewById(R.id.bell).setVisibility(View.GONE);
                    v.findViewById(R.id.count).setVisibility(View.VISIBLE);
                    TextView count =  v.findViewById(R.id.count);
                    count.setText(String.valueOf(snapshot.getChildrenCount()));
                }else {
                    v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.count).setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        v.findViewById(R.id.bell).setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
            v.findViewById(R.id.count).setVisibility(View.GONE);
        });
        v.findViewById(R.id.count).setOnClickListener(v1 -> {
            startActivity(new Intent(getActivity(), NotificationScreen.class));
            v.findViewById(R.id.bell).setVisibility(View.VISIBLE);
            v.findViewById(R.id.count).setVisibility(View.GONE);
        });

        //Live
        liveView = v.findViewById(R.id.live_list);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        liveView.setLayoutManager(linearLayoutManager2);
        modelLives = new ArrayList<>();

         FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 CircleImageView circleImageView = v.findViewById(R.id.circleImageView);
                 if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()){
                     Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(circleImageView);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });


        //Pod
        podView = v.findViewById(R.id.pod_list);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        podView.setLayoutManager(linearLayoutManager3);
        modelLiveList = new ArrayList<>();

        //Story
        storyView = v.findViewById(R.id.story_list);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        modelStories = new ArrayList<>();


        //OtherId
        progressBar = v.findViewById(R.id.progressBar);
        nothing = v.findViewById(R.id.nothing);

        return v;
    }

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void getAllPost() {

        if (currentPage*TOTAL_ITEM_EACH_LOAD>0){
            FirebaseDatabase.getInstance().getReference("Posts").limitToLast(currentPage*TOTAL_ITEM_EACH_LOAD)
                    .addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            modelPosts.clear();
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ModelPost modelPost = ds.getValue(ModelPost.class);
                                for (String id : followingList){
                                    if (Objects.requireNonNull(modelPost).getId().equals(id)){
                                        modelPosts.add(modelPost);
                                    }
                                }
                            }
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(getActivity(), modelPosts);
                            post.setAdapter(adapterPost);
                            progressBar.setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                progressBar.setVisibility(View.GONE);
                                post.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                                more.setVisibility(View.GONE);
                            }else {
                                progressBar.setVisibility(View.GONE);
                                post.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
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

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
         FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                followingList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readLive();
                readPod();
                readStory();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                reference.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int i = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            for (String s : followingList){
                                if (Objects.requireNonNull(snapshot.child("id").getValue()).toString().equals(s)){
                                    i++;
                                }
                            }
                        }
                        initial = i;
                        getAllPost();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readStory(){
        FirebaseDatabase.getInstance().getReference("Story").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                modelStories.clear();
                for (String id : followingList){
                    int countStory = 0;
                    ModelStory modelStory = null;
                    for (DataSnapshot snapshot1 : snapshot.child(id).getChildren()){
                        modelStory = snapshot1.getValue(ModelStory.class);
                        if (timecurrent > Objects.requireNonNull(modelStory).getTimestart() && timecurrent < modelStory.getTimeend()){
                            countStory++;
                        }
                    }
                    if (countStory > 0){
                        modelStories.add(modelStory);
                    }
                }
                adapterStory = new AdapterStory(getContext(), modelStories);
                storyView.setAdapter(adapterStory);
                adapterStory.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPod() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Podcast");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelLiveList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.hasChild("userid")){
                        ModelLive modelLive = ds.getValue(ModelLive.class);
                        for (String id : followingList){
                            if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelLive).getUserid()) && Objects.requireNonNull(modelLive).getUserid().equals(id)){
                                modelLiveList.add(modelLive);
                            }
                        }
                    }
                    podcast = new AdapterPodcast(getActivity(), modelLiveList);
                    podView.setAdapter(podcast);
                    podcast.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void readLive(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Live");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelLives.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.hasChild("userid")){
                        ModelLive modelLive = ds.getValue(ModelLive.class);
                        for (String id : followingList){
                            if (!Objects.requireNonNull(firebaseUser).getUid().equals(Objects.requireNonNull(modelLive).getUserid()) && Objects.requireNonNull(modelLive).getUserid().equals(id)){
                                modelLives.add(modelLive);
                            }
                        }
                    }
                    live = new AdapterLive(getActivity(), modelLives);
                    liveView.setAdapter(live);
                    live.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}