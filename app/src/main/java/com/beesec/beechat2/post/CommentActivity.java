package com.beesec.beechat2.post;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
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
import com.nguyencse.URLEmbeddedData;
import com.nguyencse.URLEmbeddedView;
import com.beesec.beechat2.GetTimeAgo;
import com.beesec.beechat2.MediaViewActivity;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.StickersPost;
import com.beesec.beechat2.adapter.AdapterComment;
import com.beesec.beechat2.calling.RingingActivity;
import com.beesec.beechat2.groupVoiceCall.RingingGroupVoiceActivity;
import com.beesec.beechat2.model.ModelComment;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.send.SendToGroupActivity;
import com.beesec.beechat2.send.SendToUserActivity;
import com.beesec.beechat2.who.LikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;
import timber.log.Timber;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

@SuppressWarnings("ALL")
public class CommentActivity extends AppCompatActivity implements View.OnClickListener{

    //String
    String postID,hisId;

    //Bottom
    BottomSheetDialog comment_more;
    LinearLayout image,video,gif;

    //Post
    CircleImageView dp;
    ImageView verified,activity,mediaView,like_img,more;
    TextView name,username,time,feeling,location,like_text,topName;
    SocialTextView text,bg_text;
    VoicePlayerView voicePlayerView;
    LinearLayout likeLayout,commentLayout,viewsLayout,layout,share;
    TextView noLikes,noComments,noViews;
    ImageView thumb,love,laugh,wow,angry,sad;
    LinearLayout likeButton,likeButtonTwo,main;
    RelativeLayout line;

    VideoView play;
    URLEmbeddedView urlEmbeddedView;
    MediaPlayer mp;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }


    //Comments
    List<ModelComment> commentsList;
    AdapterComment adapterComments;
    RecyclerView recyclerView;

    //Permission
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int VIDEO_PICK_CODE = 1003;
    private static final int PERMISSION_CODE = 1001;

    private RequestQueue requestQueue;
    private boolean notify = false;

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
        setContentView(R.layout.activity_comment);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.like);

        main = findViewById(R.id.main);
        urlEmbeddedView = findViewById(R.id.uev);
        requestQueue = Volley.newRequestQueue(CommentActivity.this);

        //GetPostId
        //GIF
        if (getIntent().hasExtra("gif")){
            postID = getIntent().getStringExtra("postID");
            String timeStamp = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("cId", timeStamp);
            hashMap.put("comment", getIntent().getStringExtra("gif"));
            hashMap.put("timestamp",  timeStamp);
            hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("type", "gif");
            hashMap.put("pId", postID);
            FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments").child(timeStamp).setValue(hashMap);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
             }else {
            postID = getIntent().getStringExtra("postID");
        }


        //Post
        dp = findViewById(R.id.dp);
        recyclerView = findViewById(R.id.recycler_view);
        verified = findViewById(R.id.verified);
        name = findViewById(R.id.name);
        username = findViewById(R.id.username);
        time = findViewById(R.id.time);
        activity = findViewById(R.id.activity);
        topName = findViewById(R.id.topName);
        feeling = findViewById(R.id.feeling);
        location = findViewById(R.id.location);
        text = findViewById(R.id.text);
        mediaView = findViewById(R.id.mediaView);
        bg_text = findViewById(R.id.bg_text);
        share = findViewById(R.id.share);
        play = findViewById(R.id.play);
        voicePlayerView = findViewById(R.id.voicePlayerView);
        likeLayout = findViewById(R.id.likeLayout);
        commentLayout = findViewById(R.id.commentLayout);
        viewsLayout = findViewById(R.id.viewsLayout);
        layout = findViewById(R.id.layout);
        noLikes =  findViewById(R.id.noLikes);
        noComments  =  findViewById(R.id.noComments);
        noViews  =  findViewById(R.id.noViews);
        like_text =  findViewById(R.id.like_text);
        like_img  =  findViewById(R.id.like_img);
        thumb  =  findViewById(R.id.thumb);
        love  =  findViewById(R.id.love);
        laugh  =  findViewById(R.id.laugh);
        wow  =  findViewById(R.id.wow);
        angry  = findViewById(R.id.angry);
        likeButton  =  findViewById(R.id.likeButton);
        sad = findViewById(R.id.sad);
        likeButtonTwo = findViewById(R.id.likeButtonTwo);
        line = findViewById(R.id.line);
        more = findViewById(R.id.more);

        //Header
        findViewById(R.id.back).setOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        //Comment
        loadComments();

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
                            Intent intent = new Intent(CommentActivity.this, RingingActivity.class);
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

        //Add
        findViewById(R.id.add).setOnClickListener(v -> {
            comment_more.show();
        });
        commentMore();

        //CommentText
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.comment_send).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mComment = editText.getText().toString();
            if (mComment.isEmpty()){
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Type something", Snackbar.LENGTH_LONG).show();
            }else {
                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cId",  timeStamp);
                hashMap.put("comment", mComment);
                hashMap.put("timestamp",  timeStamp);
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("type", "text");
                hashMap.put("pId", postID);
                FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments").child(timeStamp).setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
                editText.setText("");
                addToHisNotification(hisId, "Commented on your post");
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "Commented on your post");
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
    protected void onStart() {
        super.onStart();
        postInfo();
    }

    private void postInfo() {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                urlEmbeddedView.setOnClickListener(v -> {

                    List<String> extractedUrls = extractUrls(snapshot.child("text").getValue().toString());

                    for (String s : extractedUrls)
                    {
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        startActivity(openUrlIntent);
                    }


                });

                if (!snapshot.child("text").getValue().toString().isEmpty()) {

                    List<String> extractedUrls = extractUrls(snapshot.child("text").getValue().toString());

                    for (String url : extractedUrls)
                    {
                        urlEmbeddedView.setVisibility(View.VISIBLE);

                        urlEmbeddedView.setURL(url, new URLEmbeddedView.OnLoadURLListener() {
                            @Override
                            public void onLoadURLCompleted(URLEmbeddedData data) {
                                urlEmbeddedView.title(data.getTitle());
                                urlEmbeddedView.description(data.getDescription());
                                urlEmbeddedView.host(data.getHost());
                                urlEmbeddedView.thumbnail(data.getThumbnailURL());
                                urlEmbeddedView.favor(data.getFavorURL());
                            }
                        });
                    }

                }
                
                //UserInfo
                FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.child("id").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        
                        
                        
                        
                        hisId = snapshot.child("id").getValue().toString();
                        if (!snapshot.child("photo").getValue().toString().isEmpty()) Picasso.get().load(snapshot.child("photo").getValue().toString()).into(dp);
                        name.setText(snapshot.child("name").getValue().toString());
                        username.setText(snapshot.child("username").getValue().toString());
                        topName.setText(snapshot.child("name").getValue().toString());

                        //SetOnClick
                        dp.setOnClickListener(v -> {
                            if (!snapshot.child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                intent.putExtra("hisUID", snapshot.child("id").getValue().toString());
                                startActivity(intent);
                            }else {
                                Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                            }
                        });
                        name.setOnClickListener(v -> {
                            if (!snapshot.child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                intent.putExtra("hisUID", snapshot.child("id").getValue().toString());
                                startActivity(intent);
                            }else {
                                Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                            }
                        });
                        username.setOnClickListener(v -> {
                            if (!snapshot.child("id").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                intent.putExtra("hisUID", snapshot.child("id").getValue().toString());
                                startActivity(intent);
                            }else {
                                Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Time
                long lastTime = Long.parseLong(snapshot.child("pTime").getValue().toString());
                time.setText(GetTimeAgo.getTimeAgo(lastTime));

                //Extra
                FirebaseDatabase.getInstance().getReference("postExtra").child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (!snapshot.child("location").getValue().toString().isEmpty()) location.setText(" . " + snapshot.child("location").getValue().toString());

                            location.setOnClickListener(v -> {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.co.in/maps?q=" + snapshot.child("location").getValue().toString()));
                                startActivity(i);
                            });

                            if (!snapshot.child("feeling").getValue().toString().isEmpty()) feeling.setText(" - " + snapshot.child("feeling").getValue().toString());

                            if(!snapshot.child("feeling").getValue().toString().isEmpty()){
                                String mFeeling = snapshot.child("feeling").getValue().toString();
                                if (mFeeling.contains("Traveling")){
                                    activity.setImageResource(R.drawable.airplane);
                                }else if (mFeeling.contains("Watching")){
                                    activity.setImageResource(R.drawable.watching);
                                }else if (mFeeling.contains("Listening")){
                                    activity.setImageResource(R.drawable.listening);
                                }else if (mFeeling.contains("Thinking")){
                                    activity.setImageResource(R.drawable.thinking);
                                }else if (mFeeling.contains("Celebrating")){
                                    activity.setImageResource(R.drawable.celebration);
                                }else if (mFeeling.contains("Looking")){
                                    activity.setImageResource(R.drawable.looking);
                                }else if (mFeeling.contains("Playing")){
                                    activity.setImageResource(R.drawable.playing);
                                }else if (mFeeling.contains("happy")){
                                    activity.setImageResource(R.drawable.smiling);
                                } else if (mFeeling.contains("loved")){
                                    activity.setImageResource(R.drawable.love);
                                } else if (mFeeling.contains("sad")){
                                    activity.setImageResource(R.drawable.sad);
                                }else if (mFeeling.contains("crying")){
                                    activity.setImageResource(R.drawable.crying);
                                }else if (mFeeling.contains("angry")){
                                    activity.setImageResource(R.drawable.angry);
                                }else if (mFeeling.contains("confused")){
                                    activity.setImageResource(R.drawable.confused);
                                }else if (mFeeling.contains("broken")){
                                    activity.setImageResource(R.drawable.broken);
                                }else if (mFeeling.contains("cool")){
                                    activity.setImageResource(R.drawable.cool);
                                }else if (mFeeling.contains("funny")){
                                    activity.setImageResource(R.drawable.joy);
                                }else if (mFeeling.contains("tired")){
                                    activity.setImageResource(R.drawable.tired);
                                }else if (mFeeling.contains("shock")){
                                    activity.setImageResource(R.drawable.shocked);
                                }else if (mFeeling.contains("love")){
                                    activity.setImageResource(R.drawable.heart);
                                }else if (mFeeling.contains("sleepy")){
                                    activity.setImageResource(R.drawable.sleeping);
                                }else if (mFeeling.contains("expressionless")){
                                    activity.setImageResource(R.drawable.muted);
                                }else if (mFeeling.contains("blessed")){
                                    activity.setImageResource(R.drawable.angel);
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //PostDetails
                String type = snapshot.child("type").getValue().toString();
                if (!type.equals("bg")){
                    text.setLinkText(snapshot.child("text").getValue().toString());
                    text.setOnLinkClickListener((i, s) -> {
                        if (i == 1){

                            Intent intent = new Intent(CommentActivity.this, SearchActivity.class);
                            intent.putExtra("hashtag", s);
                            startActivity(intent);

                        }else
                        if (i == 2){
                            String username = s.replaceFirst("@","");
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            Query query = ref.orderByChild("username").equalTo(username.trim());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            String id = ds.child("id").getValue().toString();
                                            if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                Snackbar.make(main,"It's you", Snackbar.LENGTH_LONG).show();
                                            }else {
                                                Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                                intent.putExtra("hisUID", id);
                                                startActivity(intent);
                                            }
                                        }
                                    }else {
                                        Snackbar.make(main,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Snackbar.make(main,error.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                        else if (i == 16){
                            if (!s.startsWith("https://") && !s.startsWith("http://")){
                                s = "http://" + s;
                            }
                            Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                            startActivity(openUrlIntent);
                        }else if (i == 4){
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                            startActivity(intent);
                        }else if (i == 8){
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, s);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "");
                            startActivity(intent);

                        }
                    });
                }
                if (type.equals("image")){
                    mediaView.setVisibility(View.VISIBLE);
                    findViewById(R.id.media).setVisibility(View.VISIBLE);
                    Picasso.get().load(snapshot.child("meme").getValue().toString()).into(mediaView);
                }
                if (type.equals("gif")){
                    mediaView.setVisibility(View.VISIBLE);
                    findViewById(R.id.media).setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext()).load(snapshot.child("meme").getValue().toString()).thumbnail(0.1f).into(mediaView);
                }
                if (type.equals("video")){
                    mediaView.setVisibility(View.VISIBLE);
                    play.setVisibility(View.VISIBLE);
                    findViewById(R.id.media).setVisibility(View.VISIBLE);
                    play.setVideoURI(Uri.parse(snapshot.child("vine").getValue().toString()));
                    play.start();
                    play.setOnPreparedListener(mp -> mp.setLooping(true));
                    MediaController mediaController = new MediaController(CommentActivity.this);
                    mediaController.setAnchorView(play);
                    play.setMediaController(mediaController);
                }
                if (type.equals("bg")){
                    findViewById(R.id.media).setVisibility(View.VISIBLE);
                    Picasso.get().load(snapshot.child("meme").getValue().toString()).into(mediaView);
                    bg_text.setLinkText(snapshot.child("text").getValue().toString());
                    bg_text.setOnLinkClickListener((i, s) -> {
                        if (i == 1){

                            Intent intent = new Intent(CommentActivity.this, SearchActivity.class);
                            intent.putExtra("hashtag", s);
                            startActivity(intent);

                        }else
                        if (i == 2){
                            String username = s.replaceFirst("@","");
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            Query query = ref.orderByChild("username").equalTo(username.trim());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            String id = ds.child("id").getValue().toString();
                                            if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                Snackbar.make(main,"It's you", Snackbar.LENGTH_LONG).show();
                                            }else {
                                                Intent intent = new Intent(CommentActivity.this, UserProfileActivity.class);
                                                intent.putExtra("hisUID", id);
                                                startActivity(intent);
                                            }
                                        }
                                    }else {
                                        Snackbar.make(main,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Snackbar.make(main,error.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                        else if (i == 16){
                            if (!s.startsWith("https://") && !s.startsWith("http://")){
                                s = "http://" + s;
                            }
                            Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                            startActivity(openUrlIntent);
                        }else if (i == 4){
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                            startActivity(intent);
                        }else if (i == 8){
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, s);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "");
                            startActivity(intent);
                        }
                    });
                    bg_text.setVisibility(View.VISIBLE);
                    text.setVisibility(View.GONE);
                    mediaView.setVisibility(View.VISIBLE);
                }
                if (type.equals("audio")){
                    findViewById(R.id.media).setVisibility(View.VISIBLE);
                    mediaView.setVisibility(View.GONE);
                    voicePlayerView.setVisibility(View.VISIBLE);
                    voicePlayerView.setAudio(snapshot.child("meme").getValue().toString());
                }

                //CheckLikes
                FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            likeLayout.setVisibility(View.VISIBLE);
                            line.setVisibility(View.VISIBLE);
                            noLikes.setText(String.valueOf(snapshot.getChildrenCount()));
                            if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                //CheckNew
                                FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){

                                            String react = snapshot.child("type").getValue().toString();
                                            if (react.equals("like")){
                                                like_img.setImageResource(R.drawable.ic_thumb);
                                                like_text.setText("Like");
                                            }
                                            if (react.equals("love")){
                                                like_img.setImageResource(R.drawable.ic_love);
                                                like_text.setText("Love");
                                            }
                                            if (react.equals("laugh")){
                                                like_img.setImageResource(R.drawable.ic_laugh);
                                                like_text.setText("Haha");
                                            }
                                            if (react.equals("wow")){
                                                like_img.setImageResource(R.drawable.ic_wow);
                                                like_text.setText("Wow");
                                            }
                                            if (react.equals("sad")){
                                                like_img.setImageResource(R.drawable.ic_sad);
                                                like_text.setText("Sad");
                                            }
                                            if (react.equals("angry")){
                                                like_img.setImageResource(R.drawable.ic_angry);
                                                like_text.setText("Angry");
                                            }

                                        }else {
                                            FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()){
                                                        like_img.setImageResource(R.drawable.ic_thumb);
                                                        like_text.setText("Like");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }else if (!snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                like_img.setImageResource(R.drawable.ic_like);
                                like_text.setText("Like");
                            }
                            //QuickShow
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("like").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0) {
                                        thumb.setVisibility(View.VISIBLE);
                                    }else {
                                        FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    thumb.setVisibility(View.VISIBLE);
                                                }else {
                                                    thumb.setVisibility(View.GONE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("love").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0){
                                        love.setVisibility(View.VISIBLE);
                                    }else {
                                        love.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("wow").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0){
                                        wow.setVisibility(View.VISIBLE);
                                    }else {
                                        wow.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("angry").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0){
                                        angry.setVisibility(View.VISIBLE);
                                    }else {
                                        angry.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("laugh").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0){
                                        laugh.setVisibility(View.VISIBLE);
                                    }else {
                                        laugh.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).orderByChild("type").equalTo("sad").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getChildrenCount()>0){
                                        sad.setVisibility(View.VISIBLE);
                                    }else {
                                        sad.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            likeLayout.setVisibility(View.GONE);
                            line.setVisibility(View.GONE);
                            like_img.setImageResource(R.drawable.ic_like);
                            like_text.setText("Like");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                //Like
                ReactionsConfig config = new ReactionsConfigBuilder(CommentActivity.this)
                        .withReactions(new int[]{
                                R.drawable.ic_thumb,
                                R.drawable.ic_love,
                                R.drawable.ic_laugh,
                                R.drawable.ic_wow,
                                R.drawable.ic_sad,
                                R.drawable.ic_angry
                        })
                        .withPopupAlpha(1)
                        .build();

                ReactionPopup popup = new ReactionPopup(CommentActivity.this, config, (position1) -> {

                    mp.start();

                    if (position1 == 0) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "like");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }else if (position1 == 1) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "love");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }
                    else if (position1 == 2) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "laugh");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }      else if (position1 == 3) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "wow");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }
                    else if (position1 == 4) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "sad");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }
                    else if (position1 == 5) {
                        FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                snapshot.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    addToHisNotification(hisId, "Liked on your post");
                                    notify = true;
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ModelUser user = snapshot.getValue(ModelUser.class);
                                            if (notify){
                                                sendNotification(hisId, Objects.requireNonNull(user).getName(), "Liked on your post");
                                            }
                                            notify = false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("type", "angry");
                                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        return true;
                    }

                    return true;
                });

                //LikeFunctions
                likeButtonTwo.setOnTouchListener(popup);
                FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                likeButtonTwo.setVisibility(View.GONE);
                                likeButton.setVisibility(View.VISIBLE);
                            }else {
                                likeButton.setVisibility(View.GONE);
                                likeButtonTwo.setVisibility(View.VISIBLE);
                            }
                        }else {
                            likeButton.setVisibility(View.GONE);
                            likeButtonTwo.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                likeButton.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Likes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(postID).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("Likes").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child(postID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }));

                //Share
                Context wrapper = new ContextThemeWrapper(CommentActivity.this, R.style.popupMenuStyle);
                PopupMenu sharePop = new PopupMenu(wrapper, share);
                sharePop.getMenu().add(Menu.NONE,0,0, "App");
                sharePop.getMenu().add(Menu.NONE,1,1, "Chat");
                sharePop.getMenu().add(Menu.NONE,2,2, "Group");
                sharePop.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 0){
                        if (type.equals("text")){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                            intent.putExtra(Intent.EXTRA_TEXT, snapshot.child("text").getValue().toString()+ " \nSee the post "+"www.app.myfriend.com/post/"+postID);
                            startActivity(Intent.createChooser(intent, "Share Via"));
                        }else if (type.equals("image")){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                            intent.putExtra(Intent.EXTRA_TEXT, snapshot.child("text").getValue().toString() + " \nSee the post "+"www.app.myfriend.com/post/"+postID);
                            startActivity(Intent.createChooser(intent, "Share Via"));
                        }else if (type.equals("audio")){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                            intent.putExtra(Intent.EXTRA_TEXT, snapshot.child("text").getValue().toString() + " \nSee the post "+"www.app.myfriend.com/post/"+postID);
                            startActivity(Intent.createChooser(intent, "Share Via"));
                        }else if (type.equals("gif")){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                            intent.putExtra(Intent.EXTRA_TEXT, snapshot.child("text").getValue().toString() + " \nSee the post "+"www.app.myfriend.com/post/"+postID);
                            startActivity(Intent.createChooser(intent, "Share Via"));
                        }else if (type.equals("video")){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                            intent.putExtra(Intent.EXTRA_TEXT, snapshot.child("text").getValue().toString() + " \nSee the post "+"www.app.myfriend.com/post/"+postID);
                            startActivity(Intent.createChooser(intent, "Share Via"));
                        }else {
                            Snackbar.make(main,"This type of post can't be shared", Snackbar.LENGTH_LONG).show();
                        }
                    }
                    if (item.getItemId() == 1){
                        Intent intent = new Intent(CommentActivity.this, SendToUserActivity.class);
                        intent.putExtra("type", "post");
                        intent.putExtra("uri", postID);
                        startActivity(intent);
                    }
                    if (item.getItemId() == 2){
                        Intent intent = new Intent( CommentActivity.this, SendToGroupActivity.class);
                        intent.putExtra("type", "post");
                        intent.putExtra("uri", postID);
                        startActivity(intent);
                    }
                    return false;
                });
                share.setOnClickListener(v -> sharePop.show());
                findViewById(R.id.send).setOnClickListener(v -> sharePop.show());

                //More
                Context moreWrapper = new ContextThemeWrapper(CommentActivity.this, R.style.popupMenuStyle);
                PopupMenu morePop = new PopupMenu(moreWrapper, more);
                morePop.getMenu().add(Menu.NONE,1,1, "Save");
                morePop.getMenu().add(Menu.NONE,2,2, "Download");
                morePop.getMenu().add(Menu.NONE,4,4, "Copy");
                morePop.getMenu().add(Menu.NONE,5,5, "Report");
                morePop.getMenu().add(Menu.NONE,9,9, "Liked by");
                if (type.equals("image") || type.equals("video")){
                    morePop.getMenu().add(Menu.NONE,8,8, "Fullscreen");
                }
                morePop.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1){
                        FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(postID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(postID).removeValue();
                                    Snackbar.make(main,"Unsaved", Snackbar.LENGTH_LONG).show();
                                }else{
                                    FirebaseDatabase.getInstance().getReference().child("Saves").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child(postID).setValue(true);
                                    Snackbar.make(main,"Saved", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Snackbar.make(main,error.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        });

                    }
                    if (item.getItemId() == 9){
                        Intent intent = new Intent(CommentActivity.this, LikedActivity.class);
                        intent.putExtra("id", postID);
                        startActivity(intent);
                    }
                    if (item.getItemId() == 2){
                        if (type.equals("text") || type.equals("bg")){
                            Snackbar.make(main,"This type of post can't be downloaded", Snackbar.LENGTH_LONG).show();
                        }else if (type.equals("video")){
                            Snackbar.make(main,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.child("vine").getValue().toString()));
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalFilesDir(CommentActivity.this, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                            Objects.requireNonNull(downloadManager).enqueue(request);
                        }else {
                            Snackbar.make(main,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.child("meme").getValue().toString()));
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalFilesDir(CommentActivity.this, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".png");
                            Objects.requireNonNull(downloadManager).enqueue(request);
                        }
                    }else if (item.getItemId() == 4){
                        Snackbar.make(main,"Copied", Snackbar.LENGTH_LONG).show();
                        if (type.equals("text")){
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString());
                            clipboard.setPrimaryClip(clip);
                        }else if (type.equals("image")){

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString() + " " + snapshot.child("meme").getValue().toString());
                            clipboard.setPrimaryClip(clip);

                        }else if (type.equals("audio")){

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString() + " " + snapshot.child("meme").getValue().toString());
                            clipboard.setPrimaryClip(clip);

                        }else if (type.equals("gif")){

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString() + " " + snapshot.child("meme").getValue().toString());
                            clipboard.setPrimaryClip(clip);

                        }else if (type.equals("video")){

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString() + " " + snapshot.child("vine").getValue().toString());
                            clipboard.setPrimaryClip(clip);

                        }else {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("text", snapshot.child("text").getValue().toString() + " " + snapshot.child("meme").getValue().toString());
                            clipboard.setPrimaryClip(clip);

                        }
                    }else if (item.getItemId() == 5){
                        FirebaseDatabase.getInstance().getReference().child("ReportPost").child(postID).setValue(true);
                        Snackbar.make(main,"Reported", Snackbar.LENGTH_LONG).show();
                    }else  if (item.getItemId() == 7){
                        if (type.equals("text")){
                            FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).getRef().removeValue();
                            Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                        }else if (type.equals("video")){
                            FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.child("vine").getValue().toString()).delete().addOnCompleteListener(task -> {
                                FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).getRef().removeValue();
                                Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                            });
                        }else if (type.equals("bg")){
                            FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).getRef().removeValue();
                            Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                        }
                        else if (type.equals("image")){
                            FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.child("meme").getValue().toString()).delete().addOnCompleteListener(task -> {
                                FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).getRef().removeValue();
                                Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Posts").child(postID).getRef().removeValue();
                            Snackbar.make(main,"Deleted", Snackbar.LENGTH_LONG).show();
                        }
                        onBackPressed();
                    }  else if (item.getItemId() == 8){
                        switch (type) {
                            case "image":

                                Intent intent = new Intent(CommentActivity.this, MediaViewActivity.class);
                                intent.putExtra("type", "image");
                                intent.putExtra("uri", snapshot.child("meme").getValue().toString());
                                startActivity(intent);

                                break;
                            case "video":

                                Intent intent1 = new Intent(CommentActivity.this, MediaViewActivity.class);
                                intent1.putExtra("type", "video");
                                intent1.putExtra("uri", snapshot.child("vine").getValue().toString());
                                startActivity(intent1);

                                break;
                        }
                    }
                    return false;
                });
                more.setOnClickListener(v -> morePop.show());

                //ProgressBar
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                //MediaLayout
                RelativeLayout mediaViewLayout = findViewById(R.id.media);
                mediaViewLayout.setOnClickListener(v -> {
                    switch (type) {
                        case "image":

                            Intent intent = new Intent(CommentActivity.this, MediaViewActivity.class);
                            intent.putExtra("type", "image");
                            intent.putExtra("uri", snapshot.child("meme").getValue().toString());
                            startActivity(intent);

                            break;
                        case "video":

                            Intent intent1 = new Intent(CommentActivity.this, MediaViewActivity.class);
                            intent1.putExtra("type", "video");
                            intent1.putExtra("uri", snapshot.child("vine").getValue().toString());
                            startActivity(intent1);

                            break;
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void commentMore() {
        if (comment_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.comment_more, null);
            image = view.findViewById(R.id.image);
            image.setOnClickListener(this);
            video = view.findViewById(R.id.video);
            video.setOnClickListener(this);
            gif = view.findViewById(R.id.gif);
            gif.setOnClickListener(this);
            comment_more = new BottomSheetDialog(this);
            comment_more.setContentView(view);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image:
                comment_more.cancel();

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
                comment_more.cancel();

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
                case R.id.gif:

                    comment_more.cancel();
                    Intent s = new Intent(CommentActivity.this, StickersPost.class);
                    s.putExtra("activity", "comment");
                    s.putExtra("postID", postID);
                    startActivity(s);

                    break;
                    
        }
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

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null){
            Uri img_uri = Objects.requireNonNull(data).getData();
            uploadImage(img_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && requestCode == 130 && data != null){
            Uri img_uri = Objects.requireNonNull(data).getData();
            uploadImage(img_uri);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
        }
        if (resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplicationContext(), video_uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMilli = Long.parseLong(time);
            retriever.release();

            if (timeInMilli > 600000){
                Snackbar.make(main, "Video must be of 10 minutes or less", Snackbar.LENGTH_LONG).show();
            }else {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                Snackbar.make(main, "Please wait, sending...", Snackbar.LENGTH_LONG).show();
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
                new CommentActivity.CompressVideo().execute("false",video_uri.toString(),file.getPath());
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @SuppressLint("StaticFieldLeak")
    private class CompressVideo extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... strings) {
            String videoPath = null;
            try {
                Uri mUri = Uri.parse(strings[1]);
                videoPath = SiliCompressor.with(CommentActivity.this)
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
        String timeStamp = ""+System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("comment_video/" + timeStamp);
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cId",  timeStamp);
                hashMap.put("comment", downloadUri.toString());
                hashMap.put("timestamp",  timeStamp);
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("type", "video");
                hashMap.put("pId", postID);
                FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments").child(timeStamp).setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
                addToHisNotification(hisId, "Commented on your post");
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "Commented on your post");
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

    private void uploadImage(Uri dp_uri) {
        String timeStamp = ""+System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("comment_photo/" + timeStamp);
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cId",  timeStamp);
                hashMap.put("comment", downloadUri.toString());
                hashMap.put("timestamp",  timeStamp);
                hashMap.put("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("type", "image");
                hashMap.put("pId", postID);
                FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments").child(timeStamp).setValue(hashMap);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Snackbar.make(main, "Comment sent", Snackbar.LENGTH_LONG).show();
                addToHisNotification(hisId, "Commented on your post");
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            sendNotification(hisId, Objects.requireNonNull(user).getName(), "Commented on your post");
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

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        commentsList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Posts").child(postID).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentsList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelComment modelComments = ds.getValue(ModelComment.class);
                    commentsList.add(modelComments);
                    Collections.reverse(commentsList);
                    adapterComments = new AdapterComment(CommentActivity.this, commentsList);
                    recyclerView.setAdapter(adapterComments);
                    adapterComments.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addToHisNotification(String hisUid, String message){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", postID);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", message);
        hashMap.put("sUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Count").child(timestamp).setValue(true);
    }

    private void sendNotification(final String hisId, final String name,final String message){

        /*if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().toString().isEmpty()){
            String username = getResources().getString(R.string.your_email);
            String password = getResources().getString(R.string.your_password);
            String messageToSend = message;
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator(){
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication(){
                            return new PasswordAuthentication(username, password);
                        }
                    });

            FirebaseDatabase.getInstance().getReference().child("Users").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child("email").getValue().toString().isEmpty()){
                        String em = snapshot.child("email").getValue().toString();

                        try {
                            Message message1 = new MimeMessage(session);
                            message1.setFrom(new InternetAddress(username));
                            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                            message1.setSubject("New Message - "+ getResources().getString(R.string.app_name));
                            message1.setText(name + " " +messageToSend);
                            Transport.send(message1);
                        }catch (MessagingException e){
                            throw new RuntimeException(e);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

         */

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "New Notification", hisId, "profile", R.drawable.logo);
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

}