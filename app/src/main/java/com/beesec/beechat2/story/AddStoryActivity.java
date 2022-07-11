package com.beesec.beechat2.story;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.iceteck.silicompressorr.SiliCompressor;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.R;
import com.beesec.beechat2.faceFilters.FaceFilters;
import com.beesec.beechat2.send.ImageEditingActivity;
import com.beesec.beechat2.send.VideoEditingActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("ALL")
public class AddStoryActivity extends AppCompatActivity {

    //Permission
    private static final int IMAGE_PICKER_SELECT = 1000;

    //Uri
    Uri selectedMediaUri;

    //Id
    ImageView image;
    VideoView video;

    //Strings
    String type;

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_story);

        //back
        findViewById(R.id.back).setOnClickListener(v -> {
            Intent i = new Intent(AddStoryActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        //Camera
        findViewById(R.id.camera).setOnClickListener(v -> startActivity(new Intent(AddStoryActivity.this, FaceFilters.class)));

        //Gallery
        findViewById(R.id.gallery).setOnClickListener(v -> {
            @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/* video/*");
            startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
        });

        //Id
        image = findViewById(R.id.image);
        video = findViewById(R.id.videoView);

        //edit
        findViewById(R.id.edit).setOnClickListener(v -> {
            Intent i;
            if (type.equals("image")){
                i = new Intent(AddStoryActivity.this, ImageEditingActivity.class);
            }else {
                i = new Intent(AddStoryActivity.this, VideoEditingActivity.class);
            }
            i.putExtra("type", "story");
            i.putExtra("uri", selectedMediaUri.toString());
            startActivity(i);
        });

        if (getIntent().hasExtra("type")){
            String uri = getIntent().getStringExtra("uri");
            if (getIntent().getStringExtra("type").equals("image")){
                image.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                Picasso.get().load(uri).into(image);
                type = "image";
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
            }else {
                video.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
                type = "video";
                video.setVideoURI(Uri.parse(uri));
                video.start();
                video.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                });
            }
        }

        //post
        findViewById(R.id.post).setOnClickListener(v -> {
            if (selectedMediaUri != null) {
                if (type.equals("image")) {
                    uploadImage();
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else if (type.equals("video")) {
                    compressVideo();
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
                }
            }else {
                Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void compressVideo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        new CompressVideo().execute("false",selectedMediaUri.toString(),file.getPath());
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(AddStoryActivity.this)
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

    private void uploadVideo(Uri videoUri) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String storyId = reference.push().getKey();
        long timeend = System.currentTimeMillis()+86400000;

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Story/" + "Story_" + timeStamp;
        FirebaseStorage.getInstance().getReference().child(filePathAndName).putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
            if (uriTask.isSuccessful()) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageUri", downloadUri);
                hashMap.put("timestart", ServerValue.TIMESTAMP);
                hashMap.put("timeend", timeend);
                hashMap.put("storyid", storyId);
                hashMap.put("type", "video");
                hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                assert storyId != null;
                reference.child(storyId).setValue(hashMap);

                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.GONE);
                type = "";

                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    private void uploadImage() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String storyId = reference.push().getKey();
        long timeend = System.currentTimeMillis()+86400000;

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Story/" + "Story_" + timeStamp;
        FirebaseStorage.getInstance().getReference().child(filePathAndName).putFile(selectedMediaUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
            if (uriTask.isSuccessful()) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageUri", downloadUri);
                hashMap.put("timestart", ServerValue.TIMESTAMP);
                hashMap.put("timeend", timeend);
                hashMap.put("storyid", storyId);
                hashMap.put("type", "image");
                hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                assert storyId != null;
                reference.child(storyId).setValue(hashMap);

                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.GONE);
                type = "";

                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_SELECT) {
            assert data != null;
            selectedMediaUri = data.getData();
            if (selectedMediaUri.toString().contains("image")) {
                image.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                Picasso.get().load(selectedMediaUri).into(image);
                type = "image";
                findViewById(R.id.edit).setVisibility(View.VISIBLE);
            } else if (selectedMediaUri.toString().contains("video")) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), selectedMediaUri);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                retriever.release();

                if (timeInMilli > 30000){
                    Snackbar.make(findViewById(R.id.main), "Video must be of 30 seconds or less", Snackbar.LENGTH_LONG).show();
                }else {

                    video.setVisibility(View.VISIBLE);
                    image.setVisibility(View.GONE);
                    findViewById(R.id.edit).setVisibility(View.VISIBLE);
                    type = "video";
                    video.setVideoURI(selectedMediaUri);
                    video.start();
                    video.setOnPreparedListener(mp -> {
                        mp.setLooping(true);
                        mp.setVolume(0, 0);
                    });

                }

            }else {
                type = "";
                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.GONE);
            }
        }else {
            type = "";
            video.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            findViewById(R.id.edit).setVisibility(View.GONE);
        }
    }

}