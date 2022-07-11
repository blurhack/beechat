package com.beesec.beechat2.send;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.beesec.beechat2.marketPlace.PostProductActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.post.CreatePostActivity;
import com.beesec.beechat2.reel.VideoEditActivity;

public class SendMediaActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_send_media);

        //Strings
        String type =getIntent().getStringExtra("type");
        String uri =getIntent().getStringExtra("uri");

        if (type.equals("video")){
            findViewById(R.id.product).setVisibility(View.GONE);
            findViewById(R.id.imageEdit).setVisibility(View.GONE);
            findViewById(R.id.videoEdit).setVisibility(View.VISIBLE);
        }else {
            findViewById(R.id.reels).setVisibility(View.GONE);
            findViewById(R.id.imageEdit).setVisibility(View.VISIBLE);
            findViewById(R.id.videoEdit).setVisibility(View.GONE);
        }


        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.videoEdit).setOnClickListener(v -> {
            Intent intent = new Intent(SendMediaActivity.this, VideoEditingActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });

        findViewById(R.id.imageEdit).setOnClickListener(v -> {
            Intent intent = new Intent(SendMediaActivity.this, ImageEditingActivity.class);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });

        //Post
        findViewById(R.id.post).setOnClickListener(v -> {
            if (type.equals("video")){
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), Uri.parse(uri));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                retriever.release();

                if (timeInMilli > 600000){
                    Snackbar.make(v, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(SendMediaActivity.this, CreatePostActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("uri", uri);
                    startActivity(intent);
                }
            }else {
                Intent intent = new Intent(SendMediaActivity.this, CreatePostActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });

        //Product
        findViewById(R.id.product).setOnClickListener(v -> {
            Intent intent = new Intent(SendMediaActivity.this, PostProductActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("uri", uri);
            startActivity(intent);
        });

        findViewById(R.id.reels).setOnClickListener(v -> {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(SendMediaActivity.this, Uri.parse(uri));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 60000){
                Snackbar.make(v, "Video must be of 1 minutes or less", Snackbar.LENGTH_LONG).show();
            }else
            {
                Intent intent = new Intent(SendMediaActivity.this, VideoEditActivity.class);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });

        //user
        findViewById(R.id.user).setOnClickListener(v -> {

            if (type.equals("video")) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), Uri.parse(uri));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                retriever.release();

                if (timeInMilli > 50000) {
                    Snackbar.make(v, "Video must be of 5 minutes or less", Snackbar.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(SendMediaActivity.this, SendToUserActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("uri", uri);
                    startActivity(intent);
                }
            }else {
                Intent intent = new Intent(SendMediaActivity.this, SendToUserActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }

        });

        //group
        findViewById(R.id.group).setOnClickListener(v -> {
            if (type.equals("video")) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), Uri.parse(uri));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMilli = Long.parseLong(time);
                retriever.release();

                if (timeInMilli > 50000) {
                    Snackbar.make(v, "Video must be of 5 minutes or less", Snackbar.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(SendMediaActivity.this, SendToGroupActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("uri", uri);
                    startActivity(intent);
                }
            }else {
                Intent intent = new Intent(SendMediaActivity.this, SendToGroupActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });

    }
}