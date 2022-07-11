package com.beesec.beechat2.menu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.SharedMode;
import com.beesec.beechat2.live.activities.GoBroadcastActivity;
import com.beesec.beechat2.marketPlace.MarketPlaceActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.admin.AdminActivity;
import com.beesec.beechat2.meeting.MeetingActivity;
import com.beesec.beechat2.podcast.GoPodcastBroadcastActivity;
import com.beesec.beechat2.profile.EditProfileActivity;
import com.beesec.beechat2.reel.ReelActivity;
import com.beesec.beechat2.search.LocationActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.send.ImageEditingActivity;
import com.beesec.beechat2.send.VideoEditingActivity;
import com.beesec.beechat2.watchParty.StartWatchPartyActivity;
import com.beesec.beechat2.welcome.IntroLast;

import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("ALL")
public class MenuActivity extends AppCompatActivity {

    NightMode sharedPref;
    SharedMode sharedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        sharedMode = new SharedMode(this);
        if (!sharedMode.loadNightModeState().isEmpty()){
            setApplicationLocale(sharedMode.loadNightModeState());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.balance).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, WithdrawActivity.class)));

        TextView money = findViewById(R.id.money);

        FirebaseDatabase.getInstance().getReference().child("Balance").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    money.setText(snapshot.child("balance").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.market).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MarketPlaceActivity.class)));

        findViewById(R.id.reel).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, ReelActivity.class)));

        findViewById(R.id.group).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TermsActivity.class)));

        findViewById(R.id.party).setOnClickListener(v -> {

            Query q = FirebaseDatabase.getInstance().getReference().child("Party").orderByChild("from").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (ds.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            ds.getRef().removeValue();
                            startActivity(new Intent(MenuActivity.this, StartWatchPartyActivity.class));
                        }else {
                            startActivity(new Intent(MenuActivity.this, StartWatchPartyActivity.class));
                        }
                    }
                    startActivity(new Intent(MenuActivity.this, StartWatchPartyActivity.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

        findViewById(R.id.meeting).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MeetingActivity.class)));

        findViewById(R.id.podcast).setOnClickListener(v -> {

            String room = String.valueOf(System.currentTimeMillis());
            Query query = FirebaseDatabase.getInstance().getReference().child("Podcast").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        snapshot.getRef().removeValue();
                        Intent i = new Intent(getApplicationContext(), GoPodcastBroadcastActivity.class);
                        i.putExtra("name", room);
                        i.putExtra("type", "host");
                        startActivity(i);
                    }else {
                        Intent i = new Intent(getApplicationContext(), GoPodcastBroadcastActivity.class);
                        i.putExtra("name", room);
                        i.putExtra("type", "host");
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });

        findViewById(R.id.saved).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SavedActivity.class)));

        findViewById(R.id.live).setOnClickListener(v -> {

            String room = String.valueOf(System.currentTimeMillis());
            Query query = FirebaseDatabase.getInstance().getReference().child("Live").orderByChild("userid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        snapshot.getRef().removeValue();
                        Intent i = new Intent(MenuActivity.this, GoBroadcastActivity.class);
                        i.putExtra("name", room);
                        i.putExtra("type", "host");
                        startActivity(i);
                    }else {
                        Intent i = new Intent(MenuActivity.this, GoBroadcastActivity.class);
                        i.putExtra("name", room);
                        i.putExtra("type", "host");
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        });


        findViewById(R.id.near).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, LocationActivity.class)));

        findViewById(R.id.camera).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, TranslationActivity.class)));

        findViewById(R.id.editImage).setOnClickListener(v -> pickImage());

        findViewById(R.id.editVideo).setOnClickListener(v -> pickVideo());

        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, SearchActivity.class)));

        findViewById(R.id.verify).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, VerificationActivity.class)));

        findViewById(R.id.logOut).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MenuActivity.this, IntroLast.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.email).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditEmailActivity.class)));

        findViewById(R.id.pass).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditPasswordActivity.class)));

        findViewById(R.id.profile).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditProfileActivity.class)));

        findViewById(R.id.phone).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditNumberActivity.class)));

        findViewById(R.id.policy).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, PrivacyActivity.class)));


        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("phone").getValue().toString().isEmpty()){
                    findViewById(R.id.email).setVisibility(View.VISIBLE);
                    findViewById(R.id.pass).setVisibility(View.VISIBLE);
                    findViewById(R.id.phone).setVisibility(View.GONE);
                }else {
                    findViewById(R.id.email).setVisibility(View.GONE);
                    findViewById(R.id.pass).setVisibility(View.GONE);
                    findViewById(R.id.phone).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.nightSwitch);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch dimSwitch = findViewById(R.id.dimSwitch);

        if (sharedPref.loadNightModeState().equals("night")){
            aSwitch.setChecked(true);
            dimSwitch.setChecked(false);
        }
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                sharedPref.setNightModeState("night");
                dimSwitch.setChecked(false);
            }else {
                if (dimSwitch.isChecked()){
                    sharedPref.setNightModeState("dim");
                    dimSwitch.setChecked(true);
                    aSwitch.setChecked(false);
                }else {
                    sharedPref.setNightModeState("day");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(false);
                }

            }
            restartApp();
        });

        if (sharedPref.loadNightModeState().equals("dim")){
            dimSwitch.setChecked(true);
            aSwitch.setChecked(false);
        }
        dimSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                sharedPref.setNightModeState("dim");
                aSwitch.setChecked(false);
            }else {
                if (aSwitch.isChecked()){
                    sharedPref.setNightModeState("night");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(true);
                }else {
                    sharedPref.setNightModeState("day");
                    dimSwitch.setChecked(false);
                    aSwitch.setChecked(false);
                }

            }
            restartApp();
        });

        findViewById(R.id.invite).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, InviteActivity.class)));

        //Admin
        FirebaseDatabase.getInstance().getReference("Admin").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    findViewById(R.id.admin).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.admin).setVisibility(View.VISIBLE); // CHnage this to GONE
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.admin).setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, AdminActivity.class)));

    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, 1);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == 1 && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            Intent intent = new Intent(MenuActivity.this, VideoEditingActivity.class);
            intent.putExtra("uri", video_uri.toString());
            startActivity(intent);
        }
        if (resultCode == RESULT_OK && requestCode == 2 && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            Intent intent = new Intent(MenuActivity.this, ImageEditingActivity.class);
            intent.putExtra("uri", dp_uri.toString());
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        Objects.requireNonNull(i).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void setApplicationLocale(String locale) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(locale.toLowerCase()));
        } else {
            config.locale = new Locale(locale.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

}