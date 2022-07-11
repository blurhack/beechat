package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelReel;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.beesec.beechat2.reel.ReelActivity;
import com.beesec.beechat2.reel.ReelCommentActivity;
import com.beesec.beechat2.search.SearchActivity;
import com.beesec.beechat2.send.SendToGroupActivity;
import com.beesec.beechat2.send.SendToUserActivity;
import com.beesec.beechat2.who.ReelLikedActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

@SuppressWarnings("ALL")
public class AdapterReel extends RecyclerView.Adapter<AdapterReel.AdapterReelHolder>{

    private final List<ModelReel> modelReels;

    BottomSheetDialog share_options,reel_options;
    LinearLayout app,groups,users,download,save,delete,copy,report,liked;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReel(List<ModelReel> modelReels) {
        this.modelReels = modelReels;
    }

    @NonNull
    @Override
    public AdapterReelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterReelHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterReelHolder holder, int position) {
        holder.setVideoData(modelReels.get(position));
        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ReelCommentActivity.class);
            intent.putExtra("item", String.valueOf(position));
            intent.putExtra("id", modelReels.get(position).getpId());
            intent.putExtra("type", ReelActivity.getType());
            intent.putExtra("his", modelReels.get(position).getId());
            holder.itemView.getContext().startActivity(intent);
        });

        holder.share.setOnClickListener(v1 -> {
            share_bottom(holder, position);
            share_options.show();
        });

        holder.more.setOnClickListener(v1 -> {
            more_bottom(holder, position);
            reel_options.show();
        });

        requestQueue = Volley.newRequestQueue(holder.itemView.getContext());

    }

    private void share_bottom(AdapterReelHolder holder, int position) {
        if (share_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.share_options, null);
            app = view.findViewById(R.id.app);
            groups = view.findViewById(R.id.groups);
            users = view.findViewById(R.id.users);

            app.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, modelReels.get(position).getText() + " " + modelReels.get(position).getVideo());
                holder.itemView.getContext().startActivity(Intent.createChooser(intent, "Share Via"));
            });

            groups.setOnClickListener(v -> {
                Intent intent = new Intent( holder.itemView.getContext(), SendToGroupActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", modelReels.get(position).getVideo());
                holder.itemView.getContext().startActivity(intent);
            });

            users.setOnClickListener(v -> {
                Intent intent = new Intent( holder.itemView.getContext(), SendToUserActivity.class);
                intent.putExtra("type", "reel");
                intent.putExtra("uri", modelReels.get(position).getVideo());
                holder.itemView.getContext().startActivity(intent);
            });

            share_options = new BottomSheetDialog(holder.itemView.getContext());
            share_options.setContentView(view);
        }
    }

    private void more_bottom(AdapterReelHolder holder, int position) {
        if (reel_options == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.reel_options, null);
            delete = view.findViewById(R.id.delete);
            download = view.findViewById(R.id.download);
            save = view.findViewById(R.id.save);
            copy = view.findViewById(R.id.copy);
            report = view.findViewById(R.id.report);
            liked = view.findViewById(R.id.liked);

            if (!modelReels.get(position).getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                delete.setVisibility(View.GONE);
            }else {
                report.setVisibility(View.GONE);
            }

            liked.setOnClickListener(v -> {
                reel_options.cancel();
                Intent intent = new Intent(holder.itemView.getContext(), ReelLikedActivity.class);
                intent.putExtra("reelId", modelReels.get(position).getpId());
                holder.itemView.getContext().startActivity(intent);
            });

            view.findViewById(R.id.share).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
                intent.putExtra(Intent.EXTRA_TEXT, " \nWatch the reels "+"www.app.myfriend.com/reel/"+modelReels.get(position).getpId());
                holder.itemView.getContext().startActivity(Intent.createChooser(intent, "Share Via"));
            });

            delete.setOnClickListener(v -> {
                reel_options.cancel();
                FirebaseStorage.getInstance().getReferenceFromUrl(modelReels.get(position).getVideo()).delete().addOnCompleteListener(task -> {
                    FirebaseDatabase.getInstance().getReference().child("Reels").child(modelReels.get(position).getpId()).getRef().removeValue();
                    Snackbar.make(holder.itemView,"Deleted", Snackbar.LENGTH_LONG).show();
                });
            });

            save.setOnClickListener(v -> {
                reel_options.cancel();
                FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(modelReels.get(position).getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(modelReels.get(position).getpId()).removeValue();
                            Snackbar.make(holder.itemView,"Unsaved", Snackbar.LENGTH_LONG).show();
                        }else{
                            FirebaseDatabase.getInstance().getReference().child("SavesReel").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(modelReels.get(position).getpId()).setValue(true);
                            Snackbar.make(holder.itemView,"Saved", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(holder.itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

            });

            download.setOnClickListener(v -> {
                reel_options.cancel();
                Snackbar.make(holder.itemView,"Please wait downloading", Snackbar.LENGTH_LONG).show();
                DownloadManager downloadManager = (DownloadManager) v.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelReels.get(position).getVideo()));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(v.getContext(), DIRECTORY_DOWNLOADS, String.valueOf(System.currentTimeMillis()) + ".mp4");
                Objects.requireNonNull(downloadManager).enqueue(request);
            });

            copy.setOnClickListener(v -> {
                reel_options.cancel();
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", modelReels.get(position).getText() + " " + modelReels.get(position).getVideo());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(holder.itemView,"Copied", Snackbar.LENGTH_LONG).show();

            });

            report.setOnClickListener(v -> {
                reel_options.cancel();
                FirebaseDatabase.getInstance().getReference().child("ReportReel").child(modelReels.get(position).getpId()).setValue(true);
                Snackbar.make(holder.itemView,"Reported", Snackbar.LENGTH_LONG).show();
            });

            reel_options = new BottomSheetDialog(holder.itemView.getContext());
            reel_options.setContentView(view);
        }
    }


    @Override
    public int getItemCount() {
        return modelReels.size();
    }

    class AdapterReelHolder extends RecyclerView.ViewHolder{

        final VideoView videoView;
        final LinearLayout like;
        final LinearLayout comment;
        final ImageView share;
        final CircleImageView avatar;
        final TextView name;
        final TextView textLike;
        final TextView textComment;
        final SocialTextView description;
        final ImageView like_img;
        final ImageView more;
        final TextView views;

        public AdapterReelHolder(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.videoView);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            avatar = itemView.findViewById(R.id.avatar);
            like_img  = itemView.findViewById(R.id.like_img);
            textLike = itemView.findViewById(R.id.textLike);
            textComment = itemView.findViewById(R.id.textComment);
            more = itemView.findViewById(R.id.more);
            views = itemView.findViewById(R.id.views);

        }

        void setVideoData(ModelReel modelReel){
            //TextView
            description.setLinkText(modelReel.getText());
            FirebaseDatabase.getInstance().getReference().child("Users").child(modelReel.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    name.setText("@"+snapshot.child("name").getValue().toString());
                    name.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                        intent.putExtra("hisUID", modelReel.getId());
                        v.getContext().startActivity(intent);
                    });
                    avatar.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), UserProfileActivity.class);
                        intent.putExtra("hisUID", modelReel.getId());
                        v.getContext().startActivity(intent);
                    });
                    if (!snapshot.child("photo").getValue().toString().isEmpty()){
                        Picasso.get().load(snapshot.child("photo").getValue().toString()).into(avatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

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

            //SocialText
            description.setOnLinkClickListener((i, s) -> {
                if (i == 1){

                    Intent intent = new Intent(itemView.getContext(), SearchActivity.class);
                    intent.putExtra("hashtag", s);
                    itemView.getContext().startActivity(intent);

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
                                    String id = ds.child("id").getValue().toString();
                                    if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        Snackbar.make(itemView,"It's you", Snackbar.LENGTH_LONG).show();
                                    }else {
                                        Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                                        intent.putExtra("hisUID", id);
                                        itemView.getContext().startActivity(intent);
                                    }
                                }
                            }else {
                                Snackbar.make(itemView,"Invalid username, can't find user with this username", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Snackbar.make(itemView,error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                else if (i == 16){
                    if (!s.startsWith("https://") && !s.startsWith("http://")){
                        s = "http://" + s;
                    }
                    Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                    itemView.getContext().startActivity(openUrlIntent);
                }else if (i == 4){
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                    itemView.getContext().startActivity(intent);
                }else if (i == 8){
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, s);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "");
                    itemView.getContext().startActivity(intent);

                }
            });

            //Likes
            like.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(modelReel.getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(modelReel.getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(modelReel.getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                            addToHisNotification(modelReel.getId(), "Liked on your reel", modelReel.getpId());
                            notify = true;
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUser user = snapshot.getValue(ModelUser.class);
                                    if (notify){
                                        sendNotification(modelReel.getId(), Objects.requireNonNull(user).getName(), "liked on your reel", itemView.getContext());
                                    }
                                    notify = false;
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
            });
            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(modelReel.getpId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        like_img.setImageResource(R.drawable.ic_liked_reel);
                    }else {
                        like_img.setImageResource(R.drawable.ic_reel_like);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            FirebaseDatabase.getInstance().getReference().child("ReelsLike").child(modelReel.getpId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if (snapshot.exists()){
                       textLike.setText(String.valueOf(snapshot.getChildrenCount()));
                       textLike.setVisibility(View.VISIBLE);
                   }else {
                       textLike.setVisibility(View.GONE);
                   }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Comment
            FirebaseDatabase.getInstance().getReference("Reels").child(modelReel.getpId()).child("Comment").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        textComment.setText(String.valueOf(snapshot.getChildrenCount()));
                        textComment.setVisibility(View.VISIBLE);
                    }else {
                        textComment.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Video
            videoView.setVideoPath(modelReel.getVideo());
            videoView.setOnPreparedListener(mp -> {
                itemView.findViewById(R.id.pb).setVisibility(View.GONE);
                mp.start();
                mp.setLooping(true);
                FirebaseDatabase.getInstance().getReference("ReelViews").child(modelReel.getpId()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
            });


        }

    }

    private void sendNotification(final String hisId, final String name, final String message, Context context){

       /* String username = context.getResources().getString(R.string.your_email);
        String password = context.getResources().getString(R.string.your_password);
        String messageToSend = message;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator(){
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication(){
                        return new PasswordAuthentication(username, password);
                    }
                });

        FirebaseDatabase.getInstance().getReference().child("Users").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child("email").getValue().toString().isEmpty()){
                    String em = snapshot.child("email").getValue().toString();

                    try {
                        Message message1 = new MimeMessage(session);
                        message1.setFrom(new InternetAddress(username));
                        message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                        message1.setSubject("New Message - "+context.getResources().getString(R.string.app_name));
                        message1.setText(name + " " +messageToSend);
                        Transport.send(message1);
                    }catch (MessagingException e){
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        */

        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "New Message", hisId, "profile", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAoAVZ-Vk:APA91bH7bjTYlktpJ53F9XkNPbmnUMw-csCIbocmKGKPGRPzvBAYXQ0S0XGxP3bVAylQmM6nOW9iOLPz18jUy8GrtA4OlPSe5XffhxnHd9cKlZD6XPbc9IZ7RePBvbAU-CPJ2v7_yybJ");
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addToHisNotification(String hisUid, String message, String reelId){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", reelId);
        hashMap.put("type", "reel");
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", message);
        hashMap.put("sUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Count").child(timestamp).setValue(true);
    }

}
