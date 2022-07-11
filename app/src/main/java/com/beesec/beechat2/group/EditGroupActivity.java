package com.beesec.beechat2.group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class EditGroupActivity extends AppCompatActivity implements View.OnClickListener {

    //ID
    ImageView cover,editCover,editDp;
    CircleImageView dp;
    EditText name,username,link;
    SocialEditText bio;
    ConstraintLayout main;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch mSwitch;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int COVER_IMAGE_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1001;

    //String
    String mDp;
    String id;
    String getUsername;
    boolean isThere = false;

    //Bottom
    BottomSheetDialog dp_edit,cover_edit;
    LinearLayout upload,delete,video,image,trash;

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
        setContentView(R.layout.activity_edit_group);

        id = getIntent().getStringExtra("group");

        //Declaring
        cover = findViewById(R.id.cover);
        editCover = findViewById(R.id.editCover);
        editDp = findViewById(R.id.editDp);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        link = findViewById(R.id.link);
        main = findViewById(R.id.main);
        mSwitch = findViewById(R.id.mSwitch);

        //EditImage
        editDp.setOnClickListener(v -> dp_edit.show());

        //EditCover
        editCover.setOnClickListener(v -> cover_edit.show());

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //GroupInfo
        FirebaseDatabase.getInstance().getReference().child("Groups").child(id).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                name.setText(Objects.requireNonNull(snapshot.child("gName").getValue()).toString());

                bio.setText(Objects.requireNonNull(snapshot.child("gBio").getValue()).toString());

                username.setText(Objects.requireNonNull(snapshot.child("gUsername").getValue()).toString());
                getUsername = snapshot.child("gUsername").getValue().toString();

                link.setText(Objects.requireNonNull(snapshot.child("gLink").getValue()).toString());

                if (!snapshot.child("gIcon").getValue().toString().isEmpty()) {
                    Picasso.get().load(Objects.requireNonNull(snapshot.child("gIcon").getValue()).toString()).into(dp);
                }

                mDp = snapshot.child("gIcon").getValue().toString();

                //Private
                FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Privacy").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String privacy = snapshot.child("type").getValue().toString();
                            mSwitch.setChecked(privacy.equals("private"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Cover
                FirebaseDatabase.getInstance().getReference().child("Groups").child(id).child("Cover").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            isThere = true;
                            if (!snapshot.child("cover").getValue().toString().isEmpty()){
                                Picasso.get().load(Objects.requireNonNull(snapshot.child("cover").getValue()).toString()).into(cover);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                findViewById(R.id.progressBar).setVisibility(View.GONE);

                //Bottom
                edit_dp();
                edit_cover();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Privacy
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                //private
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("type", "private");
                FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Privacy").setValue(hashMap);
                Snackbar.make(buttonView, "Set to private", Snackbar.LENGTH_LONG).show();
            }else {
                //public
                FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Privacy").removeValue();
                Snackbar.make(buttonView, "Set to public", Snackbar.LENGTH_LONG).show();
            }
        });

        //Save
        findViewById(R.id.signUp).setOnClickListener(v -> {

            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mName = name.getText().toString().trim();
            String mUsername = username.getText().toString().trim();
            String mBio = bio.getText().toString().trim();
            String mLink = link.getText().toString().trim();

            if (mName.isEmpty()){
                Snackbar.make(v, "Enter your name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else if (mUsername.isEmpty()){
                Snackbar.make(v, "Enter your username", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }else {
                if (!getUsername.equals(mUsername)){
                    Query usernameQuery = FirebaseDatabase.getInstance().getReference().child("Groups").orderByChild("gUsername").equalTo(mUsername);
                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount()>0){
                                Snackbar.make(v,"Username already exist, try with new one", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }else {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("gName", ""+ mName);
                                hashMap.put("gUsername", ""+mUsername);
                                hashMap.put("gBio", ""+ mBio);
                                hashMap.put("gLink", ""+ mLink);
                                FirebaseDatabase.getInstance().getReference("Groups").child(id).updateChildren(hashMap);
                                Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(v,error.getMessage(), Snackbar.LENGTH_LONG).show();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    });
                }else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("gName", ""+ mName);
                    hashMap.put("gUsername", ""+mUsername);
                    hashMap.put("gBio", ""+ mBio);
                    hashMap.put("gLink", ""+ mLink);
                    FirebaseDatabase.getInstance().getReference("Groups").child(id).updateChildren(hashMap);
                    Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }

        });

    }

    private void edit_cover() {
        if (cover_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.cover_edit, null);
            image = view.findViewById(R.id.image);
            video = view.findViewById(R.id.video);
            trash = view.findViewById(R.id.trash);
            video.setVisibility(View.GONE);
            image.setOnClickListener(this);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 130);
            });
            trash.setOnClickListener(this);
            if (!isThere){
                trash.setVisibility(View.GONE);
            }
            cover_edit = new BottomSheetDialog(this);
            cover_edit.setContentView(view);
        }
    }

    private void edit_dp() {
        if (dp_edit == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dp_edit, null);
            upload = view.findViewById(R.id.upload);
            delete = view.findViewById(R.id.delete);
            upload.setOnClickListener(this);
            delete.setOnClickListener(this);
            LinearLayout camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(v -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent , 120);
            });
            dp_edit = new BottomSheetDialog(this);
            dp_edit.setContentView(view);
            if (mDp.isEmpty()){
                delete.setVisibility(View.GONE);
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickCoverImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, COVER_IMAGE_PICK_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(dp_uri).into(dp);
            try {
                uploadDp(dp_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();

        }
        if (resultCode == RESULT_OK && requestCode == 120 && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(dp_uri).into(dp);
            try {
                uploadDp(dp_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();

        }
        if (resultCode == RESULT_OK && requestCode == 130 && data != null){
            Uri cover_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(cover_uri).into(cover);
            cover.setVisibility(View.VISIBLE);
            try {
                uploadCoverImage(cover_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();

        }
        if (resultCode == RESULT_OK && requestCode == COVER_IMAGE_PICK_CODE && data != null){
            Uri cover_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(cover_uri).into(cover);
            cover.setVisibility(View.VISIBLE);
            try {
                uploadCoverImage(cover_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void uploadCoverImage(Uri cover_uri) throws IOException {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("group_cover/" + ""+System.currentTimeMillis());
        storageReference.putFile(cover_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cover", downloadUri.toString());

                if (isThere){
                    FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Cover").updateChildren(hashMap);
                }else {
                    FirebaseDatabase.getInstance().getReference("Groups").child(id).child("Cover").setValue(hashMap);
                    isThere = true;
                }

                Snackbar.make(main, "Cover photo updated", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void uploadDp(Uri dp_uri) throws IOException {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("group_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("gIcon", ""+ downloadUri.toString());
                FirebaseDatabase.getInstance().getReference("Groups").child(id).updateChildren(hashMap);
                Snackbar.make(main, "Profile photo updated", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upload:

                dp_edit.cancel();

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
            case R.id.delete:

                dp_edit.cancel();

                if (!mDp.isEmpty()){
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(mDp);
                    picRef.delete().addOnCompleteListener(task -> {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("gIcon", "");
                        FirebaseDatabase.getInstance().getReference("Groups").child(id).updateChildren(hashMap);
                        Snackbar.make(main, "Profile photo deleted", Snackbar.LENGTH_LONG).show();
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    });
                }

                break;
            case R.id.image:

                cover_edit.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickCoverImage();
                    }
                }
                else {
                    pickCoverImage();
                }

                break;
            case R.id.trash:

                cover_edit.cancel();

                //Cover
                FirebaseDatabase.getInstance().getReference().child("Groups").child(id).child("Cover").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String uri = snapshot.child("uri").getValue().toString();
                            StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(uri);
                            picRef.delete().addOnCompleteListener(task -> {
                                snapshot.getRef().removeValue();
                                Snackbar.make(main, "Cover deleted", Snackbar.LENGTH_LONG).show();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                break;
        }
    }


}