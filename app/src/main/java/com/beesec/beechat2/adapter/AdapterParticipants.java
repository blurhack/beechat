package com.beesec.beechat2.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

@SuppressWarnings("ALL")
public class AdapterParticipants extends RecyclerView.Adapter<AdapterParticipants.HolderParticipantsAdd>{

    private final Context context;
    private final List<ModelUser> userList;
    private final String groupId;
    private final String myGroupRole;


    private RequestQueue requestQueue;
    private boolean notify = false;

    public AdapterParticipants(Context context, List<ModelUser> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.user_list, parent, false);
        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, int position) {

        requestQueue = Volley.newRequestQueue(context);

        ModelUser modelUser = userList.get(position);
        String mName = modelUser.getName();
        String mUsername = modelUser.getUsername();
        String dp = modelUser.getPhoto();
        String uid = modelUser.getId();

        holder.name.setText(mName);

        if (userList.get(position).getVerified().equals("yes"))  holder.verified.setVisibility(View.VISIBLE);

        try {
            Picasso.get().load(dp).placeholder(R.drawable.avatar).into(holder.circleImageView);

        }catch (Exception e){
            Picasso.get().load(R.drawable.avatar).into(holder.circleImageView);
        }
        checkAlreadyExists(modelUser, holder,mUsername);
        holder.itemView.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).child("Participants").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String hisPrevRole = ""+snapshot.child("role").getValue();
                                String[] options;
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Choose Option");
                                if (myGroupRole.equals("creator")){
                                    if (hisPrevRole.equals("admin")){
                                        options = new String[]{"Remove Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                removeAdmin(modelUser, holder);

                                            } else {
                                                removeParticipants(modelUser, holder);
                                            }
                                        }).show();
                                    }
                                    else if (hisPrevRole.equals("participant"))
                                    {
                                        options = new String[]{"Make Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                makeAdmin(modelUser, holder);

                                            } else {
                                                removeParticipants(modelUser, holder);
                                            }
                                        }).show();

                                    }
                                }
                                else if (myGroupRole.equals("admin")){
                                    switch (hisPrevRole) {
                                        case "creator":
                                            Snackbar.make(holder.itemView, "Creator of the group", Snackbar.LENGTH_LONG).show();
                                            break;
                                        case "admin":
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    removeAdmin(modelUser, holder);

                                                } else {
                                                    removeParticipants(modelUser, holder);
                                                }
                                            }).show();
                                            break;
                                        case "participant":
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    makeAdmin(modelUser, holder);

                                                } else {
                                                    removeParticipants(modelUser, holder);
                                                }
                                            }).show();
                                            break;
                                    }
                                }
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Add Participant")
                                        .setMessage("Add this user in this group?")
                                        .setPositiveButton("Add", (dialog, which) -> addParticipants(modelUser, holder)).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        });
        holder.username.setText(mUsername);
    }

    private void addParticipants(ModelUser modelUser, HolderParticipantsAdd holder) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", modelUser.getId());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).setValue(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show());
        notify = true;
        FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (notify){
                    sendNotification(modelUser.getId(), Objects.requireNonNull(user).getName(), "added you to group");
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void makeAdmin(ModelUser modelUser, HolderParticipantsAdd holder) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Admin made", Toast.LENGTH_SHORT).show());

        notify = true;
        FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (notify){
                    sendNotification(modelUser.getId(), Objects.requireNonNull(user).getName(), "Made you admin");
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void removeParticipants(ModelUser modelUser, HolderParticipantsAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User removed from the group", Toast.LENGTH_SHORT).show());
    }

    private void removeAdmin(ModelUser modelUser, HolderParticipantsAdd holder) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Admin removed", Toast.LENGTH_SHORT).show());
    }

    private void checkAlreadyExists(ModelUser modelUser, HolderParticipantsAdd holder, String mUsername) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.username.setText(mUsername + " - " +hisRole);
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

    static class HolderParticipantsAdd extends RecyclerView.ViewHolder{

        private final CircleImageView circleImageView;
        private final TextView name;
        private final TextView username;
        private final ImageView verified;

        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            verified  = itemView.findViewById(R.id.verified);

        }
    }


    private void sendNotification(final String hisId, final String name,final String message){

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

}
