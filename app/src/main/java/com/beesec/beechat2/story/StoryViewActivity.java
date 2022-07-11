package com.beesec.beechat2.story;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.beesec.beechat2.GetTimeAgo;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelStory;
import com.beesec.beechat2.who.ViewedActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

@SuppressWarnings("ALL")
public class StoryViewActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    //Declare
    String userid;
    int counter = 0;
    long pressTime = 0L;
    final long limit = 500L;

    //Id
    StoriesProgressView storiesProgressView;
    ImageView sImage;
    VideoView sVideo;
    TextView name,time,seen;
    CircleImageView dp;

    //List
    List<String> layouts;
    List<String> storyids;


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
            findViewById(R.id.delete_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.highlight).setVisibility(View.VISIBLE);
            findViewById(R.id.message).setVisibility(View.GONE);
        }else {
            findViewById(R.id.message).setVisibility(View.VISIBLE);
        }

        //view
        findViewById(R.id.seen_layout).setOnClickListener(v -> {
            Intent intent = new Intent(StoryViewActivity.this, ViewedActivity.class);
            intent.putExtra("id",userid);
            intent.putExtra("storyid",storyids.get(counter));
            startActivity(intent);
        });

        //Delete
        findViewById(R.id.delete_layout).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure ?");
            builder.setPositiveButton("Delete",
                    (dialog, which) -> {
                        Toast.makeText(StoryViewActivity.this, "Please wait deleting", Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference("Story")
                                .child(userid).child(storyids.get(counter)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.child("imageUri").getValue().toString()).delete();
                                snapshot.getRef().removeValue();
                                finish();
                                onBackPressed();
                                Toast.makeText(StoryViewActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    });
            builder.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //Edit
        EditText sendMessage = findViewById(R.id.sendMessage);
        sendMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    storiesProgressView.resume();
                }else {
                    storiesProgressView.pause();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.imageView2).setOnClickListener(v -> {
            if (sendMessage.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a message", Snackbar.LENGTH_SHORT).show();
            }else {
                try {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("receiver", userid);
                    hashMap.put("msg",  sendMessage.getText().toString());
                    hashMap.put("isSeen", false);
                    hashMap.put("timestamp", storyids.get(counter));
                    hashMap.put("type", "story");
                    FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                    Snackbar.make(v, "Message sent", Snackbar.LENGTH_SHORT).show();
                    sendMessage.setText("");
                    storiesProgressView.resume();
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });


        //Highlight
        findViewById(R.id.highlight).setOnClickListener(v -> {

            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyids.get(counter)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyids.get(counter)).removeValue();
                        Snackbar.make(v, "Removed from HighLight", Snackbar.LENGTH_LONG).show();
                    }else {
                        FirebaseDatabase.getInstance().getReference("Story").child(userid).child(storyids.get(counter)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.hasChild("type")){
                                    if (snapshot.child("type").getValue().toString().equals("image")){
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                        hashMap.put("storyid", storyids.get(counter));
                                        hashMap.put("type", "image");
                                        hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyids.get(counter)).setValue(hashMap);
                                        Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                    }else if (snapshot.child("type").getValue().toString().equals("video")){

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                        hashMap.put("storyid", storyids.get(counter));
                                        hashMap.put("type", "video");
                                        hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyids.get(counter)).setValue(hashMap);
                                        Snackbar.make(v, "Added to HighLight", Snackbar.LENGTH_LONG).show();
                                    }
                                }else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uri", snapshot.child("imageUri").getValue().toString());
                                    hashMap.put("storyid", storyids.get(counter));
                                    hashMap.put("type", "image");
                                    hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("High").child(storyids.get(counter)).setValue(hashMap);
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
        //Display
        FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyids.get(++counter)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                //Time
                long lastTime = Long.parseLong(snapshot.child("timestart").getValue().toString());
                time.setText(GetTimeAgo.getTimeAgo(lastTime));

                if (snapshot.hasChild("type")){

                    if (snapshot.child("type").getValue().toString().equals("image")){
                        sImage.setVisibility(View.VISIBLE);
                        sVideo.setVisibility(View.GONE);
                        Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                    }else if (snapshot.child("type").getValue().toString().equals("video")){

                        sImage.setVisibility(View.GONE);
                        sVideo.setVisibility(View.VISIBLE);
                        sVideo.setVideoPath(snapshot.child("imageUri").getValue().toString());
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
                    Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        //Display
        FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyids.get(--counter)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                //Time
                long lastTime = Long.parseLong(snapshot.child("timestart").getValue().toString());
                time.setText(GetTimeAgo.getTimeAgo(lastTime));

                if (snapshot.hasChild("type")){

                    if (snapshot.child("type").getValue().toString().equals("image")){
                        sImage.setVisibility(View.VISIBLE);
                        sVideo.setVisibility(View.GONE);
                        Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                    }else if (snapshot.child("type").getValue().toString().equals("video")){
                        sImage.setVisibility(View.GONE);
                        sVideo.setVisibility(View.VISIBLE);
                        sVideo.setVideoPath(snapshot.child("imageUri").getValue().toString());
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
                    Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenNumber(storyids.get(counter));
    }


    private void getStories(String userid) {
        layouts = new ArrayList<>();
        storyids = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layouts.clear();
                storyids.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    ModelStory modelStory = snapshot1.getValue(ModelStory.class);
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > Objects.requireNonNull(modelStory).getTimestart() && timecurrent < modelStory.getTimeend()){
                        layouts.add(modelStory.imageUri);
                        storyids.add(modelStory.storyid);
                    }
                }


                storiesProgressView.setStoriesCount(layouts.size());
                storiesProgressView.setStoryDuration(8000);
                storiesProgressView.setStoriesListener(StoryViewActivity.this);
                storiesProgressView.startStories(counter);
                addView(storyids.get(counter));
                seenNumber(storyids.get(counter));

                //Display
                FirebaseDatabase.getInstance().getReference("Story")
                        .child(userid).child(storyids.get(counter)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        //Time
                        long lastTime = Long.parseLong(snapshot.child("timestart").getValue().toString());
                        time.setText(GetTimeAgo.getTimeAgo(lastTime));

                        if (snapshot.hasChild("type")){

                            if (snapshot.child("type").getValue().toString().equals("image")){
                                sImage.setVisibility(View.VISIBLE);
                                sVideo.setVisibility(View.GONE);
                                Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                            }else if (snapshot.child("type").getValue().toString().equals("video")){

                                sImage.setVisibility(View.GONE);
                                sVideo.setVisibility(View.VISIBLE);
                                sVideo.setVideoPath(snapshot.child("imageUri").getValue().toString());
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
                            Glide.with(StoryViewActivity.this).load(snapshot.child("imageUri").getValue().toString()).into(sImage);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


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

    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").child(userid)
                .child(storyid).child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
    }
    private void seenNumber(String storyid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userid).child(storyid).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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