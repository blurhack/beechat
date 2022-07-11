package com.beesec.beechat2.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdminActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        requestQueue = Volley.newRequestQueue(AdminActivity.this);

        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Id
        TextView userNo = findViewById(R.id.userNo);
        TextView postNo = findViewById(R.id.postNo);
        TextView groupsNo = findViewById(R.id.groupsNo);
        TextView reelNo = findViewById(R.id.reelNo);
        TextView onlineNo = findViewById(R.id.onlineNo);
        TextView sellNo = findViewById(R.id.sellNo);
        TextView liveNo = findViewById(R.id.liveNo);
        TextView podcastNo = findViewById(R.id.podcastNo);
        TextView partyNo = findViewById(R.id.partyNo);
        TextView meetNO = findViewById(R.id.meetNO);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.adSwitch);

        FirebaseDatabase.getInstance().getReference("Ads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                aSwitch.setChecked(Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("on"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            if (isChecked){
                hashMap.put("type", "on");
            }else {
                hashMap.put("type", "off");
            }
            FirebaseDatabase.getInstance().getReference("Ads").updateChildren(hashMap);
        });


        findViewById(R.id.remove).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, RemoveVerifiedActivity.class)));
        findViewById(R.id.reportProducts).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ReportedProductActivity.class)));
        findViewById(R.id.reportedUser).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ReportedUserActivity.class)));
        findViewById(R.id.reportGroup).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ReportedGroupsActivity.class)));
        findViewById(R.id.reportedPost).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ReportedPostActivity.class)));
        findViewById(R.id.reportReels).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ReportedReelsActivity.class)));
        findViewById(R.id.verification).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, VerificationRequestActivity.class)));
        findViewById(R.id.warnUser).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, WarnUserActivity.class)));
        findViewById(R.id.warnGroup).setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, WarnGroupActivity.class)));

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userNo.setText(String.valueOf(snapshot.child("Users").getChildrenCount()));
                postNo.setText(String.valueOf(snapshot.child("Posts").getChildrenCount()));
                groupsNo.setText(String.valueOf(snapshot.child("Groups").getChildrenCount()));
                reelNo.setText(String.valueOf(snapshot.child("Reels").getChildrenCount()));
                sellNo.setText(String.valueOf(snapshot.child("Product").getChildrenCount()));
                liveNo.setText(String.valueOf(snapshot.child("Live").getChildrenCount()));
                podcastNo.setText(String.valueOf(snapshot.child("Podcast").getChildrenCount()));
                partyNo.setText(String.valueOf(snapshot.child("Party").getChildrenCount()));
                meetNO.setText(String.valueOf(snapshot.child("Chats").getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("Users").orderByChild("status").equalTo("online").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onlineNo.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //announcement
        findViewById(R.id.announcement).setOnClickListener(v -> findViewById(R.id.extra).setVisibility(View.VISIBLE));

        findViewById(R.id.imageView4).setOnClickListener(v -> findViewById(R.id.extra).setVisibility(View.GONE));

        EditText email = findViewById(R.id.email);
        findViewById(R.id.login).setOnClickListener(v -> {
            if (email.getText().toString().isEmpty()){
                Snackbar.make(v, "Enter a message", Snackbar.LENGTH_SHORT).show();
            }else {
                notify = true;
                FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ModelUser user = snapshot.getValue(ModelUser.class);
                        if (notify){
                            FirebaseDatabase.getInstance().getReference("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()){
                                        sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), email.getText().toString());
                                        addToHisNotification(ds.getKey(), email.getText().toString());
                                        Toast.makeText(AdminActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                                        findViewById(R.id.extra).setVisibility(View.GONE);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        notify = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

    }

    private void addToHisNotification(String hisUid, String message){
        String timestamp = ""+System.currentTimeMillis();
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", message);
        hashMap.put("sUid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Notifications").child(timestamp).setValue(hashMap);
        FirebaseDatabase.getInstance().getReference("Users").child(hisUid).child("Count").child(timestamp).setValue(true);

        /*if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().isEmpty()){
            String username = getResources().getString(R.string.your_email);
            String password = getResources().getString(R.string.your_password);
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

            FirebaseDatabase.getInstance().getReference().child("Users").child(hisUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.child("email").getValue().toString().isEmpty()){
                        String em = snapshot.child("email").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();

                        try {
                            Message message1 = new MimeMessage(session);
                            message1.setFrom(new InternetAddress(username));
                            message1.setRecipients(Message.RecipientType.TO, InternetAddress.parse(em));
                            message1.setSubject("New Message - "+ getResources().getString(R.string.app_name));
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

    }

    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "Announcement", hisId, "profile", R.drawable.logo);
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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}