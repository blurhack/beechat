package com.beesec.beechat2.marketPlace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.MediaViewActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.chat.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class ProductDetailsActivity extends AppCompatActivity {

    String pId;

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
        setContentView(R.layout.activity_product_details);

        pId = getIntent().getStringExtra("pId");

        //Id
        ImageView cover = findViewById(R.id.cover);
        TextView price = findViewById(R.id.price);
        TextView title = findViewById(R.id.title);
        TextView des = findViewById(R.id.des);
        TextView type = findViewById(R.id.type);
        TextView location = findViewById(R.id.location);
        TextView user = findViewById(R.id.user);
        CircleImageView dp  = findViewById(R.id.dp);

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        FirebaseDatabase.getInstance().getReference("Product").child(pId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(cover);
                price.setText("$"+Objects.requireNonNull(snapshot.child("price").getValue()).toString());
                title.setText(Objects.requireNonNull(snapshot.child("title").getValue()).toString());
                des.setText(Objects.requireNonNull(snapshot.child("des").getValue()).toString());
                type.setText(Objects.requireNonNull(snapshot.child("type").getValue()).toString());
                location.setText(Objects.requireNonNull(snapshot.child("location").getValue()).toString());

                if (Objects.requireNonNull(snapshot.child("id").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){

                    findViewById(R.id.share).setVisibility(View.GONE);
                    findViewById(R.id.message).setVisibility(View.GONE);
                    findViewById(R.id.delete).setVisibility(View.VISIBLE);

                }else {

                    findViewById(R.id.share).setVisibility(View.VISIBLE);
                    findViewById(R.id.message).setVisibility(View.VISIBLE);
                    findViewById(R.id.delete).setVisibility(View.GONE);

                }

                FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(snapshot.child("id").getValue()).toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                        if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()){
                            Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(dp);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                findViewById(R.id.delete).setOnClickListener(view -> {
                    FirebaseDatabase.getInstance().getReference("Product").child(pId).getRef().removeValue();
                    Toast.makeText(ProductDetailsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    finish();
                });

                cover.setOnClickListener(v -> {
                    Intent intent = new Intent(ProductDetailsActivity.this, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", Objects.requireNonNull(snapshot.child("photo").getValue()).toString());
                    startActivity(intent);
                });

                findViewById(R.id.message).setOnClickListener(v -> {
                    Intent intent = new Intent(ProductDetailsActivity.this, ChatActivity.class);
                    intent.putExtra("hisUID", Objects.requireNonNull(snapshot.child("id").getValue()).toString());
                    startActivity(intent);
                });

                findViewById(R.id.share).setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/*");
                    intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                    intent.putExtra(Intent.EXTRA_TEXT, title.getText().toString() + ", " + "$"+price.getText().toString() + " \nPlease click on the link to buy "+"www.app.myfriend.com/product/"+pId);
                    startActivity(Intent.createChooser(intent, "Share Via"));
                });

                findViewById(R.id.report).setOnClickListener(view -> {
                    FirebaseDatabase.getInstance().getReference().child("ReportProduct").child(pId).setValue(true);
                    Snackbar.make(view,"Reported", Snackbar.LENGTH_LONG).show();
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}