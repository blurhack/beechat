package com.beesec.beechat2.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.beesec.beechat2.MediaViewActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterHigh;
import com.beesec.beechat2.adapter.AdapterPost;
import com.beesec.beechat2.adapter.AdapterReelView;
import com.beesec.beechat2.calling.RingingActivity;
import com.beesec.beechat2.chat.ChatActivity;
import com.beesec.beechat2.groupVoiceCall.RingingGroupVoiceActivity;
import com.beesec.beechat2.model.ModelHigh;
import com.beesec.beechat2.model.ModelPost;
import com.beesec.beechat2.model.ModelReel;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.who.FollowersActivity;
import com.beesec.beechat2.who.FollowingActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    //Id
    VideoView videoView;
    ImageView cover,verify,more;
    CircleImageView dp;
    TextView name,username,location;
    SocialTextView bio,link;
    TextView followers,following,posts,topName;
    LinearLayout following_ly,followers_ly,link_layout,location_layout;
    Button follow,unfollow;
    LinearLayout scroll;

    //String
    String hisUID;
    boolean isBlocked = false;

    //Bottom
    BottomSheetDialog more_options;
    LinearLayout message,report,block,poke;
    TextView text;

    //Post
    AdapterPost adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;

    private static final int TOTAL_ITEM_EACH_LOAD = 6;
    private int currentPage = 1;
    Button load;
    long initial;
    TextView nothing;

    //Story
    private AdapterHigh adapterHigh;
    private List<ModelHigh> modelHighs;
    RecyclerView storyView;

    private RequestQueue requestQueue;
    private boolean notify = false;

    //Reel
    RecyclerView reelView;
    AdapterReelView adapterReelView;
    List<ModelReel> modelReel;

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
        setContentView(R.layout.activity_user_profile);

        requestQueue = Volley.newRequestQueue(UserProfileActivity.this);

        //GetHisId

        hisUID = getIntent().getStringExtra("hisUID");

        //Declaring
        videoView = findViewById(R.id.video);
        cover = findViewById(R.id.cover);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        bio = findViewById(R.id.bio);
        username = findViewById(R.id.username);
        location = findViewById(R.id.location);
        link = findViewById(R.id.link);
        followers = findViewById(R.id.followers);
        following = findViewById(R.id.following);
        posts = findViewById(R.id.posts);
        following_ly = findViewById(R.id.linearLayout5);
        followers_ly = findViewById(R.id.linearLayout4);
        verify = findViewById(R.id.verify);
        follow = findViewById(R.id.follow);
        unfollow = findViewById(R.id.unfollow);
        more = findViewById(R.id.more);
        scroll = findViewById(R.id.scroll);
        location_layout = findViewById(R.id.location_layout);
        link_layout = findViewById(R.id.link_layout);
        topName = findViewById(R.id.topName);

        //OnStart
        findViewById(R.id.details).setVisibility(View.GONE);
        findViewById(R.id.bio).setVisibility(View.GONE);
        findViewById(R.id.name).setVisibility(View.GONE);
        findViewById(R.id.followers).setVisibility(View.GONE);
        findViewById(R.id.following).setVisibility(View.GONE);
        findViewById(R.id.posts).setVisibility(View.GONE);

        //more
        more.setOnClickListener(v -> more_options.show());

        findViewById(R.id.menu).setOnClickListener(v -> more_options.show());

        findViewById(R.id.edit).setOnClickListener(v -> onBackPressed());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists()){
                        for (DataSnapshot dataSnapshot1 : ds.child("Voice").getChildren()){
                            if (Objects.requireNonNull(dataSnapshot1.child("type").getValue()).toString().equals("calling")){
                                if (!Objects.requireNonNull(dataSnapshot1.child("from").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    if (!dataSnapshot1.child("end").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        if (!dataSnapshot1.child("ans").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            Intent intent = new Intent(getApplicationContext(), RingingGroupVoiceActivity.class);
                                            intent.putExtra("room", Objects.requireNonNull(dataSnapshot1.child("room").getValue()).toString());
                                            intent.putExtra("group", Objects.requireNonNull(ds.child("groupId").getValue()).toString());
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Reels
        reelView = findViewById(R.id.reel);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        reelView.setLayoutManager(gridLayoutManager);
        modelReel = new ArrayList<>();

        //Call
        Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (Objects.requireNonNull(ds.child("type").getValue()).toString().equals("calling")){
                            Intent intent = new Intent(UserProfileActivity.this, RingingActivity.class);
                            intent.putExtra("room", Objects.requireNonNull(ds.child("room").getValue()).toString());
                            intent.putExtra("from", Objects.requireNonNull(ds.child("from").getValue()).toString());
                            intent.putExtra("call", Objects.requireNonNull(ds.child("call").getValue()).toString());
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Firebase
        FirebaseDatabase.getInstance().getReference().child("Users").child(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mDp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                String mName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                String mUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                String mBio = Objects.requireNonNull(snapshot.child("bio").getValue()).toString();
                String mLocation = Objects.requireNonNull(snapshot.child("location").getValue()).toString();
                String mLink = Objects.requireNonNull(snapshot.child("link").getValue()).toString();
                String mVerify = Objects.requireNonNull(snapshot.child("verified").getValue()).toString();

                if (!mDp.isEmpty()){
                    Picasso.get().load(mDp).placeholder(R.drawable.avatar).into(dp);
                }

                name.setText(mName);
                username.setText(mUsername);
                location.setText(mLocation);
                bio.setLinkText(mBio);
                link.setLinkText(mLink);
                topName.setText(mUsername);

                bio.setOnLinkClickListener((i, s) -> {
                    if (i == 1){

                        Intent intent = new Intent(UserProfileActivity.this, SearchActivity.class);
                        intent.putExtra("hashtag", s);
                        startActivity(intent);

                    }else
                    if (i == 2){
                        String username = s.replaceFirst("@","");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = ref.orderByChild("username").equalTo(username.trim());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        String id = Objects.requireNonNull(ds.child("id").getValue()).toString();
                                        if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            Snackbar.make(scroll,"It's you", Snackbar.LENGTH_LONG).show();
                                        }else {
                                            Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                                            intent.putExtra("hisUID", id);
                                            startActivity(intent);
                                        }
                                    }
                                }else {
                                    Snackbar.make(scroll,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Snackbar.make(scroll,error.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                    else if (i == 16){
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        startActivity(openUrlIntent);
                    }else if (i == 4){
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                        startActivity(intent);
                    }else if (i == 8){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, s);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        startActivity(intent);

                    }
                });
                link.setOnLinkClickListener((i, s) -> {
                    if (i == 1){

                        Intent intent = new Intent(UserProfileActivity.this, SearchActivity.class);
                        intent.putExtra("hashtag", s);
                        startActivity(intent);

                    }else
                    if (i == 2){
                        String username = s.replaceFirst("@","");
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = ref.orderByChild("username").equalTo(username.trim());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        String id = Objects.requireNonNull(ds.child("id").getValue()).toString();
                                        if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            Snackbar.make(scroll,"It's you", Snackbar.LENGTH_LONG).show();
                                        }else {
                                            Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                                            intent.putExtra("hisUID", id);
                                            startActivity(intent);
                                        }
                                    }
                                }else {
                                    Snackbar.make(scroll,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Snackbar.make(scroll,error.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                    else if (i == 16){
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        startActivity(openUrlIntent);
                    }else if (i == 4){
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                        startActivity(intent);
                    }else if (i == 8){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, s);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        startActivity(intent);

                    }
                });

                location.setOnClickListener(v -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + mLocation));
                    startActivity(i);
                });

                if (mVerify.equals("yes")){
                    verify.setVisibility(View.VISIBLE);
                }else {
                    verify.setVisibility(View.GONE);
                }

                if (bio.getText().length()>0){
                    bio.setVisibility(View.VISIBLE);
                }

                if (location.getText().length()>0){
                    location_layout.setVisibility(View.VISIBLE);
                }else{
                    location_layout.setVisibility(View.GONE);
                }

                if (link.getText().length()>0){
                    link_layout.setVisibility(View.VISIBLE);
                }else{
                    link_layout.setVisibility(View.GONE);
                }

                //OnDone
                findViewById(R.id.details).setVisibility(View.VISIBLE);
                findViewById(R.id.name).setVisibility(View.VISIBLE);
                findViewById(R.id.followers).setVisibility(View.VISIBLE);
                findViewById(R.id.following).setVisibility(View.VISIBLE);
                findViewById(R.id.posts).setVisibility(View.VISIBLE);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(scroll,error.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

        //Cover
        FirebaseDatabase.getInstance().getReference().child("Cover").child(hisUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                    String uri = Objects.requireNonNull(snapshot.child("uri").getValue()).toString();

                    if (type.equals("image")){
                        Picasso.get().load(uri).placeholder(R.drawable.cover).into(cover);
                        videoView.setVisibility(View.GONE);
                        cover.setVisibility(View.VISIBLE);
                    }else if (type.equals("video")){

                        videoView.setVisibility(View.VISIBLE);
                        cover.setVisibility(View.GONE);
                        videoView.setVideoURI(Uri.parse(uri));
                        videoView.start();
                        videoView.setOnPreparedListener(mp -> {
                            mp.setLooping(true);
                            mp.setVolume(0, 0);
                        });
                        setDimension();
                        videoView.setOnClickListener(v -> {
                            Intent i = new Intent(getApplicationContext(), MediaViewActivity.class);
                            i.putExtra("type", "video");
                            i.putExtra("uri", uri);
                            startActivity(i);
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Snackbar.make(scroll,error.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

        //Follow
        follow.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Following").child(hisUID).setValue(true);
            FirebaseDatabase.getInstance().getReference().child("Follow").child(hisUID)
                    .child("Followers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
            follow.setVisibility(View.GONE);
            unfollow.setVisibility(View.VISIBLE);
            addToHisNotification(hisUID, "Started following you");
            notify = true;
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUser user = snapshot.getValue(ModelUser.class);
                    if (notify){
                        sendNotification(hisUID, Objects.requireNonNull(user).getName(), "Started following you");
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        //Post
        storyView = findViewById(R.id.story);
        LinearLayoutManager linearLayoutManager5 = new LinearLayoutManager(UserProfileActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        storyView.setLayoutManager(linearLayoutManager5);
        modelHighs = new ArrayList<>();
        readStory();

        //UnFollow
        unfollow.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Following").child(hisUID).removeValue();
            FirebaseDatabase.getInstance().getReference().child("Follow").child(hisUID)
                    .child("Followers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
            unfollow.setVisibility(View.GONE);
            follow.setVisibility(View.VISIBLE);
        });

        //method
        isFollowing();
        checkBlocked();

        //bottom
        options();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelPost post = snapshot.getValue(ModelPost.class);
                    if (Objects.requireNonNull(post).getId().equals(hisUID)){
                        i++;
                    }
                }
                initial = i;
                posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Post
        post = findViewById(R.id.post);
        post.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this));
        modelPosts = new ArrayList<>();
        getAllPost();

        load = findViewById(R.id.load);
        load.setOnClickListener(v1 -> loadMoreData());
        nothing = findViewById(R.id.nothing);

        getFollowers();
        getFollowing();

        followers_ly.setOnClickListener(v -> {
        Intent intent = new Intent(UserProfileActivity.this, FollowersActivity.class);
        intent.putExtra("id", hisUID);
        startActivity(intent);
        });

        following_ly.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, FollowingActivity.class);
            intent.putExtra("id", hisUID);
            startActivity(intent);
        });

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getReel();
                    reelView.setVisibility(View.VISIBLE);
                    post.setVisibility(View.GONE);
                    load.setVisibility(View.GONE);
                } else if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    post.setVisibility(View.VISIBLE);
                    reelView.setVisibility(View.GONE);
                    load.setVisibility(View.VISIBLE);
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



    private void getReel() {
        FirebaseDatabase.getInstance().getReference("Reels")
                .orderByChild("id").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelReel.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelReel modelPost = ds.getValue(ModelReel.class);
                            modelReel.add(modelPost);
                            adapterReelView = new AdapterReelView(modelReel);
                            reelView.setAdapter(adapterReelView);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterReelView.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                reelView.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
                            }
                        }

                        if (!snapshot.exists()){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            reelView.setVisibility(View.GONE);
                            nothing.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void  getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisUID).child("Followers");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void  getFollowing(){
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(hisUID).child("Following");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStory() {
        FirebaseDatabase.getInstance().getReference("Users").child(hisUID).child("High").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelHighs.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelHigh modelStory = ds.getValue(ModelHigh.class);
                    modelHighs.add(modelStory);
                }
                adapterHigh = new AdapterHigh(UserProfileActivity.this, modelHighs);
                storyView.setAdapter(adapterHigh);
                adapterHigh.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void getAllPost() {
        FirebaseDatabase.getInstance().getReference("Posts").limitToLast(currentPage*TOTAL_ITEM_EACH_LOAD)
                .orderByChild("id").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelPosts.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelPost modelPost = ds.getValue(ModelPost.class);
                            modelPosts.add(modelPost);
                            Collections.reverse(modelPosts);
                            adapterPost = new AdapterPost(UserProfileActivity.this, modelPosts);
                            post.setAdapter(adapterPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (adapterPost.getItemCount() == 0){
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                post.setVisibility(View.GONE);
                                nothing.setVisibility(View.VISIBLE);
                            }else {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                post.setVisibility(View.VISIBLE);
                                nothing.setVisibility(View.GONE);
                                if(adapterPost.getItemCount() == initial){
                                    load.setVisibility(View.GONE);
                                    currentPage--;
                                }else {
                                    load.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        if(!snapshot.exists()){
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            post.setVisibility(View.GONE);
                            nothing.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkBlocked() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(hisUID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                text.setText("Unblock");
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void isFollowing() {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("Following");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(hisUID).exists()){
                    follow.setVisibility(View.GONE);
                    unfollow.setVisibility(View.VISIBLE);
                }else {
                    follow.setVisibility(View.VISIBLE);
                    unfollow.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void options() {
        if (more_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.user_more, null);
            message = view.findViewById(R.id.message);
            poke = view.findViewById(R.id.poke);
            block = view.findViewById(R.id.block);
            report = view.findViewById(R.id.report);
            text = view.findViewById(R.id.text);
            message.setOnClickListener(this);
            poke.setOnClickListener(this);
            block.setOnClickListener(this);
            report.setOnClickListener(this);

            view.findViewById(R.id.shareurl).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, name.getText().toString() + " Profile link " + "www.app.myfriend.com/user/" + hisUID + "\nDownload the app "+"https://play.google.com/store/apps/details?id=com.beesec.beechat2");
                startActivity(Intent.createChooser(intent, "Share Via"));
            });

            more_options = new BottomSheetDialog(this);
            more_options.setContentView(view);
        }
    }

    private void BlockUser() {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("id", hisUID);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").child(hisUID).setValue(hashMap);
        Snackbar.make(scroll, "Blocked",Snackbar.LENGTH_LONG).show();

    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(hisUID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue();
                                Snackbar.make(scroll, "UnBlocked",Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(scroll, error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.message:
                more_options.cancel();
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.putExtra("hisUID", hisUID);
                startActivity(intent);

                break;
            case R.id.poke:
                more_options.cancel();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisUID, Objects.requireNonNull(user).getName(), "Has poked you");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                addToHisNotification(hisUID, "Has poked you");
                break;
            case R.id.block:

                more_options.cancel();
                if (isBlocked){
                    unBlockUser();
                }else {
                    BlockUser();
                }

                break;
            case R.id.report:

                more_options.cancel();
                FirebaseDatabase.getInstance().getReference().child("userReport").child(hisUID).setValue(true);
                Snackbar.make(scroll, "Reported",Snackbar.LENGTH_LONG).show();

                break;
        }
    }

    private void setDimension() {

        float videoProportion = getVideoProportion();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenHeight / (float) screenWidth;
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();

        if (videoProportion < screenProportion) {
            lp.height= screenHeight;
            lp.width = (int) ((float) screenHeight / videoProportion);
        } else {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth * videoProportion);
        }
        videoView.setLayoutParams(lp);
    }

    private float getVideoProportion(){
        return 1.5f;
    }

    private void addToHisNotification(String hisUid, String message){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", message);
        hashMap.put("sUid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Count").child(timestamp).setValue(true);
    }

    private void sendNotification(final String hisId, final String name,final String message){

       /* String username = getResources().getString(R.string.your_email);
        String password = getResources().getString(R.string.your_password);
        String messageToSend = message;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator(){
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(username, password);
                    }
                });

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("email").getValue().toString().isEmpty()){
                    String em = snapshot.child("email").getValue().toString();

                    try {
                        Message message1 = new MimeMessage(session);
                        message1.setFrom(new InternetAddress(username));
                        message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                        message1.setSubject("New Message - "+getResources().getString(R.string.app_name));
                        message1.setText(name + " " +messageToSend);
                        Transport.send(message1);
                    }catch (MessagingException e){
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        */


        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "New Notification", hisId, "profile", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAoAVZ-Vk:APA91bH7bjTYlktpJ53F9XkNPbmnUMw-csCIbocmKGKPGRPzvBAYXQ0S0XGxP3bVAylQmM6nOW9iOLPz18jUy8GrtA4OlPSe5XffhxnHd9cKlZD6XPbc9IZ7RePBvbAU-CPJ2v7_yybJ");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}