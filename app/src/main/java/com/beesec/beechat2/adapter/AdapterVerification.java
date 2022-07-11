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
import com.beesec.beechat2.model.ModelVerification;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterVerification extends RecyclerView.Adapter<AdapterVerification.MyHolder>{

    final Context context;
    final List<ModelVerification> userList;

    public AdapterVerification(Context context, List<ModelVerification> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.verification_view, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getuID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

        holder.fName.setText(userList.get(position).getName());
        holder.fUsername.setText(userList.get(position).getUsername());
        holder.known.setText(userList.get(position).getKnown());
        holder.govt.setText(userList.get(position).getLink());

        holder.reject.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference().child("Verification").child(userList.get(position).getvId()).getRef().removeValue();
            Snackbar.make(v, "Rejected", Snackbar.LENGTH_LONG).show();
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
        });

        holder.accept.setOnClickListener(v -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("verified", "yes");
            FirebaseDatabase.getInstance().getReference("Users").child(userList.get(position).getuID()).updateChildren(hashMap);
            FirebaseDatabase.getInstance().getReference().child("Verification").child(userList.get(position).getvId()).getRef().removeValue();
            Snackbar.make(v, "Accepted", Snackbar.LENGTH_LONG).show();
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.height = 0;
            holder.itemView.setLayoutParams(params);
           /* if (!Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()).isEmpty()){
                String username = context.getResources().getString(R.string.your_email);
                String password = context.getResources().getString(R.string.your_password);
                String messageToSend = "Verified badge has been added to your profile";
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

                FirebaseDatabase.getInstance().getReference().child("Users").child(userList.get(position).getuID()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!Objects.requireNonNull(snapshot.child("email").getValue()).toString().isEmpty()){
                            String em = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                            String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();

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

        final CircleImageView dp;
        final TextView name;
        final TextView username;
        final TextView fName;
        final TextView fUsername;
        final TextView known;
        final TextView govt;
        final Button accept;
        final Button reject;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            fName = itemView.findViewById(R.id.fName);
            fUsername = itemView.findViewById(R.id.fUsername);
            known = itemView.findViewById(R.id.known);
            govt = itemView.findViewById(R.id.govt);
            accept = itemView.findViewById(R.id.accept);
            reject = itemView.findViewById(R.id.reject);

        }

    }
}
