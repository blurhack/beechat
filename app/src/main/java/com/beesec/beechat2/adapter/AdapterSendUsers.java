package com.beesec.beechat2.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.send.SendToUserActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterSendUsers extends RecyclerView.Adapter<AdapterSendUsers.MyHolder>{

    final Context context;
    final List<ModelUser> userList;

    public AdapterSendUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    private RequestQueue requestQueue;
    private boolean notify = false;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(holder.itemView.getContext());

        holder.name.setText(userList.get(position).getName());

        holder.username.setText(userList.get(position).getUsername());

        if (userList.get(position).getPhoto().isEmpty()){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getPhoto()).into(holder.dp);
        }

        if (userList.get(position).getVerified().equals("yes"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            Snackbar.make(v, "Please wait sending...", Snackbar.LENGTH_LONG).show();
            switch (SendToUserActivity.getType()) {
                case "image": {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_photo/" + "" + System.currentTimeMillis());
                    storageReference.putFile(Uri.parse(SendToUserActivity.getUri())).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap.put("receiver", userList.get(position).getId());
                            hashMap.put("msg", downloadUri.toString());
                            hashMap.put("isSeen", false);
                            hashMap.put("timestamp", "" + System.currentTimeMillis());
                            hashMap.put("type", "image");
                            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                            Snackbar.make(v, "Sent", Snackbar.LENGTH_LONG).show();
                            notify = true;
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUser user = snapshot.getValue(ModelUser.class);
                                    if (notify) {
                                        sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "sent a image");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    });
                    break;
                }
                case "video": {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_video/" + "" + System.currentTimeMillis());
                    storageReference.putFile(Uri.parse(SendToUserActivity.getUri())).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            hashMap.put("receiver", userList.get(position).getId());
                            hashMap.put("msg", downloadUri.toString());
                            hashMap.put("isSeen", false);
                            hashMap.put("timestamp", "" + System.currentTimeMillis());
                            hashMap.put("type", "video");
                            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                            Snackbar.make(v, "Sent", Snackbar.LENGTH_LONG).show();
                            notify = true;
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelUser user = snapshot.getValue(ModelUser.class);
                                    if (notify) {
                                        sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "sent a video");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    });
                    break;
                }
                case "reel": {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("receiver", userList.get(position).getId());
                    hashMap.put("msg", SendToUserActivity.getUri());
                    hashMap.put("isSeen", false);
                    hashMap.put("timestamp", "" + System.currentTimeMillis());
                    hashMap.put("type", "reel");
                    FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                    Snackbar.make(v, "Sent", Snackbar.LENGTH_LONG).show();
                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify) {
                                sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "sent a reel");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    break;
                }
                case "post": {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("receiver", userList.get(position).getId());
                    hashMap.put("msg", SendToUserActivity.getUri());
                    hashMap.put("isSeen", false);
                    hashMap.put("timestamp", "" + System.currentTimeMillis());
                    hashMap.put("type", "post");
                    FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                    Snackbar.make(v, "Sent", Snackbar.LENGTH_LONG).show();
                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify) {
                                sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "sent a post");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    break;
                }
                case "meet": {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap.put("receiver", userList.get(position).getId());
                    hashMap.put("msg", "Meeting id " + SendToUserActivity.getUri());
                    hashMap.put("isSeen", false);
                    hashMap.put("timestamp", SendToUserActivity.getUri());
                    hashMap.put("type", "meet");
                    FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                    Snackbar.make(v, "Sent", Snackbar.LENGTH_LONG).show();
                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify) {
                                sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "sent meeting id");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    break;
                }
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
        final TextView name;
        final TextView username;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
        }

    }

    private void sendNotification(final String hisId, final String name,final String message){
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


}
