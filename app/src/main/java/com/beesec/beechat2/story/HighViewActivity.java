package com.beesec.beechat2.story;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

@SuppressWarnings("ALL")
public class HighViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    //Declare
    String userid;
    long pressTime = 0L;
    final long limit = 500L;

    //Id
    StoriesProgressView storiesProgressView;
    ImageView sImage;
    VideoView sVideo;
    TextView name,time,seen;
    CircleImageView dp;
    String storyId;



    private final View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_view);

        //HisId
        userid = getIntent().getStringExtra("userid");
        storyId = getIntent().getStringExtra("story");

        //ID
        View reverse =  findViewById(R.id.reverse);
        View skip =  findViewById(R.id.skip);
        storiesProgressView =  findViewById(R.id.stories);
        sImage = findViewById(R.id.image);
        sVideo = findViewById(R.id.video);
        time = findViewById(R.id.time);
        name = findViewById(R.id.name);
        dp = findViewById(R.id.dp);
        seen = findViewById(R.id.seen);
        findViewById(R.id.time).setVisibility(View.GONE);
        findViewById(R.id.dot).setVisibility(View.GONE);

        //Get
        getStories(userid);
        getUserDetails(userid);

        //View
        reverse.setOnClickListener(v -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);

        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);

        //Me
        if (userid.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            findViewById(R.id.seen_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.highlight).setVisibility(View.VISIBLE);
        }

        //Todo
        findViewById(R.id.seen_layout).setVisibility(View.GONE);
        findViewById(R.id.sendMessage).setVisibility(View.GONE);
        findViewById(R.id.imageView2).setVisibility(View.GONE);

        //Highlight
        findViewById(R.id.highlight).setOnClickListener(v -> {

            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyId).removeValue();
                        Snackbar.make(v, "Removed from HighLight", Snackbar.LENGTH_LONG).show();
                    }else {
                        FirebaseDatabase.getInstance().getReference("Users").child(userid).child("High").child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("type")){
                                    if (snapshot.child("type").getValue().toString().equals("image")){
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                        hashMap.put("storyid", storyId);
                                        hashMap.put("type", "image");
                                        hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyId).setValue(hashMap);
                                        Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                    }else if (snapshot.child("type").getValue().toString().equals("video")){

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                        hashMap.put("storyid", storyId);
                                        hashMap.put("type", "video");
                                        hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyId).setValue(hashMap);
                                        Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                    }
                                }else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                    hashMap.put("storyid", storyId);
                                    hashMap.put("type", "image");
                                    hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyId).setValue(hashMap);
                                    Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

    }

    private void getUserDetails(String userid) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                if (!snapshot.child("photo").getValue().toString().isEmpty()){
                    Picasso.get().load(snapshot.child("photo").getValue().toString()).into(dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNext() {
     finish();
    }

    @Override
    public void onPrev() {
        finish();
    }


    private void getStories(String userid) {
        FirebaseDatabase.getInstance().getReference("Users").child(userid).child("High").child(storyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                storiesProgressView.setStoriesCount(1);
                storiesProgressView.setStoryDuration(8000);
                storiesProgressView.setStoriesListener(HighViewActivity.this);
                storiesProgressView.startStories(0);
                addView();
                seenNumber();


                if (snapshot.hasChild("type")){

                    if (snapshot.child("type").getValue().toString().equals("image")){
                        sImage.setVisibility(View.VISIBLE);
                        sVideo.setVisibility(View.GONE);
                        Glide.with(HighViewActivity.this).load(snapshot.child("uri").getValue().toString()).into(sImage);

                    }else if (snapshot.child("type").getValue().toString().equals("video")){

                        sImage.setVisibility(View.GONE);
                        sVideo.setVisibility(View.VISIBLE);
                        sVideo.setVideoPath(snapshot.child("uri").getValue().toString());
                        sVideo.setOnPreparedListener(mp -> {
                            sVideo.start();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        });
                        storiesProgressView.pause();
                        sVideo.setOnCompletionListener(mp -> {

                            sVideo.pause();
                            storiesProgressView.skip();
                        });

                    }
                }else {
                    sImage.setVisibility(View.VISIBLE);
                    sVideo.setVisibility(View.GONE);
                    Glide.with(HighViewActivity.this).load(snapshot.child("uri").getValue().toString()).into(sImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void addView(){
        FirebaseDatabase.getInstance().getReference("Users").child(userid).child("High").child(storyId).child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
    }
    private void seenNumber(){
        FirebaseDatabase.getInstance().getReference("Users").child(userid).child("High").child(storyId).child("views").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}