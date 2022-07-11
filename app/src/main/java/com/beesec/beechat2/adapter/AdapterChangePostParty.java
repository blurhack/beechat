package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.GetTimeAgo;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelPost;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.watchParty.ChangeWatchPartyActivity;
import com.beesec.beechat2.watchParty.StartPartyActivity;
import com.beesec.beechat2.watchParty.StartYouTubeActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterChangePostParty extends RecyclerView.Adapter<AdapterChangePostParty.MyHolder>{

    final Context context;
    final List<ModelPost> modelPosts;

    public AdapterChangePostParty(Context context, List<ModelPost> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.post_list_party, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(modelPosts.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()) Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.dp);
                holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                holder.username.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());

                //SetOnClick
                holder.dp.setOnClickListener(v -> {
                    if (!modelPosts.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelPosts.get(position).getId());
                        context.startActivity(intent);
                    }else {
                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                    }
                });
                holder.name.setOnClickListener(v -> {
                    if (!modelPosts.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelPosts.get(position).getId());
                        context.startActivity(intent);
                    }else {
                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                    }
                });
                holder.username.setOnClickListener(v -> {
                    if (!modelPosts.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelPosts.get(position).getId());
                        context.startActivity(intent);
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
        long lastTime = Long.parseLong(modelPosts.get(position).getpTime());
        holder.time.setText(GetTimeAgo.getTimeAgo(lastTime));

        //Extra
        FirebaseDatabase.getInstance().getReference("postExtra").child(modelPosts.get(position).getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    if (!Objects.requireNonNull(snapshot.child("location").getValue()).toString().isEmpty()) //noinspection UnnecessaryToStringCall
                        holder.location.setText(" . " + Objects.requireNonNull(snapshot.child("location").getValue()).toString());

                    if (!Objects.requireNonNull(snapshot.child("feeling").getValue()).toString().isEmpty()) //noinspection UnnecessaryToStringCall
                        holder.feeling.setText(" - " + Objects.requireNonNull(snapshot.child("feeling").getValue()).toString());

                    if(!Objects.requireNonNull(snapshot.child("feeling").getValue()).toString().isEmpty()){
                        String mFeeling = Objects.requireNonNull(snapshot.child("feeling").getValue()).toString();
                        if (mFeeling.contains("Traveling")){
                            holder.activity.setImageResource(R.drawable.airplane);
                        }else if (mFeeling.contains("Watching")){
                            holder.activity.setImageResource(R.drawable.watching);
                        }else if (mFeeling.contains("Listening")){
                            holder.activity.setImageResource(R.drawable.listening);
                        }else if (mFeeling.contains("Thinking")){
                            holder.activity.setImageResource(R.drawable.thinking);
                        }else if (mFeeling.contains("Celebrating")){
                            holder.activity.setImageResource(R.drawable.celebration);
                        }else if (mFeeling.contains("Looking")){
                            holder.activity.setImageResource(R.drawable.looking);
                        }else if (mFeeling.contains("Playing")){
                            holder.activity.setImageResource(R.drawable.playing);
                        }else if (mFeeling.contains("happy")){
                            holder.activity.setImageResource(R.drawable.smiling);
                        } else if (mFeeling.contains("loved")){
                            holder.activity.setImageResource(R.drawable.love);
                        } else if (mFeeling.contains("sad")){
                            holder.activity.setImageResource(R.drawable.sad);
                        }else if (mFeeling.contains("crying")){
                            holder.activity.setImageResource(R.drawable.crying);
                        }else if (mFeeling.contains("angry")){
                            holder.activity.setImageResource(R.drawable.angry);
                        }else if (mFeeling.contains("confused")){
                            holder.activity.setImageResource(R.drawable.confused);
                        }else if (mFeeling.contains("broken")){
                            holder.activity.setImageResource(R.drawable.broken);
                        }else if (mFeeling.contains("cool")){
                            holder.activity.setImageResource(R.drawable.cool);
                        }else if (mFeeling.contains("funny")){
                            holder.activity.setImageResource(R.drawable.joy);
                        }else if (mFeeling.contains("tired")){
                            holder.activity.setImageResource(R.drawable.tired);
                        }else if (mFeeling.contains("shock")){
                            holder.activity.setImageResource(R.drawable.shocked);
                        }else if (mFeeling.contains("love")){
                            holder.activity.setImageResource(R.drawable.heart);
                        }else if (mFeeling.contains("sleepy")){
                            holder.activity.setImageResource(R.drawable.sleeping);
                        }else if (mFeeling.contains("expressionless")){
                            holder.activity.setImageResource(R.drawable.muted);
                        }else if (mFeeling.contains("blessed")){
                            holder.activity.setImageResource(R.drawable.angel);
                        }
                    }

                    if (Objects.requireNonNull(snapshot.child("privacy").getValue()).toString().equals("No one")){
                        if (!modelPosts.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                            params.height = 0;
                            holder.itemView.setLayoutParams(params);
                        }

                    }else if (Objects.requireNonNull(snapshot.child("privacy").getValue()).toString().equals("Followers")){
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(modelPosts.get(position).getId())
                                .child("Followers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) &&  !modelPosts.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                    params.height = 0;
                                    holder.itemView.setLayoutParams(params);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //PostDetails
        String type = modelPosts.get(position).getType();
        if (!type.equals("bg")){
            holder.text.setLinkText(modelPosts.get(position).getText());
        }

        if (type.equals("video")){
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.play.setVisibility(View.VISIBLE);
            Glide.with(context).asBitmap().load(modelPosts.get(position).getVine()).thumbnail(0.1f).into(holder.mediaView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (type.equals("video")){

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("link", modelPosts.get(position).getVine());
                hashMap.put("type", "video");

                FirebaseDatabase.getInstance().getReference().child("Party").child(ChangeWatchPartyActivity.getId()).updateChildren(hashMap);
                FirebaseDatabase.getInstance().getReference().child("Party").child(ChangeWatchPartyActivity.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String timeStamp = ""+System.currentTimeMillis();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("ChatId", timeStamp);
                        hashMap.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        hashMap.put("msg", "Started a new video");
                        FirebaseDatabase.getInstance().getReference().child("Party").child(ChangeWatchPartyActivity.getId()).child("Chats").child(timeStamp).setValue(hashMap);

                        if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")){
                            Intent intent = new Intent(context, StartYouTubeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", ChangeWatchPartyActivity.getId());
                            context.startActivity(intent);
                        }else {
                            Intent intent = new Intent(context, StartPartyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("room", ChangeWatchPartyActivity.getId());
                            context.startActivity(intent);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }


    @Override
    public int getItemCount() {
        return modelPosts.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView activity;
        final ImageView mediaView;
        final ImageView play;
        final ImageView like_img;
        final TextView name;
        final TextView username;
        final TextView time;
        final TextView feeling;
        final TextView location;
        final TextView like_text;
        final SocialTextView text;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            time = itemView.findViewById(R.id.time);
            activity = itemView.findViewById(R.id.activity);
            feeling = itemView.findViewById(R.id.feeling);
            location = itemView.findViewById(R.id.location);
            text = itemView.findViewById(R.id.text);
            mediaView = itemView.findViewById(R.id.mediaView);
            play = itemView.findViewById(R.id.play);
            like_text =  itemView.findViewById(R.id.like_text);
            like_img  =  itemView.findViewById(R.id.like_img);
        }

    }
}
