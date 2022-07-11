package com.beesec.beechat2.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.beesec.beechat2.model.ModelUser;
import com.beesec.beechat2.notifications.Data;
import com.beesec.beechat2.notifications.Sender;
import com.beesec.beechat2.notifications.Token;
import com.beesec.beechat2.profile.UserProfileActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterReportUsers extends RecyclerView.Adapter<AdapterReportUsers.MyHolder>{

    final Context context;
    final List<ModelUser> userList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterReportUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        holder.name.setText(userList.get(position).getName());

        holder.username.setText(userList.get(position).getUsername());

        if (userList.get(position).getPhoto().isEmpty()){
            Picasso.get().load(R.drawable.avatar).into(holder.dp);
        }else {
            Picasso.get().load(userList.get(position).getPhoto()).into(holder.dp);
        }

        if (userList.get(position).getVerified().equals("yes"))  holder.verified.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v, Gravity.END);

            popupMenu.getMenu().add(Menu.NONE,1,0, "Send warning to user");
            popupMenu.getMenu().add(Menu.NONE,2,0, "Remove from report");
            popupMenu.getMenu().add(Menu.NONE,3,0, "View user profile");

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == 1) {
                    Snackbar.make(v, "Warning sent", Snackbar.LENGTH_LONG).show();
                    FirebaseDatabase.getInstance().getReference("warn").child("user").child(userList.get(position).getId()).setValue(true);
                    //Notification
                    String timestamp = ""+System.currentTimeMillis();
                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("pId", "");
                    hashMap.put("timestamp", timestamp);
                    hashMap.put("pUid", userList.get(position).getId());
                    hashMap.put("notification", "You have got a warning by the admin");
                    hashMap.put("sUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getId()).child("Notifications").child(timestamp).setValue(hashMap);
                    FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getId()).child("Count").child(timestamp).setValue(true);
                    notify = true;
                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ModelUser user = snapshot.getValue(ModelUser.class);
                            if (notify){
                                sendNotification(userList.get(position).getId(), Objects.requireNonNull(user).getName(), "You have got a warning by the admin");
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

                if (id == 2) {
                    FirebaseDatabase.getInstance().getReference().child("userReport").child(userList.get(position).getId()).getRef().removeValue();
                    Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
                    ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                }

                if (id == 3) {
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtra("hisUID", userList.get(position).getId());
                    context.startActivity(intent);
                }

                return false;
            });
            popupMenu.show();
        });

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Time
                if (snapshot.child("status").getValue().toString().equals("online")) holder.online.setVisibility(View.VISIBLE);

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

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            verified = itemView.findViewById(R.id.verified);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            online = itemView.findViewById(R.id.imageView2);
        }

    }

    private void sendNotification(final String hisId, final String name,final String message){

       /* if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().isEmpty()){
            String username = context.getResources().getString(R.string.your_email);
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
                        String name = snapshot.child("name").getValue().toString();

                        try {
                            Message message1 = new MimeMessage(session);
                            message1.setFrom(new InternetAddress(username));
                            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                            message1.setSubject("New Message - "+ context.getResources().getString(R.string.app_name));
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
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
