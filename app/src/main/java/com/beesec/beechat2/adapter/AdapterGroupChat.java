package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.Point;
import com.beesec.beechat2.MediaViewActivity;
import com.beesec.beechat2.R;
import com.beesec.beechat2.group.GroupChatActivity;
import com.beesec.beechat2.meeting.MeetingActivity;
import com.beesec.beechat2.model.ModelGroupChat;
import com.beesec.beechat2.post.CommentActivity;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.reel.ViewReelActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.story.ChatStoryViewActivity;
import com.beesec.beechat2.story.HighViewActivity;
import com.beesec.beechat2.watchParty.StartPartyActivity;
import com.beesec.beechat2.watchParty.StartYouTubeActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


@SuppressWarnings("ALL")
public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.MyHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private final List<ModelGroupChat> modelChats;
    String postType;
    BottomSheetDialog reel_options;

    public AdapterGroupChat(Context context, List<ModelGroupChat> modelChats) {
        this.context = context;
        this.modelChats = modelChats;
    }

    @NonNull
    @Override
    public AdapterGroupChat.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_left_list, parent, false);

           return new MyHolder(view);
       }
        View view = LayoutInflater.from(context).inflate(R.layout.group_chat_right_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdapterGroupChat.MyHolder holder, final int position) {

        if (modelChats.get(position).getType().equals("text")){
            holder.text.setVisibility(View.VISIBLE);

            holder.text.setLinkText(modelChats.get(position).getMsg());
            holder.text.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    context.startActivity(intent);

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
                                        Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(context, UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                }
                            }else {
                                Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (i == 16){
                    if (!s.startsWith("https://") && !s.startsWith("http://")){
                        s = "http://" + s;
                    }
                    Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    context.startActivity(openUrlIntent);
                }else if (i == 4){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                    context.startActivity(intent);
                }else if (i == 8){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, s);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    context.startActivity(intent);

                }
            });


        }else if (modelChats.get(position).getType().equals("image")){
            holder.media.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
            Picasso.get().load(modelChats.get(position).getMsg()).into(holder.media);
        }
        else if (modelChats.get(position).getType().equals("doc")){
            holder.doc.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
        }
        else if (modelChats.get(position).getType().equals("video")){
            holder.play.setVisibility(View.VISIBLE);
            holder.media.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(modelChats.get(position).getMsg()).thumbnail(0.1f).into(holder.media);
        }else if (modelChats.get(position).getType().equals("audio")){
            holder.voicePlayerView.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
            holder.voicePlayerView.setAudio(modelChats.get(position).getMsg());
        }else if (modelChats.get(position).getType().equals("gif")){
            holder.media.setVisibility(View.VISIBLE);
            holder.media_layout.setVisibility(View.VISIBLE);
            Glide.with(context).load(modelChats.get(position).getMsg()).thumbnail(0.1f).into(holder.media);
        }
        else if (modelChats.get(position).getType().equals("location")){

            FirebaseDatabase.getInstance().getReference().child("Location").child(modelChats.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){

                        double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                        double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());

                        MapboxStaticMap staticImage = MapboxStaticMap.builder()
                                .accessToken("sk.eyJ1Ijoic3BhY2VzdGVyIiwiYSI6ImNrbmg2djJmdzJpZGQyd2xjeTk3a2twNTQifQ.iIiTRT_GwIYwFMsCWP5XGA")
                                .styleId(StaticMapCriteria.DARK_STYLE)
                                .cameraPoint(Point.fromLngLat(longitude, latitude))
                                .cameraZoom(13)
                                .width(250) // Image width
                                .height(200) // Image height
                                .retina(true) // Retina 2x image will be returned
                                .build();

                        holder.media.setVisibility(View.VISIBLE);
                        holder.media_layout.setVisibility(View.VISIBLE);
                        String imageUrl = staticImage.url().toString();
                        Picasso.get().load(imageUrl).into(holder.media);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        else if (modelChats.get(position).getType().equals("post")){
            holder.post.setVisibility(View.VISIBLE);
            FirebaseDatabase.getInstance().getReference().child("Posts").child(modelChats.get(position).getMsg()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        postType = snapshot.child("type").getValue().toString();

                        FirebaseDatabase.getInstance().getReference("Users").child(snapshot.child("id").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //Top
                                holder.head.setVisibility(View.VISIBLE);
                                holder.name.setText(snapshot.child("name").getValue().toString());
                                if (!snapshot.child("photo").getValue().toString().isEmpty()) {
                                    Picasso.get().load(snapshot.child("photo").getValue().toString()).into(holder.avatar);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        //Mid
                        if (snapshot.child("type").getValue().toString().equals("image")){
                            holder.post_media.setVisibility(View.VISIBLE);
                            Picasso.get().load(snapshot.child("meme").getValue().toString()).into( holder.post_media);
                        }
                        if (snapshot.child("type").getValue().toString().equals("vine")){
                            holder.post_media.setVisibility(View.VISIBLE);
                            holder.post_play.setVisibility(View.VISIBLE);
                            Glide.with(context).asBitmap().load(snapshot.child("vine").getValue().toString()).thumbnail(0.1f).into(holder.post_media);
                        }
                        if (snapshot.child("type").getValue().toString().equals("gif")){
                            holder.post_media.setVisibility(View.VISIBLE);
                            Glide.with(context).load(modelChats.get(position).getMsg()).thumbnail(0.1f).into(holder.post_media);
                        }
                        if (snapshot.child("type").getValue().toString().equals("audio")){
                            holder.post_voicePlayerView.setVisibility(View.VISIBLE);
                            holder.post_voicePlayerView.setAudio(snapshot.child("meme").getValue().toString());
                        }
                        if (snapshot.child("type").getValue().toString().equals("bg")){
                            holder.post_media.setVisibility(View.VISIBLE);
                            Picasso.get().load(snapshot.child("meme").getValue().toString()).into( holder.post_media);
                        }

                        //Bottom
                        holder.post_text.setVisibility(View.VISIBLE);

                        holder.post_text.setLinkText(snapshot.child("text").getValue().toString());
                        holder.post_text.setOnLinkClickListener((i, s) -> {
                            if (i == 1){

                                Intent intent = new Intent(context, SearchActivity.class);
                                intent.putExtra("hashtag", s);
                                context.startActivity(intent);
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
                                                    Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                                }else {
                                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                                    intent.putExtra("hisUID", id);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    context.startActivity(intent);
                                                }
                                            }
                                        }else {
                                            Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else if (i == 16){
                                if (!s.startsWith("https://") && !s.startsWith("http://")){
                                    s = "http://" + s;
                                }
                                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                                context.startActivity(openUrlIntent);
                            }else if (i == 4){
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                                context.startActivity(intent);
                            }else if (i == 8){
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL, s);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                context.startActivity(intent);

                            }
                        });
                    }else {
                        Query query = FirebaseDatabase.getInstance().getReference().child("Chat").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    ds.getRef().removeValue();
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

            //Click
            holder.itemView.setOnClickListener(v -> {
                if (modelChats.get(position).getType().equals("post")){

                    if (postType.equals("video")){
                        FirebaseDatabase.getInstance().getReference().child("Views").child(modelChats.get(position).getMsg()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        Intent intent = new Intent(context, CommentActivity.class);
                        intent.putExtra("postID", modelChats.get(position).getMsg());
                        context.startActivity(intent);
                    }else {
                        Intent intent = new Intent(context, CommentActivity.class);
                        intent.putExtra("postID", modelChats.get(position).getMsg());
                        context.startActivity(intent);
                    }

                }else  if (modelChats.get(position).getType().equals("image")){
                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent);
                }else if (modelChats.get(position).getType().equals("video")){
                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "video");
                    intent.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent);
                }
            });

        }else if (modelChats.get(position).getType().equals("voice_call")){
            holder.main.setVisibility(View.GONE);
            holder.call_layout.setVisibility(View.VISIBLE);
            holder.call.setText(modelChats.get(position).getMsg());
            holder.call_img.setImageResource(R.drawable.ic_audio_call);
        }
        else if (modelChats.get(position).getType().equals("video_call")){
            holder.main.setVisibility(View.GONE);
            holder.call_layout.setVisibility(View.VISIBLE);
            holder.call.setText(modelChats.get(position).getMsg());
            holder.call_img.setImageResource(R.drawable.ic_video_call);
        }else if (modelChats.get(position).getType().equals("reel")){
            holder.main.setVisibility(View.VISIBLE);
            holder.reelView.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(modelChats.get(position).getMsg()).thumbnail(0.1f).into(holder.reelSource);
        }
        
        //More
        holder.itemView.setOnLongClickListener(v -> {
            more_bottom(holder, position);
            reel_options.show();
            return false;
        });
        holder.text.setOnLongClickListener(v -> {
            more_bottom(holder, position);
            reel_options.show();
            return false;
        });
        holder.media.setOnLongClickListener(v -> {
            more_bottom(holder, position);
            reel_options.show();
            return false;
        });
        holder.voicePlayerView.setOnLongClickListener(v -> {
            more_bottom(holder, position);
            reel_options.show();
            return false;
        });
        holder.post.setOnLongClickListener(v -> {
            more_bottom(holder, position);
            reel_options.show();
            return false;
        });

        //Click
        holder.media_layout.setOnClickListener(v -> {

            switch (modelChats.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(modelChats.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });


                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child( modelChats.get(position).getTimestamp()).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String timeStamp = ""+System.currentTimeMillis();
                            HashMap<String, Object> hashMap1 = new HashMap<>();
                            hashMap1.put("ChatId", timeStamp);
                            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap1.put("msg", "has joined");
                            FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                            if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                                Intent intent = new Intent(context, StartYouTubeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }else {
                                Intent intent = new Intent(context, StartPartyActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "reel":

                    Intent intent3 = new Intent(context, ViewReelActivity.class);
                    intent3.putExtra("id", modelChats.get(position).getMsg());
                    context.startActivity(intent3);

                    break;

                case "story":

                    Intent intent9 = new Intent(context, ChatStoryViewActivity.class);
                    intent9.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent9);

                    break;

                case "high":

                    Intent intent2 = new Intent(context, HighViewActivity.class);
                    intent2.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent2);

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(modelChats.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                                double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", modelChats.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }

        });
        holder.media.setOnClickListener(v -> {

            switch (modelChats.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(modelChats.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });


                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child( modelChats.get(position).getTimestamp()).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String timeStamp = ""+System.currentTimeMillis();
                            HashMap<String, Object> hashMap1 = new HashMap<>();
                            hashMap1.put("ChatId", timeStamp);
                            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap1.put("msg", "has joined");
                            FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                            if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                                Intent intent = new Intent(context, StartYouTubeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }else {
                                Intent intent = new Intent(context, StartPartyActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "reel":

                    Intent intent3 = new Intent(context, ViewReelActivity.class);
                    intent3.putExtra("id", modelChats.get(position).getMsg());
                    context.startActivity(intent3);

                    break;

                case "story":

                    Intent intent9 = new Intent(context, ChatStoryViewActivity.class);
                    intent9.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent9);

                    break;

                case "high":

                    Intent intent2 = new Intent(context, HighViewActivity.class);
                    intent2.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent2);

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(modelChats.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                                double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", modelChats.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }

        });

        holder.itemView.setOnClickListener(v -> {
            switch (modelChats.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(modelChats.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });


                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", modelChats.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child( modelChats.get(position).getTimestamp()).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String timeStamp = ""+System.currentTimeMillis();
                            HashMap<String, Object> hashMap1 = new HashMap<>();
                            hashMap1.put("ChatId", timeStamp);
                            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap1.put("msg", "has joined");
                            FirebaseDatabase.getInstance().getReference().child("Party").child(modelChats.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                            if (snapshot.child("type").getValue().toString().equals("upload_youtube")){
                                Intent intent = new Intent(context, StartYouTubeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }else {
                                Intent intent = new Intent(context, StartPartyActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("room", modelChats.get(position).getTimestamp());
                                context.startActivity(intent);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "reel":

                    Intent intent3 = new Intent(context, ViewReelActivity.class);
                    intent3.putExtra("id", modelChats.get(position).getMsg());
                    context.startActivity(intent3);

                    break;

                case "story":

                    Intent intent9 = new Intent(context, ChatStoryViewActivity.class);
                    intent9.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent9);

                    break;

                case "high":

                    Intent intent2 = new Intent(context, HighViewActivity.class);
                    intent2.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent2);

                    break;
                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(modelChats.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                                double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", modelChats.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });
        holder.reelSource.setOnClickListener(v -> {

            switch (modelChats.get(position).getType()){
                case "reel":

                    Intent intent3 = new Intent(context, ViewReelActivity.class);
                    intent3.putExtra("id", modelChats.get(position).getMsg());
                    context.startActivity(intent3);

                    break;
                case "story":

                    Intent intent = new Intent(context, ChatStoryViewActivity.class);
                    intent.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent);

                    break;

                case "high":

                    Intent intent2 = new Intent(context, HighViewActivity.class);
                    intent2.putExtra("userid", modelChats.get(position).getMsg());
                    context.startActivity(intent2);

                    break;

            }

        });

        holder.seen.setVisibility(View.GONE);


        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(modelChats.get(position).getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.sender_name.setText(snapshot.child("name").getValue().toString());
                if (!snapshot.child("photo").getValue().toString().isEmpty()) Picasso.get().load(snapshot.child("photo").getValue().toString()).into(holder.dp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void downloadDoc(Context context, String directoryDownloads, String url, String extension) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri1);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, directoryDownloads, extension);
        Objects.requireNonNull(downloadManager).enqueue(request);
    }

    private void more_bottom(MyHolder holder, int position) {
        if (reel_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.chat_item_bottom, null);
            TextView time = view.findViewById(R.id.time);

            @SuppressLint("SimpleDateFormat") String value = new java.text.SimpleDateFormat("dd/MM/yy - h:mm a").
                    format(new java.util.Date(Long.parseLong(modelChats.get(position).getTimestamp()) * 1000));

            time.setText(value);

            LinearLayout report = view.findViewById(R.id.report);
            report.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference().child("ReportChat").child(modelChats.get(position).getTimestamp()).setValue(true);
                Snackbar.make(holder.itemView,"Reported", Snackbar.LENGTH_LONG).show();
            });

            LinearLayout delete = view.findViewById(R.id.delete);
            delete.setOnClickListener(v -> {
                String type = modelChats.get(position).getType();
                if (type.equals("text")){
                    Query query =  FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.getGroupId()).child("Message").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else if (type.equals("voice_call")){
                    Query query = FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.getGroupId()).child("Message").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();

                                Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("room").equalTo(modelChats.get(position).getTimestamp());
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            ds.getRef().removeValue();
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
                }
                else if (type.equals("video_call")){
                    Query query = FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.getGroupId()).child("Message").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();

                                Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("room").equalTo(modelChats.get(position).getTimestamp());
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            ds.getRef().removeValue();
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
                }
                else if (type.equals("post")){
                    Query query = FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.getGroupId()).child("Message").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }else if (type.equals("reel")){
                    Query query = FirebaseDatabase.getInstance().getReference().child("Chats").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else  if (type.equals("gif")){
                    Query query = FirebaseDatabase.getInstance().getReference().child("Chats").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                ds.getRef().removeValue();
                                Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else{
                    FirebaseStorage.getInstance().getReferenceFromUrl(modelChats.get(position).getMsg()).delete().addOnCompleteListener(task -> {
                        Query query = FirebaseDatabase.getInstance().getReference("Groups").child(GroupChatActivity.getGroupId()).child("Message").orderByChild("timestamp").equalTo(modelChats.get(position).getTimestamp());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    ds.getRef().removeValue();
                                    Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    });
                }
            });

            LinearLayout download = view.findViewById(R.id.download);
            download.setOnClickListener(v -> {
                String type = modelChats.get(position).getType();

                if (type.equals("doc")){
                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(modelChats.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                }
                else if (type.equals("video")){
                    Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelChats.get(position).getMsg()));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                    Objects.requireNonNull(downloadManager).enqueue(request);
                }else if (type.equals("reel")){
                    Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelChats.get(position).getMsg()));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                    Objects.requireNonNull(downloadManager).enqueue(request);
                }
                else if (type.equals("image")){
                    Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelChats.get(position).getMsg()));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".png");
                    Objects.requireNonNull(downloadManager).enqueue(request);
                }else if (type.equals("audio")){
                    Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelChats.get(position).getMsg()));
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(context, DIRECTORY_DOWNLOADS, System.currentTimeMillis() + ".mp3");
                    Objects.requireNonNull(downloadManager).enqueue(request);
                }else {
                    Snackbar.make(holder.itemView,"This type of message can't be downloaded", Snackbar.LENGTH_LONG).show();
                }
            });

            LinearLayout copy = view.findViewById(R.id.copy);
            copy.setOnClickListener(v -> {
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", modelChats.get(position).getMsg());
                clipboard.setPrimaryClip(clip);
            });

            LinearLayout maximize = view.findViewById(R.id.maximize);

            switch (modelChats.get(position).getType()) {
                case "image":
                    maximize.setVisibility(View.VISIBLE);
                case "video":
                    maximize.setVisibility(View.VISIBLE);
                    break;
            }

            if (modelChats.get(position).getType().equals("image") || modelChats.get(position).getType().equals("video")  || modelChats.get(position).getType().equals("doc") || modelChats.get(position).getType().equals("audio") || modelChats.get(position).getType().equals("reel")){
                download.setVisibility(View.VISIBLE);
            }else {
                download.setVisibility(View.GONE);
            }

            maximize.setOnClickListener(v -> {
                switch (modelChats.get(position).getType()) {
                    case "image":

                        Intent intent = new Intent(context, MediaViewActivity.class);
                        intent.putExtra("type", "image");
                        intent.putExtra("uri", modelChats.get(position).getMsg());
                        context.startActivity(intent);

                        break;
                    case "video":

                        Intent intent1 = new Intent(context, MediaViewActivity.class);
                        intent1.putExtra("type", "video");
                        intent1.putExtra("uri", modelChats.get(position).getMsg());
                        context.startActivity(intent1);

                        break;
                }
            });

            reel_options = new BottomSheetDialog(holder.itemView.getContext());
            reel_options.setContentView(view);
        }
    }

    @Override
    public int getItemCount() {
        return modelChats.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final TextView seen;
        final TextView sender_name;
        final SocialTextView text;
        final ImageView media;
        final ImageView post_media;
        final ImageView play;
        final ImageView post_play;
        final CircleImageView dp;
        final VoicePlayerView voicePlayerView;
        final VoicePlayerView post_voicePlayerView;
        final RelativeLayout media_layout;
        final RelativeLayout doc;

        //Post
        final TextView name;
        final SocialTextView post_text;
        final CircleImageView avatar;
        final LinearLayout head;
        final LinearLayout post;

        //Call
        final LinearLayout call_layout;
        final LinearLayout main;
        ImageView call_img;
        final TextView call;

        //Reel
        final CardView reelView;
        final ImageView reelSource;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            media =  itemView.findViewById(R.id.media);
            play =  itemView.findViewById(R.id.play);
            doc  =  itemView.findViewById(R.id.doc);
            voicePlayerView =  itemView.findViewById(R.id.voicePlayerView);
            avatar  =  itemView.findViewById(R.id.avatar);
            name  =  itemView.findViewById(R.id.name);
            head  =  itemView.findViewById(R.id.head);
            post_media =  itemView.findViewById(R.id.post_media);
            post_play =  itemView.findViewById(R.id.post_play);
            post_voicePlayerView  =  itemView.findViewById(R.id.post_voicePlayerView);
            post_text  =  itemView.findViewById(R.id.post_text);
            seen   =  itemView.findViewById(R.id.seen);
            media_layout =  itemView.findViewById(R.id.media_layout);
            post =  itemView.findViewById(R.id.post);
            call_layout =  itemView.findViewById(R.id.call_layout);
            call_img =  itemView.findViewById(R.id.call_img);
            call_img =  itemView.findViewById(R.id.call_img);
            call  =  itemView.findViewById(R.id.call);
            main =  itemView.findViewById(R.id.main);
            sender_name =  itemView.findViewById(R.id.sender_name);
            dp =  itemView.findViewById(R.id.dp);
            reelView=  itemView.findViewById(R.id.reel);
            reelSource=  itemView.findViewById(R.id.reelSource);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (modelChats.get(position).getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

}

