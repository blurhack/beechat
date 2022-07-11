package com.beesec.beechat2.reel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.iceteck.silicompressorr.SiliCompressor;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.post.PrivacyPick;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("ALL")
public class PostReelActivity extends AppCompatActivity implements PrivacyPick.SingleChoiceListener{

    String uri;
    String privacy = "";
    String comment = "yes";
    EditText socialEditText;

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
        setContentView(R.layout.activity_post_reel);

        //back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //String
        uri =getIntent().getStringExtra("value");

        //Video
        ImageView video = findViewById(R.id.imageView3);
        Glide.with(getApplicationContext()).asBitmap().load(uri).thumbnail(0.1f).into(video);

        //Privacy
        findViewById(R.id.privacy).setOnClickListener(v -> {
            DialogFragment dialogFragment = new PrivacyPick();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "Single Choice Dialog");
        });

        //Comments
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.comment);
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                comment = "yes";
            }else {
                comment = "no";
            }
        });

        //Post
         socialEditText = findViewById(R.id.socialEditText);
        findViewById(R.id.post).setOnClickListener(v -> {
            if (socialEditText.getText().toString().isEmpty()){
                Snackbar.make(v,"Enter Description", Snackbar.LENGTH_SHORT).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                compressVideo();
            }
        });

    }

    private void compressVideo() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        new CompressVideo().execute("false",uri.toString(),file.getPath());
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(PostReelActivity.this)
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
        //Upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("reel_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String timeStamp = String.valueOf(System.currentTimeMillis());
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("pId", timeStamp);
                hashMap.put("text", Objects.requireNonNull(socialEditText.getText()).toString());
                hashMap.put("comment", comment);
                assert downloadUri != null;
                hashMap.put("video", downloadUri.toString());
                hashMap.put("pTime", timeStamp);
                hashMap.put("privacy", privacy);
                FirebaseDatabase.getInstance().getReference("Reels").child(timeStamp).setValue(hashMap);
                Snackbar.make(findViewById(R.id.main),"Post Uploaded", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class );
                    startActivity(intent);
                    finish();
                },200);
            }
        });

    }


    @Override
   public void onPositiveButtonClicked(String[] list, int position) {
      privacy = list[position];
  }



   @Override
  public void onNegativeButtonClicked() {

  }

}