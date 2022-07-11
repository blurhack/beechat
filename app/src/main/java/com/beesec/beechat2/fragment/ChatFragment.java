package com.beesec.beechat2.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.beesec.beechat2.R;
import com.beesec.beechat2.adapter.AdapterChatList;
import com.beesec.beechat2.chat.CreateChatActivity;
import com.beesec.beechat2.group.GroupFragment;
import com.beesec.beechat2.model.ModelChatList;
import com.beesec.beechat2.model.ModelUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    List<ModelChatList> chatlistList;
    List<ModelUser> userList;
    AdapterChatList adapterChatList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        MobileAds.initialize(getContext(), initializationStatus -> {
        });
        AdView mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FirebaseDatabase.getInstance().getReference("Ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("on")){
                    mAdView.setVisibility(View.VISIBLE);
                }else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        view.findViewById(R.id.edit).setOnClickListener(v -> startActivity(new Intent(getContext(), GroupFragment.class)));

        //Create
        view.findViewById(R.id.menu).setOnClickListener(v -> startActivity(new Intent(getContext(), CreateChatActivity.class)));

        recyclerView = view.findViewById(R.id.chatList);
        chatlistList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("Chatlist").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChatList chatlist = ds.getValue(ModelChatList.class);
                    chatlistList.add(chatlist);
                }
                if (!snapshot.exists()){
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    view.findViewById(R.id.found).setVisibility(View.VISIBLE);
                }else {
                    view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.found).setVisibility(View.GONE);
                    loadChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);
                    for (ModelChatList chatlist: chatlistList){
                        if (Objects.requireNonNull(user).getId() != null && user.getId().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    adapterChatList = new AdapterChatList(getActivity(), userList);
                    recyclerView.setAdapter(adapterChatList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}