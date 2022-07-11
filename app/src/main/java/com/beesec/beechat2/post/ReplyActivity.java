package com.beesec.beechat2.post;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.NightMode;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterCommentReply;
import com.beesec.beechat2.model.ModelCommentReply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ReplyActivity extends AppCompatActivity {

    String commentId;

    List<ModelCommentReply> modelCommentReplies;
    AdapterCommentReply adapterCommentReply;
    RecyclerView recyclerView;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState().equals("night")){
            setTheme(R.style.DarkTheme);
        }else if (sharedPref.loadNightModeState().equals("dim")){
            setTheme(R.style.DimTheme);
        }else setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);


        recyclerView = findViewById(R.id.chat_rv);

        commentId = getIntent().getStringExtra("cId");


        modelCommentReplies = new ArrayList<>();
        loadComments();


        //Send
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.message_send).setOnClickListener(v -> {

            if (editText.getText().toString().isEmpty()){
                Snackbar.make(v, "Type a comment", Snackbar.LENGTH_SHORT).show();
            }else {
                String timeStamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("cId",  timeStamp);
                hashMap.put("comment", editText.getText().toString());
                hashMap.put("timestamp",  timeStamp);
                hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("rId", commentId);
                FirebaseDatabase.getInstance().getReference("Reply").child(commentId).child(timeStamp).setValue(hashMap);

                Snackbar.make(v, "Comment sent", Snackbar.LENGTH_SHORT).show();
                editText.setText("");
            }

        });

    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        modelCommentReplies = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Reply").child(commentId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelCommentReplies.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelCommentReply modelComments = ds.getValue(ModelCommentReply.class);
                    modelCommentReplies.add(modelComments);
                    adapterCommentReply = new AdapterCommentReply(ReplyActivity.this, modelCommentReplies);
                    recyclerView.setAdapter(adapterCommentReply);
                    adapterCommentReply.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}