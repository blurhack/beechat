package com.beesec.beechat2.welcome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.beesec.beechat2.R;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter;
    TabLayout tabIndicator;
    Button next;
    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //ini view
        next = findViewById(R.id.next);
        tabIndicator = findViewById(R.id.indicator);

        //Fill list screen
        final List<ScreenItem> mList = new ArrayList<>();
        mList.add(new ScreenItem("Post video, image & text\n" +
                "With hashtag and mention\n" +
                "your friends",R.drawable.ic_one));
        mList.add(new ScreenItem("Watch or create reels with\n" +
                "face filters camera",R.drawable.ic_two));
        mList.add(new ScreenItem("Private chat with video &\n" +
                "voice calls",R.drawable.ic_three));
        mList.add(new ScreenItem("Create or join groups & \n" +
                "group chat with group\n" +
                "Video & voice call",R.drawable.ic_four));
        mList.add(new ScreenItem("Create or join Live stream\n" +
                "& chat during stream ",R.drawable.ic_five));
        mList.add(new ScreenItem("Create or join Live stream\n" +
                "& chat during stream ",R.drawable.ic_five));

        //Setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);

        //setup tabLayout with pagerView
        tabIndicator.setupWithViewPager(screenPager);

        //Next btn click
        next.setOnClickListener(view -> {

            position = screenPager.getCurrentItem();
            if (position < mList.size()){
                position++;
                screenPager.setCurrentItem(position);
            }
            //When reached last
            if (position == mList.size()-1) {

                loadLastScreen();

            }
        });

        //tabLayout last
        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == mList.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void loadLastScreen() {
        Intent intent = new Intent(getApplicationContext(), IntroLast.class );
        startActivity(intent);
        finish();
    }

}