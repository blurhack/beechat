package com.beesec.beechat2;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;


@SuppressWarnings("ALL")
public class MediaViewActivity extends AppCompatActivity {

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_media_view);

        //GetUri
        String uri = getIntent().getStringExtra("uri");
        String type = getIntent().getStringExtra("type");

        //Video
        if (type.equals("video")){
            VideoView videoView = findViewById(R.id.videoView);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(uri));
            videoView.start();
            videoView.setOnPreparedListener(mp -> mp.setLooping(true));
            MediaController mediaController = new MediaController(MediaViewActivity.this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }else if (type.equals("image")){
            PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
            photoView.setVisibility(View.VISIBLE);
            Picasso.get().load(uri).into(photoView);
        }


    }
}