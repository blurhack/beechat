package com.beesec.beechat2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelLiveChat;


import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterLiveChat extends RecyclerView.Adapter<AdapterLiveChat.MyHolder>{

    final Context context;
    final List<ModelLiveChat> modelLiveChats;

    public AdapterLiveChat(Context context, List<ModelLiveChat> modelLiveChats) {
        this.context = context;
        this.modelLiveChats = modelLiveChats;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.live_chat, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String hisUID = modelLiveChats.get(position).getUserId();
        String message = modelLiveChats.get(position).getMsg();

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                String dp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();
                String mVerified = Objects.requireNonNull(snapshot.child("verified").getValue()).toString();

                if (!dp.isEmpty()){
                    Glide.with(context).asBitmap().load(dp).into(holder.live_photo);
                }else {
                    Glide.with(context).asBitmap().load(R.drawable.avatar).into(holder.live_photo);
                }
                holder.story_username.setText(mUsername);
                if (mVerified.isEmpty()){
                    holder.verify.setVisibility(View.GONE);
                }else {
                    holder.verify.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.msg.setText(message);

    }


    @Override
    public int getItemCount() {
        return modelLiveChats.size();
    }

    @SuppressWarnings("CanBeFinal")
    static class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView live_photo;
        TextView story_username,msg;
        ImageView verify;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            live_photo = itemView.findViewById(R.id.dp);
            story_username = itemView.findViewById(R.id.username);
            msg = itemView.findViewById(R.id.msg);
            verify = itemView.findViewById(R.id.verify);
        }
    }
}
