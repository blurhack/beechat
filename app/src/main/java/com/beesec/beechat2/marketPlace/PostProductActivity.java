package com.beesec.beechat2.marketPlace;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

@SuppressWarnings("ALL")
public class PostProductActivity extends AppCompatActivity implements TypePick.SingleChoiceListener, CatPick.SingleChoiceListener{

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int LOCATION_PICK_CODE = 1009;

    //String
    Uri dp_uri;
    String typeString;
    String catString;

    //Id
    RoundedImageView roundedImageView;
    TextView type;
    TextView cat;

    //EdiText
    EditText title,price,des,location;
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
        setContentView(R.layout.activity_post_product);

        //Id
        roundedImageView = findViewById(R.id.cover);
        cat = findViewById(R.id.category);
        type = findViewById(R.id.condition);

        //back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        cat.setOnClickListener(v -> {
            DialogFragment dialogFragment = new CatPick();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "Single Choice Dialog");
        });

        type.setOnClickListener(v -> {
            DialogFragment dialogFragment = new TypePick();
            dialogFragment.setCancelable(false);
            dialogFragment.show(getSupportFragmentManager(), "Single Choice Dialog");
        });

        //Cover
        findViewById(R.id.cover).setOnClickListener(v -> {
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

        findViewById(R.id.locate).setOnClickListener(v -> {
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken("sk.eyJ1Ijoic3BhY2VzdGVyIiwiYSI6ImNrbmg2djJmdzJpZGQyd2xjeTk3a2twNTQifQ.iIiTRT_GwIYwFMsCWP5XGA")
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#ffffff"))
                            .build(PlaceOptions.MODE_CARDS))
                    .build(this);
            startActivityForResult(intent, LOCATION_PICK_CODE);
        });

        //EdiText
         title = findViewById(R.id.title);
         price = findViewById(R.id.price);
         des = findViewById(R.id.des);
         location = findViewById(R.id.loc);

        //Post
        findViewById(R.id.login).setOnClickListener(v -> {
            if (title.getText().toString().isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please enter title", Snackbar.LENGTH_LONG).show();
            }else if (price.getText().toString().isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please enter price", Snackbar.LENGTH_LONG).show();
            }else if (des.getText().toString().isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please enter description", Snackbar.LENGTH_LONG).show();
            }else if (location.getText().toString().isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please enter location", Snackbar.LENGTH_LONG).show();
            }else if (catString.isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please choose category", Snackbar.LENGTH_LONG).show();
            }else if (typeString.isEmpty()){
                Snackbar.make(findViewById(R.id.main), "Please choose condition", Snackbar.LENGTH_LONG).show();
            }else if (dp_uri == null){
                Snackbar.make(findViewById(R.id.main), "Please upload image", Snackbar.LENGTH_LONG).show();
            } else {
                compressImage(dp_uri);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }
        });

        //Intent
        if(getIntent().hasExtra("uri") && getIntent().hasExtra("type")){
         if (getIntent().getStringExtra("type").equals("image")){
             dp_uri = Uri.parse(getIntent().getStringExtra("uri"));
             Picasso.get().load(dp_uri).into(roundedImageView);
            }
        }

    }

    private void compressImage(Uri image_uri) {

        //Upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("product_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(image_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String timeStamp = String.valueOf(System.currentTimeMillis());
                HashMap<Object, String> hashMap = new HashMap<>();
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("pId", timeStamp);
                hashMap.put("title", title.getText().toString());
                hashMap.put("price", price.getText().toString());
                hashMap.put("des", des.getText().toString());
                hashMap.put("location", location.getText().toString());
                hashMap.put("cat", cat.getText().toString());
                hashMap.put("type", type.getText().toString());
                hashMap.put("photo", downloadUri.toString());
                FirebaseDatabase.getInstance().getReference().child("Product").child(timeStamp).setValue(hashMap);
                title.setText("");
                price.setText("");
                des.setText("");
                location.setText("");
                cat.setText("");
                type.setText("");
                catString = "";
                typeString = "";
                roundedImageView.setImageResource(R.drawable.upload_product);
                Snackbar.make(findViewById(R.id.main), "Product Posted", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
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
            dp_uri = data.getData();
              Picasso.get().load(dp_uri).into(roundedImageView);
        }
        //Location
        if (resultCode == Activity.RESULT_OK && requestCode == LOCATION_PICK_CODE && data != null) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            location.setText(feature.text());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCatPickPositiveButtonClicked(String[] list, int position) {
        catString = list[position];
        cat.setText(catString);
    }

    @Override
    public void onCatPickNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        typeString = list[position];
        type.setText(typeString);
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}