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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.chat.ChatActivity;
import com.beesec.beechat2.model.ModelChat;
import com.beesec.beechat2.model.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder>{

    final Context context;
    final List<ModelUser> modelChatLists;

    public AdapterChatList(Context context, List<ModelUser> modelChatLists) {
        this.context = context;
        this.modelChatLists = modelChatLists;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.chat_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(modelChatLists.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Time
                if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("online")) holder.online.setVisibility(View.VISIBLE);

                //Name
                holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());

                //DP
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.dp);

                //Verify
                if (Objects.requireNonNull(snapshot.child("verified").getValue()).toString().equals("yes")) holder.verified.setVisibility(View.VISIBLE);

                //Typing
                if (Objects.requireNonNull(snapshot.child("typingTo").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    holder.message.setText("Typing...");
                }else {
                    //LastMessage
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
                    reference.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            holder.message.setText("No Message");
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ModelChat chat = ds.getValue(ModelChat.class);
                                if (chat == null){
                                    continue;
                                }
                                String sender = chat.getSender();
                                String receiver = chat.getReceiver();
                                if(sender == null || receiver == null){
                                    continue;
                                }
                                if (chat.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && chat.getSender().equals(modelChatLists.get(position).getId()) || chat.getReceiver().equals(modelChatLists.get(position).getId()) && chat.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    switch (chat.getType()) {
                                        case "image":
                                            holder.message.setText("Sent a photo");
                                            break;
                                        case "video":
                                            holder.message.setText("Sent a video");
                                            break;
                                        case "post":
                                            holder.message.setText("Sent a post");
                                            break;
                                        case "gif":
                                            holder.message.setText("Sent a GIF");
                                            break;
                                        case "audio":
                                            holder.message.setText("Sent a audio");
                                        case "doc":
                                            holder.message.setText("Sent a document");
                                            break;
                                        case "location":
                                            holder.message.setText("Sent a location");
                                            break;
                                        case "party":
                                            holder.message.setText("Sent a party invitation");
                                            break;
                                        case "reel":
                                            holder.message.setText("Sent a reel");
                                            break;
                                        case "story":
                                        case "high":
                                            holder.message.setText("Sent a story");
                                            break;
                                        default:
                                            holder.message.setText(chat.getMsg());
                                            break;
                                    }
                                }
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
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("hisUID", modelChatLists.get(position).getId());
            context.startActivity(intent);
            holder.count.setVisibility(View.GONE);
            holder.count.setText("");
        });

        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()){

                    if (Objects.requireNonNull(ds.child("sender").getValue()).toString().equals(modelChatLists.get(position).getId()) && Objects.requireNonNull(ds.child("receiver").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        ModelChat post = ds.getValue(ModelChat.class);
                        assert post != null;
                        if (Boolean.parseBoolean(ds.child("isSeen").getValue().toString()) == false){
                            i++;
                        }
                    }
                }
                if (i != 0){
                    holder.count.setVisibility(View.VISIBLE);
                    holder.count.setText(""+i);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return modelChatLists.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView online;
        final TextView name;
        final TextView message;
        final TextView count;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            count = itemView.findViewById(R.id.count);
            online = itemView.findViewById(R.id.imageView2);
        }

    }
}
