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
import com.beesec.beechat2.model.ModelHigh;
import com.beesec.beechat2.story.HighViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterHigh extends RecyclerView.Adapter<AdapterHigh.MyHolder>{

    final Context context;
    final List<ModelHigh> modelHighs;

    public AdapterHigh(Context context, List<ModelHigh> modelHighs) {
        this.context = context;
        this.modelHighs = modelHighs;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.story_list, parent, false);
        return new MyHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        seenStory(holder, modelHighs.get(position).getUserid());

        FirebaseDatabase.getInstance().getReference("Users").child(modelHighs.get(position).getUserid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()){
                    Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (modelHighs.get(position).type.equals("image")){
            Picasso.get().load(modelHighs.get(position).uri).into(holder.story_photo);
            Picasso.get().load(modelHighs.get(position).uri).into(holder.story_photo_seen);
        }else {
            Glide.with(context).load(modelHighs.get(position).uri).thumbnail(0.1f).into(holder.story_photo);
            Glide.with(context).load(modelHighs.get(position).uri).thumbnail(0.1f).into(holder.story_photo_seen);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HighViewActivity.class);
            intent.putExtra("userid", modelHighs.get(position).getUserid());
            intent.putExtra("story", modelHighs.get(position).getStoryid());
            context.startActivity(intent);
        });

    }

    private void seenStory(MyHolder holder, String userId) {
        FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    if (!snapshot1.child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists()){
                        i++;
                    }
                }
                if (i > 0){
                    holder.story_photo.setVisibility(View.VISIBLE);
                    holder.story_photo_seen.setVisibility(View.GONE);
                }else {
                    holder.story_photo.setVisibility(View.GONE);
                    holder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return modelHighs.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        public final RoundedImageView story_photo;
        public final RoundedImageView story_photo_seen;
        final CircleImageView dp;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.roundedImageView);
            story_photo_seen = itemView.findViewById(R.id.seen);
            dp =  itemView.findViewById(R.id.dp);
        }
    }
}
