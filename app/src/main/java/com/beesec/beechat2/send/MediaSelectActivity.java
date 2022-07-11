package com.beesec.beechat2.send;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

@SuppressWarnings("ALL")
public class MediaSelectActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_SELECT = 1000;

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/* video/*");
        startActivityForResult(pickIntent, IMAGE_PICKER_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER_SELECT) {
            Uri selectedMediaUri = data.getData();
            if (selectedMediaUri.toString().contains("image")) {
                Intent intent = new Intent(MediaSelectActivity.this, SendMediaActivity.class);
                intent.putExtra("type", "image");
                intent.putExtra("uri", selectedMediaUri.toString());
                startActivity(intent);
                finish();
            } else if (selectedMediaUri.toString().contains("video")) {
                Intent intent = new Intent(MediaSelectActivity.this, SendMediaActivity.class);
                intent.putExtra("type", "video");
                intent.putExtra("uri", selectedMediaUri.toString());
                startActivity(intent);
                finish();
            }else {
                onBackPressed();
            }
        }else {
            onBackPressed();
        }
    }
}