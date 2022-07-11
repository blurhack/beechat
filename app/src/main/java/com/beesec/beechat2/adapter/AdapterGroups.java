package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.group.GroupProfileActivity;
import com.beesec.beechat2.model.ModelGroups;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroups extends RecyclerView.Adapter<AdapterGroups.MyHolder>{

    final Context context;
    final List<ModelGroups> userList;

    public AdapterGroups(Context context, List<ModelGroups> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.group_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.name.setText(userList.get(position).getgName());

        holder.username.setText(userList.get(position).getgUsername());

        if (userList.get(position).getgIcon().isEmpty()){
            Picasso.get().load(R.drawable.group).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getgIcon()).into(holder.dp);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupProfileActivity.class);
            intent.putExtra("group", userList.get(position).getGroupId());
            intent.putExtra("type", "");
            context.startActivity(intent);
        });

        //Private
        FirebaseDatabase.getInstance().getReference("Groups").child(userList.get(position).getGroupId()).child("Privacy").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String privacy = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                    if (privacy.equals("private")){
                        FirebaseDatabase.getInstance().getReference().child("Groups").child(userList.get(position).getGroupId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists()){
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

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
        }

    }
}
