package com.beesec.beechat2.watchParty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iceteck.silicompressorr.SiliCompressor;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterChangePostParty;
import com.beesec.beechat2.model.ModelPost;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class ChangeWatchPartyActivity extends AppCompatActivity {

    //Post
    AdapterChangePostParty adapterPost;
    List<ModelPost> modelPosts;
    RecyclerView post;
    ConstraintLayout main;

    //Permission
    private static final int VIDEO_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1001;

    //URI
    Uri video_uri,audio_uri;
    private static String videoId = "";
    private static String id;
    public static String getId() {
        return id;
    }
    public ChangeWatchPartyActivity(){

    }

    //Other
    private static final int TOTAL_ITEM_EACH_LOAD = 8;
    private int currentPage = 1;
    Button more;
    long initial;

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
        setContentView(R.layout.activity_start_watch_party);

        id = getIntent().getStringExtra("room");

        more = findViewById(R.id.more);

        //back
        findViewById(R.id.imageView).setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                    Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        //Post
        post = findViewById(R.id.post_rv);
        main = findViewById(R.id.main);
        post.setLayoutManager(new LinearLayoutManager(ChangeWatchPartyActivity.this));
        modelPosts = new ArrayList<>();
        getAllPost();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
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

        //TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    findViewById(R.id.post_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.web_layout).setVisibility(View.GONE);
                    findViewById(R.id.upload_layout).setVisibility(View.GONE);
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    getAllPost();
                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    findViewById(R.id.post_layout).setVisibility(View.GONE);
                    findViewById(R.id.web_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.upload_layout).setVisibility(View.GONE);
                }
                else if (tabLayout.getSelectedTabPosition() == 2) {
                    findViewById(R.id.post_layout).setVisibility(View.GONE);
                    findViewById(R.id.web_layout).setVisibility(View.GONE);
                    findViewById(R.id.upload_layout).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        EditText editText = findViewById(R.id.searchPost);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

        //Web
        EditText party_web_edit = findViewById(R.id.party_web_edit);
        findViewById(R.id.createWeb).setOnClickListener(v -> {

            if (party_web_edit.getText().toString().contains("youtu")){
                getVideoId(party_web_edit.getText().toString());
                webParty("upload_youtube", videoId);
            }else if (party_web_edit.getText().toString().contains("dailymotion")){
                String dmId = party_web_edit.getText().toString().replaceFirst("https://www.dailymotion.com/video/","");
                webParty("upload_dailymotion", dmId.trim());
            }else {
                Snackbar.make(v, "Paste the link from YouTube & DailyMotion", Snackbar.LENGTH_LONG).show();
            }

        });

        //VideoUpload
        findViewById(R.id.videoU).setOnClickListener(v -> {
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
        });

        findViewById(R.id.createMeet).setOnClickListener(v -> {
            if (!Uri.EMPTY.equals(audio_uri) && !Uri.EMPTY.equals(video_uri)){
                Snackbar.make(findViewById(R.id.main), "Please upload a video", Snackbar.LENGTH_LONG).show();
            }
        });

        more.setOnClickListener(v -> loadMoreData());

    }

    private void webParty(String type, String link) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("link", link);
        hashMap.put("type", type);

        FirebaseDatabase.getInstance().getReference().child("Party").child(ChangeWatchPartyActivity.getId()).updateChildren(hashMap);
        FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("ChatId", timeStamp);
                hashMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("msg", "Started a new video");
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap);

                if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                    Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static String getVideoId(@NonNull String videoUrl) {
        String regex = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if(matcher.find()){
            videoId = matcher.group(1);
        }
        return videoId;
    }

    private void loadMoreData() {
        currentPage++;
        getAllPost();
    }

    private void filter(final String query) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPosts.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost mPost = ds.getValue(ModelPost.class);
                    if (mPost.getText().toLowerCase().contains(query.toLowerCase())) {
                        if (ds.child("type").getValue().toString().equals("video")) {
                            modelPosts.add(mPost);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    }
                    adapterPost = new AdapterChangePostParty(ChangeWatchPartyActivity.this, modelPosts);
                    post.setAdapter(adapterPost);
                    adapterPost.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllPost() {
        FirebaseDatabase.getInstance().getReference("Posts").limitToFirst(currentPage*TOTAL_ITEM_EACH_LOAD).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPosts.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost mPost = ds.getValue(ModelPost.class);
                    if (ds.child("type").getValue().toString().equals("video")) {
                        modelPosts.add(mPost);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                    adapterPost = new AdapterChangePostParty(ChangeWatchPartyActivity.this, modelPosts);
                    post.setAdapter(adapterPost);
                    adapterPost.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
            video_uri = Objects.requireNonNull(data).getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 600000){
                Snackbar.make(main, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBarP).setVisibility(View.VISIBLE);
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                new CompressVideo().execute("false",video_uri.toString(),file.getPath());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(ChangeWatchPartyActivity.this)
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
            sendVideo(videoUri);
        }
    }

    private void sendVideo(Uri videoUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("party_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("link", downloadUri.toString());
                hashMap.put("type", "upload_video");

                FirebaseDatabase.getInstance().getReference().child("Party").child(ChangeWatchPartyActivity.getId()).updateChildren(hashMap);
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String timeStamp = ""+System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("ChatId", timeStamp);
                        hashMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("msg", "Started a new video");
                        FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap);

                        if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                            Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", id);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", id);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseDatabase.getInstance().getReference().child("Party").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                    Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", id);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}