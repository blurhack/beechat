package com.beesec.beechat2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelLive;
import com.beesec.beechat2.podcast.GoPodacstAudienceActivity;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ALL")
public class AdapterPodcast extends RecyclerView.Adapter<AdapterPodcast.MyHolder>{

    final Context context;
    final List<ModelLive> modelLives;
    String mUsername;

    int channelProfile;
    public static final String channelMessage = "com.beesec.beechat2.CHANNEL";
    public static final String profileMessage = "com.beesec.beechat2.PROFILE";

    public AdapterPodcast(Context context, List<ModelLive> modelLives) {
        this.context = context;
        this.modelLives = modelLives;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.podcast_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressWarnings("Convert2Lambda")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String hisUID = modelLives.get(position).getUserid();
        String room = modelLives.get(position).getRoom();

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsername = snapshot.child("username").getValue().toString();
                String dp = snapshot.child("photo").getValue().toString();
                if (!dp.isEmpty()){
                    Glide.with(context).asBitmap().load(dp).into(holder.live_photo);
                }else {
                    Glide.with(context).asBitmap().load(R.drawable.avatar).into(holder.live_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference().child("Podcast").child(room).child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("ChatId", timeStamp);
                hashMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("msg", mUsername + " has joined");
                FirebaseDatabase.getInstance().getReference().child("Podcast").child(room).child("Chats").child(timeStamp).setValue(hashMap);

                Intent intent = new Intent(context, GoPodacstAudienceActivity.class);
                intent.putExtra("room", room);
                intent.putExtra("type", "aud");
                context.startActivity(intent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return modelLives.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        RoundedImageView live_photo;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            live_photo = itemView.findViewById(R.id.roundedImageView);
        }
    }
}
