package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.GetTimeAgo;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelCommentReel;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings({"ALL", "unused"})
public class AdapterCommentReel extends RecyclerView.Adapter<AdapterCommentReel.MyHolder>{

    final Context context;
    final List<ModelCommentReel> modelComments;
    NightMode nightMode;

    @SuppressWarnings("unused")
    public AdapterCommentReel(Context context, List<ModelCommentReel> modelComments) {
        this.context = context;
        this.modelComments = modelComments;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        nightMode = new NightMode(context);
        if (nightMode.loadNightModeState().equals("night")){
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list_night, parent, false);   return new MyHolder(view);
        }else if (nightMode.loadNightModeState().equals("dim")){
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list_dim, parent, false);   return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.comment_list, parent, false);   return new MyHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(modelComments.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()) Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.dp);
                if (Objects.requireNonNull(snapshot.child("verified").getValue()).toString().equals("yes")) holder.verified.setVisibility(View.VISIBLE);

                //SetOnClick
                holder.dp.setOnClickListener(v -> {
                    if (!modelComments.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelComments.get(position).getId());
                        context.startActivity(intent);
                    }else {
                        Snackbar.make(v,"It's you",Snackbar.LENGTH_LONG).show();
                    }
                });
                holder.name.setOnClickListener(v -> {
                    if (!modelComments.get(position).getId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("hisUID", modelComments.get(position).getId());
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

        //Comment
        holder.comment.setVisibility(View.VISIBLE);

        holder.comment.setLinkText(modelComments.get(position).getComment());
        holder.comment.setOnLinkClickListener((i, s) -> {
            if (i == 1){

                Intent intent = new Intent(context, SearchActivity.class);
                intent.putExtra("hashtag", s);
                context.startActivity(intent);
            }else
            if (i == 2){
                String username = s.replaceFirst("@","");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                Query query = ref.orderByChild("username").equalTo(username.trim());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String id = Objects.requireNonNull(ds.child("id").getValue()).toString();
                                if (id.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                                    Snackbar.make(holder.itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                }else {
                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra("hisUID", id);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                            }
                        }else {
                            Snackbar.make(holder.itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            else if (i == 16){
                if (!s.startsWith("https://") && !s.startsWith("http://")){
                    s = "http://" + s;
                }
                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                context.startActivity(openUrlIntent);
            }else if (i == 4){
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                context.startActivity(intent);
            }else if (i == 8){
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, s);
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                context.startActivity(intent);

            }
        });

        //More
        Context moreWrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
        PopupMenu morePop = new PopupMenu(moreWrapper, holder.more);
        morePop.getMenu().add(Menu.NONE,1,1, "Report");
        morePop.getMenu().add(Menu.NONE,2,2, "Delete");
        morePop.getMenu().add(Menu.NONE,4,4, "Copy");
        morePop.setOnMenuItemClickListener(item -> {
           if (item.getItemId() == 4){
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", modelComments.get(position).getComment());
                clipboard.setPrimaryClip(clip);
            }else if (item.getItemId() == 1){
                FirebaseDatabase.getInstance().getReference().child("ReportComment").child(modelComments.get(position).getcId()).setValue(true);
                Snackbar.make(holder.itemView,"Reported", Snackbar.LENGTH_LONG).show();
            }else  if (item.getItemId() == 2){
               FirebaseDatabase.getInstance().getReference().child("Reels").child(modelComments.get(position).getpId()).child("Comment").child(modelComments.get(position).getcId()).getRef().removeValue();
               Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
            }
            return false;
        });
        holder.more.setOnClickListener(v -> morePop.show());

        //Time
        long lastTime = Long.parseLong(modelComments.get(position).getTimestamp());
        holder.time.setText(GetTimeAgo.getTimeAgo(lastTime));

        //CheckLikes
        FirebaseDatabase.getInstance().getReference().child("cLikes").child(modelComments.get(position).getcId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.likeText.setVisibility(View.VISIBLE);
                    holder.noLikes.setText(String.valueOf(snapshot.getChildrenCount()));
                }else {
                    holder.likeText.setVisibility(View.GONE);
                    holder.noLikes.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Like
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(new int[]{
                        R.drawable.ic_thumb,
                        R.drawable.ic_love,
                        R.drawable.ic_laugh,
                        R.drawable.ic_wow,
                        R.drawable.ic_sad,
                        R.drawable.ic_angry
                })
                .withPopupAlpha(1)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (position1) -> {

            if (position1 == 0) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "like");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }else if (position1 == 1) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "love");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 2) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "laugh");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }      else if (position1 == 3) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "wow");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 4) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "sad");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
            else if (position1 == 5) {
                FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child( modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        snapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("type", "angry");
                            FirebaseDatabase.getInstance().getReference().child("Reaction").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().setValue(hashMap);
                            FirebaseDatabase.getInstance().getReference().child("cLikes").child( modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }

            return true;
        });

        //LikeFunctions
        holder.like.setOnTouchListener(popup);
        FirebaseDatabase.getInstance().getReference().child("cLikes").child(modelComments.get(position).getcId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        holder.like.setVisibility(View.GONE);
                        holder.liked.setVisibility(View.VISIBLE);
                    }else {
                        holder.liked.setVisibility(View.GONE);
                        holder.like.setVisibility(View.VISIBLE);
                    }
                }else {
                    holder.liked.setVisibility(View.GONE);
                    holder.like.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.liked.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("cLikes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(modelComments.get(position).getcId()).hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                    FirebaseDatabase.getInstance().getReference().child("cLikes").child(modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }));

        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("like").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0) {
                    holder.thumb.setVisibility(View.VISIBLE);
                }else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(modelComments.get(position).getcId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                holder.thumb.setVisibility(View.VISIBLE);
                            }else {
                                holder.thumb.setVisibility(View.GONE);
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
        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("love").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    holder.love.setVisibility(View.VISIBLE);
                }else {
                    holder.love.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("wow").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    holder.wow.setVisibility(View.VISIBLE);
                }else {
                    holder.wow.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("angry").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    holder.angry.setVisibility(View.VISIBLE);
                }else {
                    holder.angry.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("laugh").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    holder.laugh.setVisibility(View.VISIBLE);
                }else {
                    holder.laugh.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("Reaction").child(modelComments.get(position).getcId()).orderByChild("type").equalTo("sad").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount()>0){
                    holder.sad.setVisibility(View.VISIBLE);
                }else {
                    holder.sad.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public int getItemCount() {
        return modelComments.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView time;
        final TextView like;
        final TextView liked;
        final TextView noLikes;
        final TextView likeText;
        final ImageView verified;
        final ImageView play;
        final ImageView more;
        final SocialTextView comment;
        final RelativeLayout media_layout;
        final ImageView media;
        final ImageView thumb;
        final ImageView love;
        final ImageView laugh;
        final ImageView wow;
        final ImageView angry;
        final ImageView sad;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            thumb  =  itemView.findViewById(R.id.thumb);
            love  =  itemView.findViewById(R.id.love);
            laugh  =  itemView.findViewById(R.id.laugh);
            wow  =  itemView.findViewById(R.id.wow);
            angry  =  itemView.findViewById(R.id.angry);
            sad =  itemView.findViewById(R.id.sad);
            liked = itemView.findViewById(R.id.liked);
            more = itemView.findViewById(R.id.more);
            like = itemView.findViewById(R.id.like);
            time = itemView.findViewById(R.id.time);
            media = itemView.findViewById(R.id.media);
            play = itemView.findViewById(R.id.play);
            media_layout = itemView.findViewById(R.id.media_layout);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            comment = itemView.findViewById(R.id.username);
            verified = itemView.findViewById(R.id.verified);
            likeText  = itemView.findViewById(R.id.likeText);
            noLikes  = itemView.findViewById(R.id.noLikes);
        }

    }
}
