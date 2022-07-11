package com.beesec.beechat2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.group.JoinRequestActivity;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsersJoin extends RecyclerView.Adapter<AdapterUsersJoin.MyHolder>{

    final Context context;
    final List<ModelUser> userList;

    public AdapterUsersJoin(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list_join, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.name.setText(userList.get(position).getName());

        holder.username.setText(userList.get(position).getUsername());

        if (userList.get(position).getPhoto().isEmpty()){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getPhoto()).into(holder.dp);
        }

        if (userList.get(position).getVerified().equals("yes"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("hisUID", userList.get(position).getId());
            context.startActivity(intent);
        });

        holder.reject.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Groups").child(JoinRequestActivity.getGroup()).child("Request").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    snapshot.getRef().removeValue();
                    Toast.makeText(context, "User rejected", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        holder.accept.setOnClickListener(v -> {
            String timestamp = ""+System.currentTimeMillis();
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", userList.get(position).getId());
            hashMap.put("role", "participant");
            hashMap.put("timestamp", ""+timestamp);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(JoinRequestActivity.getGroup()).child("Participants").child(userList.get(position).getId()).setValue(hashMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show());
            FirebaseDatabase.getInstance().getReference().child("Groups").child(JoinRequestActivity.getGroup()).child("Request").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
         });

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Time
                if (Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("online")) holder.online.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView verified;
        final ImageView online;
        final TextView name;
        final TextView username;
        final Button accept;
        final Button reject;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            online = itemView.findViewById(R.id.imageView2);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);

        }

    }
}
