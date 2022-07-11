package com.beesec.beechat2.adapter;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelReel;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.profile.UserProfileActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReportReelView extends RecyclerView.Adapter<AdapterReportReelView.AdapterReelHolder>{

    private final List<ModelReel> modelReels;
    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReportReelView(List<ModelReel> modelReels) {
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
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), v, Gravity.END);


                popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to user");
                popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from report");
                popupMenu.getMenu().add(Menu.NONE,3,0, "View user profile");
                popupMenu.getMenu().add(Menu.NONE,4,0, "Delete reel");

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == 1) {
                        FirebaseDatabase.getInstance().getReference("warn").child("user").child( modelReel.getId()).setValue(true);
                        //Notification
                        String timestamp = ""+System.currentTimeMillis();
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("pId", "");
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("pUid",  modelReel.getId());
                        hashMap.put("notification", "You have got a warning by the admin");
                        hashMap.put("sUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        FirebaseDatabase.getInstance().getReference("Users").child( modelReel.getId()).child("Notifications").child(timestamp).setValue(hashMap);
                        FirebaseDatabase.getInstance().getReference("Users").child( modelReel.getId()).child("Count").child(timestamp).setValue(true);
                        notify = true;
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ModelUser user = snapshot.getValue(ModelUser.class);
                                if (notify){
                                    sendNotification( modelReel.getId(), Objects.requireNonNull(user).getName(), "You have got a warning by the admin", itemView);
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                        Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                    }

                    if (id == 2) {
                        FirebaseDatabase.getInstance().getReference().child("ReportReel").child(modelReel.getpId()).getRef().removeValue();
                        Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                        ViewGroup.LayoutParams params = itemView.getLayoutParams();
                        params.height = 0;
                        itemView.setLayoutParams(params);
                    }

                    if (id == 3) {
                        Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
                        intent.putExtra("hisUID", modelReel.getId());
                        itemView.getContext().startActivity(intent);
                    }

                    if (id == 4){
                        FirebaseDatabase.getInstance().getReference("ReelViews").child(modelReel.getpId()).getRef().removeValue();
                        Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                        ViewGroup.LayoutParams params = itemView.getLayoutParams();
                        params.height = 0;
                        itemView.setLayoutParams(params);
                        notify = true;
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ModelUser user = snapshot.getValue(ModelUser.class);
                                if (notify){
                                    sendNotification( modelReel.getId(), Objects.requireNonNull(user).getName(), "Your reel has been deleted", itemView);
                                }
                                notify = false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    return false;
                });
                popupMenu.show();
            });

        }

    }

    private void sendNotification(final String hisId, final String name, final String message, View itemView){

       /* if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().isEmpty()){
            String username = itemView.getContext().getResources().getString(R.string.your_email);
            String password = itemView.getContext().getResources().getString(R.string.your_password);
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
                        String name = snapshot.child("name").getValue().toString();

                        try {
                            Message message1 = new MimeMessage(session);
                            message1.setFrom(new InternetAddress(username));
                            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                            message1.setSubject("New Message - "+ itemView.getContext().getResources().getString(R.string.app_name));
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

        }

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
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " " + message, "Warning", hisId, "profile", R.drawable.logo);
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

}
