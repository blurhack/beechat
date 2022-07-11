package com.beesec.beechat2.reel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

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
import com.beesec.beechat2.R;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.send.SendToGroupActivity;
import com.beesec.beechat2.send.SendToUserActivity;
import com.beesec.beechat2.who.ReelLikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

@SuppressWarnings("ALL")
public class ViewReelActivity extends AppCompatActivity {

    VideoView videoView;
    LinearLayout like, comment;
    ImageView share;
    CircleImageView avatar;
    TextView name,textLike,textComment;
    SocialTextView description;
    ImageView like_img;
    String reelId;
    String uri;
    String text,hisId;

    BottomSheetDialog share_options;
    LinearLayout app,groups,users;

    BottomSheetDialog reel_options;
    LinearLayout download,save,delete,copy,report,liked;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reel);
        
        uri = getIntent().getStringExtra("id");

        videoView = findViewById(R.id.videoView);
        like = findViewById(R.id.like);
        comment = findViewById(R.id.comment);
        share = findViewById(R.id.share);
        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        avatar = findViewById(R.id.avatar);
        like_img  = findViewById(R.id.like_img);
        textLike = findViewById(R.id.textLike);
        textComment = findViewById(R.id.textComment);

        Query query = FirebaseDatabase.getInstance().getReference().child("Reels").orderByChild("video").equalTo(uri);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()){

                    reelId = ds.child("pId").getValue().toString();

                    FirebaseDatabase.getInstance().getReference("Reels").child(reelId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            text = snapshot.child("text").getValue().toString();

                            hisId = ds.child("id").getValue().toString();

                            //TextView
                            description.setLinkText(snapshot.child("text").getValue().toString());
                            FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.child("id").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    name.setText("@"+snapshot.child("name").getValue().toString());
                                    name.setOnClickListener(v -> {
                                        Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                                        intent.putExtra("hisUID", snapshot.child("id").getValue().toString());
                                        v.getContext().startActivity(intent);
                                    });
                                    avatar.setOnClickListener(v -> {
                                        Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                                        intent.putExtra("hisUID", snapshot.child("id").getValue().toString());
                                        v.getContext().startActivity(intent);
                                    });
                                    if (!snapshot.child("photo").getValue().toString().isEmpty()){
                                        Picasso.get().load(snapshot.child("photo").getValue().toString()).into(avatar);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //Views
                            TextView views = findViewById(R.id.views);
                            FirebaseDatabase.getInstance().getReference("ReelViews").child(reelId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        views.setVisibility(View.VISIBLE);
                                        views.setText(String.valueOf(snapshot.getChildrenCount()));
                                    }else {
                                        views.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //more
                            createMoreBottom();
                            findViewById(R.id.more).setOnClickListener(v1 -> {
                                reel_options.show();
                            });

                            //SocialText
                            description.setOnLinkClickListener((i, s) -> {
                                if (i == 1){

                                    Intent intent = new Intent(ViewReelActivity.this, SearchActivity.class);
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
                                                        Snackbar.make(findViewById(R.id.main),"It's you", Snackbar.LENGTH_LONG).show();
                                                    }else {
                                                        Intent intent = new Intent(ViewReelActivity.this, UserProfileActivity.class);
                                                        intent.putExtra("hisUID", id);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }else {
                                                Snackbar.make(findViewById(R.id.main),"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
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

                            //Likes
                            like.setOnClickListener(v -> {
                                FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(reelId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(reelId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                        }else {
                                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(reelId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            });
                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(reelId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        like_img.setImageResource(R.drawable.ic_liked_reel);
                                    }else {
                                        like_img.setImageResource(R.drawable.ic_reel_like);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(reelId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        textLike.setText(String.valueOf(snapshot.getChildrenCount()));
                                        textLike.setVisibility(View.VISIBLE);
                                    }else {
                                        textLike.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //Comment
                            FirebaseDatabase.getInstance().getReference("Reels").child(reelId).child("Comment").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        textComment.setText(String.valueOf(snapshot.getChildrenCount()));
                                        textComment.setVisibility(View.VISIBLE);
                                    }else {
                                        textComment.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                            //Video
                            videoView.setVideoPath(snapshot.child("video").getValue().toString());
                            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    findViewById(R.id.pb).setVisibility(View.GONE);
                                    mp.start();
                                    mp.setLooping(true);
                                    FirebaseDatabase.getInstance().getReference("ReelViews").child(reelId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                }
                            });


                            //Comment
                            comment.setOnClickListener(v -> {
                                Intent intent = new Intent(ViewReelActivity.this, ReelCommentActivity.class);
                                intent.putExtra("item", "0");
                                intent.putExtra("id", reelId);
                                intent.putExtra("type", "view");
                                startActivity(intent);
                            });

                            //Share
                            share.setOnClickListener(v1 -> {
                                share_bottom();
                                share_options.show();
                            });



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

    }

    private void createMoreBottom() {
        if (reel_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(ViewReelActivity.this).inflate(R.layout.reel_options, null);
            delete = view.findViewById(R.id.delete);
            download = view.findViewById(R.id.download);
            save = view.findViewById(R.id.save);
            copy = view.findViewById(R.id.copy);
            report = view.findViewById(R.id.report);
            liked = view.findViewById(R.id.liked);

            if (!hisId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                delete.setVisibility(View.GONE);
            }else {
                report.setVisibility(View.GONE);
            }

            liked.setOnClickListener(v -> {
                Intent intent = new Intent(ViewReelActivity.this, ReelLikedActivity.class);
                intent.putExtra("reelId", reelId);
                startActivity(intent);
            });

            delete.setOnClickListener(v -> {
                FirebaseStorage.getInstance().getReferenceFromUrl(uri).delete().addOnCompleteListener(task -> {
                    FirebaseDatabase.getInstance().getReference().child("Reels").child(reelId).getRef().removeValue();
                    Snackbar.make(v,"Deleted", Snackbar.LENGTH_LONG).show();
                    onBackPressed();
                });
            });

            save.setOnClickListener(v -> {

                FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(reelId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(reelId).removeValue();
                            Snackbar.make(v,"Unsaved", Snackbar.LENGTH_LONG).show();
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(reelId).setValue(true);
                            Snackbar.make(v,"Saved", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            });

            view.findViewById(R.id.share).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, " \nWatch the reels "+"www.app.myfriend.com/reel/"+reelId);
                startActivity(Intent.createChooser(intent, "Share Via"));
            });


            download.setOnClickListener(v -> {
                Snackbar.make(v,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                DownloadManager downloadManager = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(v.getContext(), DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                Objects.requireNonNull(downloadManager).enqueue(request);
            });

            copy.setOnClickListener(v -> {

                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", text+ " " +uri);
                clipboard.setPrimaryClip(clip);
                Snackbar.make(v,"Copied", Snackbar.LENGTH_LONG).show();

            });

            report.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference().child("ReportReel").child(reelId).setValue(true);
                Snackbar.make(v,"Reported", Snackbar.LENGTH_LONG).show();
            });

            reel_options = new BottomSheetDialog(ViewReelActivity.this);
            reel_options.setContentView(view);
        }
    }

    private void share_bottom() {
        if (share_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(ViewReelActivity.this).inflate(R.layout.share_options, null);
            app = view.findViewById(R.id.app);
            groups = view.findViewById(R.id.groups);
            users = view.findViewById(R.id.users);

            app.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, text + " " + uri);
                 startActivity(Intent.createChooser(intent, "Share Via"));
            });

            groups.setOnClickListener(v -> {
                Intent intent = new Intent( ViewReelActivity.this, SendToGroupActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", uri);
                startActivity(intent);
            });

            users.setOnClickListener(v -> {
                Intent intent = new Intent( ViewReelActivity.this, SendToUserActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", uri);
               startActivity(intent);
            });

            share_options = new BottomSheetDialog(ViewReelActivity.this);
            share_options.setContentView(view);
        }
    }

}