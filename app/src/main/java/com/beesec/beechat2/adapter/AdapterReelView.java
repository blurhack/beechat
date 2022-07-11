package com.beesec.beechat2.adapter;

import android.content.Intent;
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
import com.beesec.beechat2.model.ModelReel;
import com.beesec.beechat2.reel.ViewReelActivity;

import java.util.List;

@SuppressWarnings("ALL")
public class AdapterReelView extends RecyclerView.Adapter<AdapterReelView.AdapterReelHolder>{

    private final List<ModelReel> modelReels;

    public AdapterReelView(List<ModelReel> modelReels) {
        this.modelReels = modelReels;
    }

    @NonNull
    @Override
    public AdapterReelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterReelHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_post_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReelHolder holder, int position) {
        holder.setVideoData(modelReels.get(position));
    }


    @Override
    public int getItemCount() {
        return modelReels.size();
    }

    class AdapterReelHolder extends RecyclerView.ViewHolder{

        final ImageView video;
        final TextView views;

        public AdapterReelHolder(@NonNull View itemView) {
            super(itemView);

            video = itemView.findViewById(R.id.image);
            views = itemView.findViewById(R.id.views);

        }

        void setVideoData(ModelReel modelReel){

            //Views
            FirebaseDatabase.getInstance().getReference("ReelViews").child(modelReel.getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        views.setVisibility(View.VISIBLE);
                        views.setText(String.valueOf(snapshot.getChildrenCount()));
                    }else {
                        views.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Video
            Glide.with(itemView.getContext()).asBitmap().load(modelReel.getVideo()).thumbnail(0.1f).into(video);

            //Click

            video.setOnClickListener(v -> {

                Intent intent = new Intent(itemView.getContext(), ViewReelActivity.class);
                intent.putExtra("id", modelReel.getVideo());
                itemView.getContext().startActivity(intent);

            });

        }

    }

}
