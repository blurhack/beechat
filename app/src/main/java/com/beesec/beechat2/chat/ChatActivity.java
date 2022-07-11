package com.beesec.beechat2.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.essam.simpleplacepicker.MapActivity;
import com.essam.simpleplacepicker.utils.SimplePlacePicker;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.iceteck.silicompressorr.SiliCompressor;
import com.beesec.beechat2.GetTimeAgo;
import com.beesec.beechat2.MainActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.Stickers;
import com.beesec.beechat2.ThemeMode;
import com.beesec.beechat2.adapter.AdapterChat;
import com.beesec.beechat2.calling.RingingActivity;
import com.beesec.beechat2.faceFilters.FaceFilters;
import com.beesec.beechat2.groupVoiceCall.RingingGroupVoiceActivity;
import com.beesec.beechat2.meeting.MeetingActivity;
import com.beesec.beechat2.model.ModelChat;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.calling.CallingActivity;
import com.beesec.beechat2.watchParty.StartWatchPartyActivity;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class ChatActivity extends AppCompatActivity implements GiphyDialogFragment.GifSelectionListener, View.OnClickListener {

    //String
    String hisId,mName;
    boolean isShown = false;
    public static final String fileName = "recorded.3gp";
    final String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
    AdapterChat adapterChat;
    List<ModelChat> nChat;

    //Bottom
    BottomSheetDialog post_more,theme_more;
    LinearLayout image,video,audio,watch_party,camera,document,location,recorder,meeting,stickers;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int VIDEO_PICK_CODE = 1002;
    private static final int AUDIO_PICK_CODE = 1003;
    private static final int DOC_PICK_CODE = 1004;
    private static final int PERMISSION_CODE = 1001;
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PERMISSION_REQ_CODE = 1 << 3;
    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //ID
    LinearLayout main;
    MediaRecorder mediaRecorder;
    RecyclerView recyclerView;

    private RequestQueue requestQueue;
    private boolean notify = false;

    NightMode sharedPref;
    ThemeMode themeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        themeMode = new ThemeMode(this);

            if (sharedPref.loadNightModeState().equals("night")){
                setTheme(R.style.DarkTheme);
            }else if (sharedPref.loadNightModeState().equals("dim")){
                setTheme(R.style.DimTheme);
            }else {
                if (themeMode.loadNightModeState().equals("day")) {
                    setTheme(R.style.DayChatTheme);
                } else if (themeMode.loadNightModeState().equals("dark")) {
                    setTheme(R.style.DarkChatTheme);
                } else if (themeMode.loadNightModeState().equals("red")) {
                    setTheme(R.style.RedChatTheme);
                } else if (themeMode.loadNightModeState().equals("purple")) {
                    setTheme(R.style.PurpleChatTheme);
                } else if (themeMode.loadNightModeState().equals("green")) {
                    setTheme(R.style.GreenChatTheme);
                } else if (themeMode.loadNightModeState().equals("maroon")) {
                    setTheme(R.style.MarronChatTheme);
                } else if (themeMode.loadNightModeState().equals("navy")) {
                    setTheme(R.style.NavyChatTheme);
                } else if (themeMode.loadNightModeState().equals("orange")) {
                    setTheme(R.style.OrangeChatTheme);
                } else if (themeMode.loadNightModeState().equals("yellow")) {
                    setTheme(R.style.YellowChatTheme);
                }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        requestQueue = Volley.newRequestQueue(ChatActivity.this);

        //GetUSERID
        hisId = getIntent().getStringExtra("hisUID");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", ""+System.currentTimeMillis());
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
            if (getIntent().hasExtra("type")){
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else {
                onBackPressed();
            }
        });

        //Id
        main = findViewById(R.id.main);
        RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = findViewById(R.id.record_button);
        recyclerView = findViewById(R.id.chatList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        if (isShown){
            check();
        }

        //IMPORTANT
        recordButton.setRecordView(recordView);

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setOutputFile(file);

                startRecording();
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                stopRecording();
            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Snackbar.make(main, "Recording must be greater than one Second", Snackbar.LENGTH_LONG).show();
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()){
                        for (DataSnapshot dataSnapshot1 : ds.child("Voice").getChildren()){
                            if (dataSnapshot1.child("type").getValue().toString().equals("calling")){

                                if (!dataSnapshot1.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    if (!dataSnapshot1.child("end").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        if (!dataSnapshot1.child("ans").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            Intent intent = new Intent(getApplicationContext(), RingingGroupVoiceActivity.class);
                                            intent.putExtra("room", dataSnapshot1.child("room").getValue().toString());
                                            intent.putExtra("group", ds.child("groupId").getValue().toString());
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Call
        Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (ds.child("type").getValue().toString().equals("calling")){
                            Intent intent = new Intent(ChatActivity.this, RingingActivity.class);
                            intent.putExtra("room", ds.child("room").getValue().toString());
                            intent.putExtra("from", ds.child("from").getValue().toString());
                            intent.putExtra("call", ds.child("call").getValue().toString());
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(hisId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Time
                TextView time = findViewById(R.id.time);
                if (snapshot.child("status").getValue().toString().equals("online")){
                    time.setText("Active");
                    findViewById(R.id.online).setVisibility(View.VISIBLE);
                }else {
                    long lastTime = Long.parseLong(snapshot.child("status").getValue().toString());
                    time.setText(GetTimeAgo.getTimeAgo(lastTime));
                    findViewById(R.id.online).setVisibility(View.GONE);
                }

                //Name
                mName = snapshot.child("name").getValue().toString();
                TextView name = findViewById(R.id.name);
                name.setText(snapshot.child("name").getValue().toString());

                //DP
                CircleImageView dp = findViewById(R.id.dp);
                if (!snapshot.child("photo").getValue().toString().isEmpty())  Picasso.get().load(snapshot.child("photo").getValue().toString()).into(dp);

                //Verify
                if (snapshot.child("verified").getValue().toString().equals("yes")) findViewById(R.id.verify).setVisibility(View.VISIBLE);

                //Click
                dp.setOnClickListener(v -> {
                    Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                    intent.putExtra("hisUID", hisId);
                    startActivity(intent);
                });

                name.setOnClickListener(v -> {
                    Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                    intent.putExtra("hisUID", hisId);
                    startActivity(intent);
                });

                MediaPlayer mp = MediaPlayer.create(ChatActivity.this, R.raw.typing);

                //Typing
                if (snapshot.child("typingTo").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    findViewById(R.id.typing).setVisibility(View.VISIBLE);
                    mp.start();
                }else {
                    findViewById(R.id.typing).setVisibility(View.GONE);
                    mp.stop();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //VideoCall
        findViewById(R.id.video_call).setOnClickListener(v -> {

            notify = true;
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUser user = snapshot.getValue(ModelUser.class);
                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(user).getName(), "is video calling you");
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            String room = ""+System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("to", hisId);
            hashMap.put("room", room);
            hashMap.put("call", "video");
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).setValue(hashMap);

            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap2.put("receiver", hisId);
            hashMap2.put("msg", mName + " has video called");
            hashMap2.put("isSeen", false);
            hashMap2.put("timestamp", room);
            hashMap2.put("type", "video_call");
            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap2);

            Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
            intent.putExtra("room", room);
            intent.putExtra("to", hisId);
            intent.putExtra("call", "video");
            startActivity(intent);

        });

        //VoiceCall
        findViewById(R.id.audio_call).setOnClickListener(v -> {

            notify = true;
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUser user = snapshot.getValue(ModelUser.class);
                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(user).getName(), "is audio calling you");
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            String room = ""+System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("to", hisId);
            hashMap.put("room", room);
            hashMap.put("call", "voice");
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).setValue(hashMap);

            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap2.put("receiver", hisId);
            hashMap2.put("msg", mName + " has voice called");
            hashMap2.put("isSeen", false);
            hashMap2.put("timestamp", room);
            hashMap2.put("type", "voice_call");
            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap2);

            Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
            intent.putExtra("room", room);
            intent.putExtra("to", hisId);
            intent.putExtra("call", "voice");
            startActivity(intent);

        });


        //EditText
        EditText editText = findViewById(R.id.editText);

        //Typing
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HashMap<String, Object> hashMap = new HashMap<>();
                if (count == 0){
                    hashMap.put("typingTo", "noOne");
                }else {
                    hashMap.put("typingTo", hisId);
                }
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Send
        findViewById(R.id.message_send).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()){
                Snackbar.make(v,"Type a message", Snackbar.LENGTH_LONG).show();
            }else {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg", editText.getText().toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "text");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            String msg = editText.getText().toString();
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), msg);
                            editText.setText("");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        //Bottom
        addAttachment();
        addTheme();
        readMessage();
        seenMessage();
        chatList();
        findViewById(R.id.add).setOnClickListener(v -> post_more.show());
    }

    private void chatList() {
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(hisId);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef1.child("id").setValue(hisId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef2.child("id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startRecording() {
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();

        } catch(RuntimeException stopException) {
            // handle cleanup here
        }
        sendRec();
    }

    private void sendRec() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        Toast.makeText(this, "Please wait, Sending...", Toast.LENGTH_SHORT).show();
        Uri audio_uri = Uri.fromFile(new File(file));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_audio/" + ""+System.currentTimeMillis());
        storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "audio");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a voice note");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

    }

    private void addAttachment() {
        if (post_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.chat_more, null);
            image = view.findViewById(R.id.image);
            image.setOnClickListener(this);
            video = view.findViewById(R.id.video);
            video.setOnClickListener(this);
            audio = view.findViewById(R.id.audio);
            audio.setOnClickListener(this);
            document = view.findViewById(R.id.document);
            document.setOnClickListener(this);
            location = view.findViewById(R.id.location);
            location.setOnClickListener(this);
            watch_party = view.findViewById(R.id.watch_party);
            watch_party.setOnClickListener(this);
            camera = view.findViewById(R.id.camera);
            camera.setOnClickListener(this);
            recorder = view.findViewById(R.id.recorder);
            recorder.setOnClickListener(this);
            meeting = view.findViewById(R.id.meeting);
            meeting.setOnClickListener(this);

            view.findViewById(R.id.theme).setOnClickListener(view1 -> {
                post_more.dismiss();
                theme_more.show();
            });
            view.findViewById(R.id.gif).setOnClickListener(view1 -> {
                post_more.dismiss();
                //GIF
                Giphy.INSTANCE.configure(ChatActivity.this, "LpHCYFEd73GGn7A6mh29w2Ey6C2yPcR4", false);

                final GPHSettings settings = new GPHSettings();
                settings.setTheme(GPHTheme.Automatic);
                final GiphyDialogFragment dialog = GiphyDialogFragment.Companion.newInstance(settings);
                dialog.show(getSupportFragmentManager(), "giphy_dialog");
            });

            stickers = view.findViewById(R.id.stickers);
            stickers.setOnClickListener(this);

            post_more = new BottomSheetDialog(this);
            post_more.setContentView(view);
        }
    }

    private void addTheme() {
        if (theme_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.theme_more, null);

            view.findViewById(R.id.camera).setOnClickListener(view1 -> {
                themeMode.setNightModeState("day");
                restartApp();
            });

            view.findViewById(R.id.image).setOnClickListener(view1 -> {
                themeMode.setNightModeState("dark");
                restartApp();
            });

            view.findViewById(R.id.video).setOnClickListener(view1 -> {
                themeMode.setNightModeState("red");
                restartApp();
            });

            view.findViewById(R.id.audio).setOnClickListener(view1 -> {
                themeMode.setNightModeState("purple");
                restartApp();
            });



            view.findViewById(R.id.background).setOnClickListener(view1 -> {
                themeMode.setNightModeState("green");
                restartApp();
            });

            view.findViewById(R.id.gif).setOnClickListener(view1 -> {
                themeMode.setNightModeState("maroon");
                restartApp();
            });

            view.findViewById(R.id.feeling).setOnClickListener(view1 -> {
                themeMode.setNightModeState("navy");
                restartApp();
            });

            view.findViewById(R.id.meeting).setOnClickListener(view1 -> {
                themeMode.setNightModeState("orange");
                restartApp();
            });

            view.findViewById(R.id.reels).setOnClickListener(view1 -> {
                themeMode.setNightModeState("yellow");
                restartApp();
            });

            theme_more = new BottomSheetDialog(this);
            theme_more.setContentView(view);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", ""+System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
    }


    @Override
    protected void onStart() {
        super.onStart();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", "online");
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", ""+System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
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
            if (requestCode == PERMISSION_REQ_CODE) {
                boolean granted = true;
                for (int result : grantResults) {
                    granted = (result == PackageManager.PERMISSION_GRANTED);
                    if (!granted) break;
                }

                if (granted) {
                } else {
                    Snackbar.make(main, "Permission is required", Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image:

                post_more.cancel();

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
            case R.id.video:

                post_more.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickVideo();
                    }
                }
                else {
                    pickVideo();
                }

                break;
            case R.id.audio:

                post_more.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickAudio();
                    }
                }
                else {
                    pickAudio();
                }
                break;
            case  R.id.location:

                post_more.cancel();

                Intent intent = new Intent(this, MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(SimplePlacePicker.API_KEY, "AIzaSyCoezJQ7_58c0bLHXF5wBCjA8-5W0BzJ30");
                intent.putExtras(bundle);
                //noinspection deprecation
                startActivityForResult(intent, SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE);

                break;

            case  R.id.document:

                post_more.cancel();

                //Check Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else {
                        pickDoc();
                    }
                }
                else {
                    pickDoc();
                }

                break;

            case  R.id.stickers:

                post_more.cancel();

                Intent s = new Intent(ChatActivity.this, Stickers.class);
                s.putExtra("type", "user");
                s.putExtra("id", hisId);
                startActivity(s);

                break;

            case  R.id.recorder:

                post_more.cancel();

                check();

                if (isShown){
                    findViewById(R.id.mediaRecord).setVisibility(View.GONE);
                    isShown = false;
                }else {
                    findViewById(R.id.mediaRecord).setVisibility(View.VISIBLE);
                    isShown = true;
                }

                break;
            case R.id.meeting:
                post_more.cancel();
                startActivity(new Intent(ChatActivity.this, MeetingActivity.class));
                break;

            case R.id.watch_party:
                post_more.cancel();

                Query q = FirebaseDatabase.getInstance().getReference().child("Party").orderByChild("from").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (ds.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                ds.getRef().removeValue();
                                startActivity(new Intent(ChatActivity.this, StartWatchPartyActivity.class));
                            }else {
                                startActivity(new Intent(ChatActivity.this, StartWatchPartyActivity.class));
                            }
                        }
                        startActivity(new Intent(ChatActivity.this, StartWatchPartyActivity.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;

            case R.id.camera:
                post_more.cancel();
                startActivity(new Intent(ChatActivity.this, FaceFilters.class));
                break;

        }
    }

    private void check() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {

        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }


    @SuppressLint("ObsoleteSdkInt")
    private void pickDoc() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("*/*");
        startActivityForResult(intent, DOC_PICK_CODE);
    }

    private void pickAudio() {
         Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
        startActivityForResult(intent, AUDIO_PICK_CODE);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            sendImage(dp_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, Sending...", Snackbar.LENGTH_LONG).show();

        }
        if(resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 50000){
                Snackbar.make(main, "Video must be of 5 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Snackbar.make(main, "Please wait, Sending...", Snackbar.LENGTH_LONG).show();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                new CompressVideo().execute("false",video_uri.toString(),file.getPath());
            }
        }
        if (resultCode == RESULT_OK && requestCode == AUDIO_PICK_CODE && data != null){
            Uri audio_uri = Objects.requireNonNull(data).getData();
            sendAudio(audio_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, Sending...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && requestCode == DOC_PICK_CODE && data != null){
            Uri doc_uri = Objects.requireNonNull(data).getData();
            sendDoc(doc_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, Sending...", Snackbar.LENGTH_LONG).show();
        }

        if (resultCode == RESULT_OK && requestCode == SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE && data != null) {

            double lon = data.getDoubleExtra(SimplePlacePicker.LOCATION_LNG_EXTRA,0);
            double lat = data.getDoubleExtra(SimplePlacePicker.LOCATION_LAT_EXTRA,0);

            String time = ""+System.currentTimeMillis();
            //Message
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("receiver", hisId);
            hashMap.put("msg", time);
            hashMap.put("isSeen", false);
            hashMap.put("timestamp", time);
            hashMap.put("type", "location");
            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);

            //Location
            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("latitude", String.valueOf(lat));
            hashMap2.put("longitude", String.valueOf(lon));
            hashMap2.put("id", time);
            FirebaseDatabase.getInstance().getReference().child("Location").child(time).setValue(hashMap2);

            notify = true;
            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUser user = snapshot.getValue(ModelUser.class);
                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent location");
                    }
                    notify = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


            Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();

        }

        if (resultCode == RESULT_OK && requestCode == PLACE_PICKER_REQUEST && data != null){
            Place place = PlacePicker.getPlace(data, this);
            String latitude = String.valueOf(place.getLatLng().latitude);
            String longitude = String.valueOf(place.getLatLng().longitude);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDoc(Uri doc_uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_doc/" + ""+System.currentTimeMillis());
        storageReference.putFile(doc_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "doc");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a Document");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        });
    }

    private void sendAudio(Uri audio_uri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_audio/" + ""+System.currentTimeMillis());
        storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "audio");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a audio");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    @Override
    public void didSearchTerm(@NotNull String s) {

    }

    @Override
    public void onDismissed(@NotNull GPHContentType gphContentType) {

    }

    @Override
    public void onGifSelected(@NotNull Media media, @org.jetbrains.annotations.Nullable String s, @NotNull GPHContentType gphContentType) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg",  media.getUrl());
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "gif");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
        notify = true;
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (notify){
                    sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a gif");
                }
                notify = false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(ChatActivity.this)
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
            sendVideo(videoUri);
        }
    }

    private void sendVideo(Uri videoUri){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "video");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a video");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private void sendImage(Uri dp_uri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "image");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Sent", Snackbar.LENGTH_LONG).show();
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "has sent a image");
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

    }

    private void readMessage(){

        //ID
        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Get
        nChat = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChat chat = snapshot.getValue(ModelChat.class);
                    if (snapshot.hasChild("msg")){
                        if (Objects.requireNonNull(chat).getReceiver().equals(myUid) && chat.getSender().equals(hisId) ||
                                chat.getReceiver().equals(hisId) && chat.getSender().equals(myUid)){
                            nChat.add(chat);
                        }
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, nChat);
                    recyclerView.setAdapter(adapterChat);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(){

        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ModelChat modelChat = snapshot.getValue(ModelChat.class);
                    if (snapshot.hasChild("msg")){
                        if (Objects.requireNonNull(modelChat).getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && modelChat.getSender().equals(hisId)){
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isSeen", true);
                            snapshot.getRef().updateChildren(hashMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId, "chat", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAoAVZ-Vk:APA91bH7bjTYlktpJ53F9XkNPbmnUMw-csCIbocmKGKPGRPzvBAYXQ0S0XGxP3bVAylQmM6nOW9iOLPz18jUy8GrtA4OlPSe5XffhxnHd9cKlZD6XPbc9IZ7RePBvbAU-CPJ2v7_yybJ");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void restartApp() {

        Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
        intent.putExtra("hisUID", hisId);
        startActivity(intent);

    }


}