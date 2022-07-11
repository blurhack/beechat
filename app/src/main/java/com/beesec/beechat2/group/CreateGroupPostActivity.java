package com.beesec.beechat2.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.iceteck.silicompressorr.SiliCompressor;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.beesec.beechat2.live.activities.GoBroadcastActivity;
import com.beesec.beechat2.marketPlace.PostProductActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.StickersPost;
import com.beesec.beechat2.calling.RingingActivity;
import com.beesec.beechat2.faceFilters.FaceFilters;
import com.beesec.beechat2.groupVoiceCall.RingingGroupVoiceActivity;
import com.beesec.beechat2.meeting.MeetingActivity;
import com.beesec.beechat2.podcast.GoPodcastBroadcastActivity;
import com.beesec.beechat2.post.FeelingActivity;
import com.beesec.beechat2.post.PrivacyPick;
import com.beesec.beechat2.reel.VideoEditActivity;
import com.beesec.beechat2.watchParty.StartWatchPartyActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

@SuppressWarnings("ALL")
public class CreateGroupPostActivity extends AppCompatActivity implements PrivacyPick.SingleChoiceListener, View.OnClickListener {

    //Bottom
    BottomSheetDialog post_more;
    LinearLayout image,video,audio,meeting,feeling,reels,live,podcast,watch_party,sell,camera,gif,background;

    //BG
    BottomSheetDialog bg_more;
    ImageView button;
    RoundedImageView one,two,three,four,five,six,seven,eight,nine,ten,eleven, twelve,thirteen;
    RelativeLayout bg,upload;
    ImageView bg_image;

    //EdiText
    SocialEditText bg_text,postText;
    
    //Strings
    String privacyType;
    String type = "text";
    String number = "";
    String gifURL;
    String groupID;
    String mLocation = "";

    //Main
    ConstraintLayout main;

    //Media
    RelativeLayout relativeLayout;
    ImageView imageView,cancel;
    VideoView videoView;
    VoicePlayerView voicePlayerView;

    //Id
    ImageView sticker;
    TextView what;
    TextView location;
    TextView mPrivacy;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int LOCATION_PICK_CODE = 1009;
    private static final int VIDEO_PICK_CODE = 1002;
    private static final int AUDIO_PICK_CODE = 1003;
    private static final int PERMISSION_CODE = 1001;
    private static final int REEl_PICK_CODE = 202;

    //URI
    Uri image_uri,video_uri,audio_uri,bg_uri;

    @SuppressLint("SetTextI18n")

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
        setContentView(R.layout.activity_create_post);
        
        groupID = getIntent().getStringExtra("group");

        //SetUserInfo
        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //DP
                ImageView mDp = findViewById(R.id.circleImageView);
                String dp = Objects.requireNonNull(snapshot.child("gIcon").getValue()).toString();
                if (!dp.isEmpty()) Picasso.get().load(dp).into(mDp);

                //Name
                TextView mName = findViewById(R.id.name);
                mName.setText(Objects.requireNonNull(snapshot.child("gName").getValue()).toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //AddPost
        findViewById(R.id.constraintLayout2).setOnClickListener(v -> post_more.show());

        //Main
        main = findViewById(R.id.main);

        //Media
        relativeLayout = findViewById(R.id.relativeLayout);
        imageView = findViewById(R.id.image);
        videoView = findViewById(R.id.video);
        voicePlayerView = findViewById(R.id.voicePlayerView);
        cancel = findViewById(R.id.cancel);
        location = findViewById(R.id.location);
        mPrivacy = findViewById(R.id.privacy);

        //Bottom
        addPost();
        addBG();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()){
                        for (DataSnapshot dataSnapshot1 : ds.child("Voice").getChildren()){
                            if (dataSnapshot1.child("type").getValue().toString().equals("calling")){
                                if (!dataSnapshot1.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    if (!dataSnapshot1.child("end").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        if (!dataSnapshot1.child("ans").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            Intent intent = new Intent(getApplicationContext(), RingingGroupVoiceActivity.class);
                                            intent.putExtra("room", dataSnapshot1.child("room").getValue().toString());
                                            intent.putExtra("group", ds.child("groupId").getValue().toString());
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

        //Call
        Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (ds.child("type").getValue().toString().equals("calling")){
                            Intent intent = new Intent(CreateGroupPostActivity.this, RingingActivity.class);
                            intent.putExtra("room", ds.child("room").getValue().toString());
                            intent.putExtra("from", ds.child("from").getValue().toString());
                            intent.putExtra("call", ds.child("call").getValue().toString());
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

        //BG
        bg = findViewById(R.id.bg);
        bg_image = findViewById(R.id.bg_image);
        bg_text = findViewById(R.id.bg_text);

        //Privacy
        findViewById(R.id.setPrivacy).setOnClickListener(v -> {
            DialogFragment dialogFragment = new PrivacyPick();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "Single Choice Dialog");
        });

        //Location
        findViewById(R.id.add_location).setOnClickListener(v -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken("sk.eyJ1Ijoic3BhY2VzdGVyIiwiYSI6ImNrbmg2djJmdzJpZGQyd2xjeTk3a2twNTQifQ.iIiTRT_GwIYwFMsCWP5XGA")
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#ffffff"))
                            .build(PlaceOptions.MODE_CARDS))
                    .build(this);
            startActivityForResult(intent, LOCATION_PICK_CODE);
        });

        //feeling
         sticker = findViewById(R.id.img);
         what = findViewById(R.id.value);

        findViewById(R.id.feeling).setOnClickListener(v -> {
            FeelingActivity dialogFragment = FeelingActivity.newInstance();
            dialogFragment.setCallBack((type, value) -> {
                //activity
                switch (type) {
                    case "traveling":
                        sticker.setImageResource(R.drawable.airplane);
                        what.setText("Traveling to " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "watching":
                        sticker.setImageResource(R.drawable.watching);
                        what.setText("Watching " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "listening":
                        sticker.setImageResource(R.drawable.listening);
                        what.setText("Listening " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "thinking":
                        sticker.setImageResource(R.drawable.thinking);
                        what.setText("Thinking about " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "celebrating":
                        sticker.setImageResource(R.drawable.celebration);
                        what.setText("Celebrating " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "looking":
                        sticker.setImageResource(R.drawable.looking);
                        what.setText("Looking for " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "playing":
                        sticker.setImageResource(R.drawable.playing);
                        what.setText("Playing " + value);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                }
                //feeling
                switch (type) {
                    case "happy":
                        sticker.setImageResource(R.drawable.smiling);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "loved":
                        sticker.setImageResource(R.drawable.love);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "sad":
                        sticker.setImageResource(R.drawable.sad);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "crying":
                        sticker.setImageResource(R.drawable.crying);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "angry":
                        sticker.setImageResource(R.drawable.angry);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "confused":
                        sticker.setImageResource(R.drawable.confused);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "broken":
                        sticker.setImageResource(R.drawable.broken);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "cool":
                        sticker.setImageResource(R.drawable.cool);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "funny":
                        sticker.setImageResource(R.drawable.joy);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "tired":
                        sticker.setImageResource(R.drawable.tired);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "shock":
                        sticker.setImageResource(R.drawable.shocked);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "love":
                        sticker.setImageResource(R.drawable.heart);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "sleepy":
                        sticker.setImageResource(R.drawable.sleeping);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "expressionless":
                        sticker.setImageResource(R.drawable.muted);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                    case "blessed":
                        sticker.setImageResource(R.drawable.angel);
                        what.setText("Feeling " + type);
                        findViewById(R.id.gap).setVisibility(View.VISIBLE);
                        break;
                }

            });
            dialogFragment.show(getSupportFragmentManager(), "tag");
        });

        //Cancel
        cancel.setOnClickListener(v -> {
            relativeLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            voicePlayerView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            type = "text";
        });

        //EditText
        postText = findViewById(R.id.postText);

        //Upload
        findViewById(R.id.button).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.button).setVisibility(View.GONE);
            //BG
            if (type.equals("bg")){
                //TextEmpty
              if (Objects.requireNonNull(bg_text.getText()).toString().isEmpty()){
                  Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                  findViewById(R.id.progressBar).setVisibility(View.GONE);
                  findViewById(R.id.button).setVisibility(View.VISIBLE);
              }else {

                  if (number.equals("upload")){
                      uploadBG();
                  }else {

                      String timeStamp = String.valueOf(System.currentTimeMillis());
                      HashMap<Object, String> hashMap = new HashMap<>();
                      hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                      hashMap.put("pId", timeStamp);
                      hashMap.put("group", groupID);
                      hashMap.put("text", bg_text.getText().toString());
                      hashMap.put("type", type);
                      hashMap.put("meme", number);
                      hashMap.put("vine", "noVideo");
                      hashMap.put("pTime", timeStamp);
                     FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                      Snackbar.make(v,"Post Uploaded", Snackbar.LENGTH_LONG).show();
                    
                      //extra
                      postExtra(timeStamp);

                  }
              }
            }else if (type.equals("text")){
                //TextEmpty
                if (Objects.requireNonNull(postText.getText()).toString().isEmpty()){
                    Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.button).setVisibility(View.VISIBLE);
                }else {
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("pId", timeStamp);
                    hashMap.put("group", groupID);
                    hashMap.put("text", postText.getText().toString());
                    hashMap.put("type", type);
                    hashMap.put("meme", "noImage");
                    hashMap.put("vine", "noVideo");
                    hashMap.put("pTime", timeStamp);
                   FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                    Snackbar.make(v,"Post Uploaded", Snackbar.LENGTH_LONG).show();
                  
                    //extra
                    postExtra(timeStamp);
                }
            }else if (type.equals("image")){
                //TextEmpty
                if (Objects.requireNonNull(postText.getText()).toString().isEmpty()){
                    Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                }else {
                    compressImage(image_uri);
                }
            }else if (type.equals("gif")){
                //TextEmpty
                if (Objects.requireNonNull(postText.getText()).toString().isEmpty()){
                    Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                }else {
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("pId", timeStamp);
                    hashMap.put("group", groupID);
                    hashMap.put("text", postText.getText().toString());
                    hashMap.put("type", type);
                    hashMap.put("meme", gifURL);
                    hashMap.put("vine", "noVideo");
                    hashMap.put("pTime", timeStamp);
                   FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                    Snackbar.make(v,"Post Uploaded", Snackbar.LENGTH_LONG).show();
                  
                    //extra
                    postExtra(timeStamp);
                }
            }else if (type.equals("video")){
                //TextEmpty
                if (Objects.requireNonNull(postText.getText()).toString().isEmpty()){
                    Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                }else {
                  compressVideo();
                }
            }else if (type.equals("audio")){
                //TextEmpty
                if (Objects.requireNonNull(postText.getText()).toString().isEmpty()){
                    Snackbar.make(v,"Type something", Snackbar.LENGTH_LONG).show();
                }else {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_audio/" + ""+System.currentTimeMillis());
                    storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            String timeStamp = String.valueOf(System.currentTimeMillis());
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            hashMap.put("pId", timeStamp);
                            hashMap.put("text", Objects.requireNonNull(postText.getText()).toString());
                            hashMap.put("type", type);
                            hashMap.put("group", groupID);
                            assert downloadUri != null;
                            hashMap.put("meme", downloadUri.toString());
                            hashMap.put("vine", "noVideo");
                            hashMap.put("pTime", timeStamp);
                           FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                            Snackbar.make(main,"Post Uploaded", Snackbar.LENGTH_LONG).show();
                          
                            //extra
                            postExtra(timeStamp);
                        }
                    });
                }
            }

        });

        //GIF
        if (getIntent().hasExtra("gif")){
            type = "gif";
            gifURL = getIntent().getStringExtra("gif");
            relativeLayout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            Glide.with(CreateGroupPostActivity.this).load(getIntent().getStringExtra("gif")).thumbnail(0.1f).into(imageView);
        }

    }

    private void uploadBG() {

        //Upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(bg_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String timeStamp = String.valueOf(System.currentTimeMillis());
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("pId", timeStamp);
                hashMap.put("text", bg_text.getText().toString());
                hashMap.put("type", type);
                hashMap.put("group", groupID);
                hashMap.put("meme", downloadUri.toString());
                hashMap.put("vine", "noVideo");
                hashMap.put("pTime", timeStamp);
               FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                Snackbar.make(main,"Post Uploaded", Snackbar.LENGTH_LONG).show();
              
                //extra
                postExtra(timeStamp);
            }
        });


    }

    private void postExtra(String timeStamp) {
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("privacy", ""+mPrivacy.getText().toString());
        hashMap.put("feeling", ""+what.getText().toString());
        hashMap.put("location", ""+mLocation);
        FirebaseDatabase.getInstance().getReference("postExtra").child(timeStamp).setValue(hashMap);
        setDefault();
    }


    private void compressVideo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        new CreateGroupPostActivity.CompressVideo().execute("false",video_uri.toString(),file.getPath());
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(CreateGroupPostActivity.this)
                        .compressVideo(mUri,strings[2]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return videoPath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            File file = new File(s);
            Uri videoUri = Uri.fromFile(file);
            uploadVideo(videoUri);
        }
    }

    private void uploadVideo(Uri videoUri){

        //Upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String timeStamp = String.valueOf(System.currentTimeMillis());
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("pId", timeStamp);
                hashMap.put("text", Objects.requireNonNull(postText.getText()).toString());
                hashMap.put("type", type);
                assert downloadUri != null;
                hashMap.put("meme", "noImage");
                hashMap.put("group", groupID);
                hashMap.put("vine", downloadUri.toString());
                hashMap.put("pTime", timeStamp);
               FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                Snackbar.make(main,"Post Uploaded", Snackbar.LENGTH_LONG).show();
              
                //extra
                postExtra(timeStamp);
            }
        });

    }

    private void compressImage(Uri image_uri) {

        //Upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("post_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(image_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String timeStamp = String.valueOf(System.currentTimeMillis());
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("group", groupID);
                hashMap.put("pId", timeStamp);
                hashMap.put("text", Objects.requireNonNull(postText.getText()).toString());
                hashMap.put("type", type);
                assert downloadUri != null;
                hashMap.put("meme", downloadUri.toString());
                hashMap.put("vine", "noVideo");
                hashMap.put("pTime", timeStamp);
               FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Posts").child(timeStamp).setValue(hashMap);
                Snackbar.make(main,"Post Uploaded", Snackbar.LENGTH_LONG).show();
              
                //extra
                postExtra(timeStamp);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setDefault() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.button).setVisibility(View.VISIBLE);
        number = "";
        bg.setVisibility(View.GONE);
        bg_text.setVisibility(View.GONE);
        bg_image.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        voicePlayerView.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        type = "text";
        location.setText("Add Location");
        what.setText("");
        mPrivacy.setText("Everyone");
        findViewById(R.id.gap).setVisibility(View.GONE);
        postText.setText("");
        bg_text.setText("");
    }

    private void addBG() {
        if (bg_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.background_post, null);

            one = view.findViewById(R.id.one);
            one.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_one.jpeg?alt=media&token=2b1f2c65-14c0-4d49-8938-aac10900c082").into(one);

            two = view.findViewById(R.id.two);
            two.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_two.jpeg?alt=media&token=55222523-9e8e-4132-8f6c-33d055410146").into(two);

            three = view.findViewById(R.id.three);
            three.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_three.jpeg?alt=media&token=879afce6-96e4-417e-ba43-7241987953b4").into(three);

            four = view.findViewById(R.id.four);
            four.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_four.jpeg?alt=media&token=964f050c-160d-44dc-8967-db3d0aea3031").into(four);

            five = view.findViewById(R.id.five);
            five.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_five.jpeg?alt=media&token=28f7bae1-7c88-4edb-b434-90ae5f98ddf9").into(five);

            six = view.findViewById(R.id.six);
            six.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_six.jpeg?alt=media&token=425521dc-d8ed-46f6-80d7-e906d3eb5592").into(six);

            seven = view.findViewById(R.id.seven);
            seven.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_seven.jpeg?alt=media&token=efe084a6-3cfd-432f-9775-6da5b772b1d1").into(seven);

            eight = view.findViewById(R.id.eight);
            eight.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_eight.jpeg?alt=media&token=2c802174-4f32-401d-b1ce-7fe81c279977").into(eight);

            nine = view.findViewById(R.id.nine);
            nine.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_nine.jpeg?alt=media&token=88db96a6-9f7f-4ee1-ab7c-9428e2a2cc9d").into(nine);

            ten = view.findViewById(R.id.ten);
            ten.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_ten.jpeg?alt=media&token=b8d3c3e8-c3bc-43fe-b3c3-1fad72ad1190").into(ten);

            eleven = view.findViewById(R.id.eleven);
            eleven.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_11.jpeg?alt=media&token=de7f6fb9-0560-44ba-a165-87cc722d02c9").into(eleven);

            twelve = view.findViewById(R.id.twelve);
            twelve.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_twelve.jpeg?alt=media&token=90dccc95-48c1-4944-adba-647c52ec01b3").into(twelve);

            thirteen = view.findViewById(R.id.thirteen);
            thirteen.setOnClickListener(this);
            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_13.jpeg?alt=media&token=0f1acff5-682b-44fb-b517-525ed4e715eb").into(thirteen);

            upload = view.findViewById(R.id.upload);
            upload.setOnClickListener(this);

            button = view.findViewById(R.id.button);
            button.setOnClickListener(this);

            bg_more = new BottomSheetDialog(this);
            bg_more.setContentView(view);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Location
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_PICK_CODE && data != null) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            location.setText(feature.text());
            mLocation = feature.text();
        }

        //IMAGE
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
             image_uri = Objects.requireNonNull(data).getData();
            type = "image";
            relativeLayout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            Picasso.get().load(image_uri).into(imageView);
        }

        //VIDEO
        if (resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
             video_uri = Objects.requireNonNull(data).getData();

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 600000){
                Snackbar.make(main, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {

                type = "video";
                relativeLayout.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);

                videoView.setVideoURI(video_uri);
                videoView.start();
                videoView.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                });

            }

        }

        //AUDIO
        if (resultCode == RESULT_OK && requestCode == AUDIO_PICK_CODE && data != null){
             audio_uri = Objects.requireNonNull(data).getData();
            type = "audio";
            relativeLayout.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            voicePlayerView.setVisibility(View.VISIBLE);
            String aud = getRealPathFromURI(this, audio_uri);
            voicePlayerView.setAudio(aud);
        }

        //Reel
        if(resultCode == RESULT_OK && requestCode == REEl_PICK_CODE && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 10000){
                Snackbar.make(findViewById(R.id.main), "Video must be of 1 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {
                Intent intent = new Intent(CreateGroupPostActivity.this, VideoEditActivity.class);
                intent.putExtra("uri", video_uri.toString());
                startActivity(intent);
             }
        }

        //BG
        if (resultCode == RESULT_OK && requestCode == 252 && data != null){
             bg_uri = Objects.requireNonNull(data).getData();
            type = "bg";
            number = "upload";
            bg.setVisibility(View.VISIBLE);
            bg_text.setVisibility(View.VISIBLE);
            bg_image.setVisibility(View.VISIBLE);
            Picasso.get().load(bg_uri).into(imageView);
        }

    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void addPost() {
        if (post_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.post_more, null);
            image = view.findViewById(R.id.image);
            image.setOnClickListener(this);
            video = view.findViewById(R.id.video);
            video.setOnClickListener(this);
            audio = view.findViewById(R.id.audio);
            audio.setOnClickListener(this);
            feeling = view.findViewById(R.id.feeling);
            feeling.setOnClickListener(this);
            meeting = view.findViewById(R.id.meeting);
            meeting.setOnClickListener(this);
            reels = view.findViewById(R.id.reels);
            reels.setOnClickListener(this);
            live = view.findViewById(R.id.live);
            live.setOnClickListener(this);
            podcast = view.findViewById(R.id.podcast);
            podcast.setOnClickListener(this);
            watch_party = view.findViewById(R.id.watch_party);
            watch_party.setOnClickListener(this);
            sell = view.findViewById(R.id.sell);
            sell.setOnClickListener(this);
            camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(this);
            gif = view.findViewById(R.id.gif);
            gif.setOnClickListener(this);
            background = view.findViewById(R.id.background);
            background.setOnClickListener(this);
            post_more = new BottomSheetDialog(this);
            post_more.setContentView(view);
        }
    }


    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        privacyType = list[position];
       mPrivacy.setText(privacyType);
    }

    @Override
    public void onNegativeButtonClicked() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(main, "Storage permission allowed", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(main, "Storage permission is required", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image:

                post_more.cancel();

                number = "";
                bg.setVisibility(View.GONE);
                bg_text.setVisibility(View.GONE);
                bg_image.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                type = "text";

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickImage();
                    }
                }
                else {
                    pickImage();
                }


                break;
            case R.id.video:

                post_more.cancel();

                number = "";
                bg.setVisibility(View.GONE);
                bg_text.setVisibility(View.GONE);
                bg_image.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                type = "text";

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickVideo();
                    }
                }
                else {
                    pickVideo();
                }
                break;
            case R.id.audio:

                post_more.cancel();

                number = "";
                bg.setVisibility(View.GONE);
                bg_text.setVisibility(View.GONE);
                bg_image.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                type = "text";

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickAudio();
                    }
                }
                else {
                    pickAudio();
                }
                break;
            case R.id.feeling:
                post_more.cancel();
                FeelingActivity dialogFragment = FeelingActivity.newInstance();
                dialogFragment.setCallBack((type, value) -> {
                    //activity
                    switch (type) {
                        case "traveling":
                            sticker.setImageResource(R.drawable.airplane);
                            what.setText("Traveling to " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "watching":
                            sticker.setImageResource(R.drawable.watching);
                            what.setText("Watching " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "listening":
                            sticker.setImageResource(R.drawable.listening);
                            what.setText("Listening " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "thinking":
                            sticker.setImageResource(R.drawable.thinking);
                            what.setText("Thinking about " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "celebrating":
                            sticker.setImageResource(R.drawable.celebration);
                            what.setText("Celebrating " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "looking":
                            sticker.setImageResource(R.drawable.looking);
                            what.setText("Looking for " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "playing":
                            sticker.setImageResource(R.drawable.playing);
                            what.setText("Playing " + value);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                    }
                    //feeling
                    switch (type) {
                        case "happy":
                            sticker.setImageResource(R.drawable.smiling);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "loved":
                            sticker.setImageResource(R.drawable.love);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "sad":
                            sticker.setImageResource(R.drawable.sad);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "crying":
                            sticker.setImageResource(R.drawable.crying);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "angry":
                            sticker.setImageResource(R.drawable.angry);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "confused":
                            sticker.setImageResource(R.drawable.confused);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "broken":
                            sticker.setImageResource(R.drawable.broken);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "cool":
                            sticker.setImageResource(R.drawable.cool);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "funny":
                            sticker.setImageResource(R.drawable.joy);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "tired":
                            sticker.setImageResource(R.drawable.tired);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "shock":
                            sticker.setImageResource(R.drawable.shocked);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "love":
                            sticker.setImageResource(R.drawable.heart);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "sleepy":
                            sticker.setImageResource(R.drawable.sleeping);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "expressionless":
                            sticker.setImageResource(R.drawable.muted);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                        case "blessed":
                            sticker.setImageResource(R.drawable.angel);
                            what.setText("Feeling " + type);
                            findViewById(R.id.gap).setVisibility(View.VISIBLE);
                            break;
                    }

                });
                dialogFragment.show(getSupportFragmentManager(), "tag");
                break;
            case R.id.meeting:
                post_more.cancel();
                startActivity(new Intent(CreateGroupPostActivity.this, MeetingActivity.class));
                break;
            case R.id.reels:
                post_more.cancel();
                selectReel();
                break;
            case R.id.live:
                String room = String.valueOf(System.currentTimeMillis());
                Query query = FirebaseDatabase.getInstance().getReference().child("Live").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            snapshot.getRef().removeValue();
                            Intent i = new Intent(CreateGroupPostActivity.this, GoBroadcastActivity.class);
                            i.putExtra("name", room);
                            i.putExtra("type", "host");
                            startActivity(i);
                        }else {
                            Intent i = new Intent(CreateGroupPostActivity.this, GoBroadcastActivity.class);
                            i.putExtra("name", room);
                            i.putExtra("type", "host");
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.podcast:
                post_more.cancel();
                createPod();
                break;
            case R.id.watch_party:
                post_more.cancel();

                Query q = FirebaseDatabase.getInstance().getReference().child("Party").orderByChild("from").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                ds.getRef().removeValue();
                                startActivity(new Intent(CreateGroupPostActivity.this, StartWatchPartyActivity.class));
                            }else {
                                startActivity(new Intent(CreateGroupPostActivity.this, StartWatchPartyActivity.class));
                            }
                        }
                        startActivity(new Intent(CreateGroupPostActivity.this, StartWatchPartyActivity.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.sell:
                post_more.cancel();
                startActivity(new Intent(CreateGroupPostActivity.this, PostProductActivity.class));
                break;
            case R.id.camera:
                post_more.cancel();
                startActivity(new Intent(CreateGroupPostActivity.this, FaceFilters.class));
                break;
            case  R.id.gif:

                //GIF

                post_more.cancel();

                number = "";
                bg.setVisibility(View.GONE);
                bg_text.setVisibility(View.GONE);
                bg_image.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                type = "text";

                Intent intent = new Intent(CreateGroupPostActivity.this, StickersPost.class);
                intent.putExtra("activity", "group");
                startActivity(intent);


                break;

            case  R.id.background:

                bg_more.show();
                post_more.cancel();

                number = "";
                bg.setVisibility(View.GONE);
                bg_text.setVisibility(View.GONE);
                bg_image.setVisibility(View.GONE);

                relativeLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                voicePlayerView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                type = "text";


                break;

            case  R.id.one:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_one.jpeg?alt=media&token=2b1f2c65-14c0-4d49-8938-aac10900c082";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_one.jpeg?alt=media&token=2b1f2c65-14c0-4d49-8938-aac10900c082").into(bg_image);

                break;

            case  R.id.two:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_two.jpeg?alt=media&token=55222523-9e8e-4132-8f6c-33d055410146";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_two.jpeg?alt=media&token=55222523-9e8e-4132-8f6c-33d055410146").into(bg_image);

                break;

            case  R.id.three:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_three.jpeg?alt=media&token=879afce6-96e4-417e-ba43-7241987953b4";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_three.jpeg?alt=media&token=879afce6-96e4-417e-ba43-7241987953b4").into(bg_image);

                break;

            case  R.id.four:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_four.jpeg?alt=media&token=964f050c-160d-44dc-8967-db3d0aea3031";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_four.jpeg?alt=media&token=964f050c-160d-44dc-8967-db3d0aea3031").into(bg_image);

                break;

            case  R.id.five:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_five.jpeg?alt=media&token=28f7bae1-7c88-4edb-b434-90ae5f98ddf9";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_five.jpeg?alt=media&token=28f7bae1-7c88-4edb-b434-90ae5f98ddf9").into(bg_image);

                break;

            case  R.id.six:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_six.jpeg?alt=media&token=425521dc-d8ed-46f6-80d7-e906d3eb5592";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_six.jpeg?alt=media&token=425521dc-d8ed-46f6-80d7-e906d3eb5592").into(bg_image);

                break;

            case  R.id.seven:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_seven.jpeg?alt=media&token=efe084a6-3cfd-432f-9775-6da5b772b1d1";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_seven.jpeg?alt=media&token=efe084a6-3cfd-432f-9775-6da5b772b1d1").into(bg_image);

                break;

            case  R.id.eight:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_eight.jpeg?alt=media&token=2c802174-4f32-401d-b1ce-7fe81c279977";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_eight.jpeg?alt=media&token=2c802174-4f32-401d-b1ce-7fe81c279977").into(bg_image);

                break;

            case  R.id.nine:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_nine.jpeg?alt=media&token=88db96a6-9f7f-4ee1-ab7c-9428e2a2cc9d";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_nine.jpeg?alt=media&token=88db96a6-9f7f-4ee1-ab7c-9428e2a2cc9d").into(bg_image);

                break;

            case  R.id.ten:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_ten.jpeg?alt=media&token=b8d3c3e8-c3bc-43fe-b3c3-1fad72ad1190";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_ten.jpeg?alt=media&token=b8d3c3e8-c3bc-43fe-b3c3-1fad72ad1190").into(bg_image);

                break;

            case  R.id.eleven:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_11.jpeg?alt=media&token=de7f6fb9-0560-44ba-a165-87cc722d02c9";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_11.jpeg?alt=media&token=de7f6fb9-0560-44ba-a165-87cc722d02c9").into(bg_image);

                break;

            case  R.id.twelve:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_twelve.jpeg?alt=media&token=90dccc95-48c1-4944-adba-647c52ec01b3";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_twelve.jpeg?alt=media&token=90dccc95-48c1-4944-adba-647c52ec01b3").into(bg_image);

                break;

            case  R.id.thirteen:

                bg_more.cancel();
                type = "bg";
                number = "https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_13.jpeg?alt=media&token=0f1acff5-682b-44fb-b517-525ed4e715eb";
                bg.setVisibility(View.VISIBLE);
                bg_text.setVisibility(View.VISIBLE);
                bg_image.setVisibility(View.VISIBLE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/storage-4e152.appspot.com/o/bg_13.jpeg?alt=media&token=0f1acff5-682b-44fb-b517-525ed4e715eb").into(bg_image);

                break;

            case R.id.upload:

                bg_more.cancel();
                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickBG();
                    }
                }
                else {
                    pickBG();
                }


                break;

            case R.id.button:

                bg_more.cancel();

                break;
        }
    }

    private void createPod() {
        String room = String.valueOf(System.currentTimeMillis());
        Query query = FirebaseDatabase.getInstance().getReference().child("Podcast").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    snapshot.getRef().removeValue();
                    Intent i = new Intent(getApplicationContext(), GoPodcastBroadcastActivity.class);
                    i.putExtra("name", room);
                    i.putExtra("type", "host");
                    startActivity(i);
                }else {
                    Intent i = new Intent(getApplicationContext(), GoPodcastBroadcastActivity.class);
                    i.putExtra("name", room);
                    i.putExtra("type", "host");
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectReel() {
        //Check Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else {
                pickReel();
            }
        }
        else {
            pickReel();
        }
    }

    private void pickReel() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, REEl_PICK_CODE);
    }

    private void pickBG() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 252);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    private void pickAudio() {
         Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_PICK_CODE);
    }

}