package com.beesec.beechat2.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelPartyChat;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterPartyChat extends RecyclerView.Adapter<AdapterPartyChat.MyHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    final Context context;
    final List<ModelPartyChat> modelPartyChat;

    public AdapterPartyChat(Context context, List<ModelPartyChat> modelPartyChat) {
        this.context = context;
        this.modelPartyChat = modelPartyChat;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.party_chat_right, parent, false);

            return new MyHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.party_chat_left, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String hisUID = modelPartyChat.get(position).getUserId();
        String message = modelPartyChat.get(position).getMsg();

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mUsername = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                String dp = Objects.requireNonNull(snapshot.child("photo").getValue()).toString();

                if (!dp.isEmpty()){
                    Glide.with(context).asBitmap().load(dp).into(holder.live_photo);
                }else {
                    Glide.with(context).asBitmap().load(R.drawable.avatar).into(holder.live_photo);
                }
                holder.story_username.setText(mUsername);
             
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.msg.setText(message);

    }


    @Override
    public int getItemCount() {
        return modelPartyChat.size();
    }


    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView live_photo;
        final TextView story_username;
        final TextView msg;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            live_photo = itemView.findViewById(R.id.dp);
            story_username = itemView.findViewById(R.id.username);
            msg = itemView.findViewById(R.id.msg);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (modelPartyChat.get(position).getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

}
