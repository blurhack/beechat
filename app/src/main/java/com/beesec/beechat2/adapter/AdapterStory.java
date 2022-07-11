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
import com.beesec.beechat2.model.ModelStory;
import com.beesec.beechat2.story.StoryViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterStory extends RecyclerView.Adapter<AdapterStory.ViewHolder> {

    private final Context context;
    private final List<ModelStory>storyList;

    public AdapterStory(Context context, List<ModelStory> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ModelStory story = storyList.get(position);
        userInfo(viewHolder, story.getUserid(), position);

        seenStory(viewHolder, story.getUserid());
        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryViewActivity.class);
            intent.putExtra("userid", story.getUserid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public final RoundedImageView story_photo;
        public final RoundedImageView story_photo_seen;
        final CircleImageView dp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo = itemView.findViewById(R.id.roundedImageView);
            story_photo_seen = itemView.findViewById(R.id.seen);
           dp =  itemView.findViewById(R.id.dp);
        }
    }

    private void userInfo (ViewHolder viewHolder, String userId, int pos){

        FirebaseDatabase.getInstance().getReference("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()){
                    Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(viewHolder.dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       FirebaseDatabase.getInstance().getReference("Cover").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                    String uri = Objects.requireNonNull(snapshot.child("uri").getValue()).toString();

                    if (type.equals("image")){
                        Glide.with(context).load(uri).into(viewHolder.story_photo);
                        Glide.with(context).load(uri).into(viewHolder.story_photo_seen);
                    }else if (type.equals("video")){
                        Glide.with(context).load(uri).thumbnail(0.1f).into(viewHolder.story_photo);
                        Glide.with(context).load(uri).thumbnail(0.1f).into(viewHolder.story_photo_seen);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenStory(ViewHolder viewHolder, String userId){
     FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
           if (!snapshot1.child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists() && System.currentTimeMillis() < Objects.requireNonNull(snapshot1.getValue(ModelStory.class)).getTimeend()){
               i++;
           }
                }
                if (i > 0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                }else {
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
