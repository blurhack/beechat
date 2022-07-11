package com.beesec.beechat2.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("ALL")
public class StepThreeActivity extends AppCompatActivity {

    ImageView imageView;
    String id;
    Uri dp_uri;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

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
        setContentView(R.layout.activity_step_three);

        id = getIntent().getStringExtra("group");

        imageView = findViewById(R.id.circleImageView2);

        imageView.setOnClickListener(v -> {
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
        });

        findViewById(R.id.imageView).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddMemberActivity.class);
            intent.putExtra("group", id);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.next).setOnClickListener(v -> {

            if (dp_uri == null){
                Snackbar.make(findViewById(R.id.main), "Please upload a cover", Snackbar.LENGTH_LONG).show();
            }else {
                uploadDp(dp_uri);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(R.id.main), "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
            }

        });

    }

    private void uploadDp(Uri dp_uri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("group_cover/" + ""+System.currentTimeMillis());
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cover", downloadUri.toString());
                FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Cover").setValue(hashMap);

                Snackbar.make(findViewById(R.id.main), "Cover photo updated", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), AddMemberActivity.class);
                intent.putExtra("group", id);
                startActivity(intent);
                finish();
            }
        });

    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.main), "Storage permission allowed", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(R.id.main), "Storage permission is required", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
            dp_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(dp_uri).into(imageView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}