package com.beesec.beechat2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.model.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterVerificationAlready extends RecyclerView.Adapter<AdapterVerificationAlready.MyHolder>{

    final Context context;
    final List<ModelUser> userList;

    public AdapterVerificationAlready(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.verification_view_already, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.name.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                holder.username.setText(Objects.requireNonNull(snapshot.child("username").getValue()).toString());
                if (!Objects.requireNonNull(snapshot.child("photo").getValue()).toString().isEmpty()){
                    Picasso.get().load(Objects.requireNonNull(snapshot.child("photo").getValue()).toString()).into(holder.dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.remove.setOnClickListener(v -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("verified", "");
            FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getId()).updateChildren(hashMap);
            Snackbar.make(v, "Removed", Snackbar.LENGTH_LONG).show();
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
         /*   if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().isEmpty()){
                String username = context.getResources().getString(R.string.your_email);
                String password = context.getResources().getString(R.string.your_password);
                String messageToSend = "Your verified badge has been deleted";
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

                FirebaseDatabase.getInstance().getReference().child("Users").child(userList.get(position).getId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

         CircleImageView dp;
         TextView name;
         TextView username;
        Button remove;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            remove = itemView.findViewById(R.id.remove);

        }

    }
}
